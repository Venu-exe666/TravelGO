package com.travelgo.controller;

import com.travelgo.entity.Booking;
import com.travelgo.entity.Review;
import com.travelgo.entity.User;
import com.travelgo.enums.BookingStatus;
import com.travelgo.service.BookingService;
import com.travelgo.service.ReviewService;
import com.travelgo.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for Customer Review submission.
 */
@Controller
@RequestMapping("/customer/reviews")
public class CustomerReviewController {

    private final ReviewService reviewService;
    private final BookingService bookingService;
    private final UserService userService;

    public CustomerReviewController(ReviewService reviewService,
                                   BookingService bookingService,
                                   UserService userService) {
        this.reviewService = reviewService;
        this.bookingService = bookingService;
        this.userService = userService;
    }

    private User resolveUser(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.getUserByEmail(userDetails.getUsername()).orElse(null);
    }

    /**
     *    Prevents malicious users from submitting reviews under someone else's trip!
     * 4. BUSINESS RULE CHECK: Ensures booking status is CONFIRMED.
     *    (Customers can only review tours they have actually completed/confirmed).
     * 5. Instantiates Review entity, sets user, booking, tour package, star rating (1-5), and feedback comment.
     * 6. Saves review via reviewService.
     * 7. Redirects back to booking detail page with success message.
     */
    @PostMapping
    public String submitReview(@RequestParam("bookingId") Long bookingId,
                              @RequestParam("rating") Integer rating,
                              @RequestParam(value = "comment", required = false) String comment,
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

            // Status check â€” reviews only for CONFIRMED bookings
            if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
                redirectAttributes.addFlashAttribute("errorMessage", "Reviews can only be submitted for CONFIRMED bookings.");
                return "redirect:/customer/bookings/" + bookingId;
            }

            Review review = new Review();
            review.setUser(user);
            review.setBooking(booking);
            review.setTourPackage(booking.getTourPackage());
            review.setRating(rating);
            review.setComment(comment);
            reviewService.save(review);

            redirectAttributes.addFlashAttribute("successMessage", "Review submitted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to submit review: " + e.getMessage());
        }
        return "redirect:/customer/bookings/" + bookingId;
    }
}
