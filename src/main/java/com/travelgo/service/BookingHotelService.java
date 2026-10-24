package com.travelgo.service;

import com.travelgo.entity.BookingHotel;
import com.travelgo.repository.BookingHotelRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookingHotelService {

    private final BookingHotelRepository repository;

    /**
     * Injects BookingHotelRepository.
     */
    public BookingHotelService(BookingHotelRepository repository) {
        this.repository = repository;
    }

    /** Retrieves all hotel bookings */
    public List<BookingHotel> findAll() {
        return repository.findAll();
    }

    /** Finds a hotel reservation record by its ID */
    public Optional<BookingHotel> findById(Long id) {
        return repository.findById(id);
    }

    /** Saves or updates a hotel reservation for a booking */
    public BookingHotel save(BookingHotel entity) {
        return repository.save(entity);
    }

    /** Deletes a hotel reservation by ID */
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /**
     * Finds the hotel reservation details linked to a specific booking ID.
     */
    public Optional<BookingHotel> findByBookingId(Long bookingId) {
        return repository.findByBooking_Id(bookingId);
    }
}
