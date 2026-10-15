package com.hotelbooking.support;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Rebuilds the schema from the migrations on every test run (also on a reused PostgreSQL database). */
@Configuration
public class FlywayTestConfig {

    @Bean
    FlywayMigrationStrategy cleanMigrateStrategy() {
        return flyway -> {
            flyway.clean();
            flyway.migrate();
        };
    }
}
