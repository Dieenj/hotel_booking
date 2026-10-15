package com.hotelbooking.booking.dto;

import com.hotelbooking.booking.entity.Booking;
import com.hotelbooking.booking.enums.BookingStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter @Builder
public class BookingResponse {

    private Long id;
    private String bookingCode;
    private Long roomTypeId;
    private String roomTypeName;
    private Long hotelId;
    private String hotelName;
    private String hotelAddress;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Long nights;
    private Integer guests;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private String specialRequest;
    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private LocalDateTime createdAt;
    /** Whether the guest has already reviewed this stay. */
    private boolean reviewed;
    /** Whether the guest can still cancel (see {@link Booking#isCancellableByGuest}). */
    private boolean cancellable;

    public static BookingResponse from(Booking booking) {
        return from(booking, false);
    }

    public static BookingResponse from(Booking booking, boolean reviewed) {
        return BookingResponse.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .roomTypeId(booking.getRoomType().getId())
                .roomTypeName(booking.getRoomType().getName())
                .hotelId(booking.getRoomType().getHotel().getId())
                .hotelName(booking.getRoomType().getHotel().getName())
                .hotelAddress(booking.getRoomType().getHotel().getAddress())
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .nights(ChronoUnit.DAYS.between(booking.getCheckIn(), booking.getCheckOut()))
                .guests(booking.getGuests())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .specialRequest(booking.getSpecialRequest())
                .guestName(booking.getGuestName())
                .guestEmail(booking.getGuestEmail())
                .guestPhone(booking.getGuestPhone())
                .createdAt(booking.getCreatedAt())
                .reviewed(reviewed)
                .cancellable(booking.isCancellableByGuest(LocalDate.now()))
                .build();
    }
}
