package com.hotelbooking.review.dto;

import com.hotelbooking.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter @Builder
public class ReviewResponse {

    private Long id;
    private Long userId;
    private String userName;
    private Long hotelId;
    private Long bookingId;
    private Integer rating;
    private Integer cleanlinessRating;
    private Integer serviceRating;
    private Integer locationRating;
    private Integer valueRating;
    private String comment;
    private LocalDateTime createdAt;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFullName())
                .hotelId(review.getHotel().getId())
                .bookingId(review.getBooking().getId())
                .rating(review.getRating())
                .cleanlinessRating(review.getCleanlinessRating())
                .serviceRating(review.getServiceRating())
                .locationRating(review.getLocationRating())
                .valueRating(review.getValueRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
