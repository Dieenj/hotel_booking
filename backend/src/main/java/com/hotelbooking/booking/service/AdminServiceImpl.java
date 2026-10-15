package com.hotelbooking.booking.service;

import com.hotelbooking.booking.dto.DashboardStats;
import com.hotelbooking.booking.enums.BookingStatus;
import com.hotelbooking.booking.repository.BookingRepository;
import com.hotelbooking.hotel.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private static final List<BookingStatus> REVENUE_STATUSES =
            List.of(BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN, BookingStatus.COMPLETED);

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;

    @Override
    public DashboardStats getDashboardStats() {
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        long totalBookings = bookingRepository.countActiveBookingsSince(startOfMonth);
        BigDecimal revenue = bookingRepository.sumRevenueSince(startOfMonth, REVENUE_STATUSES);
        long totalHotels   = hotelRepository.count();

        var recentBookings = bookingRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(b -> DashboardStats.RecentBooking.builder()
                        .bookingCode(b.getBookingCode())
                        .guestName(b.getGuestName())
                        .hotelName(b.getRoomType().getHotel().getName())
                        .status(b.getStatus().name())
                        .totalPrice(b.getTotalPrice())
                        .createdAt(b.getCreatedAt().toString())
                        .build())
                .toList();

        return DashboardStats.builder()
                .totalBookingsThisMonth(totalBookings)
                .revenueThisMonth(revenue != null ? revenue : BigDecimal.ZERO)
                .totalHotels(totalHotels)
                .recentBookings(recentBookings)
                .build();
    }
}
