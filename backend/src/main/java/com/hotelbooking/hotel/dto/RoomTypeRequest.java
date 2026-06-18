package com.hotelbooking.hotel.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter
public class RoomTypeRequest {

    @NotBlank(message = "Room type name is required")
    private String name;

    private String description;

    @NotNull(message = "Max capacity is required")
    @Min(value = 1, message = "Max capacity must be at least 1")
    private Integer maxCapacity;

    private Double area;

    @NotNull(message = "Base price is required")
    @Min(value = 0, message = "Base price must be positive")
    private BigDecimal basePrice;

    @NotNull(message = "Total rooms is required")
    @Min(value = 1, message = "Total rooms must be at least 1")
    private Integer totalRooms;

    private List<String> amenities;
    private List<String> images;
}
