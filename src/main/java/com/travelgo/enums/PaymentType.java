package com.travelgo.enums;

/**
 * Differentiates between what kind of fee is being paid.
 * 
 * [VALUES]:
 * - VISA_DOCUMENTATION: Standalone fee for visa document verification and processing.
 * - FULL_PACKAGE: Total charge for the entire tour package, flights, and hotel bookings.
 */
public enum PaymentType {
    VISA_DOCUMENTATION, FULL_PACKAGE
}
