package com.hotelbooking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.jwt.secret=test-secret-key-that-is-at-least-256-bits-long-for-testing",
        "app.jwt.expiration=86400000",
        "app.cors.allowed-origins=http://localhost:5173",
        "spring.mail.host=localhost",
        "spring.mail.port=25"
})
class HotelBookingApplicationTests {

    @Test
    void contextLoads() {
    }
}
