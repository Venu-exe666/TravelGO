package com.travelgo.enums;

/**
 * Represents the complete workflow states of a visa application.
 * 
 * [WORKFLOW STEPS]:
 * 1. PENDING_DOCUMENTS: Application initiated; customer has not yet uploaded required documents (passport, photo, etc.).
 * 2. DOCUMENTS_SUBMITTED: Customer uploaded all required files.
 * 3. DOCUMENTS_VERIFIED: Visa Officer reviewed the files and verified they are valid.
 * 4. PAYMENT_PENDING: Visa processing fee invoice generated.
 * 5. PROCESSING: Application submitted to embassy/consulate for review.
 * 6. APPROVED: Visa granted by embassy.
 * 7. REJECTED: Visa application rejected by embassy.
 * 8. EXPIRED: Application timed out without submission.
 */
public enum VisaStatus {
    PENDING_DOCUMENTS, DOCUMENTS_SUBMITTED, DOCUMENTS_VERIFIED, PAYMENT_PENDING, PROCESSING, APPROVED, REJECTED, EXPIRED
}
