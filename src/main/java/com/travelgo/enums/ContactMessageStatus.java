package com.travelgo.enums;

/**
 * Status lifecycle of a customer inquiry / support ticket submitted via the contact form.
 * 
 * [VALUES]:
 * - OPEN: New message received, awaiting staff attention.
 * - IN_PROGRESS: A staff consultant is investigating or drafting a response.
 * - RESOLVED: The staff member replied to the customer's question.
 * - CLOSED: Support ticket archived.
 */
public enum ContactMessageStatus {
    OPEN, IN_PROGRESS, RESOLVED, CLOSED
}
