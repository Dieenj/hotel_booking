package com.hotelbooking.hotel.repository;

import com.hotelbooking.hotel.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    List<RoomType> findByHotelIdAndActiveTrue(Long hotelId);
}
