package com.travelgo.enums;

/**
 * Represents the lifecycle stages of a customer tour booking.
 * 
 * [STATE TRANSITIONS]:
 * - PENDING: Initial state when customer reserves a package.
 * - PROCESSING: Staff is reviewing details or awaiting payment.
 * - PAID_AWAITING_TICKETS: Full payment received; visa officer must release the travel documents.
 * - CONFIRMED: Tickets and booking details have been released to the customer.
 * - EXPIRED: Customer did not pay before the payment deadline.
 * - CANCELLED: Booking was cancelled by customer or staff.
 */
public enum BookingStatus {
    PENDING, PROCESSING, PAID_AWAITING_TICKETS, CONFIRMED, EXPIRED, CANCELLED
}
