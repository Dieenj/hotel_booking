package com.hotelbooking.hotel.repository;

import com.hotelbooking.hotel.entity.RoomType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    List<RoomType> findByHotelIdAndActiveTrue(Long hotelId);

    @Query("""
            SELECT rt.hotel.id AS hotelId, MIN(rt.basePrice) AS minPrice
            FROM RoomType rt
            WHERE rt.active = true AND rt.hotel.id IN :hotelIds
            GROUP BY rt.hotel.id
            """)
    List<HotelMinPrice> findMinPricesByHotelIds(@Param("hotelIds") Collection<Long> hotelIds);

    /**
     * Locks the room type row so inventory changes for it are serialized
     * (covers inventory rows that do not exist yet and so cannot be locked themselves).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT rt FROM RoomType rt WHERE rt.id = :id")
    Optional<RoomType> findByIdForUpdate(@Param("id") Long id);
}
