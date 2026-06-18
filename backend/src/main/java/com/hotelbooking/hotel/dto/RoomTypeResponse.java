package com.hotelbooking.hotel.dto;

import com.hotelbooking.hotel.entity.RoomType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter @Builder
public class RoomTypeResponse {

    private Long id;
    private Long hotelId;
    private String hotelName;
    private String name;
    private String description;
    private Integer maxCapacity;
    private Double area;
    private BigDecimal basePrice;
    private Integer totalRooms;
    private Integer availableRooms;
    private List<String> amenities;
    private List<String> images;
    private boolean active;

    public static RoomTypeResponse from(RoomType roomType) {
        return RoomTypeResponse.builder()
                .id(roomType.getId())
                .hotelId(roomType.getHotel().getId())
                .hotelName(roomType.getHotel().getName())
                .name(roomType.getName())
                .description(roomType.getDescription())
                .maxCapacity(roomType.getMaxCapacity())
                .area(roomType.getArea())
                .basePrice(roomType.getBasePrice())
                .totalRooms(roomType.getTotalRooms())
                .amenities(roomType.getAmenities())
                .images(roomType.getImages())
                .active(roomType.isActive())
                .build();
    }

    public static RoomTypeResponse from(RoomType roomType, Integer availableRooms) {
        RoomTypeResponse response = from(roomType);
        return RoomTypeResponse.builder()
                .id(response.getId())
                .hotelId(response.getHotelId())
                .hotelName(response.getHotelName())
                .name(response.getName())
                .description(response.getDescription())
                .maxCapacity(response.getMaxCapacity())
                .area(response.getArea())
                .basePrice(response.getBasePrice())
                .totalRooms(response.getTotalRooms())
                .availableRooms(availableRooms)
                .amenities(response.getAmenities())
                .images(response.getImages())
                .active(response.isActive())
                .build();
    }
}
