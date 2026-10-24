package com.travelgo.service;

import com.travelgo.entity.Booking;
import com.travelgo.entity.Payment;
import com.travelgo.enums.BookingStatus;
import com.travelgo.enums.PaymentStatus;
import com.travelgo.enums.PaymentType;
import com.travelgo.repository.BookingRepository;
import com.travelgo.repository.PaymentRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingServiceTicketReleaseTests {

    @Test
    void officerCanIssueTicketsOnlyAfterFullPayment() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        BookingService service = new BookingService(bookingRepository, paymentRepository);
        Booking booking = new Booking();
        booking.setId(15L);
        booking.setBookingStatus(BookingStatus.PAID_AWAITING_TICKETS);
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentType(PaymentType.FULL_PACKAGE);
        payment.setPaymentStatus(PaymentStatus.PAID);

        when(bookingRepository.findById(15L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBooking_Id(15L)).thenReturn(List.of(payment));
        when(bookingRepository.save(booking)).thenReturn(booking);

        Booking released = service.issueTickets(15L);

        assertEquals(BookingStatus.CONFIRMED, released.getBookingStatus());
        verify(bookingRepository).save(booking);
    }
}
