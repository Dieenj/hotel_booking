package com.hotelbooking;

import com.hotelbooking.booking.service.BookingService;
import com.hotelbooking.support.IntegrationTestSupport;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Every read endpoint must work with open-in-view disabled (no lazy loading after the service returns)
 * and must not issue a query per row.
 */
class ReadEndpointsIntegrationTest extends IntegrationTestSupport {

    @Autowired private BookingService bookingService;
    @Autowired private EntityManagerFactory entityManagerFactory;

    private String admin;
    private String guest;
    private long hotelId;
    private long roomTypeId;
    private long bookingId;

    private final LocalDate checkIn = daysFromNow(20);
    private final LocalDate checkOut = daysFromNow(22);

    @BeforeEach
    void setUp() throws Exception {
        admin = createAdmin();
        guest = registerGuest();
        hotelId = createHotel(admin);
        roomTypeId = createRoomType(admin, hotelId, 5);
        bookingId = bookOk(guest, roomTypeId, checkIn, checkOut);
        mockMvc.perform(withAuth(post("/api/bookings/" + bookingId + "/pay"), guest))
                .andExpect(status().isOk());
        bookingService.completeFinishedBookings(checkOut);
        mockMvc.perform(withAuth(post("/api/reviews"), guest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("bookingId", bookingId, "rating", 4))))
                .andExpect(status().isCreated());
    }

    @Test
    void publicReadEndpointsWork() throws Exception {
        mockMvc.perform(get("/api/hotels")).andExpect(status().isOk());
        mockMvc.perform(get("/api/hotels/" + hotelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewCount").value(1))
                .andExpect(jsonPath("$.data.averageRating").value(4.0));
        mockMvc.perform(get("/api/hotels/" + hotelId + "/room-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].hotelName").exists());
        mockMvc.perform(get("/api/hotels/" + hotelId + "/room-types")
                        .param("checkIn", checkIn.toString())
                        .param("checkOut", checkOut.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].availableRooms").value(4));
        mockMvc.perform(get("/api/room-types/" + roomTypeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hotelName").exists());
        mockMvc.perform(get("/api/hotels/" + hotelId + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].userName").value("Test Guest"));
    }

    @Test
    void guestReadEndpointsWork() throws Exception {
        mockMvc.perform(withAuth(get("/api/bookings"), guest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].hotelName").exists());
        mockMvc.perform(withAuth(get("/api/bookings"), guest).param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
        mockMvc.perform(withAuth(get("/api/bookings/" + bookingId), guest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roomTypeName").value("Deluxe"));
        mockMvc.perform(withAuth(get("/api/reviews/my"), guest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void adminReadEndpointsWork() throws Exception {
        mockMvc.perform(withAuth(get("/api/admin/bookings"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].hotelName").exists());
        mockMvc.perform(withAuth(get("/api/admin/bookings"), admin).param("hotelId", String.valueOf(hotelId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
        mockMvc.perform(withAuth(get("/api/admin/stats"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recentBookings[0].hotelName").exists());
    }

    @Test
    void hotelSearchQueryCountDoesNotGrowWithResults() throws Exception {
        for (int i = 0; i < 5; i++) {
            createHotel(admin);
        }

        long queries = countQueries(() -> mockMvc.perform(get("/api/hotels").param("size", "20"))
                .andExpect(status().isOk()));

        // page + count + rating summary + batched element collections; not 2 queries per hotel.
        assertThat(queries).isLessThanOrEqualTo(6);
    }

    @Test
    void bookingListQueryCountDoesNotGrowWithResults() throws Exception {
        for (int i = 0; i < 4; i++) {
            long otherRoomType = createRoomType(admin, createHotel(admin), 1);
            bookOk(guest, otherRoomType, checkIn, checkOut);
        }

        long queries = countQueries(() -> mockMvc.perform(withAuth(get("/api/bookings"), guest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(5)));

        // user lookup (filter + service) + page + count + batched collections; not per booking.
        assertThat(queries).isLessThanOrEqualTo(8);
    }

    private long countQueries(ThrowingRunnable action) throws Exception {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        action.run();
        return statistics.getPrepareStatementCount();
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
