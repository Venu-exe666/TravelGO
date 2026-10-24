package com.travelgo.entity;

import com.travelgo.enums.BookingStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Core entity representing a customer's tour package reservation.
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Links the booking to the customer account who placed the reservation.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Links the booking to the reserved tour package.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_package_id", nullable = false)
    private TourPackage tourPackage;

    @Column(nullable = false)
    private LocalDate travelDate;

    @Column(nullable = false)
    private Integer numberOfTravelers;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal packageUnitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPackageAmount;

    /**
     * - Stores enum as text (e.g. 'PENDING', 'CONFIRMED') in SQL instead of numbers (0, 1).
     * - This prevents data corruption if new enum values are inserted in code.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus bookingStatus;

    /**
     * Payment deadline is automatically set to at least 3 days before travel date.
     */
    private LocalDateTime packagePaymentDeadline;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * Sets creation timestamp upon initial booking creation.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Refreshes updatedAt timestamp when booking status or details change.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public TourPackage getTourPackage() { return tourPackage; }
    public void setTourPackage(TourPackage tourPackage) { this.tourPackage = tourPackage; }
    public LocalDate getTravelDate() { return travelDate; }
    public void setTravelDate(LocalDate travelDate) { this.travelDate = travelDate; }
    public Integer getNumberOfTravelers() { return numberOfTravelers; }
    public void setNumberOfTravelers(Integer numberOfTravelers) { this.numberOfTravelers = numberOfTravelers; }
    public BigDecimal getPackageUnitPrice() { return packageUnitPrice; }
    public void setPackageUnitPrice(BigDecimal packageUnitPrice) { this.packageUnitPrice = packageUnitPrice; }
    public BigDecimal getTotalPackageAmount() { return totalPackageAmount; }
    public void setTotalPackageAmount(BigDecimal totalPackageAmount) { this.totalPackageAmount = totalPackageAmount; }
    public BookingStatus getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(BookingStatus bookingStatus) { this.bookingStatus = bookingStatus; }
    public LocalDateTime getPackagePaymentDeadline() { return packagePaymentDeadline; }
    public void setPackagePaymentDeadline(LocalDateTime packagePaymentDeadline) { this.packagePaymentDeadline = packagePaymentDeadline; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
