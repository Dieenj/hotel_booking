package com.hotelbooking.review.service;

import com.hotelbooking.common.response.PageResponse;
import com.hotelbooking.review.dto.ReviewRequest;
import com.hotelbooking.review.dto.ReviewResponse;

public interface ReviewService {
    PageResponse<ReviewResponse> getHotelReviews(Long hotelId, int page, int size);
    PageResponse<ReviewResponse> getMyReviews(String email, int page, int size);
    ReviewResponse createReview(String userEmail, ReviewRequest request);
}
