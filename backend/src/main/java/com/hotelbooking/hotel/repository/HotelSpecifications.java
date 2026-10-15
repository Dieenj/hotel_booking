package com.hotelbooking.hotel.repository;

import com.hotelbooking.hotel.entity.Hotel;
import org.springframework.data.jpa.domain.Specification;

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

    private static Specification<Hotel> containsIgnoreCase(String attribute, String value) {
        String pattern = "%" + escapeLike(value.toLowerCase()) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get(attribute)), pattern, LIKE_ESCAPE);
    }

    /** Makes user-typed % and _ match literally. */
    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
