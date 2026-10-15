package com.hotelbooking.hotel.dto;

import java.time.LocalDate;

/**
 * Hotel search filters; every field is optional. With {@code checkIn}/{@code checkOut} only hotels
 * with a room type free for the whole stay match; {@code guests} requires a room type that fits them.
 */
public record HotelSearchCriteria(
        String city,
        String name,
        Integer minRating,
        LocalDate checkIn,
        LocalDate checkOut,
        Integer guests
) {
    public boolean hasDates() {
        return checkIn != null && checkOut != null;
    }
}
