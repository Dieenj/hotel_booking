package com.hotelbooking.review.repository;

/** Aggregated review stats for one hotel. */
public interface HotelRatingSummary {
    Long getHotelId();
    Double getAverageRating();
    Long getReviewCount();
}
