package com.hotelbooking.hotel.service;

import com.hotelbooking.hotel.dto.RoomTypeRequest;
import com.hotelbooking.hotel.dto.RoomTypeResponse;
import com.hotelbooking.hotel.entity.RoomType;

import java.time.LocalDate;
import java.util.List;

public interface RoomTypeService {
    List<RoomTypeResponse> getRoomTypesByHotel(Long hotelId, LocalDate checkIn, LocalDate checkOut);
    RoomTypeResponse getRoomTypeById(Long id);
    RoomTypeResponse createRoomType(Long hotelId, RoomTypeRequest request);
    RoomTypeResponse updateRoomType(Long id, RoomTypeRequest request);
    void deleteRoomType(Long id);
    RoomType findActiveRoomTypeById(Long id);
}
