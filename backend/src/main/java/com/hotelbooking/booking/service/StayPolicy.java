package com.hotelbooking.booking.service;

import com.hotelbooking.common.exception.BusinessException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Rules shared by availability checks and bookings for a stay's date range. */
public final class StayPolicy {

    public static final int MAX_NIGHTS = 30;

    private StayPolicy() {
    }

    public static void validateRange(LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn)) {
            throw new BusinessException("Check-out date must be after check-in date");
        }
        if (ChronoUnit.DAYS.between(checkIn, checkOut) > MAX_NIGHTS) {
            throw new BusinessException("Maximum booking duration is " + MAX_NIGHTS + " nights");
        }
    }
}
