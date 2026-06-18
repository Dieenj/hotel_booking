package com.hotelbooking.review.repository;

import com.hotelbooking.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByHotelId(Long hotelId, Pageable pageable);

    Page<Review> findByUserId(Long userId, Pageable pageable);

    boolean existsByBookingId(Long bookingId);

    Optional<Review> findByBookingId(Long bookingId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.hotel.id = :hotelId")
    Double findAverageRatingByHotelId(@Param("hotelId") Long hotelId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.hotel.id = :hotelId")
    long countByHotelId(@Param("hotelId") Long hotelId);
}
