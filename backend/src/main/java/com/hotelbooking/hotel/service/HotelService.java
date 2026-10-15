package com.hotelbooking.hotel.service;

import com.hotelbooking.common.response.PageResponse;
import com.hotelbooking.hotel.dto.HotelRequest;
import com.hotelbooking.hotel.dto.HotelResponse;
import com.hotelbooking.hotel.dto.HotelSearchCriteria;
import com.hotelbooking.hotel.entity.Hotel;

public interface HotelService {
    PageResponse<HotelResponse> searchHotels(HotelSearchCriteria criteria, int page, int size, String sort);
    HotelResponse getHotelById(Long id);
    HotelResponse createHotel(HotelRequest request);
    HotelResponse updateHotel(Long id, HotelRequest request);
    void deleteHotel(Long id);
    Hotel findActiveHotelById(Long id);
    long getTotalCount();
}
