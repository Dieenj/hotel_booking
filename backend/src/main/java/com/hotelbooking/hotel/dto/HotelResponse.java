package com.hotelbooking.hotel.dto;

import com.hotelbooking.hotel.entity.Hotel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Builder
public class HotelResponse {

    private Long id;
    private String name;
    private String address;
    private String city;
    private String country;
    private String description;
    private Integer starRating;
    private List<String> amenities;
    private List<String> images;
    private boolean active;
    private Double averageRating;
    private Long reviewCount;
    private LocalDateTime createdAt;

    public static HotelResponse from(Hotel hotel) {
        return HotelResponse.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .address(hotel.getAddress())
                .city(hotel.getCity())
                .country(hotel.getCountry())
                .description(hotel.getDescription())
                .starRating(hotel.getStarRating())
                .amenities(hotel.getAmenities())
                .images(hotel.getImages())
                .active(hotel.isActive())
                .createdAt(hotel.getCreatedAt())
                .build();
    }

    public static HotelResponse from(Hotel hotel, Double averageRating, Long reviewCount) {
        return HotelResponse.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .address(hotel.getAddress())
                .city(hotel.getCity())
                .country(hotel.getCountry())
                .description(hotel.getDescription())
                .starRating(hotel.getStarRating())
                .amenities(hotel.getAmenities())
                .images(hotel.getImages())
                .active(hotel.isActive())
                .averageRating(averageRating)
                .reviewCount(reviewCount)
                .createdAt(hotel.getCreatedAt())
                .build();
    }
}
