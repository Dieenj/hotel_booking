package com.hotelbooking.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.auth.entity.User;
import com.hotelbooking.auth.enums.Role;
import com.hotelbooking.auth.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base class for end-to-end API tests against an in-memory H2 database.
 * Each helper creates uniquely named data, so tests do not depend on each other.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

    protected static final String PASSWORD = "password123";

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UserRepository userRepository;
    @Autowired protected PasswordEncoder passwordEncoder;

    // ── Auth ─────────────────────────────────────────────────────────────────

    protected String registerGuest() throws Exception {
        return registerGuest("guest-" + UUID.randomUUID() + "@test.com");
    }

    protected String registerGuest(String email) throws Exception {
        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("fullName", "Test Guest", "email", email, "password", PASSWORD))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.data.token");
    }

    protected String createAdmin() throws Exception {
        String email = "admin-" + UUID.randomUUID() + "@test.com";
        userRepository.save(User.builder()
                .email(email)
                .fullName("Test Admin")
                .password(passwordEncoder.encode(PASSWORD))
                .role(Role.ADMIN)
                .build());
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", PASSWORD))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.data.token");
    }

    // ── Hotels ───────────────────────────────────────────────────────────────

    protected long createHotel(String adminToken) throws Exception {
        return createHotel(adminToken, "Hotel " + UUID.randomUUID(), "Hanoi");
    }

    protected long createHotel(String adminToken, String name, String city) throws Exception {
        String body = mockMvc.perform(post("/api/admin/hotels")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", name,
                                "address", "1 Test Street",
                                "city", city,
                                "starRating", 4))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.data.id")).longValue();
    }

    protected long createRoomType(String adminToken, long hotelId, int totalRooms) throws Exception {
        String body = mockMvc.perform(post("/api/admin/hotels/" + hotelId + "/room-types")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roomTypeJson(totalRooms)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.data.id")).longValue();
    }

    protected String roomTypeJson(int totalRooms) throws Exception {
        return json(Map.of(
                "name", "Deluxe",
                "maxCapacity", 2,
                "basePrice", 100,
                "totalRooms", totalRooms));
    }

    // ── Bookings ─────────────────────────────────────────────────────────────

    protected ResultActions book(String token, long roomTypeId, LocalDate checkIn, LocalDate checkOut)
            throws Exception {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("roomTypeId", roomTypeId);
        request.put("checkIn", checkIn.toString());
        request.put("checkOut", checkOut.toString());
        request.put("guests", 1);
        request.put("guestName", "Test Guest");
        request.put("guestEmail", "guest@test.com");
        return mockMvc.perform(post("/api/bookings")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(request)));
    }

    protected long bookOk(String token, long roomTypeId, LocalDate checkIn, LocalDate checkOut)
            throws Exception {
        String body = book(token, roomTypeId, checkIn, checkOut)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.data.id")).longValue();
    }

    protected String bookingStatus(String token, long bookingId) throws Exception {
        String body = mockMvc.perform(withAuth(get("/api/bookings/" + bookingId), token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.data.status");
    }

    protected int availableRooms(long roomTypeId, LocalDate checkIn, LocalDate checkOut) throws Exception {
        String body = mockMvc.perform(get("/api/availability")
                        .param("roomTypeId", String.valueOf(roomTypeId))
                        .param("checkIn", checkIn.toString())
                        .param("checkOut", checkOut.toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.data.availableRooms");
    }

    // ── Utilities ────────────────────────────────────────────────────────────

    protected static LocalDate daysFromNow(int days) {
        return LocalDate.now().plusDays(days);
    }

    protected MockHttpServletRequestBuilder withAuth(MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", bearer(token));
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }

    protected String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
