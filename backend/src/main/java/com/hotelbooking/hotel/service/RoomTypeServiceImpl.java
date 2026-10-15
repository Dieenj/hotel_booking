package com.hotelbooking.hotel.service;

import com.hotelbooking.booking.service.AvailabilityService;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.hotel.dto.RoomTypeRequest;
import com.hotelbooking.hotel.dto.RoomTypeResponse;
import com.hotelbooking.hotel.entity.Hotel;
import com.hotelbooking.hotel.entity.RoomType;
import com.hotelbooking.hotel.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;
    private final HotelService hotelService;
    private final AvailabilityService availabilityService;

    @Override
    public List<RoomTypeResponse> getRoomTypesByHotel(Long hotelId, LocalDate checkIn, LocalDate checkOut) {
        hotelService.findActiveHotelById(hotelId);
        List<RoomType> roomTypes = roomTypeRepository.findByHotelIdAndActiveTrue(hotelId);

        if (checkIn != null && checkOut != null) {
            return roomTypes.stream()
                    .map(rt -> RoomTypeResponse.from(rt,
                            availabilityService.getAvailableRooms(rt, checkIn, checkOut)))
                    .toList();
        }

        return roomTypes.stream().map(RoomTypeResponse::from).toList();
    }

    @Override
    public RoomTypeResponse getRoomTypeById(Long id) {
        return RoomTypeResponse.from(findActiveRoomTypeById(id));
    }

    @Transactional
    @Override
    public RoomTypeResponse createRoomType(Long hotelId, RoomTypeRequest request) {
        Hotel hotel = hotelService.findActiveHotelById(hotelId);

        RoomType roomType = RoomType.builder()
                .hotel(hotel)
                .name(request.getName())
                .description(request.getDescription())
                .maxCapacity(request.getMaxCapacity())
                .area(request.getArea())
                .basePrice(request.getBasePrice())
                .totalRooms(request.getTotalRooms())
                .amenities(request.getAmenities() != null ? request.getAmenities() : new ArrayList<>())
                .images(request.getImages() != null ? request.getImages() : new ArrayList<>())
                .build();

        return RoomTypeResponse.from(roomTypeRepository.save(roomType));
    }

    @Transactional
    @Override
    public RoomTypeResponse updateRoomType(Long id, RoomTypeRequest request) {
        RoomType roomType = findActiveRoomTypeById(id);

        roomType.setName(request.getName());
        roomType.setDescription(request.getDescription());
        roomType.setMaxCapacity(request.getMaxCapacity());
        roomType.setArea(request.getArea());
        roomType.setBasePrice(request.getBasePrice());
        if (!request.getTotalRooms().equals(roomType.getTotalRooms())) {
            availabilityService.updateTotalRooms(roomType, request.getTotalRooms());
            roomType.setTotalRooms(request.getTotalRooms());
        }
        if (request.getAmenities() != null) roomType.setAmenities(request.getAmenities());
        if (request.getImages() != null)    roomType.setImages(request.getImages());

        return RoomTypeResponse.from(roomTypeRepository.save(roomType));
    }

    @Transactional
    @Override
    public void deleteRoomType(Long id) {
        RoomType roomType = findActiveRoomTypeById(id);
        roomType.setActive(false);
        roomTypeRepository.save(roomType);
    }

    @Override
    public RoomType findActiveRoomTypeById(Long id) {
        return roomTypeRepository.findById(id)
                .filter(rt -> rt.isActive() && rt.getHotel().isActive())
                .orElseThrow(() -> new ResourceNotFoundException("RoomType", id));
    }
}
