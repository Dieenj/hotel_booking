package com.hotelbooking.booking;

import com.hotelbooking.booking.dto.BookingRequest;
import com.hotelbooking.booking.service.BookingService;
import com.hotelbooking.support.IntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookingFlowIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private BookingService bookingService;

    private String admin;
    private String guest;
    private long hotelId;

    private final LocalDate checkIn = daysFromNow(10);
    private final LocalDate checkOut = daysFromNow(12);

    @BeforeEach
    void setUp() throws Exception {
        admin = createAdmin();
        guest = registerGuest();
        hotelId = createHotel(admin);
    }

    @Test
    void untouchedDatesReportAllRoomsAvailable() throws Exception {
        long roomTypeId = createRoomType(admin, hotelId, 3);

        assertThat(availableRooms(roomTypeId, checkIn, checkOut)).isEqualTo(3);
    }

    @Test
    void cannotOverbookLastRoom() throws Exception {
        long roomTypeId = createRoomType(admin, hotelId, 1);

        bookOk(guest, roomTypeId, checkIn, checkOut);

        book(registerGuest(), roomTypeId, checkIn.plusDays(1), checkOut.plusDays(1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Room not available")));
        assertThat(availableRooms(roomTypeId, checkIn, checkOut)).isZero();
        // The check-out day is free again, so a stay starting that day fits.
        bookOk(registerGuest(), roomTypeId, checkOut, checkOut.plusDays(1));
    }

    @Test
    void concurrentBookingsForLastRoomOnlyOneSucceeds() throws Exception {
        long roomTypeId = createRoomType(admin, hotelId, 1);
        int attempts = 4;
        String[] emails = new String[attempts];
        for (int i = 0; i < attempts; i++) {
            emails[i] = "racer-" + UUID.randomUUID() + "@test.com";
            registerGuest(emails[i]);
        }

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(attempts);
        try {
            Future<?>[] results = new Future<?>[attempts];
            for (int i = 0; i < attempts; i++) {
                String email = emails[i];
                results[i] = pool.submit((Callable<Object>) () -> {
                    start.await();
                    return bookingService.createBooking(email, bookingRequest(roomTypeId));
                });
            }
            start.countDown();

            int succeeded = 0;
            for (Future<?> result : results) {
                try {
                    result.get(30, TimeUnit.SECONDS);
                    succeeded++;
                } catch (java.util.concurrent.ExecutionException ignored) {
                    // Expected for every request but one: no room left.
                }
            }
            assertThat(succeeded).isEqualTo(1);
            assertThat(availableRooms(roomTypeId, checkIn, checkOut)).isZero();
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void bookingCodeHasRandomSuffix() throws Exception {
        long roomTypeId = createRoomType(admin, hotelId, 1);

        book(guest, roomTypeId, checkIn, checkOut)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.bookingCode", matchesPattern("BK-\\d{8}-[A-Z2-9]{6}")));
    }

    @Test
    void cancellingFreesTheRoom() throws Exception {
        long roomTypeId = createRoomType(admin, hotelId, 1);
        long bookingId = bookOk(guest, roomTypeId, checkIn, checkOut);

        mockMvc.perform(withAuth(post("/api/bookings/" + bookingId + "/cancel"), guest))
                .andExpect(status().isOk());

        assertThat(availableRooms(roomTypeId, checkIn, checkOut)).isEqualTo(1);
        bookOk(registerGuest(), roomTypeId, checkIn, checkOut);
    }

    @Test
    void cannotBookRoomOfDeletedHotel() throws Exception {
        long roomTypeId = createRoomType(admin, hotelId, 1);

        mockMvc.perform(withAuth(delete("/api/admin/hotels/" + hotelId), admin))
                .andExpect(status().isOk());

        book(guest, roomTypeId, checkIn, checkOut).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/room-types/" + roomTypeId)).andExpect(status().isNotFound());
    }

    @Test
    void unpaidBookingExpiresAndReleasesRoom() throws Exception {
        long roomTypeId = createRoomType(admin, hotelId, 1);
        long bookingId = bookOk(guest, roomTypeId, checkIn, checkOut);

        bookingService.expirePendingBookings(LocalDateTime.now().plusMinutes(1));

        assertThat(bookingStatus(guest, bookingId)).isEqualTo("CANCELLED");
        assertThat(availableRooms(roomTypeId, checkIn, checkOut)).isEqualTo(1);
        mockMvc.perform(withAuth(post("/api/bookings/" + bookingId + "/pay"), guest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void paidBookingIsNotExpired() throws Exception {
        long roomTypeId = createRoomType(admin, hotelId, 1);
        long bookingId = bookOk(guest, roomTypeId, checkIn, checkOut);
        mockMvc.perform(withAuth(post("/api/bookings/" + bookingId + "/pay"), guest))
                .andExpect(status().isOk());

        bookingService.expirePendingBookings(LocalDateTime.now().plusMinutes(1));

        assertThat(bookingStatus(guest, bookingId)).isEqualTo("CONFIRMED");
    }

    @Test
    void finishedStayCanBeReviewed() throws Exception {
        long roomTypeId = createRoomType(admin, hotelId, 1);
        long bookingId = bookOk(guest, roomTypeId, checkIn, checkOut);
        mockMvc.perform(withAuth(post("/api/bookings/" + bookingId + "/pay"), guest))
                .andExpect(status().isOk());
        String review = json(Map.of("bookingId", bookingId, "rating", 5, "comment", "Great"));

        mockMvc.perform(withAuth(post("/api/reviews"), guest)
                        .contentType(MediaType.APPLICATION_JSON).content(review))
                .andExpect(status().isBadRequest());

        bookingService.completeFinishedBookings(checkOut.minusDays(1));
        assertThat(bookingStatus(guest, bookingId)).isEqualTo("CONFIRMED");

        BigDecimal revenueBefore = revenueThisMonth();
        bookingService.completeFinishedBookings(checkOut);
        assertThat(bookingStatus(guest, bookingId)).isEqualTo("COMPLETED");
        assertThat(revenueThisMonth()).isEqualByComparingTo(revenueBefore);

        mockMvc.perform(withAuth(post("/api/reviews"), guest)
                        .contentType(MediaType.APPLICATION_JSON).content(review))
                .andExpect(status().isCreated());
    }

    @Test
    void changingTotalRoomsUpdatesExistingInventory() throws Exception {
        long roomTypeId = createRoomType(admin, hotelId, 2);
        bookOk(guest, roomTypeId, checkIn, checkOut);
        bookOk(registerGuest(), roomTypeId, checkIn, checkOut);

        mockMvc.perform(withAuth(put("/api/admin/room-types/" + roomTypeId), admin)
                        .contentType(MediaType.APPLICATION_JSON).content(roomTypeJson(1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("already booked")));

        mockMvc.perform(withAuth(put("/api/admin/room-types/" + roomTypeId), admin)
                        .contentType(MediaType.APPLICATION_JSON).content(roomTypeJson(5)))
                .andExpect(status().isOk());
        assertThat(availableRooms(roomTypeId, checkIn, checkOut)).isEqualTo(3);
    }

    @Test
    void guestCannotCancelOnCheckInDayButAdminCan() throws Exception {
        long roomTypeId = createRoomType(admin, hotelId, 1);
        long bookingId = bookOk(guest, roomTypeId, daysFromNow(0), daysFromNow(1));

        mockMvc.perform(withAuth(get("/api/bookings/" + bookingId), guest))
                .andExpect(jsonPath("$.data.cancellable").value(false));
        mockMvc.perform(withAuth(post("/api/bookings/" + bookingId + "/cancel"), guest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bookings can only be cancelled before the check-in date"));
        mockMvc.perform(withAuth(post("/api/admin/bookings/" + bookingId + "/cancel"), admin))
                .andExpect(status().isOk());
    }

    @Test
    void futureBookingIsCancellable() throws Exception {
        long roomTypeId = createRoomType(admin, hotelId, 1);
        long bookingId = bookOk(guest, roomTypeId, checkIn, checkOut);

        mockMvc.perform(withAuth(get("/api/bookings/" + bookingId), guest))
                .andExpect(jsonPath("$.data.cancellable").value(true))
                .andExpect(jsonPath("$.data.reviewed").value(false));
    }

    @Test
    void reviewedFlagAndMultiStatusFilter() throws Exception {
        long roomTypeId = createRoomType(admin, hotelId, 3);
        long pending = bookOk(guest, roomTypeId, checkIn, checkOut);
        long completed = bookOk(guest, roomTypeId, checkIn, checkOut);
        mockMvc.perform(withAuth(post("/api/bookings/" + completed + "/pay"), guest)).andExpect(status().isOk());
        bookingService.completeFinishedBookings(checkOut);
        mockMvc.perform(withAuth(post("/api/reviews"), guest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("bookingId", completed, "rating", 5))))
                .andExpect(status().isCreated());

        mockMvc.perform(withAuth(get("/api/bookings/" + completed), guest))
                .andExpect(jsonPath("$.data.reviewed").value(true))
                .andExpect(jsonPath("$.data.cancellable").value(false));
        mockMvc.perform(withAuth(get("/api/bookings"), guest).param("status", "COMPLETED"))
                .andExpect(jsonPath("$.data.content[0].reviewed").value(true));
        mockMvc.perform(withAuth(get("/api/bookings"), guest).param("status", "PENDING,CONFIRMED"))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(pending));
    }

    private BookingRequest bookingRequest(long roomTypeId) {
        BookingRequest request = new BookingRequest();
        request.setRoomTypeId(roomTypeId);
        request.setCheckIn(checkIn);
        request.setCheckOut(checkOut);
        request.setGuests(1);
        request.setGuestName("Racer");
        request.setGuestEmail("racer@test.com");
        return request;
    }

    private BigDecimal revenueThisMonth() throws Exception {
        String body = mockMvc.perform(withAuth(get("/api/admin/stats"), admin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return new BigDecimal(JsonPath.read(body, "$.data.revenueThisMonth").toString());
    }
}
