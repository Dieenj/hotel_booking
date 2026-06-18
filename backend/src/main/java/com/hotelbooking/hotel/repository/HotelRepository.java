package com.hotelbooking.hotel.repository;

import com.hotelbooking.hotel.entity.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    @Query("""
            SELECT h FROM Hotel h
            WHERE h.active = true
            AND (:city IS NULL OR LOWER(h.city) LIKE LOWER(CONCAT('%', :city, '%')))
            AND (:minRating IS NULL OR h.starRating >= :minRating)
            AND (:name IS NULL OR LOWER(h.name) LIKE LOWER(CONCAT('%', :name, '%')))
            """)
    Page<Hotel> searchHotels(
            @Param("city") String city,
            @Param("minRating") Integer minRating,
            @Param("name") String name,
            Pageable pageable
    );
}
