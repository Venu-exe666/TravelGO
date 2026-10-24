package com.travelgo.controller;

import com.travelgo.entity.*;
import com.travelgo.enums.BookingStatus;
import com.travelgo.enums.ContactMessageStatus;
import com.travelgo.enums.VisaStatus;
import com.travelgo.service.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for Staff Portals.
 * Strictly separates Visa Officers (Consular & Payment verification)
 * and Package Managers / Travel Consultants (Tour Packages & Partner Inventory).
 */
@Controller
@RequestMapping("/staff")
public class StaffDashboardController {

    private final UserService userService;
    private final VisaApplicationService visaApplicationService;
    private final VisaDocumentService visaDocumentService;
    private final PaymentService paymentService;
    private final BookingService bookingService;
    private final TourPackageService tourPackageService;
    private final DestinationService destinationService;
    private final HotelService hotelService;
    private final PackageCategoryService categoryService;
    private final ContactMessageService contactMessageService;

    public StaffDashboardController(UserService userService,
                                    VisaApplicationService visaApplicationService,
                                    VisaDocumentService visaDocumentService,
                                    PaymentService paymentService,
                                    BookingService bookingService,
                                    TourPackageService tourPackageService,
                                    DestinationService destinationService,
                                    HotelService hotelService,
                                    PackageCategoryService categoryService,
                                    ContactMessageService contactMessageService) {
        this.userService = userService;
        this.visaApplicationService = visaApplicationService;
        this.visaDocumentService = visaDocumentService;
        this.paymentService = paymentService;
        this.bookingService = bookingService;
        this.tourPackageService = tourPackageService;
        this.destinationService = destinationService;
        this.hotelService = hotelService;
        this.categoryService = categoryService;
        this.contactMessageService = contactMessageService;
    }

    /**
     * Smart dispatcher: cleanly directs Visa Officers to the Visa Command Terminal
     * and Package Managers to the Inventory & Package Management Terminal.
     */
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            Optional<User> userOpt = userService.getUserByEmail(userDetails.getUsername());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                model.addAttribute("currentUser", user);
                String roleName = user.getRole() != null ? user.getRole().getRoleName() : "";
                model.addAttribute("userRole", roleName);

                if ("VISA_OFFICER".equalsIgnoreCase(roleName)) {
                    enrichVisaOfficerModel(model, null);
                    return "staff/visa-dashboard";
                } else {
                    enrichPackageManagerModel(model);
                    return "staff/package-dashboard";
                }
            }
        }
        return "redirect:/auth/login";
    }

    /**
     * Dedicated endpoint for Visa Officer Command Terminal.
     */
    @GetMapping("/visa-dashboard")
    public String visaDashboard(@RequestParam(value = "applicationId", required = false) Long applicationId,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
        if (userDetails != null) {
            userService.getUserByEmail(userDetails.getUsername()).ifPresent(user -> {
                model.addAttribute("currentUser", user);
                model.addAttribute("userRole", "VISA_OFFICER");
                enrichVisaOfficerModel(model, applicationId);
            });
        }
        return "staff/visa-dashboard";
    }

    /**
     * Dedicated endpoint for Package Manager & Inventory Terminal.
     */
    @GetMapping("/package-dashboard")
    public String packageDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            userService.getUserByEmail(userDetails.getUsername()).ifPresent(user -> {
                model.addAttribute("currentUser", user);
                String roleName = user.getRole() != null ? user.getRole().getRoleName() : "PACKAGE_MANAGER";
                model.addAttribute("userRole", roleName);
                enrichPackageManagerModel(model);
            });
        }
        return "staff/package-dashboard";
    }

    private void enrichVisaOfficerModel(Model model, Long requestedApplicationId) {
        List<VisaApplication> allApps = visaApplicationService.findAll();
        model.addAttribute("visaApplications", allApps);

        long totalCount = allApps.size();
        long pendingCount = allApps.stream()
                .filter(a -> a.getStatus() == VisaStatus.PENDING_DOCUMENTS || a.getStatus() == VisaStatus.DOCUMENTS_SUBMITTED)
                .count();
        long processingCount = allApps.stream()
                .filter(a -> a.getStatus() == VisaStatus.PROCESSING || a.getStatus() == VisaStatus.DOCUMENTS_VERIFIED || a.getStatus() == VisaStatus.PAYMENT_PENDING)
                .count();
        long approvedCount = allApps.stream()
                .filter(a -> a.getStatus() == VisaStatus.APPROVED)
                .count();
        long rejectedCount = allApps.stream()
                .filter(a -> a.getStatus() == VisaStatus.REJECTED)
                .count();

        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("processingCount", processingCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("rejectedCount", rejectedCount);

        List<VisaApplication> recent = allApps.stream()
                .sorted(Comparator.comparing(VisaApplication::getId).reversed())
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("recentApplications", recent);

        Map<Long, List<com.travelgo.entity.VisaDocument>> documentsMap = new HashMap<>();
        for (VisaApplication app : recent) {
            documentsMap.put(app.getId(), visaDocumentService.findByVisaApplicationId(app.getId()));
        }
        model.addAttribute("documentsMap", documentsMap);

        VisaApplication selectedApplication = null;
        if (requestedApplicationId != null) {
            selectedApplication = allApps.stream()
                    .filter(application -> requestedApplicationId.equals(application.getId()))
                    .findFirst()
                    .orElse(null);
        }
        if (selectedApplication == null && !recent.isEmpty()) {
            selectedApplication = recent.get(0);
        }
        model.addAttribute("selectedApplication", selectedApplication);
        if (selectedApplication != null) {
            model.addAttribute("selectedDocuments",
                    visaDocumentService.findByVisaApplicationId(selectedApplication.getId()));
            model.addAttribute("selectedPaymentPaid",
                    paymentService.hasPaidVisaAndDocumentation(selectedApplication.getBooking().getId()));
        } else {
            model.addAttribute("selectedDocuments", Collections.emptyList());
            model.addAttribute("selectedPaymentPaid", false);
        }

        model.addAttribute("totalRevenue", paymentService.getTotalRevenue());
        model.addAttribute("pendingReceivables", paymentService.getPendingReceivables());
        model.addAttribute("visaPaymentsTotal", paymentService.getVisaPaymentsTotal());
        model.addAttribute("packagePaymentsTotal", paymentService.getPackagePaymentsTotal());

        List<Payment> recentPayments = paymentService.getRecentPayments(6);
        model.addAttribute("recentPayments", recentPayments);

        List<Booking> bookings = bookingService.findAll();
        model.addAttribute("bookings", bookings);
    }

    private void enrichPackageManagerModel(Model model) {
        List<TourPackage> allPackages = tourPackageService.findAll();
        List<Booking> allBookings = bookingService.findAll();
        List<Destination> allDestinations = destinationService.findAll();
        List<Hotel> allHotels = hotelService.findAll();
        List<PackageCategory> allCategories = categoryService.findAll();
        List<ContactMessage> allInquiries = contactMessageService.findAll();

        long totalPackages = allPackages.size();
        long activePackages = allPackages.stream().filter(TourPackage::isActive).count();
        long totalDestinations = allDestinations.size();
        long totalHotels = allHotels.size();
        long totalCategories = allCategories.size();
        long totalBookings = allBookings.size();
        long pendingBookings = allBookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.PROCESSING || b.getBookingStatus() == BookingStatus.PENDING)
                .count();
        long totalInquiries = allInquiries.size();
        long openInquiries = allInquiries.stream()
                .filter(i -> i.getStatus() == ContactMessageStatus.OPEN)
                .count();

        model.addAttribute("totalPackages", totalPackages);
        model.addAttribute("activePackages", activePackages);
        model.addAttribute("totalDestinations", totalDestinations);
        model.addAttribute("totalHotels", totalHotels);
        model.addAttribute("totalCategories", totalCategories);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("totalInquiries", totalInquiries);
        model.addAttribute("openInquiries", openInquiries);

        List<TourPackage> recentPackages = allPackages.stream()
                .sorted(Comparator.comparing(TourPackage::getId).reversed())
                .limit(6)
                .collect(Collectors.toList());
        model.addAttribute("recentPackages", recentPackages);

        List<Booking> recentBookings = allBookings.stream()
                .sorted(Comparator.comparing(Booking::getId).reversed())
                .limit(6)
                .collect(Collectors.toList());
        model.addAttribute("recentBookings", recentBookings);

        List<Hotel> recentHotels = allHotels.stream()
                .sorted(Comparator.comparing(Hotel::getId).reversed())
                .limit(4)
                .collect(Collectors.toList());
        model.addAttribute("recentHotels", recentHotels);

        List<ContactMessage> recentInquiries = allInquiries.stream()
                .sorted(Comparator.comparing(ContactMessage::getId).reversed())
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("recentInquiries", recentInquiries);

        model.addAttribute("destinations", allDestinations);
        model.addAttribute("categories", allCategories);
    }
}
