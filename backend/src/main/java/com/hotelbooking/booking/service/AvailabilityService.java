package com.hotelbooking.booking.service;

import com.hotelbooking.hotel.entity.RoomType;

import java.time.LocalDate;

public interface AvailabilityService {
    int getAvailableRooms(Long roomTypeId, LocalDate checkIn, LocalDate checkOut);
    boolean isAvailable(Long roomTypeId, LocalDate checkIn, LocalDate checkOut);
    void reserve(RoomType roomType, LocalDate checkIn, LocalDate checkOut);
    void release(RoomType roomType, LocalDate checkIn, LocalDate checkOut);
}
