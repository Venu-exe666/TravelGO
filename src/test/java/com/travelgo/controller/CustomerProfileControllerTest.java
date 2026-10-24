package com.travelgo.controller;

import com.travelgo.entity.Role;
import com.travelgo.entity.User;
import com.travelgo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerProfileControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private UserDetails userDetails;

    private CustomerProfileController controller;
    private User customer;

    @BeforeEach
    void setUp() {
        controller = new CustomerProfileController(userService);
        Role customerRole = new Role("CUSTOMER", "Customer");
        customer = new User("John Traveler", "customer@travelgo.com", "encoded", customerRole);
    }

    @Test
    void settingsLoadsOnlyTheAuthenticatedCustomerRecord() {
        when(userDetails.getUsername()).thenReturn(customer.getEmail());
        when(userService.getUserByEmail(customer.getEmail())).thenReturn(Optional.of(customer));
        ConcurrentModel model = new ConcurrentModel();

        String view = controller.settings(userDetails, model);

        assertEquals("customer/settings", view);
        assertSame(customer, model.getAttribute("currentUser"));
    }

    @Test
    void validProfileUpdateUsesAuthenticatedEmailInsteadOfSubmittedUserId() {
        when(userDetails.getUsername()).thenReturn(customer.getEmail());
        when(userService.getUserByEmail(customer.getEmail())).thenReturn(Optional.of(customer));
        RedirectAttributesModelMap flash = new RedirectAttributesModelMap();

        String redirect = controller.updateSettings(
                userDetails,
                "  John Updated  ",
                " +94 77 123 4567 ",
                " Colombo, Sri Lanka ",
                flash);

        assertEquals("redirect:/customer/settings", redirect);
        verify(userService).updateCustomerProfile(
                customer.getEmail(), "John Updated", "+94 77 123 4567", "Colombo, Sri Lanka");
    }

    @Test
    void invalidPhoneIsRejectedBeforePersistence() {
        RedirectAttributesModelMap flash = new RedirectAttributesModelMap();

        String redirect = controller.updateSettings(userDetails, "John Traveler", "invalid", null, flash);

        assertEquals("redirect:/customer/settings", redirect);
        verify(userService, never()).updateCustomerProfile(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }
}
