package com.hotelbooking.booking.repository;

import com.hotelbooking.booking.entity.Booking;
import com.hotelbooking.booking.enums.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // List queries fetch roomType + hotel up front: BookingResponse reads both for every row.

    @EntityGraph(attributePaths = {"roomType", "roomType.hotel"})
    Page<Booking> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"roomType", "roomType.hotel"})
    Page<Booking> findByUserIdAndStatusIn(Long userId, Collection<BookingStatus> statuses, Pageable pageable);

    Optional<Booking> findByBookingCode(String bookingCode);

    boolean existsByBookingCode(String bookingCode);

    /** Loads a booking with a row lock so concurrent status changes are serialized. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Booking> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime cutoff);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Booking> findByStatusInAndCheckOutLessThanEqual(Collection<BookingStatus> statuses, LocalDate date);

    @EntityGraph(attributePaths = {"roomType", "roomType.hotel"})
    @Query("""
            SELECT b FROM Booking b
            WHERE (:status IS NULL OR b.status = :status)
            AND (:hotelId IS NULL OR b.roomType.hotel.id = :hotelId)
            """)
    Page<Booking> findAllByFilters(
            @Param("status") BookingStatus status,
            @Param("hotelId") Long hotelId,
            Pageable pageable
    );

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.createdAt >= :startDate AND b.status != 'CANCELLED'")
    long countActiveBookingsSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.createdAt >= :startDate AND b.status IN :statuses")
    BigDecimal sumRevenueSince(@Param("startDate") LocalDateTime startDate,
                               @Param("statuses") Collection<BookingStatus> statuses);

    @EntityGraph(attributePaths = {"roomType", "roomType.hotel"})
    List<Booking> findTop10ByOrderByCreatedAtDesc();
}
