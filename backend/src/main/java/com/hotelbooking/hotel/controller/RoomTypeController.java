package com.hotelbooking.hotel.controller;

import com.hotelbooking.common.response.ApiResponse;
import com.hotelbooking.hotel.dto.RoomTypeRequest;
import com.hotelbooking.hotel.dto.RoomTypeResponse;
import com.hotelbooking.hotel.service.RoomTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Room Types", description = "Room type management endpoints")
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    // ── Public ──────────────────────────────────────────────────────────────

    @GetMapping("/api/hotels/{hotelId}/room-types")
    @Operation(summary = "Get room types for a hotel with optional availability check")
    public ResponseEntity<ApiResponse<List<RoomTypeResponse>>> getRoomTypes(
            @PathVariable Long hotelId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                roomTypeService.getRoomTypesByHotel(hotelId, checkIn, checkOut)
        ));
    }

    @GetMapping("/api/room-types/{id}")
    @Operation(summary = "Get room type by ID")
    public ResponseEntity<ApiResponse<RoomTypeResponse>> getRoomType(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(roomTypeService.getRoomTypeById(id)));
    }

    // ── Admin ────────────────────────────────────────────────────────────────

    @PostMapping("/api/admin/hotels/{hotelId}/room-types")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create room type (Admin)")
    public ResponseEntity<ApiResponse<RoomTypeResponse>> createRoomType(
            @PathVariable Long hotelId, @Valid @RequestBody RoomTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(roomTypeService.createRoomType(hotelId, request)));
    }

    @PutMapping("/api/admin/room-types/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update room type (Admin)")
    public ResponseEntity<ApiResponse<RoomTypeResponse>> updateRoomType(
            @PathVariable Long id, @Valid @RequestBody RoomTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Room type updated", roomTypeService.updateRoomType(id, request)));
    }

    @DeleteMapping("/api/admin/room-types/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete room type (Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteRoomType(@PathVariable Long id) {
        roomTypeService.deleteRoomType(id);
        return ResponseEntity.ok(ApiResponse.message("Room type deleted successfully"));
    }
}
