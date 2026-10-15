package com.hotelbooking.hotel.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter
public class RoomTypeRequest {

    @NotBlank(message = "Room type name is required")
    @Size(max = 255, message = "Room type name must be at most 255 characters")
    private String name;

    @Size(max = 5000, message = "Description must be at most 5000 characters")
    private String description;

    @NotNull(message = "Max capacity is required")
    @Min(value = 1, message = "Max capacity must be at least 1")
    private Integer maxCapacity;

    @Positive(message = "Area must be greater than 0")
    private Double area;

    @NotNull(message = "Base price is required")
    @Positive(message = "Base price must be greater than 0")
    private BigDecimal basePrice;

    @NotNull(message = "Total rooms is required")
    @Min(value = 1, message = "Total rooms must be at least 1")
    private Integer totalRooms;

    @Size(max = 50, message = "At most 50 amenities")
    private List<@NotBlank @Size(max = 100, message = "Amenity must be at most 100 characters") String> amenities;

    @Size(max = 20, message = "At most 20 images")
    private List<@NotBlank @Size(max = 255, message = "Image URL must be at most 255 characters") String> images;
}
