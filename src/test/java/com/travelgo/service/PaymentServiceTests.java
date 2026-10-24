package com.travelgo.service;

import com.travelgo.entity.Booking;
import com.travelgo.entity.Payment;
import com.travelgo.entity.VisaApplication;
import com.travelgo.enums.PaymentStatus;
import com.travelgo.enums.PaymentType;
import com.travelgo.enums.VisaStatus;
import com.travelgo.repository.PaymentRepository;
import com.travelgo.repository.RefundRepository;
import com.travelgo.repository.VisaApplicationRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceTests {

    @Test
    void fullPackagePaymentWaitsForOfficerTicketRelease() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        VisaApplicationRepository visaRepository = mock(VisaApplicationRepository.class);
        BookingService bookingService = mock(BookingService.class);
        RefundRepository refundRepository = mock(RefundRepository.class);
        PaymentService service = new PaymentService(paymentRepository, visaRepository, bookingService, refundRepository);

        Booking booking = new Booking();
        booking.setId(91L);
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentType(PaymentType.FULL_PACKAGE);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        VisaApplication visa = new VisaApplication();
        visa.setBooking(booking);
        visa.setStatus(VisaStatus.APPROVED);

        when(visaRepository.findByBooking_Id(91L)).thenReturn(Optional.of(visa));
        when(paymentRepository.save(payment)).thenReturn(payment);

        service.processPayment(payment);

        assertEquals(PaymentStatus.PAID, payment.getPaymentStatus());
        verify(bookingService).markPaidAwaitingTicketIssue(91L);
    }

    @Test
    void clearingVisaPaymentAdvancesVisaToProcessing() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        VisaApplicationRepository visaRepository = mock(VisaApplicationRepository.class);
        BookingService bookingService = mock(BookingService.class);
        RefundRepository refundRepository = mock(RefundRepository.class);
        PaymentService service = new PaymentService(paymentRepository, visaRepository, bookingService, refundRepository);

        Booking booking = new Booking();
        booking.setId(42L);

        Payment payment = new Payment();
        payment.setId(7L);
        payment.setBooking(booking);
        payment.setPaymentType(PaymentType.VISA_DOCUMENTATION);
        payment.setPaymentStatus(PaymentStatus.PENDING);

        VisaApplication visa = new VisaApplication();
        visa.setBooking(booking);
        visa.setStatus(VisaStatus.PAYMENT_PENDING);

        when(paymentRepository.findById(7L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(visaRepository.findByBooking_Id(42L)).thenReturn(Optional.of(visa));

        service.updatePaymentStatus(7L, PaymentStatus.PAID, "WIRE-42", "Cleared");

        assertEquals(VisaStatus.PROCESSING, visa.getStatus());
        verify(visaRepository).save(visa);
    }
}
