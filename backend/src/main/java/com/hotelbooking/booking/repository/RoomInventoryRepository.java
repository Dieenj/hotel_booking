package com.hotelbooking.booking.repository;

import com.hotelbooking.booking.entity.RoomInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomInventoryRepository extends JpaRepository<RoomInventory, Long> {

    /** Inventory rows in [startDate, endDate] (both inclusive). */
    @Query("SELECT ri FROM RoomInventory ri WHERE ri.roomType.id = :roomTypeId AND ri.date BETWEEN :startDate AND :endDate")
    List<RoomInventory> findByRoomTypeIdAndDateRange(
            @Param("roomTypeId") Long roomTypeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<RoomInventory> findByRoomTypeIdAndDateGreaterThanEqual(Long roomTypeId, LocalDate date);
}
