package com.hotelbooking.booking.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter @Builder
public class DashboardStats {
    private long totalBookingsThisMonth;
    private BigDecimal revenueThisMonth;
    private long totalHotels;
    private List<RecentBooking> recentBookings;

    @Getter @Builder
    public static class RecentBooking {
        private String bookingCode;
        private String guestName;
        private String hotelName;
        private String status;
        private BigDecimal totalPrice;
        private String createdAt;
    }
}
