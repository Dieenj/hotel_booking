package com.hotelbooking.hotel.repository;

import java.math.BigDecimal;

/** Cheapest active room type price for one hotel. */
public interface HotelMinPrice {
    Long getHotelId();
    BigDecimal getMinPrice();
}
