package com.travelgo.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.travelgo.entity.*;
import com.travelgo.enums.*;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates downloadable PDF reports for Admin and Staff dashboards.
 * Uses iText PDF library to create professional reports with headers, data tables, and summaries.
 */
@Service
public class ReportService {

    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final VisaApplicationService visaApplicationService;
    private final UserService userService;
    private final TourPackageService tourPackageService;

    /** Color constants for the PDF styling */
    private static final DeviceRgb HEADER_BG = new DeviceRgb(15, 23, 42);       // Dark navy
    private static final DeviceRgb HEADER_TEXT = new DeviceRgb(255, 255, 255);   // White
    private static final DeviceRgb ROW_ALT = new DeviceRgb(241, 245, 249);       // Light gray
    private static final DeviceRgb ACCENT = new DeviceRgb(34, 197, 94);          // Green accent
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Injects all service dependencies needed to query data for each report type.
     */
    public ReportService(BookingService bookingService,
                         PaymentService paymentService,
                         VisaApplicationService visaApplicationService,
                         UserService userService,
                         TourPackageService tourPackageService) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.visaApplicationService = visaApplicationService;
        this.userService = userService;
        this.tourPackageService = tourPackageService;
    }

    // =====================================================================
    // 1. BOOKING SUMMARY REPORT
    // =====================================================================

    /**
     * Generates a PDF report listing all bookings with customer names, packages, dates, status, and pricing.
     */
    public void generateBookingReport(OutputStream out) {
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        addReportHeader(doc, "Booking Summary Report");

        List<Booking> bookings = bookingService.findAll();

        // Summary statistics
        long confirmed = bookings.stream().filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED).count();
        long pending = bookings.stream().filter(b -> b.getBookingStatus() == BookingStatus.PENDING).count();
        long cancelled = bookings.stream().filter(b -> b.getBookingStatus() == BookingStatus.CANCELLED).count();
        BigDecimal totalRevenue = bookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getTotalPackageAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        doc.add(new Paragraph("Total Bookings: " + bookings.size()
                + "  |  Confirmed: " + confirmed
                + "  |  Pending: " + pending
                + "  |  Cancelled: " + cancelled
                + "  |  Revenue: $" + totalRevenue)
                .setFontSize(10).setMarginBottom(15));

        // Data table
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 3, 2, 2, 2, 2}))
                .useAllAvailableWidth();

        addTableHeader(table, "ID", "Customer", "Package", "Travel Date", "Travelers", "Status", "Total ($)");

        for (int i = 0; i < bookings.size(); i++) {
            Booking b = bookings.get(i);
            boolean alt = i % 2 == 1;
            addTableRow(table, alt,
                    String.valueOf(b.getId()),
                    b.getUser() != null ? b.getUser().getName() : "N/A",
                    b.getTourPackage() != null ? b.getTourPackage().getName() : "N/A",
                    b.getTravelDate() != null ? b.getTravelDate().toString() : "N/A",
                    String.valueOf(b.getNumberOfTravelers()),
                    b.getBookingStatus() != null ? b.getBookingStatus().name() : "N/A",
                    b.getTotalPackageAmount() != null ? b.getTotalPackageAmount().toString() : "0.00"
            );
        }

        doc.add(table);
        addReportFooter(doc);
        doc.close();
    }

    // =====================================================================
    // 2. PAYMENT & REVENUE REPORT
    // =====================================================================

    /**
     * Generates a PDF report of all payment transactions with amounts, methods, statuses, and totals.
     */
    public void generatePaymentReport(OutputStream out) {
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        addReportHeader(doc, "Payment & Revenue Report");

        List<Payment> payments = paymentService.findAll();

        BigDecimal totalPaid = payments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PAID)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPending = payments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PENDING)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        doc.add(new Paragraph("Total Payments: " + payments.size()
                + "  |  Revenue Collected: $" + totalPaid
                + "  |  Pending: $" + totalPending)
                .setFontSize(10).setMarginBottom(15));

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 2, 2, 2, 2, 2}))
                .useAllAvailableWidth();

        addTableHeader(table, "ID", "Customer", "Type", "Amount ($)", "Method", "Status", "Date");

        for (int i = 0; i < payments.size(); i++) {
            Payment p = payments.get(i);
            boolean alt = i % 2 == 1;
            addTableRow(table, alt,
                    String.valueOf(p.getId()),
                    p.getBooking() != null && p.getBooking().getUser() != null ? p.getBooking().getUser().getName() : "N/A",
                    p.getPaymentType() != null ? p.getPaymentType().name() : "N/A",
                    p.getAmount() != null ? p.getAmount().toString() : "0.00",
                    p.getPaymentMethod() != null ? p.getPaymentMethod() : "N/A",
                    p.getPaymentStatus() != null ? p.getPaymentStatus().name() : "N/A",
                    p.getPaidAt() != null ? p.getPaidAt().format(DATE_FMT) : (p.getCreatedAt() != null ? p.getCreatedAt().format(DATE_FMT) : "N/A")
            );
        }

        doc.add(table);
        addReportFooter(doc);
        doc.close();
    }

    // =====================================================================
    // 3. VISA APPLICATIONS REPORT
    // =====================================================================

    /**
     * Generates a PDF report of all visa applications with applicant details, types, statuses, and processing dates.
     */
    public void generateVisaReport(OutputStream out) {
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        addReportHeader(doc, "Visa Applications Report");

        List<VisaApplication> visas = visaApplicationService.findAll();

        long approved = visas.stream().filter(v -> v.getStatus() == VisaStatus.APPROVED).count();
        long rejected = visas.stream().filter(v -> v.getStatus() == VisaStatus.REJECTED).count();
        long pending = visas.stream().filter(v -> v.getStatus() == VisaStatus.PENDING_DOCUMENTS || v.getStatus() == VisaStatus.DOCUMENTS_SUBMITTED || v.getStatus() == VisaStatus.PAYMENT_PENDING).count();

        doc.add(new Paragraph("Total Applications: " + visas.size()
                + "  |  Approved: " + approved
                + "  |  Rejected: " + rejected
                + "  |  Pending/In Review: " + pending)
                .setFontSize(10).setMarginBottom(15));

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 2, 2, 2, 2}))
                .useAllAvailableWidth();

        addTableHeader(table, "ID", "Applicant", "Visa Type", "Status", "Submitted", "Country");

        for (int i = 0; i < visas.size(); i++) {
            VisaApplication v = visas.get(i);
            boolean alt = i % 2 == 1;
            addTableRow(table, alt,
                    String.valueOf(v.getId()),
                    v.getBooking() != null && v.getBooking().getUser() != null ? v.getBooking().getUser().getName() : "N/A",
                    v.getVisaType() != null ? v.getVisaType().name() : "N/A",
                    v.getStatus() != null ? v.getStatus().name() : "N/A",
                    v.getCreatedAt() != null ? v.getCreatedAt().format(DATE_FMT) : "N/A",
                    v.getBooking() != null && v.getBooking().getTourPackage() != null && v.getBooking().getTourPackage().getDestination() != null ? v.getBooking().getTourPackage().getDestination().getCountry() : "N/A"
            );
        }

        doc.add(table);
        addReportFooter(doc);
        doc.close();
    }

    // =====================================================================
    // 4. USER & STAFF REPORT
    // =====================================================================

    /**
     * Generates a PDF report of all registered users with roles, status, and registration dates.
     */
    public void generateUserReport(OutputStream out) {
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        addReportHeader(doc, "User & Staff Report");

        List<User> users = userService.getAllUsers();

        long active = users.stream().filter(User::isActive).count();
        long inactive = users.stream().filter(u -> !u.isActive()).count();

        doc.add(new Paragraph("Total Users: " + users.size()
                + "  |  Active: " + active
                + "  |  Inactive: " + inactive)
                .setFontSize(10).setMarginBottom(15));

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 3, 2, 2, 2}))
                .useAllAvailableWidth();

        addTableHeader(table, "ID", "Name", "Email", "Role", "Status", "Registered");

        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            boolean alt = i % 2 == 1;
            addTableRow(table, alt,
                    String.valueOf(u.getId()),
                    u.getName(),
                    u.getEmail(),
                    u.getRole() != null ? u.getRole().getRoleName() : "N/A",
                    u.isActive() ? "Active" : "Inactive",
                    u.getCreatedAt() != null ? u.getCreatedAt().format(DATE_FMT) : "N/A"
            );
        }

        doc.add(table);
        addReportFooter(doc);
        doc.close();
    }

    // =====================================================================
    // 5. TOUR PACKAGES REPORT
    // =====================================================================

    /**
     * Generates a PDF report of all tour packages with pricing, capacity, destinations, and status.
     */
    public void generatePackageReport(OutputStream out) {
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        addReportHeader(doc, "Tour Packages Report");

        List<TourPackage> packages = tourPackageService.findAll();

        long activeCount = packages.stream().filter(TourPackage::isActive).count();

        doc.add(new Paragraph("Total Packages: " + packages.size()
                + "  |  Active: " + activeCount
                + "  |  Inactive: " + (packages.size() - activeCount))
                .setFontSize(10).setMarginBottom(15));

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 2, 2, 2, 2, 2}))
                .useAllAvailableWidth();

        addTableHeader(table, "ID", "Package Name", "Destination", "Price ($)", "Days", "Capacity", "Status");

        for (int i = 0; i < packages.size(); i++) {
            TourPackage p = packages.get(i);
            boolean alt = i % 2 == 1;
            addTableRow(table, alt,
                    String.valueOf(p.getId()),
                    p.getName(),
                    p.getDestination() != null ? p.getDestination().getCity() + ", " + p.getDestination().getCountry() : "N/A",
                    p.getBasePrice() != null ? p.getBasePrice().toString() : "0.00",
                    String.valueOf(p.getDurationDays()),
                    String.valueOf(p.getMaxCapacity()),
                    p.isActive() ? "Active" : "Inactive"
            );
        }

        doc.add(table);
        addReportFooter(doc);
        doc.close();
    }

    // =====================================================================
    // HELPER METHODS (Shared PDF styling utilities)
    // =====================================================================

    /**
     * Adds a branded header with "TravelGO" company name, report title, and generation timestamp.
     */
    private void addReportHeader(Document doc, String title) {
        doc.add(new Paragraph("TravelGO")
                .setFontSize(24)
                .setBold()
                .setFontColor(ACCENT)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(2));

        doc.add(new Paragraph(title)
                .setFontSize(16)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5));

        doc.add(new Paragraph("Generated: " + LocalDateTime.now().format(DATE_FMT))
                .setFontSize(8)
                .setFontColor(new DeviceRgb(107, 114, 128))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));
    }

    /**
     * Adds a dark-themed header row to a PDF table with column titles.
     */
    private void addTableHeader(Table table, String... headers) {
        for (String header : headers) {
            Cell cell = new Cell()
                    .add(new Paragraph(header).setFontSize(9).setBold().setFontColor(HEADER_TEXT))
                    .setBackgroundColor(HEADER_BG)
                    .setPadding(6);
            table.addHeaderCell(cell);
        }
    }

    /**
     * Adds a data row to a PDF table with optional alternating background color for readability.
     */
    private void addTableRow(Table table, boolean alternate, String... values) {
        for (String value : values) {
            Cell cell = new Cell()
                    .add(new Paragraph(value != null ? value : "").setFontSize(8))
                    .setPadding(5);
            if (alternate) {
                cell.setBackgroundColor(ROW_ALT);
            }
            table.addCell(cell);
        }
    }

    /**
     * Adds a footer line with copyright and confidentiality notice.
     */
    private void addReportFooter(Document doc) {
        doc.add(new Paragraph("\nÂ© " + java.time.Year.now().getValue() + " TravelGO - Confidential Report. Generated by the TravelGO System.")
                .setFontSize(7)
                .setFontColor(new DeviceRgb(107, 114, 128))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30));
    }
}
