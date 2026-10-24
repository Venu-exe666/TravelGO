package com.travelgo.service;

import com.travelgo.entity.Booking;
import com.travelgo.entity.Notification;
import com.travelgo.entity.User;
import com.travelgo.entity.VisaApplication;
import com.travelgo.enums.VisaStatus;
import com.travelgo.enums.VisaType;
import com.travelgo.repository.VisaApplicationRepository;
import com.travelgo.repository.VisaDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VisaApplicationService {

    private final VisaApplicationRepository repository;
    private final PaymentService paymentService;
    private final BookingService bookingService;
    private final RefundService refundService;
    private final VisaDocumentRepository visaDocumentRepository;
    private final NotificationService notificationService;

    /**
     * Injects repositories and supporting services (Payment, Booking, Refund, Notification).
     */
    public VisaApplicationService(VisaApplicationRepository repository, 
                                  PaymentService paymentService, 
                                  BookingService bookingService, 
                                  RefundService refundService, 
                                  VisaDocumentRepository visaDocumentRepository,
                                  NotificationService notificationService) {
        this.repository = repository;
        this.paymentService = paymentService;
        this.bookingService = bookingService;
        this.refundService = refundService;
        this.visaDocumentRepository = visaDocumentRepository;
        this.notificationService = notificationService;
    }

    /** Retrieves all visa applications for staff review */
    public List<VisaApplication> findAll() {
        return repository.findAll();
    }

    /** Finds a visa application by its ID */
    public Optional<VisaApplication> findById(Long id) {
        return repository.findById(id);
    }

    /** Saves or updates a visa application entity */
    public VisaApplication save(VisaApplication entity) {
        return repository.save(entity);
    }

    /** Deletes a visa application by ID */
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /**
     * Initiates a visa application when a customer books an international tour package requiring a visa.
     * 
     * [BUSINESS RULES APPLIED]:
     * - Rule 3: Sets initial status to PENDING_DOCUMENTS awaiting customer file uploads.
     */
    public VisaApplication createVisaApplication(Booking booking, VisaType type) {
        // Rule 3: Customer selects Visa Type
        VisaApplication visa = new VisaApplication();
        visa.setBooking(booking);
        visa.setVisaType(type);
        visa.setStatus(VisaStatus.PENDING_DOCUMENTS);
        return repository.save(visa);
    }

    /**
     * Executed by a Visa Officer after inspecting the customer's uploaded documents (passport, photos, etc.).
     */
    public VisaApplication markDocumentsVerified(Long visaId) {
        VisaApplication visa = findById(visaId)
                .orElseThrow(() -> new IllegalArgumentException("Visa application not found"));

        if (visa.getStatus() != VisaStatus.PENDING_DOCUMENTS && visa.getStatus() != VisaStatus.DOCUMENTS_SUBMITTED) {
            throw new IllegalStateException("Only applications pending documents can be marked as verified.");
        }

        List<com.travelgo.entity.VisaDocument> documents = visaDocumentRepository.findByVisaApplication_Id(visaId);
        if (documents.isEmpty()) {
            throw new IllegalStateException("Cannot verify documents â€” no documents have been uploaded for this application.");
        }
        for (com.travelgo.entity.VisaDocument doc : documents) {
            doc.setVerified(true);
            visaDocumentRepository.save(doc);
        }

        visa.setStatus(VisaStatus.DOCUMENTS_VERIFIED);
        VisaApplication saved = repository.save(visa);

        // Notify applicant
        sendNotification(saved.getBooking().getUser(), "Visa Documents Verified", 
                "All required documents for your visa application #" + saved.getId() + " have been successfully verified by our visa officer. Official charges will be issued shortly.");

        return saved;
    }

    /**
     * Allows a Visa Officer to issue official embassy and agency documentation charges.
     * 
     * [BUSINESS RULES APPLIED]:
     * 1. Prevents issuing charges if the applicant hasn't uploaded documents yet.
     * 2. Prevents modifying charges if the customer has already paid.
     * 3. Rule 8: Generates a 24-hour payment deadline (paymentExpiresAt = now + 24h).
     * 4. Moves status to PAYMENT_PENDING.
     * 5. Sends invoice notification to customer.
     */
    public VisaApplication setVisaCharges(Long visaId, BigDecimal visaCharge, BigDecimal docCharge) {
        VisaApplication visa = findById(visaId)
                .orElseThrow(() -> new IllegalArgumentException("Visa application not found"));

        // Check if applicant has uploaded required documents
        List<com.travelgo.entity.VisaDocument> documents = visaDocumentRepository.findByVisaApplication_Id(visaId);
        if (documents.isEmpty()) {
            throw new IllegalStateException("Cannot issue visa charges: Applicant has not uploaded any documents yet.");
        }

        // Check if VISA_DOCUMENTATION payment is already PAID
        if (paymentService.hasPaidVisaAndDocumentation(visa.getBooking().getId())) {
            throw new IllegalStateException("Cannot modify charges. Visa documentation payment is already PAID.");
        }

        // Rule 4, 6: Officer manually sets charges according to requested package
        visa.setVisaCharge(visaCharge);
        visa.setDocumentationCharge(docCharge);
        visa.setStatus(VisaStatus.PAYMENT_PENDING);
        // Rule 8: Visa & Documentation payment expires after 24 hours
        visa.setPaymentExpiresAt(LocalDateTime.now().plusHours(24));
        
        VisaApplication saved = repository.save(visa);

        // Notify applicant
        BigDecimal total = (visaCharge != null ? visaCharge : BigDecimal.ZERO)
                .add(docCharge != null ? docCharge : BigDecimal.ZERO);
        String packageName = (saved.getBooking() != null && saved.getBooking().getTourPackage() != null)
                ? saved.getBooking().getTourPackage().getName() : "Requested Tour";
        sendNotification(saved.getBooking().getUser(), "Visa Charges Issued",
                "Official visa charges totaling $" + total + " ($" + visaCharge + " Embassy Fee + $" + docCharge + " Agency Fee) have been issued for application #" + saved.getId() + " (" + packageName + "). Please complete payment within 24 hours to proceed with processing.");

        return saved;
    }

    /**
     * Allows a Visa Officer to reject insufficient uploads and request additional documents.
     * Reverts status back to PENDING_DOCUMENTS with the officer's instructions/remarks.
     */
    public VisaApplication requestAdditionalDocuments(Long visaId, String remarks) {
        VisaApplication visa = findById(visaId)
                .orElseThrow(() -> new IllegalArgumentException("Visa application not found"));

        if (visa.getStatus() == VisaStatus.APPROVED || visa.getStatus() == VisaStatus.REJECTED) {
            throw new IllegalStateException("Cannot request documents for an application that is already " + visa.getStatus());
        }

        visa.setOfficerRemarks(remarks);
        visa.setStatus(VisaStatus.PENDING_DOCUMENTS);
        VisaApplication saved = repository.save(visa);

        // Notify applicant
        String packageName = (saved.getBooking() != null && saved.getBooking().getTourPackage() != null)
                ? saved.getBooking().getTourPackage().getName() : "Requested Tour";
        sendNotification(saved.getBooking().getUser(), "Additional Visa Documents Requested",
                "Our Visa Officer has reviewed your application #" + saved.getId() + " (" + packageName + ") and requested additional documentation: \"" 
                + (remarks != null && !remarks.isBlank() ? remarks : "Please provide required/updated documents") + "\". Please upload the required documents in your visa portal.");

        return saved;
    }

    /**
     * Moves visa application into active embassy processing.
     * 
     * [SECURITY CHECK]:
     * - Strictly verifies that the customer has completed the VISA_DOCUMENTATION payment first!
     */
    public VisaApplication startProcessing(Long visaId) {
        VisaApplication visa = findById(visaId)
                .orElseThrow(() -> new IllegalArgumentException("Visa application not found"));

        // Correction 2: explicitly verify payment is PAID before setting PROCESSING
        if (!paymentService.hasPaidVisaAndDocumentation(visa.getBooking().getId())) {
            throw new IllegalStateException("Cannot start processing. Required VISA_DOCUMENTATION payment is not PAID.");
        }

        visa.setStatus(VisaStatus.PROCESSING);
        VisaApplication saved = repository.save(visa);

        // Notify applicant
        sendNotification(saved.getBooking().getUser(), "Visa Processing Started",
                "Payment received! Embassy processing has commenced for your visa application #" + saved.getId() + " (" + saved.getBooking().getTourPackage().getName() + ").");

        return saved;
    }

    /**
     * Marks the visa as APPROVED once granted by the embassy.
     * Only applications in PROCESSING state can be approved.
     */
    public VisaApplication approveVisa(Long visaId) {
        VisaApplication visa = findById(visaId)
                .orElseThrow(() -> new IllegalArgumentException("Visa application not found"));
        
        if (visa.getStatus() != VisaStatus.PROCESSING) {
            throw new IllegalStateException("Visa must be in PROCESSING status before approval. Current status: " + visa.getStatus());
        }

        // Rule 10, 12: Approve
        visa.setStatus(VisaStatus.APPROVED);
        VisaApplication saved = repository.save(visa);

        // Rule 12 & 13: Reset packagePaymentDeadline to 3 days from now upon visa approval
        Booking booking = saved.getBooking();
        booking.setPackagePaymentDeadline(LocalDateTime.now().plusDays(3));
        bookingService.save(booking);

        // Notify applicant
        sendNotification(saved.getBooking().getUser(), "Visa Application Approved!",
                "Congratulations! Your visa for " + saved.getBooking().getTourPackage().getName() + " (Application #" + saved.getId() + ") has been approved!");

        return saved;
    }

    /**
     * Handles visa rejection by the embassy.
     * 
     * [BUSINESS RULES APPLIED]:
     * 1. Sets status to REJECTED and records rejection reason.
     * 2. Automatically force-cancels the linked tour booking.
     * 3. Triggers refund calculation for refundable portion (documentation charge).
     * 4. Sends rejection notification to the customer.
     */
    public VisaApplication rejectVisa(Long visaId, String reason, com.travelgo.entity.Payment originalPayment) {
        VisaApplication visa = findById(visaId)
                .orElseThrow(() -> new IllegalArgumentException("Visa application not found"));
        
        // Correction 3: Reject logic
        visa.setStatus(VisaStatus.REJECTED);
        visa.setRejectionReason(reason);
        
        // Mark associated booking as CANCELLED
        bookingService.forceCancel(visa.getBooking().getId());
        
        // Simulate refund for doc charge only
        refundService.simulateVisaRejectionRefund(visa, originalPayment);
        
        VisaApplication saved = repository.save(visa);

        // Notify applicant
        sendNotification(saved.getBooking().getUser(), "Visa Application Update",
                "Your visa application #" + saved.getId() + " was rejected. Reason: " + reason);

        return saved;
    }

    /** Finds all visa applications submitted by a customer */
    public List<VisaApplication> findByUserId(Long userId) {
        return repository.findByBooking_User_Id(userId);
    }

    /** Finds the single visa application linked to a tour booking */
    public Optional<VisaApplication> findByBookingId(Long bookingId) {
        return repository.findByBooking_Id(bookingId);
    }

    /**
     * Saves an in-app notification message into the database for the user.
     */
    private void sendNotification(User user, String title, String message) {
        if (user == null) return;
        try {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setTitle(title);
            notification.setMessage(message);
            notificationService.save(notification);
        } catch (Exception ignored) {
        }
    }
}
