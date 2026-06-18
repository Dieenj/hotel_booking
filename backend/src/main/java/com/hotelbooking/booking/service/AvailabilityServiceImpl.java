package com.hotelbooking.booking.service;

import com.hotelbooking.booking.entity.RoomInventory;
import com.hotelbooking.booking.repository.RoomInventoryRepository;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.hotel.entity.RoomType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final RoomInventoryRepository inventoryRepository;

    @Override
    public int getAvailableRooms(Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        return checkIn.datesUntil(checkOut)
                .mapToInt(date -> inventoryRepository.findByRoomTypeIdAndDate(roomTypeId, date)
                        .map(RoomInventory::getAvailableRooms)
                        .orElse(Integer.MAX_VALUE))
                .min()
                .orElse(0);
    }

    @Override
    public boolean isAvailable(Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        return getAvailableRooms(roomTypeId, checkIn, checkOut) > 0;
    }

    @Transactional
    @Override
    public void reserve(RoomType roomType, LocalDate checkIn, LocalDate checkOut) {
        LocalDate date = checkIn;
        while (date.isBefore(checkOut)) {
            final LocalDate currentDate = date;
            RoomInventory inventory = inventoryRepository
                    .findByRoomTypeIdAndDate(roomType.getId(), currentDate)
                    .orElseGet(() -> RoomInventory.builder()
                            .roomType(roomType)
                            .date(currentDate)
                            .totalRooms(roomType.getTotalRooms())
                            .availableRooms(roomType.getTotalRooms())
                            .build());

            if (inventory.getId() != null) {
                inventory = inventoryRepository
                        .findByRoomTypeIdAndDateRangeWithLock(roomType.getId(), currentDate, currentDate)
                        .stream().findFirst()
                        .orElse(inventory);
            }

            if (inventory.getAvailableRooms() <= 0) {
                throw new BusinessException("Room not available for date: " + date);
            }

            inventory.setAvailableRooms(inventory.getAvailableRooms() - 1);
            inventory.setBookedRooms(inventory.getBookedRooms() + 1);
            inventoryRepository.save(inventory);

            date = date.plusDays(1);
        }
    }

    @Transactional
    @Override
    public void release(RoomType roomType, LocalDate checkIn, LocalDate checkOut) {
        LocalDate date = checkIn;
        while (date.isBefore(checkOut)) {
            final LocalDate currentDate = date;
            inventoryRepository.findByRoomTypeIdAndDate(roomType.getId(), currentDate)
                    .ifPresent(inventory -> {
                        inventory.setAvailableRooms(
                                Math.min(inventory.getAvailableRooms() + 1, inventory.getTotalRooms()));
                        inventory.setBookedRooms(Math.max(inventory.getBookedRooms() - 1, 0));
                        inventoryRepository.save(inventory);
                    });
            date = date.plusDays(1);
        }
    }
}
