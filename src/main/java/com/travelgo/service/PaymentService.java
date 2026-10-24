package com.travelgo.service;

import com.travelgo.entity.Booking;
import com.travelgo.entity.Payment;
import com.travelgo.entity.Refund;
import com.travelgo.entity.VisaApplication;
import com.travelgo.enums.PaymentStatus;
import com.travelgo.enums.PaymentType;
import com.travelgo.enums.RefundStatus;
import com.travelgo.enums.VisaStatus;
import com.travelgo.repository.PaymentRepository;
import com.travelgo.repository.RefundRepository;
import com.travelgo.repository.VisaApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository repository;
    private final VisaApplicationRepository visaRepository;
    private final BookingService bookingService;
    private final RefundRepository refundRepository;

    public PaymentService(PaymentRepository repository,
                          VisaApplicationRepository visaRepository,
                          BookingService bookingService,
                          RefundRepository refundRepository) {
        this.repository = repository;
        this.visaRepository = visaRepository;
        this.bookingService = bookingService;
        this.refundRepository = refundRepository;
    }

    public List<Payment> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Payment> findById(Long id) {
        return repository.findById(id);
    }

    public Payment save(Payment entity) {
        return repository.save(entity);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /**
     * System Rule 8 & 9: Staff will NOT process visa documents until this initial fee has been successfully paid.
     */
    public boolean hasPaidVisaAndDocumentation(Long bookingId) {
        return repository.findFirstByBooking_IdAndPaymentTypeAndPaymentStatus(
                bookingId, PaymentType.VISA_DOCUMENTATION, PaymentStatus.PAID
        ).isPresent();
    }

    public List<Payment> findByBookingId(Long bookingId) {
        return repository.findByBooking_IdOrderByCreatedAtDesc(bookingId);
    }

    public List<Payment> findByType(PaymentType type) {
        return repository.findByPaymentTypeOrderByCreatedAtDesc(type);
    }

    public List<Payment> findByStatus(PaymentStatus status) {
        return repository.findByPaymentStatusOrderByCreatedAtDesc(status);
    }

    public List<Payment> getRecentPayments(int limit) {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * and automatically trigger the appropriate workflow progression.
     *
     *      Then calls bookingService.confirmBooking() to officially confirm the booking!
     *    - If VISA_DOCUMENTATION: If visa application was in PAYMENT_PENDING status, advances it to PROCESSING.
     * 6. Returns the saved Payment.
     *
     * Enforces the two-stage payment model: Full package cannot be marked paid if visa is not approved.
     */
    public Payment recordManualPayment(Long bookingId,
                                       PaymentType paymentType,
                                       BigDecimal amount,
                                       String paymentMethod,
                                       String transactionReference,
                                       PaymentStatus paymentStatus,
                                       LocalDateTime paidAt,
                                       String remarks) {
        Booking booking = bookingService.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentType(paymentType);
        payment.setAmount(amount != null ? amount : BigDecimal.ZERO);
        payment.setPaymentMethod(paymentMethod != null && !paymentMethod.isBlank() ? paymentMethod : "Manual Entry");

        if (transactionReference != null && !transactionReference.isBlank()) {
            payment.setTransactionReference(transactionReference.trim());
        } else {
            payment.setTransactionReference("OFFLINE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        PaymentStatus status = paymentStatus != null ? paymentStatus : PaymentStatus.PAID;
        payment.setPaymentStatus(status);

        if (status == PaymentStatus.PAID) {
            payment.setPaidAt(paidAt != null ? paidAt : LocalDateTime.now());
        }

        Payment saved = repository.save(payment);

        // Apply downstream business logic if marked as PAID
        if (status == PaymentStatus.PAID) {
            if (paymentType == PaymentType.FULL_PACKAGE) {
                // Verify visa is approved before confirming booking
                Optional<VisaApplication> visaOpt = visaRepository.findByBooking_Id(booking.getId());
                if (visaOpt.isEmpty() || visaOpt.get().getStatus() != VisaStatus.APPROVED) {
                    throw new IllegalStateException("Cannot confirm booking: Visa application must exist and be APPROVED.");
                }
                bookingService.markPaidAwaitingTicketIssue(booking.getId());
            } else if (paymentType == PaymentType.VISA_DOCUMENTATION) {
                // If visa application exists and is PAYMENT_PENDING, advance to PROCESSING
                Optional<VisaApplication> visaOpt = visaRepository.findByBooking_Id(booking.getId());
                visaOpt.ifPresent(v -> {
                    if (v.getStatus() == VisaStatus.PAYMENT_PENDING) {
                        v.setStatus(VisaStatus.PROCESSING);
                        visaRepository.save(v);
                    }
                });
            }
        }

        return saved;
    }

    public Payment updatePaymentStatus(Long paymentId,
                                       PaymentStatus newStatus,
                                       String transactionReference,
                                       String remarks) {
        Payment payment = repository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment record not found with ID: " + paymentId));

        PaymentStatus oldStatus = payment.getPaymentStatus();
        payment.setPaymentStatus(newStatus);

        if (transactionReference != null && !transactionReference.isBlank()) {
            payment.setTransactionReference(transactionReference.trim());
        }

        if (newStatus == PaymentStatus.PAID && payment.getPaidAt() == null) {
            payment.setPaidAt(LocalDateTime.now());
        }

        Payment saved = repository.save(payment);

        // Transition side effects
        if (newStatus == PaymentStatus.PAID && oldStatus != PaymentStatus.PAID) {
            if (saved.getPaymentType() == PaymentType.FULL_PACKAGE) {
                Optional<VisaApplication> visaOpt = visaRepository.findByBooking_Id(saved.getBooking().getId());
                if (visaOpt.isEmpty() || visaOpt.get().getStatus() != VisaStatus.APPROVED) {
                    throw new IllegalStateException("Cannot confirm booking: Visa application must exist and be APPROVED.");
                }
                bookingService.markPaidAwaitingTicketIssue(saved.getBooking().getId());
            } else if (saved.getPaymentType() == PaymentType.VISA_DOCUMENTATION) {
                visaRepository.findByBooking_Id(saved.getBooking().getId()).ifPresent(visa -> {
                    if (visa.getStatus() == VisaStatus.PAYMENT_PENDING) {
                        visa.setStatus(VisaStatus.PROCESSING);
                        visaRepository.save(visa);
                    }
                });
            }
        } else if (newStatus == PaymentStatus.REFUNDED) {
            // Ensure a refund record exists
            if (refundRepository.findByPayment_Id(saved.getId()).isEmpty()) {
                Refund refund = new Refund();
                refund.setPayment(saved);
                refund.setAmount(saved.getAmount());
                refund.setReason(remarks != null && !remarks.isBlank() ? remarks : "Staff processed manual refund");
                refund.setStatus(RefundStatus.REFUND_PROCESSED);
                refund.setProcessedAt(LocalDateTime.now());
                refundRepository.save(refund);
            }
        }

        return saved;
    }

    public BigDecimal getTotalRevenue() {
        return repository.findAll().stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PAID)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getPendingReceivables() {
        return repository.findAll().stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PENDING)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getVisaPaymentsTotal() {
        return repository.findAll().stream()
                .filter(p -> p.getPaymentType() == PaymentType.VISA_DOCUMENTATION && p.getPaymentStatus() == PaymentStatus.PAID)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getPackagePaymentsTotal() {
        return repository.findAll().stream()
                .filter(p -> p.getPaymentType() == PaymentType.FULL_PACKAGE && p.getPaymentStatus() == PaymentStatus.PAID)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Key method demonstrating system integrity: Customers cannot pay for a package or confirm a booking unless
     * their visa has been formally approved by staff.
     */
    public Payment processPayment(Payment payment) {
        if (payment.getPaymentType() == PaymentType.VISA_DOCUMENTATION) {
            if (payment.getExpiresAt() != null && LocalDateTime.now().isAfter(payment.getExpiresAt())) {
                payment.setPaymentStatus(PaymentStatus.EXPIRED);
                return repository.save(payment);
            }
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
            Payment saved = repository.save(payment);
            visaRepository.findByBooking_Id(payment.getBooking().getId()).ifPresent(v -> {
                v.setStatus(VisaStatus.PROCESSING);
                visaRepository.save(v);
            });
            return saved;
        }

        if (payment.getPaymentType() == PaymentType.FULL_PACKAGE) {
            Optional<VisaApplication> visaOpt = visaRepository.findByBooking_Id(payment.getBooking().getId());
            if (visaOpt.isEmpty() || visaOpt.get().getStatus() != VisaStatus.APPROVED) {
                throw new IllegalStateException("Cannot process full package payment: A visa application must exist and be APPROVED.");
            }

            LocalDateTime deadline = payment.getBooking().getPackagePaymentDeadline();
            if (deadline != null && LocalDateTime.now().isAfter(deadline)) {
                throw new IllegalStateException("Package payment deadline has passed.");
            }

            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
            Payment savedPayment = repository.save(payment);

            bookingService.markPaidAwaitingTicketIssue(payment.getBooking().getId());

            return savedPayment;
        }

        return repository.save(payment);
    }
}
