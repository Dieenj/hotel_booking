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
import com.hotelbooking.common.response.Pagination;
import com.hotelbooking.hotel.entity.RoomType;
import com.hotelbooking.hotel.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    /** Statuses of bookings that are paid and still hold their rooms. */
    private static final List<BookingStatus> PAID_ACTIVE_STATUSES =
            List.of(BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN);

    // No 0/O/1/I so codes read back unambiguously.
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final int CODE_MAX_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RoomTypeService roomTypeService;
    private final AvailabilityService availabilityService;

    @Transactional
    @Override
    public BookingResponse createBooking(String userEmail, BookingRequest request) {
        StayPolicy.validateRange(request.getCheckIn(), request.getCheckOut());

        RoomType roomType = roomTypeService.findActiveRoomTypeById(request.getRoomTypeId());

        if (request.getGuests() > roomType.getMaxCapacity()) {
            throw new BusinessException(
                    "Room capacity exceeded. Max: " + roomType.getMaxCapacity() + " guests");
        }

        availabilityService.reserve(roomType, request.getCheckIn(), request.getCheckOut());

        User user = userRepository.getByEmailOrThrow(userEmail);

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
        return BookingResponse.from(confirm(findBookingForUser(bookingId, userEmail)));
    }

    @Transactional
    @Override
    public BookingResponse cancelBooking(Long bookingId, String userEmail) {
        return BookingResponse.from(cancel(findBookingForUser(bookingId, userEmail)));
    }

    @Override
    public PageResponse<BookingResponse> getMyBookings(String userEmail, BookingStatus status,
                                                        int page, int size) {
        User user = userRepository.getByEmailOrThrow(userEmail);
        var pageable = Pagination.of(page, size, Sort.by("createdAt").descending());

        var bookings = status != null
                ? bookingRepository.findByUserIdAndStatus(user.getId(), status, pageable)
                : bookingRepository.findByUserId(user.getId(), pageable);

        return PageResponse.from(bookings.map(BookingResponse::from));
    }

    @Override
    public BookingResponse getBookingById(Long id, String userEmail) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));

        User user = userRepository.getByEmailOrThrow(userEmail);
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
        var pageable = Pagination.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.from(
                bookingRepository.findAllByFilters(status, hotelId, pageable)
                        .map(BookingResponse::from));
    }

    @Transactional
    @Override
    public BookingResponse adminConfirm(Long bookingId) {
        return BookingResponse.from(confirm(findBookingForUpdate(bookingId)));
    }

    @Transactional
    @Override
    public BookingResponse adminCancel(Long bookingId) {
        return BookingResponse.from(cancel(findBookingForUpdate(bookingId)));
    }

    @Transactional
    @Override
    public int expirePendingBookings(LocalDateTime cutoff) {
        List<Booking> expired = bookingRepository.findByStatusAndCreatedAtBefore(BookingStatus.PENDING, cutoff);
        expired.forEach(this::cancel);
        return expired.size();
    }

    @Transactional
    @Override
    public int completeFinishedBookings(LocalDate today) {
        List<Booking> finished = bookingRepository
                .findByStatusInAndCheckOutLessThanEqual(PAID_ACTIVE_STATUSES, today);
        finished.forEach(booking -> booking.setStatus(BookingStatus.COMPLETED));
        bookingRepository.saveAll(finished);
        return finished.size();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Booking confirm(Booking booking) {
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException("Only pending bookings can be confirmed");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }

    private Booking cancel(Booking booking) {
        if (booking.getStatus() == BookingStatus.CANCELLED
                || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BusinessException("Booking cannot be cancelled in its current status");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        availabilityService.release(booking.getRoomType(), booking.getCheckIn(), booking.getCheckOut());
        return bookingRepository.save(booking);
    }

    private Booking findBookingForUpdate(Long bookingId) {
        return bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
    }

    private Booking findBookingForUser(Long bookingId, String userEmail) {
        Booking booking = findBookingForUpdate(bookingId);

        User user = userRepository.getByEmailOrThrow(userEmail);
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Access denied", HttpStatus.FORBIDDEN);
        }

        return booking;
    }

    private BigDecimal calculateTotalPrice(BigDecimal basePrice, LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        return basePrice.multiply(BigDecimal.valueOf(nights));
    }

    private String generateBookingCode() {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        for (int attempt = 0; attempt < CODE_MAX_ATTEMPTS; attempt++) {
            StringBuilder code = new StringBuilder("BK-").append(datePart).append('-');
            for (int i = 0; i < CODE_LENGTH; i++) {
                code.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
            }
            if (!bookingRepository.existsByBookingCode(code.toString())) {
                return code.toString();
            }
        }
        throw new IllegalStateException("Could not generate a unique booking code");
    }
}
