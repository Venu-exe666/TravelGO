package com.travelgo.enums;

/**
 * Represents the payment status for invoices and transactions.
 * 
 * [STATE VALUES]:
 * - PENDING: Invoice generated, awaiting customer payment.
 * - PAID: Payment successfully received and confirmed.
 * - FAILED: Payment attempt was declined or encountered a gateway error.
 * - EXPIRED: Invoice was not paid within the allowed time limit.
 * - REFUNDED: Money was refunded back to the customer following a cancellation.
 */
public enum PaymentStatus {
    PENDING, PAID, FAILED, EXPIRED, REFUNDED
}
