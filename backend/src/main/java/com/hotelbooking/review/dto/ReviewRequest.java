package com.hotelbooking.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReviewRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    @Min(1) @Max(5)
    private Integer cleanlinessRating;

    @Min(1) @Max(5)
    private Integer serviceRating;

    @Min(1) @Max(5)
    private Integer locationRating;

    @Min(1) @Max(5)
    private Integer valueRating;

    private String comment;
}
