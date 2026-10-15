package com.hotelbooking.booking.service;

import com.hotelbooking.booking.dto.BookingRequest;
import com.hotelbooking.booking.dto.BookingResponse;
import com.hotelbooking.booking.enums.BookingStatus;
import com.hotelbooking.common.response.PageResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface BookingService {
    BookingResponse createBooking(String userEmail, BookingRequest request);
    BookingResponse confirmPayment(Long bookingId, String userEmail);
    BookingResponse cancelBooking(Long bookingId, String userEmail);
    PageResponse<BookingResponse> getMyBookings(String userEmail, BookingStatus status, int page, int size);
    BookingResponse getBookingById(Long id, String userEmail);
    PageResponse<BookingResponse> getAllBookings(BookingStatus status, Long hotelId, int page, int size);
    BookingResponse adminConfirm(Long bookingId);
    BookingResponse adminCancel(Long bookingId);

    /** Cancels unpaid bookings created before {@code cutoff} and frees their rooms. Returns the count. */
    int expirePendingBookings(LocalDateTime cutoff);

    /** Marks paid bookings whose check-out is on or before {@code today} as completed. Returns the count. */
    int completeFinishedBookings(LocalDate today);
}
