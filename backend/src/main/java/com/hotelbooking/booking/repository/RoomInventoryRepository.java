package com.hotelbooking.booking.repository;

import com.hotelbooking.booking.entity.RoomInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomInventoryRepository extends JpaRepository<RoomInventory, Long> {

    Optional<RoomInventory> findByRoomTypeIdAndDate(Long roomTypeId, LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ri FROM RoomInventory ri WHERE ri.roomType.id = :roomTypeId AND ri.date BETWEEN :startDate AND :endDate")
    List<RoomInventory> findByRoomTypeIdAndDateRangeWithLock(
            @Param("roomTypeId") Long roomTypeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT ri FROM RoomInventory ri WHERE ri.roomType.id = :roomTypeId AND ri.date BETWEEN :startDate AND :endDate")
    List<RoomInventory> findByRoomTypeIdAndDateRange(
            @Param("roomTypeId") Long roomTypeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
