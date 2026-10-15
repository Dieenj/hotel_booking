package com.hotelbooking.hotel.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class HotelRequest {

    @NotBlank(message = "Hotel name is required")
    @Size(max = 255, message = "Hotel name must be at most 255 characters")
    private String name;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must be at most 255 characters")
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 255, message = "City must be at most 255 characters")
    private String city;

    @Size(max = 255, message = "Country must be at most 255 characters")
    private String country;

    @Size(max = 5000, message = "Description must be at most 5000 characters")
    private String description;

    @NotNull(message = "Star rating is required")
    @Min(value = 1, message = "Star rating must be between 1 and 5")
    @Max(value = 5, message = "Star rating must be between 1 and 5")
    private Integer starRating;

    @Size(max = 50, message = "At most 50 amenities")
    private List<@NotBlank @Size(max = 100, message = "Amenity must be at most 100 characters") String> amenities;

    @Size(max = 20, message = "At most 20 images")
    private List<@NotBlank @Size(max = 255, message = "Image URL must be at most 255 characters") String> images;
}
