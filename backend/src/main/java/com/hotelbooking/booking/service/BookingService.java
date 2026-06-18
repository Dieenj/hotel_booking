package com.hotelbooking.booking.service;

import com.hotelbooking.booking.dto.BookingRequest;
import com.hotelbooking.booking.dto.BookingResponse;
import com.hotelbooking.booking.enums.BookingStatus;
import com.hotelbooking.common.response.PageResponse;

public interface BookingService {
    BookingResponse createBooking(String userEmail, BookingRequest request);
    BookingResponse confirmPayment(Long bookingId, String userEmail);
    BookingResponse cancelBooking(Long bookingId, String userEmail);
    PageResponse<BookingResponse> getMyBookings(String userEmail, BookingStatus status, int page, int size);
    BookingResponse getBookingById(Long id, String userEmail);
    PageResponse<BookingResponse> getAllBookings(BookingStatus status, Long hotelId, int page, int size);
    BookingResponse adminConfirm(Long bookingId);
    BookingResponse adminCancel(Long bookingId);
}
