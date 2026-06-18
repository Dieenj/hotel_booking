package com.hotelbooking.review.entity;

import com.hotelbooking.auth.entity.User;
import com.hotelbooking.booking.entity.Booking;
import com.hotelbooking.common.entity.AuditableEntity;
import com.hotelbooking.hotel.entity.Hotel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "reviews")
@Getter
@SuperBuilder
@NoArgsConstructor
public class Review extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(nullable = false)
    private Integer rating;

    private Integer cleanlinessRating;
    private Integer serviceRating;
    private Integer locationRating;
    private Integer valueRating;

    @Column(columnDefinition = "TEXT")
    private String comment;
}
