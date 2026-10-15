package com.hotelbooking.config;

import com.hotelbooking.auth.entity.User;
import com.hotelbooking.auth.enums.Role;
import com.hotelbooking.auth.repository.UserRepository;
import com.hotelbooking.hotel.entity.Hotel;
import com.hotelbooking.hotel.entity.RoomType;
import com.hotelbooking.hotel.repository.HotelRepository;
import com.hotelbooking.hotel.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Fills an empty database with demo accounts, hotels and room types so the app can be tried right
 * after cloning. Runs only with the {@code dev} profile and never touches a database that has data.
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DemoDataSeeder implements ApplicationRunner {

    private static final String IMG = "https://images.unsplash.com/photo-%s?w=1200&h=800&fit=crop&auto=format";

    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0 || hotelRepository.count() > 0) {
            return;
        }

        user("admin@stayease.dev", "admin123", "Demo Admin", Role.ADMIN);
        user("guest@stayease.dev", "guest123", "Demo Guest", Role.GUEST);

        hotel("The Grand Palace Hotel", "8 Trang Tien", "Hanoi", 5,
                "Classic luxury next to Hoan Kiem Lake.",
                List.of("Wifi", "Pool", "Gym", "Restaurant", "Spa"), "1571003123894-1f0594d2b5d9",
                room("Deluxe King", 2, 32, "120", 8), room("Executive Suite", 3, 55, "240", 3));
        hotel("Lotus Bay Resort", "120 Vo Nguyen Giap", "Da Nang", 4,
                "Beachfront resort on My Khe beach.",
                List.of("Wifi", "Beach", "Pool", "Spa"), "1520250497591-112f2f40a3f4",
                room("Ocean View Double", 2, 30, "95", 10), room("Family Villa", 5, 80, "260", 2));
        hotel("Saigon Heritage Hotel", "45 Dong Khoi", "Ho Chi Minh City", 4,
                "Colonial-era building in District 1.",
                List.of("Wifi", "Pool", "Restaurant", "Bar"), "1551882547-ff40c63fe2f7",
                room("Superior Twin", 2, 26, "75", 12), room("Heritage Suite", 3, 48, "180", 4));
        hotel("Hoi An Ancient Boutique", "22 Tran Phu", "Hoi An", 4,
                "Small boutique hotel steps from the old town.",
                List.of("Wifi", "Pool", "Restaurant"), "1445019980597-93fa8acb246c",
                room("Garden Room", 2, 24, "60", 6), room("Balcony Suite", 2, 36, "110", 3));
        hotel("Pearl Bay Resort", "Bai Truong", "Phu Quoc", 5,
                "Island resort with a private beach.",
                List.of("Wifi", "Beach", "Spa", "Pool", "Bar"), "1529290130-4ca3753253ae",
                room("Beach Bungalow", 2, 40, "180", 6), room("Pool Villa", 4, 90, "350", 2));
        hotel("Ha Long Horizon Hotel", "Ha Long Road", "Ha Long", 3,
                "Bay views at a friendly price.",
                List.of("Wifi", "Restaurant", "Gym"), "1542314831-068cd1dbfeeb",
                room("Standard Double", 2, 22, "45", 15), room("Bay View Family", 4, 38, "85", 5));

        log.info("Seeded demo data. Admin: admin@stayease.dev / admin123 — Guest: guest@stayease.dev / guest123");
    }

    private void user(String email, String password, String fullName, Role role) {
        userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .fullName(fullName)
                .role(role)
                .build());
    }

    private record RoomSpec(String name, int maxCapacity, double area, String price, int totalRooms) {
    }

    private static RoomSpec room(String name, int maxCapacity, double area, String price, int totalRooms) {
        return new RoomSpec(name, maxCapacity, area, price, totalRooms);
    }

    private void hotel(String name, String address, String city, int stars, String description,
                       List<String> amenities, String imageId, RoomSpec... rooms) {
        Hotel hotel = hotelRepository.save(Hotel.builder()
                .name(name)
                .address(address)
                .city(city)
                .country("Vietnam")
                .starRating(stars)
                .description(description)
                .amenities(amenities)
                .images(List.of(IMG.formatted(imageId)))
                .build());

        for (RoomSpec spec : rooms) {
            roomTypeRepository.save(RoomType.builder()
                    .hotel(hotel)
                    .name(spec.name())
                    .maxCapacity(spec.maxCapacity())
                    .area(spec.area())
                    .basePrice(new BigDecimal(spec.price()))
                    .totalRooms(spec.totalRooms())
                    .amenities(List.of("Wifi", "Air conditioning"))
                    .images(List.of(IMG.formatted(imageId)))
                    .build());
        }
    }
}
