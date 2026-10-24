package com.travelgo.controller;

import com.travelgo.entity.Booking;
import com.travelgo.entity.Payment;
import com.travelgo.entity.User;
import com.travelgo.entity.VisaApplication;
import com.travelgo.enums.PaymentStatus;
import com.travelgo.enums.PaymentType;
import com.travelgo.service.BookingService;
import com.travelgo.service.PaymentService;
import com.travelgo.service.UserService;
import com.travelgo.service.VisaApplicationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Controller for Customer Payment operations.
 * Uses PaymentService.processPayment() for all business logic.
 * Enforces customer ownership.
 */
@Controller
@RequestMapping("/customer/payments")
public class CustomerPaymentController {

    private final PaymentService paymentService;
    private final BookingService bookingService;
    private final UserService userService;
    private final VisaApplicationService visaApplicationService;

    public CustomerPaymentController(PaymentService paymentService,
                                    BookingService bookingService,
                                    UserService userService,
                                    VisaApplicationService visaApplicationService) {
        this.paymentService = paymentService;
        this.bookingService = bookingService;
        this.userService = userService;
        this.visaApplicationService = visaApplicationService;
    }

    private User resolveUser(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.getUserByEmail(userDetails.getUsername()).orElse(null);
    }

    @GetMapping("/checkout/{bookingId}")
    public String checkoutPage(@PathVariable("bookingId") Long bookingId,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        User user = resolveUser(userDetails);
        if (user == null) return "redirect:/auth/login";

        return bookingService.findById(bookingId).map(booking -> {
            // Ownership check
            if (!booking.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Access denied.");
                return "redirect:/customer/bookings";
            }

            List<Payment> payments = paymentService.findByBookingId(bookingId);
            model.addAttribute("booking", booking);
            model.addAttribute("payments", payments);
            model.addAttribute("currentUser", user);

            Payment pendingVisaPayment = payments.stream()
                    .filter(p -> p.getPaymentType() == PaymentType.VISA_DOCUMENTATION
                            && p.getPaymentStatus() == PaymentStatus.PENDING
                            && p.getExpiresAt() != null)
                    .findFirst().orElse(null);
            if (pendingVisaPayment != null) {
                model.addAttribute("visaPaymentDeadline", pendingVisaPayment.getExpiresAt());
            }

            // Check for visa application
            visaApplicationService.findById(bookingId); // This is just for display context
            VisaApplication visa = visaApplicationService.findByUserId(user.getId()).stream()
                    .filter(v -> v.getBooking().getId().equals(bookingId))
                    .findFirst().orElse(null);
            model.addAttribute("visaApplication", visa);
            model.addAttribute("totalVisaCharge", visa != null ? visa.getTotalCharge() : BigDecimal.ZERO);

            return "customer/payment-checkout";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking not found.");
            return "redirect:/customer/bookings";
        });
    }

    @PostMapping("/process")
    public String processPayment(@RequestParam("bookingId") Long bookingId,
                                @RequestParam("paymentType") String paymentType,
                                @RequestParam("amount") BigDecimal amount,
                                @RequestParam(value = "paymentMethod", required = false) String paymentMethod,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User user = resolveUser(userDetails);
        if (user == null) return "redirect:/auth/login";

        try {
            Booking booking = bookingService.findById(bookingId)
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

            // Ownership check
            if (!booking.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Access denied.");
                return "redirect:/customer/bookings";
            }

            PaymentType type = PaymentType.valueOf(paymentType);

            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setPaymentType(type);
            payment.setAmount(amount);
            payment.setPaymentMethod(paymentMethod != null ? paymentMethod : "Online");
            payment.setPaymentStatus(PaymentStatus.PENDING);
            payment.setTransactionReference("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

            // Set expiry for visa documentation payment (24 hours)
            if (type == PaymentType.VISA_DOCUMENTATION) {
                payment.setExpiresAt(LocalDateTime.now().plusHours(24));
            }

            paymentService.processPayment(payment);

            if (type == PaymentType.FULL_PACKAGE) {
                redirectAttributes.addFlashAttribute("successMessage", "Full payment received. A visa officer will now release your tickets and booking details.");
                return "redirect:/customer/bookings/" + bookingId;
            } else if (type == PaymentType.VISA_DOCUMENTATION) {
                redirectAttributes.addFlashAttribute("successMessage", "Visa & Documentation fee payment received successfully! Embassy processing has commenced.");
                return "redirect:/customer/bookings/" + bookingId;
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Payment failed: " + e.getMessage());
        }
        return "redirect:/customer/payments/checkout/" + bookingId;
    }
}
