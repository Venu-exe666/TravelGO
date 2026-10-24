package com.travelgo.controller;

import com.travelgo.entity.User;
import com.travelgo.service.NotificationService;
import com.travelgo.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for Customer Notification listing.
 */
@Controller
@RequestMapping("/customer/notifications")
public class CustomerNotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public CustomerNotificationController(NotificationService notificationService,
                                         UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    private User resolveUser(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userService.getUserByEmail(userDetails.getUsername()).orElse(null);
    }

    @GetMapping
    public String listNotifications(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = resolveUser(userDetails);
        if (user == null) return "redirect:/auth/login";

        model.addAttribute("notifications", notificationService.findByUserId(user.getId()));
        model.addAttribute("currentUser", user);
        return "customer/notifications";
    }
}
