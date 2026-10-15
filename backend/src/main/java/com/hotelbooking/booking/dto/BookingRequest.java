package com.hotelbooking.booking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class BookingRequest {

    @NotNull(message = "Room type is required")
    private Long roomTypeId;

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date cannot be in the past")
    private LocalDate checkIn;

    @NotNull(message = "Check-out date is required")
    private LocalDate checkOut;

    @NotNull(message = "Number of guests is required")
    @Min(value = 1, message = "At least 1 guest is required")
    private Integer guests;

    @NotBlank(message = "Guest name is required")
    @Size(max = 255, message = "Guest name is too long")
    private String guestName;

    @NotBlank(message = "Guest email is required")
    @Email(message = "Invalid guest email format")
    @Size(max = 255, message = "Guest email is too long")
    private String guestEmail;

    @Size(max = 30, message = "Guest phone is too long")
    private String guestPhone;

    @Size(max = 2000, message = "Special request is too long")
    private String specialRequest;
}
