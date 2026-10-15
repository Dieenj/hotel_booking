package com.hotelbooking.hotel;

import com.hotelbooking.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HotelSearchIntegrationTest extends IntegrationTestSupport {

    @Test
    void searchWithoutFilters() throws Exception {
        mockMvc.perform(get("/api/hotels")).andExpect(status().isOk());
    }

    @Test
    void searchByCityOnlyIsCaseInsensitive() throws Exception {
        String admin = createAdmin();
        long hotelId = createHotel(admin, "Search Hotel", "Da Lat");

        mockMvc.perform(get("/api/hotels").param("city", "da lat").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].id", hasItem((int) hotelId)));
    }

    @Test
    void searchByNameOnly() throws Exception {
        String admin = createAdmin();
        long hotelId = createHotel(admin, "Unique Sunrise Inn", "Hue");

        mockMvc.perform(get("/api/hotels").param("name", "sunrise"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].id", hasItem((int) hotelId)));
    }

    @Test
    void blankFiltersAreIgnored() throws Exception {
        mockMvc.perform(get("/api/hotels").param("city", "  ").param("name", ""))
                .andExpect(status().isOk());
    }

    @Test
    void wildcardCharactersAreMatchedLiterally() throws Exception {
        String admin = createAdmin();
        long hotelId = createHotel(admin, "Plain Name Hotel", "Vung Tau");

        mockMvc.perform(get("/api/hotels").param("name", "%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].id", not(hasItem((int) hotelId))));
    }

    @Test
    void dateSearchExcludesSoldOutHotels() throws Exception {
        String admin = createAdmin();
        long soldOut = createHotel(admin, "Sold Out Inn", "Can Tho");
        long soldOutRoom = createRoomType(admin, soldOut, 1);
        long free = createHotel(admin, "Free Inn", "Can Tho");
        createRoomType(admin, free, 1);
        bookOk(registerGuest(), soldOutRoom, daysFromNow(10), daysFromNow(12));

        // Overlapping stay: only the hotel with a free room.
        mockMvc.perform(get("/api/hotels").param("city", "Can Tho")
                        .param("checkIn", daysFromNow(11).toString())
                        .param("checkOut", daysFromNow(13).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].id", hasItem((int) free)))
                .andExpect(jsonPath("$.data.content[*].id", not(hasItem((int) soldOut))));

        // Stay starting on the booked stay's check-out day: both are free.
        mockMvc.perform(get("/api/hotels").param("city", "Can Tho")
                        .param("checkIn", daysFromNow(12).toString())
                        .param("checkOut", daysFromNow(14).toString()))
                .andExpect(jsonPath("$.data.content[*].id", hasItem((int) soldOut)))
                .andExpect(jsonPath("$.data.content[*].id", hasItem((int) free)));
    }

    @Test
    void guestsFilterRequiresBigEnoughRoom() throws Exception {
        String admin = createAdmin();
        long hotelId = createHotel(admin, "Couples Retreat", "Quy Nhon");
        createRoomType(admin, hotelId, 1); // max 2 guests

        mockMvc.perform(get("/api/hotels").param("city", "Quy Nhon").param("guests", "2"))
                .andExpect(jsonPath("$.data.content[*].id", hasItem((int) hotelId)));
        mockMvc.perform(get("/api/hotels").param("city", "Quy Nhon").param("guests", "3"))
                .andExpect(jsonPath("$.data.content[*].id", not(hasItem((int) hotelId))));
    }

    @Test
    void hotelWithoutRoomTypesIsNotAvailable() throws Exception {
        String admin = createAdmin();
        long hotelId = createHotel(admin, "Empty Shell Hotel", "Buon Ma Thuot");

        mockMvc.perform(get("/api/hotels").param("city", "Buon Ma Thuot")
                        .param("checkIn", daysFromNow(5).toString())
                        .param("checkOut", daysFromNow(6).toString()))
                .andExpect(jsonPath("$.data.content[*].id", not(hasItem((int) hotelId))));
    }

    @Test
    void invalidDateSearchIsBadRequest() throws Exception {
        mockMvc.perform(get("/api/hotels").param("checkIn", daysFromNow(5).toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Provide both checkIn and checkOut, or neither"));
        mockMvc.perform(get("/api/hotels")
                        .param("checkIn", daysFromNow(5).toString())
                        .param("checkOut", daysFromNow(5).toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void minRatingFilter() throws Exception {
        mockMvc.perform(get("/api/hotels").param("minRating", "4").param("city", "Hanoi"))
                .andExpect(status().isOk());
    }
}
