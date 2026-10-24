package com.travelgo.controller;

import com.travelgo.entity.Booking;
import com.travelgo.entity.Payment;
import com.travelgo.entity.TourPackage;
import com.travelgo.entity.User;
import com.travelgo.service.BookingService;
import com.travelgo.service.PaymentService;
import com.travelgo.service.SystemSettingsService;
import com.travelgo.service.TourPackageService;
import com.travelgo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the Admin Dashboard.
 * Displays executive platform statistics, operational metrics, and workspace navigation.
 */
@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final UserService userService;
    private final SystemSettingsService settingsService;
    private final PaymentService paymentService;
    private final BookingService bookingService;
    private final TourPackageService tourPackageService;

    public AdminDashboardController(UserService userService,
                                    SystemSettingsService settingsService,
                                    PaymentService paymentService,
                                    BookingService bookingService,
                                    TourPackageService tourPackageService) {
        this.userService = userService;
        this.settingsService = settingsService;
        this.paymentService = paymentService;
        this.bookingService = bookingService;
        this.tourPackageService = tourPackageService;
    }

    /**
     * workforce breakdown, gross revenue, booking volumes, and system settings.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long totalUsers = userService.getTotalUsers();
        long activeUsers = userService.getActiveUsers();
        long inactiveUsers = userService.getInactiveUsers();
        long totalStaff = userService.getTotalStaff();

        List<User> allUsers = userService.getAllUsers();
        long customerCount = allUsers.stream()
                .filter(u -> u.getRole() != null && "CUSTOMER".equalsIgnoreCase(u.getRole().getRoleName()))
                .count();
        long visaOfficerCount = allUsers.stream()
                .filter(u -> u.getRole() != null && "VISA_OFFICER".equalsIgnoreCase(u.getRole().getRoleName()))
                .count();
        long packageManagerCount = allUsers.stream()
                .filter(u -> u.getRole() != null && "PACKAGE_MANAGER".equalsIgnoreCase(u.getRole().getRoleName()))
                .count();
        long adminCount = allUsers.stream()
                .filter(u -> u.getRole() != null && "ADMIN".equalsIgnoreCase(u.getRole().getRoleName()))
                .count();

        // Recent users (newest 5 by ID)
        List<User> recentUsers = allUsers.stream()
                .sorted(Comparator.comparing(User::getId).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // Financial metrics
        BigDecimal totalRevenue = paymentService.getTotalRevenue();
        BigDecimal pendingReceivables = paymentService.getPendingReceivables();
        List<Payment> recentPayments = paymentService.getRecentPayments(5);

        // Booking & Package metrics
        List<Booking> allBookings = bookingService.findAll();
        long totalBookings = allBookings.size();
        long confirmedBookings = allBookings.stream()
                .filter(b -> b.getBookingStatus() != null && "CONFIRMED".equalsIgnoreCase(b.getBookingStatus().name()))
                .count();
        long pendingBookings = allBookings.stream()
                .filter(b -> b.getBookingStatus() != null && ("PENDING".equalsIgnoreCase(b.getBookingStatus().name()) || "PROCESSING".equalsIgnoreCase(b.getBookingStatus().name())))
                .count();

        List<TourPackage> allPackages = tourPackageService.findAll();
        long totalPackages = allPackages.size();
        long activePackages = allPackages.stream().filter(TourPackage::isActive).count();

        // Calculate health percentage
        int activeRate = totalUsers > 0 ? (int) Math.round(((double) activeUsers / totalUsers) * 100) : 100;

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("inactiveUsers", inactiveUsers);
        model.addAttribute("totalStaff", totalStaff);
        model.addAttribute("activeRate", activeRate);

        model.addAttribute("customerCount", customerCount);
        model.addAttribute("visaOfficerCount", visaOfficerCount);
        model.addAttribute("packageManagerCount", packageManagerCount);
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("recentUsers", recentUsers);

        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        model.addAttribute("pendingReceivables", pendingReceivables != null ? pendingReceivables : BigDecimal.ZERO);
        model.addAttribute("recentPayments", recentPayments);

        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("confirmedBookings", confirmedBookings);
        model.addAttribute("pendingBookings", pendingBookings);

        model.addAttribute("totalPackages", totalPackages);
        model.addAttribute("activePackages", activePackages);

        model.addAttribute("settings", settingsService.getSettings());
        return "admin/dashboard";
    }
}
