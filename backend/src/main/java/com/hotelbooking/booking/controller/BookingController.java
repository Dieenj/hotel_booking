package com.hotelbooking.booking.controller;

import com.hotelbooking.booking.dto.BookingRequest;
import com.hotelbooking.booking.dto.BookingResponse;
import com.hotelbooking.booking.enums.BookingStatus;
import com.hotelbooking.booking.service.AvailabilityService;
import com.hotelbooking.booking.service.BookingService;
import com.hotelbooking.common.response.ApiResponse;
import com.hotelbooking.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking management endpoints")
public class BookingController {

    private final BookingService bookingService;
    private final AvailabilityService availabilityService;

    // ── Availability (public) ────────────────────────────────────────────────

    @GetMapping("/api/availability")
    @Operation(summary = "Check room availability for a date range")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkAvailability(
            @RequestParam Long roomTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut
    ) {
        int available = availabilityService.getAvailableRooms(roomTypeId, checkIn, checkOut);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "roomTypeId", roomTypeId,
                "checkIn", checkIn,
                "checkOut", checkOut,
                "availableRooms", available,
                "isAvailable", available > 0
        )));
    }

    // ── Guest ────────────────────────────────────────────────────────────────

    @PostMapping("/api/bookings")
    @Operation(summary = "Create a booking")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        BookingResponse response = bookingService.createBooking(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @GetMapping("/api/bookings")
    @Operation(summary = "Get my bookings")
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> getMyBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                bookingService.getMyBookings(userDetails.getUsername(), status, page, size)
        ));
    }

    @GetMapping("/api/bookings/{id}")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                bookingService.getBookingById(id, userDetails.getUsername())
        ));
    }

    @PostMapping("/api/bookings/{id}/pay")
    @Operation(summary = "Confirm payment for a booking (mock)")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmPayment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Payment confirmed", bookingService.confirmPayment(id, userDetails.getUsername())
        ));
    }

    @PostMapping("/api/bookings/{id}/cancel")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Booking cancelled", bookingService.cancelBooking(id, userDetails.getUsername())
        ));
    }

    // ── Admin ────────────────────────────────────────────────────────────────

    @GetMapping("/api/admin/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all bookings (Admin)")
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> getAllBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                bookingService.getAllBookings(status, hotelId, page, size)
        ));
    }

    @PostMapping("/api/admin/bookings/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Confirm booking (Admin)")
    public ResponseEntity<ApiResponse<BookingResponse>> adminConfirm(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Booking confirmed", bookingService.adminConfirm(id)));
    }

    @PostMapping("/api/admin/bookings/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancel booking (Admin)")
    public ResponseEntity<ApiResponse<BookingResponse>> adminCancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Booking cancelled", bookingService.adminCancel(id)));
    }
}
