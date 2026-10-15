package com.hotelbooking.booking.service;

import com.hotelbooking.booking.entity.RoomInventory;
import com.hotelbooking.booking.repository.RoomInventoryRepository;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.hotel.entity.RoomType;
import com.hotelbooking.hotel.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final RoomInventoryRepository inventoryRepository;
    private final RoomTypeRepository roomTypeRepository;

    @Transactional(readOnly = true)
    @Override
    public int getAvailableRooms(Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("RoomType", roomTypeId));
        return getAvailableRooms(roomType, checkIn, checkOut);
    }

    @Transactional(readOnly = true)
    @Override
    public int getAvailableRooms(RoomType roomType, LocalDate checkIn, LocalDate checkOut) {
        StayPolicy.validateRange(checkIn, checkOut);
        Map<LocalDate, RoomInventory> inventory = loadInventory(roomType.getId(), checkIn, checkOut);

        // Dates without an inventory row have had no bookings yet, so every room is free.
        return checkIn.datesUntil(checkOut)
                .mapToInt(date -> {
                    RoomInventory row = inventory.get(date);
                    return row != null ? row.getAvailableRooms() : roomType.getTotalRooms();
                })
                .min()
                .orElse(0);
    }

    @Transactional
    @Override
    public void reserve(RoomType roomType, LocalDate checkIn, LocalDate checkOut) {
        lockRoomType(roomType.getId());
        Map<LocalDate, RoomInventory> inventory = loadInventory(roomType.getId(), checkIn, checkOut);

        List<RoomInventory> updated = new ArrayList<>();
        for (LocalDate date : checkIn.datesUntil(checkOut).toList()) {
            RoomInventory row = inventory.getOrDefault(date, RoomInventory.builder()
                    .roomType(roomType)
                    .date(date)
                    .totalRooms(roomType.getTotalRooms())
                    .availableRooms(roomType.getTotalRooms())
                    .build());

            if (row.getAvailableRooms() <= 0) {
                throw new BusinessException("Room not available for date: " + date);
            }

            row.setAvailableRooms(row.getAvailableRooms() - 1);
            row.setBookedRooms(row.getBookedRooms() + 1);
            updated.add(row);
        }
        inventoryRepository.saveAll(updated);
    }

    @Transactional
    @Override
    public void release(RoomType roomType, LocalDate checkIn, LocalDate checkOut) {
        lockRoomType(roomType.getId());
        List<RoomInventory> rows = new ArrayList<>(loadInventory(roomType.getId(), checkIn, checkOut).values());
        for (RoomInventory row : rows) {
            row.setAvailableRooms(Math.min(row.getAvailableRooms() + 1, row.getTotalRooms()));
            row.setBookedRooms(Math.max(row.getBookedRooms() - 1, 0));
        }
        inventoryRepository.saveAll(rows);
    }

    @Transactional
    @Override
    public void updateTotalRooms(RoomType roomType, int newTotalRooms) {
        lockRoomType(roomType.getId());
        List<RoomInventory> rows = inventoryRepository
                .findByRoomTypeIdAndDateGreaterThanEqual(roomType.getId(), LocalDate.now());

        for (RoomInventory row : rows) {
            if (newTotalRooms < row.getBookedRooms()) {
                throw new BusinessException("Cannot set total rooms to " + newTotalRooms + ": "
                        + row.getBookedRooms() + " rooms are already booked on " + row.getDate());
            }
            row.setTotalRooms(newTotalRooms);
            row.setAvailableRooms(newTotalRooms - row.getBookedRooms());
        }
        inventoryRepository.saveAll(rows);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void lockRoomType(Long roomTypeId) {
        roomTypeRepository.findByIdForUpdate(roomTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("RoomType", roomTypeId));
    }

    /** Inventory rows for the nights of a stay, keyed by date (check-out night excluded). */
    private Map<LocalDate, RoomInventory> loadInventory(Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        return inventoryRepository
                .findByRoomTypeIdAndDateRange(roomTypeId, checkIn, checkOut.minusDays(1))
                .stream()
                .collect(Collectors.toMap(RoomInventory::getDate, Function.identity()));
    }
}
