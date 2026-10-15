package com.hotelbooking.review.service;

import com.hotelbooking.auth.entity.User;
import com.hotelbooking.auth.repository.UserRepository;
import com.hotelbooking.booking.entity.Booking;
import com.hotelbooking.booking.enums.BookingStatus;
import com.hotelbooking.booking.repository.BookingRepository;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.common.response.PageResponse;
import com.hotelbooking.common.response.Pagination;
import com.hotelbooking.hotel.entity.Hotel;
import com.hotelbooking.hotel.service.HotelService;
import com.hotelbooking.review.dto.ReviewRequest;
import com.hotelbooking.review.dto.ReviewResponse;
import com.hotelbooking.review.entity.Review;
import com.hotelbooking.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final HotelService hotelService;

    @Override
    public PageResponse<ReviewResponse> getHotelReviews(Long hotelId, int page, int size) {
        hotelService.findActiveHotelById(hotelId);
        var pageable = Pagination.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.from(
                reviewRepository.findByHotelId(hotelId, pageable).map(ReviewResponse::from));
    }

    @Override
    public PageResponse<ReviewResponse> getMyReviews(String email, int page, int size) {
        User user = userRepository.getByEmailOrThrow(email);
        var pageable = Pagination.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.from(
                reviewRepository.findByUserId(user.getId(), pageable).map(ReviewResponse::from));
    }

    @Transactional
    @Override
    public ReviewResponse createReview(String userEmail, ReviewRequest request) {
        User user = userRepository.getByEmailOrThrow(userEmail);

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", request.getBookingId()));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Access denied", HttpStatus.FORBIDDEN);
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BusinessException("You can only review completed bookings");
        }

        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new BusinessException("You have already reviewed this booking");
        }

        Hotel hotel = hotelService.findActiveHotelById(booking.getRoomType().getHotel().getId());

        Review review = Review.builder()
                .user(user)
                .hotel(hotel)
                .booking(booking)
                .rating(request.getRating())
                .cleanlinessRating(request.getCleanlinessRating())
                .serviceRating(request.getServiceRating())
                .locationRating(request.getLocationRating())
                .valueRating(request.getValueRating())
                .comment(request.getComment())
                .build();

        return ReviewResponse.from(reviewRepository.save(review));
    }
}
