package com.travelgo.controller;

import com.travelgo.entity.Booking;
import com.travelgo.entity.Payment;
import com.travelgo.entity.User;
import com.travelgo.entity.VisaApplication;
import com.travelgo.enums.BookingStatus;
import com.travelgo.enums.PaymentStatus;
import com.travelgo.enums.VisaStatus;
import com.travelgo.repository.VisaApplicationRepository;
import com.travelgo.service.BookingService;
import com.travelgo.service.PaymentService;
import com.travelgo.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Controller for Customer Dashboard.
 */
@Controller
@RequestMapping("/customer")
public class CustomerDashboardController {

    private final UserService userService;
    private final BookingService bookingService;
    private final VisaApplicationRepository visaApplicationRepository;
    private final PaymentService paymentService;

    public CustomerDashboardController(UserService userService, 
                                       BookingService bookingService, 
                                       VisaApplicationRepository visaApplicationRepository,
                                       PaymentService paymentService) {
        this.userService = userService;
        this.bookingService = bookingService;
        this.visaApplicationRepository = visaApplicationRepository;
        this.paymentService = paymentService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            userService.getUserByEmail(userDetails.getUsername()).ifPresent(user -> {
                model.addAttribute("currentUser", user);
                List<Booking> customerBookings = bookingService.findByUserId(user.getId());
                model.addAttribute("bookings", customerBookings);
                List<VisaApplication> customerVisas = visaApplicationRepository.findByBooking_User_Id(user.getId());
                model.addAttribute("visaApplications", customerVisas);

                List<Booking> activeBookings = customerBookings.stream()
                        .filter(booking -> booking.getBookingStatus() != BookingStatus.CANCELLED
                                && booking.getBookingStatus() != BookingStatus.EXPIRED)
                        .toList();
                model.addAttribute("activeBookingCount", activeBookings.size());
                model.addAttribute("confirmedBookingCount", customerBookings.stream()
                        .filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                        .count());

                Optional<Booking> upcomingBooking = activeBookings.stream()
                        .filter(booking -> booking.getTravelDate() != null
                                && !booking.getTravelDate().isBefore(LocalDate.now()))
                        .min(Comparator.comparing(Booking::getTravelDate));
                if (upcomingBooking.isEmpty()) {
                    upcomingBooking = activeBookings.stream()
                            .filter(booking -> booking.getTravelDate() != null)
                            .max(Comparator.comparing(Booking::getTravelDate));
                }
                upcomingBooking.ifPresent(booking -> {
                    model.addAttribute("upcomingBooking", booking);
                    model.addAttribute("daysUntilUpcoming",
                            Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), booking.getTravelDate())));
                });

                VisaApplication activeVisa = customerVisas.stream()
                        .filter(visa -> visa.getStatus() != VisaStatus.APPROVED
                                && visa.getStatus() != VisaStatus.REJECTED
                                && visa.getStatus() != VisaStatus.EXPIRED)
                        .max(Comparator.comparing(VisaApplication::getId))
                        .orElseGet(() -> customerVisas.stream()
                                .max(Comparator.comparing(VisaApplication::getId))
                                .orElse(null));
                model.addAttribute("activeVisa", activeVisa);
                model.addAttribute("visaProgress", visaProgress(activeVisa));

                BigDecimal settledAmount = customerBookings.stream()
                        .flatMap(booking -> paymentService.findByBookingId(booking.getId()).stream())
                        .filter(payment -> payment.getPaymentStatus() == PaymentStatus.PAID)
                        .map(Payment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                model.addAttribute("settledAmount", settledAmount);
            });
        }
        return "customer/dashboard";
    }

    private int visaProgress(VisaApplication visa) {
        if (visa == null || visa.getStatus() == null) return 0;
        return switch (visa.getStatus()) {
            case PENDING_DOCUMENTS -> 1;
            case DOCUMENTS_SUBMITTED -> 2;
            case DOCUMENTS_VERIFIED, PAYMENT_PENDING -> 3;
            case PROCESSING -> 4;
            case APPROVED -> 5;
            case REJECTED, EXPIRED -> 1;
        };
    }
}
