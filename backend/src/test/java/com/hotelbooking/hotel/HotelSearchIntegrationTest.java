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
    void minRatingFilter() throws Exception {
        mockMvc.perform(get("/api/hotels").param("minRating", "4").param("city", "Hanoi"))
                .andExpect(status().isOk());
    }
}
