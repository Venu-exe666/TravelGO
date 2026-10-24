package com.travelgo.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Centralized exception handler for the entire application.
 * 
 * [HOW IT WORKS]:
 * - @ControllerAdvice: Intercepts exceptions thrown by ANY controller across the app.
 * - Prevents the ugly default "Whitelabel Error Page" and provides clean, user-friendly feedback.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Catches business logic errors and runtime exceptions (e.g., "Booking not found", "Access denied").
     */
    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/";
    }

    /**
     * Fallback catcher for all unhandled general exceptions (e.g., database connection issues, unexpected crashes).
     */
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again.");
        return "error";
    }
}
