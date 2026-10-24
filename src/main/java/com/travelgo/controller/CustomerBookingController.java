package com.travelgo.controller;

import com.travelgo.entity.Booking;
import com.travelgo.entity.TourPackage;
import com.travelgo.entity.User;
import com.travelgo.entity.Hotel;
import com.travelgo.entity.BookingHotel;
import com.travelgo.entity.Traveler;
import com.travelgo.enums.BookingStatus;
import com.travelgo.service.BookingService;
import com.travelgo.service.TourPackageService;
import com.travelgo.service.UserService;
import com.travelgo.service.HotelService;
import com.travelgo.service.BookingHotelService;
import com.travelgo.service.VisaApplicationService;
import com.travelgo.service.VisaDocumentService;
import com.travelgo.service.TravelerService;
import com.travelgo.service.PaymentService;
import com.travelgo.service.BookingTicketPdfService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.travelgo.entity.VisaApplication;

/**
 * Controller for Customer Booking operations.
 * Enforces customer ownership on all booking actions.
 */
@Controller
@RequestMapping("/customer/bookings")
public class CustomerBookingController {

    private final BookingService bookingService;
    private final TourPackageService tourPackageService;
    private final UserService userService;
    private final HotelService hotelService;
    private final BookingHotelService bookingHotelService;
    private final VisaApplicationService visaApplicationService;
    private final VisaDocumentService visaDocumentService;
    private final TravelerService travelerService;
    private final PaymentService paymentService;
    private final BookingTicketPdfService bookingTicketPdfService;

    public CustomerBookingController(BookingService bookingService,
                                    TourPackageService tourPackageService,
                                    UserService userService,
                                    HotelService hotelService,
                                    BookingHotelService bookingHotelService,
                                    VisaApplicationService visaApplicationService,
                                    VisaDocumentService visaDocumentService,
                                    TravelerService travelerService,
                                    PaymentService paymentService,
                                    BookingTicketPdfService bookingTicketPdfService) {
        this.bookingService = bookingService;
        this.tourPackageService = tourPackageService;
        this.userService = userService;
        this.hotelService = hotelService;
        this.bookingHotelService = bookingHotelService;
        this.visaApplicationService = visaApplicationService;
        this.visaDocumentService = visaDocumentService;
        this.travelerService = travelerService;
        this.paymentService = paymentService;
        this.bookingTicketPdfService = bookingTicketPdfService;
    }

    private User resolveUser(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.getUserByEmail(userDetails.getUsername()).orElse(null);
    }

    private boolean isOwner(Booking booking, User user) {
        return user != null && booking.getUser().getId().equals(user.getId());
    }

    @GetMapping
    public String listBookings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = resolveUser(userDetails);
        if (user == null) return "redirect:/auth/login";

        List<Booking> bookings = bookingService.findByUserId(user.getId());
        model.addAttribute("bookings", bookings);
        model.addAttribute("currentUser", user);

        Map<Long, VisaApplication> visaMap = new HashMap<>();
        for (Booking b : bookings) {
            visaApplicationService.findByBookingId(b.getId()).ifPresent(va -> visaMap.put(b.getId(), va));
        }
        model.addAttribute("visaMap", visaMap);

        return "customer/bookings";
    }

    /**
     * visa application status, and uploaded documentation files.
     */
    @GetMapping("/{id}")
    public String viewBooking(@PathVariable("id") Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        User user = resolveUser(userDetails);
        if (user == null) return "redirect:/auth/login";

        return bookingService.findById(id).map(booking -> {
            if (!isOwner(booking, user)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Access denied.");
                return "redirect:/customer/bookings";
            }
            model.addAttribute("booking", booking);
            model.addAttribute("currentUser", user);
            bookingHotelService.findByBookingId(booking.getId())
                    .ifPresent(bh -> model.addAttribute("bookingHotel", bh));
            visaApplicationService.findByBookingId(booking.getId())
                    .ifPresent(va -> {
                        model.addAttribute("visaApplication", va);
                        model.addAttribute("visaDocuments", visaDocumentService.findByVisaApplicationId(va.getId()));
                    });
            return "customer/booking-detail";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking not found.");
            return "redirect:/customer/bookings";
        });
    }

    /**
     * and optionally reserves hotel accommodation rooms.
     *
     *      and sets status to DRAFT.
     * 4. HOTEL RESERVATION (if hotel selected):
     *    - Validates hotel belongs to the same destination as the tour package.
     *    - Calculates room count assuming twin-sharing: rooms = (numberOfTravelers + 1) / 2.
     *    - Calculates hotel cost = pricePerNight * rooms * durationDays.
     *    - Saves BookingHotel entity.
     * 5. Redirects to "/customer/bookings" with success flash message.
     */
    @PostMapping("/create")
    public String createBooking(@RequestParam("packageId") Long packageId,
                               @RequestParam("travelDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
                               @RequestParam("numberOfTravelers") Integer numberOfTravelers,
                               @RequestParam(value = "hotelId", required = false) Long hotelId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        User user = resolveUser(userDetails);
        if (user == null) return "redirect:/auth/login";

        try {
            TourPackage tourPkg = tourPackageService.findById(packageId)
                    .orElseThrow(() -> new IllegalArgumentException("Package not found"));

            LocalDate minAllowedDate = LocalDate.now().plusDays(4);
            if (travelDate == null || travelDate.isBefore(minAllowedDate)) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Travel date must be at least 4 days from today. Earliest allowed: " + minAllowedDate);
                return "redirect:/packages/" + packageId + "/book";
            }

            if (numberOfTravelers == null || numberOfTravelers < 1) {
                redirectAttributes.addFlashAttribute("errorMessage", "Number of travelers must be at least 1.");
                return "redirect:/packages/" + packageId + "/book";
            }

            if (tourPkg.getMaxCapacity() != null && numberOfTravelers > tourPkg.getMaxCapacity()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Number of travelers (" + numberOfTravelers + ") exceeds maximum package capacity of " + tourPkg.getMaxCapacity() + ".");
                return "redirect:/packages/" + packageId + "/book";
            }

            Booking booking = new Booking();
            booking.setUser(user);
            booking.setTourPackage(tourPkg);
            booking.setTravelDate(travelDate);
            booking.setNumberOfTravelers(numberOfTravelers);
            booking.setPackageUnitPrice(tourPkg.getBasePrice());
            booking.setTotalPackageAmount(tourPkg.getBasePrice().multiply(BigDecimal.valueOf(numberOfTravelers)));

            Booking savedBooking = bookingService.createBooking(booking);

            if (hotelId != null) {
                Hotel hotel = hotelService.findById(hotelId)
                        .orElseThrow(() -> new IllegalArgumentException("Hotel not found"));
                if (!hotel.getDestination().getId().equals(tourPkg.getDestination().getId())) {
                    throw new IllegalArgumentException("Selected hotel does not belong to the package destination.");
                }
                BookingHotel bookingHotel = new BookingHotel();
                bookingHotel.setBooking(savedBooking);
                bookingHotel.setHotel(hotel);
                bookingHotel.setCheckInDate(travelDate);
                bookingHotel.setCheckOutDate(travelDate.plusDays(tourPkg.getDurationDays()));
                int rooms = (numberOfTravelers + 1) / 2;
                bookingHotel.setNumberOfRooms(rooms);
                bookingHotel.setNumberOfNights(tourPkg.getDurationDays());
                bookingHotel.setHotelCost(hotel.getPricePerNight().multiply(BigDecimal.valueOf(rooms)).multiply(BigDecimal.valueOf(tourPkg.getDurationDays())));
                
                bookingHotelService.save(bookingHotel);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Booking created successfully!");
            return "redirect:/customer/bookings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create booking: " + e.getMessage());
            return "redirect:/packages/" + packageId + "/book";
        }
    }

    @PostMapping("/{id}/modify")
    public String modifyBooking(@PathVariable("id") Long id,
                               @RequestParam(value = "travelDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
                               @RequestParam(value = "numberOfTravelers", required = false) Integer numberOfTravelers,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        User user = resolveUser(userDetails);
        if (user == null) return "redirect:/auth/login";

        try {
            Booking booking = bookingService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

            if (!isOwner(booking, user)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Access denied.");
                return "redirect:/customer/bookings";
            }

            bookingService.modifyBooking(id, travelDate, numberOfTravelers);
            redirectAttributes.addFlashAttribute("successMessage", "Booking modified successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to modify booking: " + e.getMessage());
        }
        return "redirect:/customer/bookings/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable("id") Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        User user = resolveUser(userDetails);
        if (user == null) return "redirect:/auth/login";

        try {
            Booking booking = bookingService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

            if (!isOwner(booking, user)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Access denied.");
                return "redirect:/customer/bookings";
            }

            bookingService.cancelBooking(id);
            redirectAttributes.addFlashAttribute("successMessage", "Booking cancelled successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to cancel booking: " + e.getMessage());
        }
        return "redirect:/customer/bookings";
    }

    /**
     *    Flight tickets and vouchers are NOT issued for unconfirmed/unpaid bookings!
     * 3. Loads booking, payments, travelers list, hotel booking, and visa status into Model.
     * 4. Renders "customer/booking-confirmation" printable view.
     */
    @GetMapping("/{id}/confirmation")
    public String bookingConfirmation(@PathVariable("id") Long id,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        User user = resolveUser(userDetails);
        if (user == null) return "redirect:/auth/login";

        return bookingService.findById(id).map(booking -> {
            if (!isOwner(booking, user)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Access denied.");
                return "redirect:/customer/bookings";
            }

            if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
                redirectAttributes.addFlashAttribute("errorMessage", "Flight tickets and confirmation vouchers are only issued once your booking is fully CONFIRMED & PAID.");
                return "redirect:/customer/bookings/" + id;
            }

            model.addAttribute("booking", booking);
            model.addAttribute("currentUser", user);
            model.addAttribute("payments", paymentService.findByBookingId(booking.getId()));
            model.addAttribute("travelers", travelerService.findByBookingId(booking.getId()));
            bookingHotelService.findByBookingId(booking.getId())
                    .ifPresent(bh -> model.addAttribute("bookingHotel", bh));
            visaApplicationService.findByBookingId(booking.getId())
                    .ifPresent(va -> model.addAttribute("visaApplication", va));

            return "customer/booking-confirmation";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking not found.");
            return "redirect:/customer/bookings";
        });
    }

    @GetMapping("/{id}/ticket.pdf")
    public ResponseEntity<byte[]> downloadTicketPdf(@PathVariable("id") Long id,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        User user = resolveUser(userDetails);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        Booking booking = bookingService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (!isOwner(booking, user)) {
            return ResponseEntity.status(403).build();
        }
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            return ResponseEntity.status(409).build();
        }
        BookingHotel hotel = bookingHotelService.findByBookingId(id).orElse(null);
        byte[] pdf = bookingTicketPdfService.createTicket(booking, travelerService.findByBookingId(id), hotel);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("TravelGO-Ticket-TG-" + id + ".pdf").build());
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @PostMapping("/{id}/travelers")
    public String addTraveler(@PathVariable("id") Long id,
                             @RequestParam("fullName") String fullName,
                             @RequestParam("passportNumber") String passportNumber,
                             @RequestParam("dateOfBirth") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
                             @RequestParam(value = "gender", required = false) String gender,
                             @RequestParam(value = "nationality", required = false) String nationality,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User user = resolveUser(userDetails);
        if (user == null) return "redirect:/auth/login";

        return bookingService.findById(id).map(booking -> {
            if (!isOwner(booking, user)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Access denied.");
                return "redirect:/customer/bookings";
            }

            List<Traveler> currentTravelers = travelerService.findByBookingId(id);
            if (currentTravelers.size() >= booking.getNumberOfTravelers()) {
                redirectAttributes.addFlashAttribute("errorMessage", "All " + booking.getNumberOfTravelers() + " traveler slot(s) are already filled.");
                return "redirect:/customer/bookings/" + id + "/confirmation";
            }

            if (fullName == null || fullName.trim().length() < 2 || fullName.trim().length() > 100) {
                redirectAttributes.addFlashAttribute("errorMessage", "Traveler full name must be between 2 and 100 characters.");
                return "redirect:/customer/bookings/" + id + "/confirmation";
            }
            if (passportNumber == null || !passportNumber.trim().matches("^[A-Za-z0-9]{5,20}$")) {
                redirectAttributes.addFlashAttribute("errorMessage", "Passport number must be 5-20 alphanumeric characters (e.g. N1234567).");
                return "redirect:/customer/bookings/" + id + "/confirmation";
            }
            if (dateOfBirth == null || dateOfBirth.isAfter(LocalDate.now()) || dateOfBirth.isBefore(LocalDate.now().minusYears(120))) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please enter a valid date of birth.");
                return "redirect:/customer/bookings/" + id + "/confirmation";
            }
            if (gender != null && !gender.isBlank() && !gender.matches("^(Male|Female|Other)$")) {
                redirectAttributes.addFlashAttribute("errorMessage", "Gender must be Male, Female, or Other.");
                return "redirect:/customer/bookings/" + id + "/confirmation";
            }
            if (nationality != null && !nationality.trim().isEmpty() && (nationality.trim().length() < 2 || nationality.trim().length() > 50)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Nationality must be between 2 and 50 characters.");
                return "redirect:/customer/bookings/" + id + "/confirmation";
            }

            Traveler traveler = new Traveler();
            traveler.setBooking(booking);
            traveler.setFullName(fullName.trim());
            traveler.setPassportNumber(passportNumber.trim().toUpperCase());
            traveler.setDateOfBirth(dateOfBirth);
            traveler.setGender(gender != null && !gender.isBlank() ? gender : "Other");
            traveler.setNationality(nationality != null && !nationality.trim().isEmpty() ? nationality.trim() : "Not Specified");

            travelerService.save(traveler);
            redirectAttributes.addFlashAttribute("successMessage", "Traveler registered on flight voucher successfully!");
            return "redirect:/customer/bookings/" + id + "/confirmation";
        }).orElse("redirect:/customer/bookings");
    }
}
