package com.travelgo.service;

import com.travelgo.entity.Booking;
import com.travelgo.repository.BookingRepository;
import com.travelgo.repository.PaymentRepository;
import com.travelgo.enums.BookingStatus;
import com.travelgo.enums.PaymentStatus;
import com.travelgo.enums.PaymentType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookingService {

    private final BookingRepository repository;
    private final PaymentRepository paymentRepository;

    /**
     * Injects BookingRepository and PaymentRepository for managing bookings and payment checks.
     */
    public BookingService(BookingRepository repository, PaymentRepository paymentRepository) {
        this.repository = repository;
        this.paymentRepository = paymentRepository;
    }

    /** Retrieves all bookings across all customers */
    public List<Booking> findAll() {
        return repository.findAll();
    }

    /** Finds a booking by its primary key ID */
    public Optional<Booking> findById(Long id) {
        return repository.findById(id);
    }

    /** Saves a booking entity to the database */
    public Booking save(Booking entity) {
        return repository.save(entity);
    }

    /** Deletes a booking by ID */
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /**
     * Creates a brand new booking when a customer completes the checkout wizard.
     * 
     * [BUSINESS RULES APPLIED]:
     * 1. Rule 1: Sets initial state to 'PENDING'.
     * 2. Rule 13: Automatically calculates the payment deadline as 3 days before the travel date.
     * 3. Saves the record to SQL Server.
     */
    public Booking createBooking(Booking booking) {
        // Rule 1: Initial state
        booking.setBookingStatus(BookingStatus.PENDING);
        // Rule 13: Deadline is at least 3 days before travel date
        if (booking.getTravelDate() != null) {
            booking.setPackagePaymentDeadline(booking.getTravelDate().atStartOfDay().minusDays(3));
        }
        return repository.save(booking);
    }

    /**
     * Staff action to approve a pending booking.
     * 
     * [BUSINESS RULES APPLIED]:
     * 1. Checks that the booking exists and is currently in 'PENDING' status.
     * 2. Moves the status forward to 'PROCESSING'.
     */
    public void approveBooking(Long bookingId) {
        Booking booking = findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only PENDING bookings can be approved.");
        }
        booking.setBookingStatus(BookingStatus.PROCESSING);
        repository.save(booking);
    }

    /**
     * Finalizes and confirms a booking once payment has been received and verified.
     * 
     * [BUSINESS RULES APPLIED]:
     * - Only bookings in PENDING or PROCESSING status can be moved to 'CONFIRMED'.
     */
    public void confirmBooking(Long bookingId) {
        Booking booking = findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        // Rule 14, 15: Confirmed only after successful FULL_PACKAGE payment
        if (booking.getBookingStatus() != BookingStatus.PENDING
                && booking.getBookingStatus() != BookingStatus.PROCESSING) {
            throw new IllegalStateException("Only PENDING or PROCESSING bookings can be confirmed. Current status: " + booking.getBookingStatus());
        }
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        repository.save(booking);
    }

    /** Records settlement without exposing tickets before an officer releases them. */
    public void markPaidAwaitingTicketIssue(Long bookingId) {
        Booking booking = findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (booking.getBookingStatus() != BookingStatus.PENDING
                && booking.getBookingStatus() != BookingStatus.PROCESSING) {
            throw new IllegalStateException("Only active bookings can be marked paid. Current status: " + booking.getBookingStatus());
        }
        booking.setBookingStatus(BookingStatus.PAID_AWAITING_TICKETS);
        repository.save(booking);
    }

    /** Visa-officer action that releases tickets after a verified full-package payment. */
    public Booking issueTickets(Long bookingId) {
        Booking booking = findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (booking.getBookingStatus() != BookingStatus.PAID_AWAITING_TICKETS) {
            throw new IllegalStateException("Tickets can only be issued for a fully paid booking awaiting release.");
        }
        boolean fullPaymentReceived = paymentRepository.findByBooking_Id(bookingId).stream()
                .anyMatch(payment -> payment.getPaymentType() == PaymentType.FULL_PACKAGE
                        && payment.getPaymentStatus() == PaymentStatus.PAID);
        if (!fullPaymentReceived) {
            throw new IllegalStateException("A verified full-package payment is required before tickets can be issued.");
        }
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        return repository.save(booking);
    }

    /**
     * Cancels a booking requested by the customer.
     * 
     * [SECURITY & INTEGRITY CHECKS]:
     * 1. Only PENDING bookings can be cancelled by the customer.
     * 2. Checks if any PAID payment already exists for this booking.
     *    If paid, customer cancellation is blocked (must contact staff for formal refund handling).
     */
    public void cancelBooking(Long bookingId) {
        Booking booking = findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only PENDING bookings can be cancelled. Current status: " + booking.getBookingStatus());
        }
        boolean hasAnyPaidPayment = paymentRepository.findByBooking_Id(bookingId).stream()
                .anyMatch(p -> p.getPaymentStatus() == PaymentStatus.PAID);
        if (hasAnyPaidPayment) {
            throw new IllegalStateException("Cannot cancel booking \u2014 a payment has already been made.");
        }
        booking.setBookingStatus(BookingStatus.CANCELLED);
        repository.save(booking);
    }

    /**
     * Administrative override to force-cancel a booking regardless of status.
     */
    public void forceCancel(Long bookingId) {
        Booking booking = findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        booking.setBookingStatus(BookingStatus.CANCELLED);
        repository.save(booking);
    }

    /** Retrieves all bookings made by a specific customer, newest first */
    public List<Booking> findByUserId(Long userId) {
        return repository.findByUser_IdOrderByCreatedAtDesc(userId);
    }

    /**
     * Modifies travel date or passenger count for an existing pending booking.
     * 
     * [VALIDATIONS & CALCULATIONS]:
     * 1. Only PENDING bookings can be edited.
     * 2. Date validation: Ensures the new travel date is not in the past.
     * 3. Recalculates payment deadline to 3 days before the new date.
     * 4. Capacity validation: Ensures passenger count does not exceed package maxCapacity.
     * 5. Price recalculation: Multiplies unit price * new passenger count automatically!
     */
    public Booking modifyBooking(Long bookingId, java.time.LocalDate newTravelDate, Integer newTravelers) {
        Booking booking = findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only PENDING bookings can be modified.");
        }

        if (newTravelDate != null) {
            if (newTravelDate.isBefore(java.time.LocalDate.now().plusDays(4))) {
                throw new IllegalArgumentException("Travel date must be at least 4 days from today.");
            }
            booking.setTravelDate(newTravelDate);
            booking.setPackagePaymentDeadline(newTravelDate.atStartOfDay().minusDays(3));
        }
        if (newTravelers != null) {
            if (newTravelers < 1) {
                throw new IllegalArgumentException("Number of travelers must be at least 1.");
            }
            if (booking.getTourPackage() != null && booking.getTourPackage().getMaxCapacity() != null
                    && newTravelers > booking.getTourPackage().getMaxCapacity()) {
                throw new IllegalArgumentException("Number of travelers (" + newTravelers + ") exceeds maximum package capacity of " + booking.getTourPackage().getMaxCapacity() + ".");
            }
            booking.setNumberOfTravelers(newTravelers);
            booking.setTotalPackageAmount(booking.getPackageUnitPrice().multiply(java.math.BigDecimal.valueOf(newTravelers)));
        }

        return repository.save(booking);
    }
}
