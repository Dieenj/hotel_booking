package com.hotelbooking.review.controller;

import com.hotelbooking.common.response.ApiResponse;
import com.hotelbooking.common.response.PageResponse;
import com.hotelbooking.review.dto.ReviewRequest;
import com.hotelbooking.review.dto.ReviewResponse;
import com.hotelbooking.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Review management endpoints")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/api/hotels/{hotelId}/reviews")
    @Operation(summary = "Get reviews for a hotel")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getHotelReviews(
            @PathVariable Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getHotelReviews(hotelId, page, size)));
    }

    @GetMapping("/api/reviews/my")
    @Operation(summary = "Get my reviews")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                reviewService.getMyReviews(userDetails.getUsername(), page, size)
        ));
    }

    @PostMapping("/api/reviews")
    @Operation(summary = "Create a review for a completed booking")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ReviewResponse response = reviewService.createReview(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }
}
