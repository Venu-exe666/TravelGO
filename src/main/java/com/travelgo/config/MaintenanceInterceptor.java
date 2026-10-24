package com.travelgo.config;

import com.travelgo.service.SystemSettingsService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor that checks website maintenance status.
 * If the site is in MAINTENANCE mode:
 * - Public users are redirected to the maintenance page
 * - Admin users can still access admin pages
 * - Static resources and auth pages are always accessible
 */
@Component
public class MaintenanceInterceptor implements HandlerInterceptor {

    private final SystemSettingsService settingsService;

    /**
     * Injects the SystemSettingsService to check website status from the database.
     */
    public MaintenanceInterceptor(SystemSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    /**
     * Runs BEFORE any controller handles an incoming HTTP request.
     * Acts as a gatekeeper: if the site is in maintenance mode, public visitors are redirected.
     * 
     *    and the /maintenance page itself are always allowed through.
     * 3. Checks the database setting (isMaintenanceMode).
     * 4. If maintenance mode is TRUE and the user is visiting a public page, redirects them to "/maintenance"
     *    and returns false (stops the request from reaching the controller).
     * 5. If maintenance mode is FALSE, returns true (allows the request to proceed normally).
     * 
     * HandlerInterceptor.preHandle() is a key Spring MVC concept for cross-cutting request inspection.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String uri = request.getRequestURI();

        // Step 1: Always allow static resources, auth pages, admin pages, and maintenance page itself
        if (uri.startsWith("/css") || uri.startsWith("/js") || uri.startsWith("/images") ||
            uri.startsWith("/static") || uri.startsWith("/auth") || uri.startsWith("/admin") ||
            uri.equals("/maintenance")) {
            return true;
        }

        // Step 2: Check if maintenance mode is enabled in system settings
        if (settingsService.isMaintenanceMode()) {
            // Redirect public users to the maintenance notice page
            response.sendRedirect("/maintenance");
            return false;
        }

        return true;
    }

    /**
     * Runs AFTER the controller finishes executing, but BEFORE the HTML template is rendered.
     * Automatically injects global company settings (like phone, email, company name) into every view.
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
        // Add system settings to all page models for footer/navbar display
        if (modelAndView != null && !request.getRequestURI().startsWith("/css")
                && !request.getRequestURI().startsWith("/js")) {
            try {
                modelAndView.addObject("systemSettings", settingsService.getSettings());
            } catch (Exception e) {
                // Silently ignore if settings are not yet initialized
            }
        }
    }
}
