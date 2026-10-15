package com.hotelbooking.hotel.controller;

import com.hotelbooking.common.response.ApiResponse;
import com.hotelbooking.common.response.PageResponse;
import com.hotelbooking.hotel.dto.HotelRequest;
import com.hotelbooking.hotel.dto.HotelResponse;
import com.hotelbooking.hotel.dto.HotelSearchCriteria;
import com.hotelbooking.hotel.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@Tag(name = "Hotels", description = "Hotel management endpoints")
public class HotelController {

    private final HotelService hotelService;

    // ── Public ──────────────────────────────────────────────────────────────

    @GetMapping("/api/hotels")
    @Operation(summary = "Search hotels")
    public ResponseEntity<ApiResponse<PageResponse<HotelResponse>>> searchHotels(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) String name,
            @Parameter(description = "With checkOut: only hotels with a room free for the whole stay")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @Parameter(description = "Only hotels with a room type for this many guests")
            @RequestParam(required = false) Integer guests,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort
    ) {
        var criteria = new HotelSearchCriteria(city, name, minRating, checkIn, checkOut, guests);
        return ResponseEntity.ok(ApiResponse.ok(
                hotelService.searchHotels(criteria, page, size, sort)
        ));
    }

    @GetMapping("/api/hotels/{id}")
    @Operation(summary = "Get hotel by ID")
    public ResponseEntity<ApiResponse<HotelResponse>> getHotel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(hotelService.getHotelById(id)));
    }

    // ── Admin ────────────────────────────────────────────────────────────────

    @PostMapping("/api/admin/hotels")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create hotel (Admin)")
    public ResponseEntity<ApiResponse<HotelResponse>> createHotel(@Valid @RequestBody HotelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(hotelService.createHotel(request)));
    }

    @PutMapping("/api/admin/hotels/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update hotel (Admin)")
    public ResponseEntity<ApiResponse<HotelResponse>> updateHotel(
            @PathVariable Long id, @Valid @RequestBody HotelRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Hotel updated", hotelService.updateHotel(id, request)));
    }

    @DeleteMapping("/api/admin/hotels/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete hotel (Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.ok(ApiResponse.message("Hotel deleted successfully"));
    }
}
