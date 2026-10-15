package com.hotelbooking.booking.scheduler;

import com.hotelbooking.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Moves bookings through time-driven status changes (unpaid expiry, stay completion). */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class BookingLifecycleScheduler {

    private final BookingService bookingService;

    @Value("${app.booking.pending-ttl:30m}")
    private Duration pendingTtl;

    @Scheduled(fixedDelayString = "${app.booking.expiry-check-interval-ms:60000}")
    public void expirePendingBookings() {
        int expired = bookingService.expirePendingBookings(LocalDateTime.now().minus(pendingTtl));
        if (expired > 0) {
            log.info("Expired {} unpaid booking(s)", expired);
        }
    }

    @Scheduled(cron = "${app.booking.completion-cron:0 5 * * * *}")
    public void completeFinishedBookings() {
        int completed = bookingService.completeFinishedBookings(LocalDate.now());
        if (completed > 0) {
            log.info("Marked {} booking(s) as completed", completed);
        }
    }
}
