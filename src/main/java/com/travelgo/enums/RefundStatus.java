package com.travelgo.enums;

/**
 * Tracks customer refund requests after a booking is cancelled.
 * 
 * [VALUES]:
 * - REFUND_PENDING: Cancellation occurred; refund requested and awaiting staff/admin payout.
 * - REFUND_PROCESSED: Money has been successfully returned to the customer's account.
 */
public enum RefundStatus {
    REFUND_PENDING, REFUND_PROCESSED
}
