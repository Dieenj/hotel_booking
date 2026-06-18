package com.hotelbooking.booking.service;

import com.hotelbooking.auth.entity.User;
import com.hotelbooking.auth.enums.Role;
import com.hotelbooking.auth.repository.UserRepository;
import com.hotelbooking.booking.dto.BookingRequest;
import com.hotelbooking.booking.dto.BookingResponse;
import com.hotelbooking.booking.entity.Booking;
import com.hotelbooking.booking.enums.BookingStatus;
import com.hotelbooking.booking.repository.BookingRepository;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.common.response.PageResponse;
import com.hotelbooking.hotel.entity.RoomType;
import com.hotelbooking.hotel.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RoomTypeService roomTypeService;
    private final AvailabilityService availabilityService;

    @Transactional
    @Override
    public BookingResponse createBooking(String userEmail, BookingRequest request) {
        validateDates(request.getCheckIn(), request.getCheckOut());

        RoomType roomType = roomTypeService.findActiveRoomTypeById(request.getRoomTypeId());

        if (request.getGuests() > roomType.getMaxCapacity()) {
            throw new BusinessException(
                    "Room capacity exceeded. Max: " + roomType.getMaxCapacity() + " guests");
        }

        availabilityService.reserve(roomType, request.getCheckIn(), request.getCheckOut());

        User user = userRepository.findByEmail(userEmail).orElseThrow();

        BigDecimal totalPrice = calculateTotalPrice(
                roomType.getBasePrice(), request.getCheckIn(), request.getCheckOut());

        Booking booking = Booking.builder()
                .bookingCode(generateBookingCode())
                .user(user)
                .roomType(roomType)
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .guests(request.getGuests())
                .totalPrice(totalPrice)
                .specialRequest(request.getSpecialRequest())
                .guestName(request.getGuestName())
                .guestEmail(request.getGuestEmail())
                .guestPhone(request.getGuestPhone())
                .build();

        return BookingResponse.from(bookingRepository.save(booking));
    }

    @Transactional
    @Override
    public BookingResponse confirmPayment(Long bookingId, String userEmail) {
        Booking booking = findBookingForUser(bookingId, userEmail);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException("Only pending bookings can be confirmed");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        return BookingResponse.from(bookingRepository.save(booking));
    }

    @Transactional
    @Override
    public BookingResponse cancelBooking(Long bookingId, String userEmail) {
        Booking booking = findBookingForUser(bookingId, userEmail);

        if (booking.getStatus() == BookingStatus.CANCELLED
                || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BusinessException("Booking cannot be cancelled in its current status");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        availabilityService.release(booking.getRoomType(), booking.getCheckIn(), booking.getCheckOut());

        return BookingResponse.from(bookingRepository.save(booking));
    }

    @Override
    public PageResponse<BookingResponse> getMyBookings(String userEmail, BookingStatus status,
                                                        int page, int size) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        var bookings = status != null
                ? bookingRepository.findByUserIdAndStatus(user.getId(), status, pageable)
                : bookingRepository.findByUserId(user.getId(), pageable);

        return PageResponse.from(bookings.map(BookingResponse::from));
    }

    @Override
    public BookingResponse getBookingById(Long id, String userEmail) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));

        User user = userRepository.findByEmail(userEmail).orElseThrow();
        boolean isOwner = booking.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new BusinessException("Access denied", HttpStatus.FORBIDDEN);
        }

        return BookingResponse.from(booking);
    }

    @Override
    public PageResponse<BookingResponse> getAllBookings(BookingStatus status, Long hotelId,
                                                        int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.from(
                bookingRepository.findAllByFilters(status, hotelId, pageable)
                        .map(BookingResponse::from));
    }

    @Transactional
    @Override
    public BookingResponse adminConfirm(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException("Only pending bookings can be confirmed");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        return BookingResponse.from(bookingRepository.save(booking));
    }

    @Transactional
    @Override
    public BookingResponse adminCancel(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED
                || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BusinessException("Booking cannot be cancelled in its current status");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        availabilityService.release(booking.getRoomType(), booking.getCheckIn(), booking.getCheckOut());

        return BookingResponse.from(bookingRepository.save(booking));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Booking findBookingForUser(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        User user = userRepository.findByEmail(userEmail).orElseThrow();
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Access denied", HttpStatus.FORBIDDEN);
        }

        return booking;
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn)) {
            throw new BusinessException("Check-out date must be after check-in date");
        }
        if (ChronoUnit.DAYS.between(checkIn, checkOut) > 30) {
            throw new BusinessException("Maximum booking duration is 30 nights");
        }
    }

    private BigDecimal calculateTotalPrice(BigDecimal basePrice, LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        return basePrice.multiply(BigDecimal.valueOf(nights));
    }

    private String generateBookingCode() {
        String datePart   = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int    randomPart = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "BK-" + datePart + "-" + randomPart;
    }
}
