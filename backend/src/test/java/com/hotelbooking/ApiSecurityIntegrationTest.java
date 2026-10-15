package com.hotelbooking;

import com.hotelbooking.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiSecurityIntegrationTest extends IntegrationTestSupport {

    @Test
    void meWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Authentication required"));
    }

    @Test
    void updateProfileWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(put("/api/auth/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("fullName", "Nobody"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidTokenIsUnauthorized() throws Exception {
        mockMvc.perform(withAuth(get("/api/bookings"), "not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meWithTokenReturnsProfile() throws Exception {
        mockMvc.perform(withAuth(get("/api/auth/me"), registerGuest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("GUEST"));
    }

    @Test
    void guestCannotUseAdminEndpoints() throws Exception {
        mockMvc.perform(withAuth(get("/api/admin/stats"), registerGuest()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied"));
    }

    @Test
    void availabilityRejectsTooLongRange() throws Exception {
        long roomTypeId = createRoomType(createAdmin(), createHotel(createAdmin()), 1);

        mockMvc.perform(get("/api/availability")
                        .param("roomTypeId", String.valueOf(roomTypeId))
                        .param("checkIn", daysFromNow(1).toString())
                        .param("checkOut", daysFromNow(400).toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Maximum booking duration is 30 nights"));
    }

    @Test
    void availabilityWithMissingParamIsBadRequest() throws Exception {
        mockMvc.perform(get("/api/availability").param("roomTypeId", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bookingWithInvalidGuestEmailIsRejected() throws Exception {
        String admin = createAdmin();
        long roomTypeId = createRoomType(admin, createHotel(admin), 1);

        mockMvc.perform(withAuth(post("/api/bookings"), registerGuest())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "roomTypeId", roomTypeId,
                                "checkIn", daysFromNow(1).toString(),
                                "checkOut", daysFromNow(2).toString(),
                                "guests", 1,
                                "guestName", "Guest",
                                "guestEmail", "not-an-email"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid guest email format"));
    }

    @Test
    void canBookStartingToday() throws Exception {
        String admin = createAdmin();
        long roomTypeId = createRoomType(admin, createHotel(admin), 1);

        bookOk(registerGuest(), roomTypeId, daysFromNow(0), daysFromNow(1));
    }

    @Test
    void oversizedPageIsClamped() throws Exception {
        mockMvc.perform(get("/api/hotels").param("size", "100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size").value(100));
    }
}
