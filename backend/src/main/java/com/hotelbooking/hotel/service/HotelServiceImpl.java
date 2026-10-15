package com.hotelbooking.hotel.service;

import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.common.response.PageResponse;
import com.hotelbooking.common.response.Pagination;
import com.hotelbooking.hotel.dto.HotelRequest;
import com.hotelbooking.hotel.dto.HotelResponse;
import com.hotelbooking.hotel.entity.Hotel;
import com.hotelbooking.hotel.repository.HotelMinPrice;
import com.hotelbooking.hotel.repository.HotelRepository;
import com.hotelbooking.hotel.repository.HotelSpecifications;
import com.hotelbooking.hotel.repository.RoomTypeRepository;
import com.hotelbooking.review.repository.HotelRatingSummary;
import com.hotelbooking.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final ReviewRepository reviewRepository;
    private final RoomTypeRepository roomTypeRepository;

    @Override
    public PageResponse<HotelResponse> searchHotels(String city, Integer minRating, String name,
                                                     int page, int size, String sort) {
        Sort sortObj = switch (sort) {
            case "rating_desc" -> Sort.by("starRating").descending();
            case "name_asc"    -> Sort.by("name").ascending();
            default            -> Sort.by("createdAt").descending();
        };

        Pageable pageable = Pagination.of(page, size, sortObj);
        Page<Hotel> hotels = hotelRepository.findAll(
                HotelSpecifications.search(trimToNull(city), trimToNull(name), minRating), pageable);

        // Aggregate queries for the whole page instead of queries per hotel.
        List<Long> hotelIds = hotels.map(Hotel::getId).getContent();
        Map<Long, HotelRatingSummary> ratings = hotelIds.isEmpty()
                ? Map.of()
                : reviewRepository.summarizeByHotelIds(hotelIds).stream()
                        .collect(Collectors.toMap(HotelRatingSummary::getHotelId, Function.identity()));
        Map<Long, BigDecimal> minPrices = hotelIds.isEmpty()
                ? Map.of()
                : roomTypeRepository.findMinPricesByHotelIds(hotelIds).stream()
                        .collect(Collectors.toMap(HotelMinPrice::getHotelId, HotelMinPrice::getMinPrice));

        Page<HotelResponse> responses = hotels.map(hotel -> {
            HotelRatingSummary rating = ratings.get(hotel.getId());
            return HotelResponse.from(hotel,
                    rating != null ? rating.getAverageRating() : null,
                    rating != null ? rating.getReviewCount() : 0L,
                    minPrices.get(hotel.getId()));
        });

        return PageResponse.from(responses);
    }

    @Override
    public HotelResponse getHotelById(Long id) {
        Hotel hotel        = findActiveHotelById(id);
        Double avgRating   = reviewRepository.findAverageRatingByHotelId(id);
        Long   reviewCount = reviewRepository.countByHotelId(id);
        return HotelResponse.from(hotel, avgRating, reviewCount);
    }

    @Transactional
    @Override
    public HotelResponse createHotel(HotelRequest request) {
        Hotel hotel = Hotel.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .country(request.getCountry())
                .description(request.getDescription())
                .starRating(request.getStarRating())
                .amenities(request.getAmenities() != null ? request.getAmenities() : new ArrayList<>())
                .images(request.getImages() != null ? request.getImages() : new ArrayList<>())
                .build();

        return HotelResponse.from(hotelRepository.save(hotel));
    }

    @Transactional
    @Override
    public HotelResponse updateHotel(Long id, HotelRequest request) {
        Hotel hotel = findActiveHotelById(id);

        hotel.setName(request.getName());
        hotel.setAddress(request.getAddress());
        hotel.setCity(request.getCity());
        hotel.setCountry(request.getCountry());
        hotel.setDescription(request.getDescription());
        hotel.setStarRating(request.getStarRating());
        if (request.getAmenities() != null) hotel.setAmenities(request.getAmenities());
        if (request.getImages() != null)    hotel.setImages(request.getImages());

        return HotelResponse.from(hotelRepository.save(hotel));
    }

    @Transactional
    @Override
    public void deleteHotel(Long id) {
        Hotel hotel = findActiveHotelById(id);
        hotel.setActive(false);
        hotel.getRoomTypes().forEach(roomType -> roomType.setActive(false));
        hotelRepository.save(hotel);
    }

    @Override
    public Hotel findActiveHotelById(Long id) {
        return hotelRepository.findById(id)
                .filter(Hotel::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", id));
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Override
    public long getTotalCount() {
        return hotelRepository.count();
    }
}
