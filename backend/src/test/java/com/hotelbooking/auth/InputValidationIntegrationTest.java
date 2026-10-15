package com.hotelbooking.auth;

import com.hotelbooking.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InputValidationIntegrationTest extends IntegrationTestSupport {

    @Test
    void emailIsCaseInsensitive() throws Exception {
        String local = "Case-" + UUID.randomUUID();
        registerGuest(local + "@Test.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("fullName", "Dup", "email", local.toUpperCase() + "@TEST.COM",
                                "password", PASSWORD))))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", local.toLowerCase() + "@TEST.com", "password", PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(local.toLowerCase() + "@test.com"));
    }

    @Test
    void repeatedFailedLoginsAreThrottled() throws Exception {
        String email = "throttle-" + UUID.randomUUID() + "@test.com";
        registerGuest(email);
        String wrong = json(Map.of("email", email, "password", "wrong-password"));
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(wrong))
                    .andExpect(status().isUnauthorized());
        }

        // Locked out even with the right password.
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", PASSWORD))))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.error").value("Too many failed login attempts. Try again later."));
    }

    @Test
    void overlongHotelNameIsBadRequest() throws Exception {
        mockMvc.perform(withAuth(post("/api/admin/hotels"), createAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "a".repeat(300), "address", "a", "city", "a", "starRating", 3))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Hotel name must be at most 255 characters"));
    }

    @Test
    void zeroPriceIsRejected() throws Exception {
        String admin = createAdmin();
        long roomTypeId = createRoomType(admin, createHotel(admin), 1);

        mockMvc.perform(withAuth(put("/api/admin/room-types/" + roomTypeId), admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Deluxe", "maxCapacity", 2, "basePrice", 0, "totalRooms", 1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Base price must be greater than 0"));
    }
}
