package com.hotelbooking.hotel.repository;

import com.hotelbooking.booking.entity.RoomInventory;
import com.hotelbooking.hotel.entity.Hotel;
import com.hotelbooking.hotel.entity.RoomType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Search filters for hotels. Each filter is only added when it has a value, so no nullable
 * parameter reaches the SQL (PostgreSQL cannot infer the type of a null bind and fails).
 */
public final class HotelSpecifications {

    private static final char LIKE_ESCAPE = '\\';

    private HotelSpecifications() {
    }

    public static Specification<Hotel> search(String city, String name, Integer minRating) {
        Specification<Hotel> spec = (root, query, cb) -> cb.isTrue(root.get("active"));
        if (city != null) {
            spec = spec.and(containsIgnoreCase("city", city));
        }
        if (name != null) {
            spec = spec.and(containsIgnoreCase("name", name));
        }
        if (minRating != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("starRating"), minRating));
        }
        return spec;
    }

    /**
     * Hotels with at least one active room type that fits {@code guests} (when given) and, when dates
     * are given, has a free room every night of the stay. Nights without an inventory row have had
     * no bookings, so only rows with no rooms left rule a room type out.
     */
    public static Specification<Hotel> hasAvailableRoom(LocalDate checkIn, LocalDate checkOut, Integer guests) {
        return (root, query, cb) -> {
            Subquery<Long> roomTypes = query.subquery(Long.class);
            Root<RoomType> roomType = roomTypes.from(RoomType.class);

            List<Predicate> conditions = new ArrayList<>();
            conditions.add(cb.equal(roomType.get("hotel"), root));
            conditions.add(cb.isTrue(roomType.get("active")));
            if (guests != null) {
                conditions.add(cb.greaterThanOrEqualTo(roomType.get("maxCapacity"), guests));
            }
            if (checkIn != null && checkOut != null) {
                Subquery<Long> soldOutNights = query.subquery(Long.class);
                Root<RoomInventory> inventory = soldOutNights.from(RoomInventory.class);
                soldOutNights.select(inventory.get("id")).where(
                        cb.equal(inventory.get("roomType"), roomType),
                        cb.between(inventory.get("date"), checkIn, checkOut.minusDays(1)),
                        cb.lessThanOrEqualTo(inventory.get("availableRooms"), 0));
                conditions.add(cb.not(cb.exists(soldOutNights)));
            }

            roomTypes.select(roomType.get("id")).where(conditions.toArray(Predicate[]::new));
            return cb.exists(roomTypes);
        };
    }

    private static Specification<Hotel> containsIgnoreCase(String attribute, String value) {
        String pattern = "%" + escapeLike(value.toLowerCase()) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get(attribute)), pattern, LIKE_ESCAPE);
    }

    /** Makes user-typed % and _ match literally. */
    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
