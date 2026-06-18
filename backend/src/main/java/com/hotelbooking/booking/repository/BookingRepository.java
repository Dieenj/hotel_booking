package com.hotelbooking.booking.repository;

import com.hotelbooking.booking.entity.Booking;
import com.hotelbooking.booking.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    Page<Booking> findByUserIdAndStatus(Long userId, BookingStatus status, Pageable pageable);

    Optional<Booking> findByBookingCode(String bookingCode);

    boolean existsByRoomTypeIdAndStatusNotAndCheckInLessThanAndCheckOutGreaterThan(
            Long roomTypeId, BookingStatus status, LocalDate checkOut, LocalDate checkIn
    );

    @Query("""
            SELECT b FROM Booking b
            WHERE (:status IS NULL OR b.status = :status)
            AND (:hotelId IS NULL OR b.roomType.hotel.id = :hotelId)
            ORDER BY b.createdAt DESC
            """)
    Page<Booking> findAllByFilters(
            @Param("status") BookingStatus status,
            @Param("hotelId") Long hotelId,
            Pageable pageable
    );

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.createdAt >= :startDate AND b.status != 'CANCELLED'")
    long countActiveBookingsSince(@Param("startDate") java.time.LocalDateTime startDate);

    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.createdAt >= :startDate AND b.status = 'CONFIRMED'")
    java.math.BigDecimal sumRevenueSince(@Param("startDate") java.time.LocalDateTime startDate);

    List<Booking> findTop10ByOrderByCreatedAtDesc();
}
