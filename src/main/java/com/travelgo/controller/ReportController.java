package com.travelgo.controller;

import com.travelgo.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Handles PDF report generation and download for Admin and Staff dashboards.
 */
@Controller
public class ReportController {

    private final ReportService reportService;

    /**
     * Injects the ReportService which contains all PDF generation logic.
     */
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // =====================================================================
    // ADMIN REPORT PAGES & DOWNLOADS
    // =====================================================================

    @GetMapping("/admin/reports")
    public String adminReportsPage() {
        return "admin/reports";
    }

    @GetMapping("/admin/reports/bookings")
    public void downloadBookingReport(HttpServletResponse response) throws IOException {
        setPdfHeaders(response, "TravelGO_Bookings_Report_" + LocalDate.now() + ".pdf");
        reportService.generateBookingReport(response.getOutputStream());
    }

    @GetMapping("/admin/reports/payments")
    public void downloadPaymentReport(HttpServletResponse response) throws IOException {
        setPdfHeaders(response, "TravelGO_Payments_Report_" + LocalDate.now() + ".pdf");
        reportService.generatePaymentReport(response.getOutputStream());
    }

    @GetMapping("/admin/reports/visas")
    public void downloadVisaReport(HttpServletResponse response) throws IOException {
        setPdfHeaders(response, "TravelGO_Visas_Report_" + LocalDate.now() + ".pdf");
        reportService.generateVisaReport(response.getOutputStream());
    }

    @GetMapping("/admin/reports/users")
    public void downloadUserReport(HttpServletResponse response) throws IOException {
        setPdfHeaders(response, "TravelGO_Users_Report_" + LocalDate.now() + ".pdf");
        reportService.generateUserReport(response.getOutputStream());
    }

    @GetMapping("/admin/reports/packages")
    public void downloadPackageReport(HttpServletResponse response) throws IOException {
        setPdfHeaders(response, "TravelGO_Packages_Report_" + LocalDate.now() + ".pdf");
        reportService.generatePackageReport(response.getOutputStream());
    }

    // =====================================================================
    // STAFF REPORT PAGES & DOWNLOADS
    // =====================================================================

    @GetMapping("/staff/reports")
    public String staffReportsPage() {
        return "staff/reports";
    }

    @GetMapping("/staff/reports/bookings")
    public void staffDownloadBookingReport(HttpServletResponse response) throws IOException {
        setPdfHeaders(response, "TravelGO_Bookings_Report_" + LocalDate.now() + ".pdf");
        reportService.generateBookingReport(response.getOutputStream());
    }

    @GetMapping("/staff/reports/visas")
    public void staffDownloadVisaReport(HttpServletResponse response) throws IOException {
        setPdfHeaders(response, "TravelGO_Visas_Report_" + LocalDate.now() + ".pdf");
        reportService.generateVisaReport(response.getOutputStream());
    }

    @GetMapping("/staff/reports/packages")
    public void staffDownloadPackageReport(HttpServletResponse response) throws IOException {
        setPdfHeaders(response, "TravelGO_Packages_Report_" + LocalDate.now() + ".pdf");
        reportService.generatePackageReport(response.getOutputStream());
    }

    // =====================================================================
    // HELPER METHOD
    // =====================================================================

    /**
     * Sets the HTTP response headers to tell the browser this is a downloadable PDF file.
     *
     * [HOW IT WORKS]:
     * - Content-Type: application/pdf tells the browser the response body is a PDF.
     * - Content-Disposition: attachment triggers a file download dialog instead of displaying in browser.
     */
    private void setPdfHeaders(HttpServletResponse response, String filename) {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
    }
}
