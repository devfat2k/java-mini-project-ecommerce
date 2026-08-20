package com.devfat.mini_ecommerce.shared.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Flyway migration strategy: repair trước khi migrate để tự động đồng bộ
 * checksum trong flyway_schema_history với file migration hiện tại.
 *
 * <p>Chỉ chạy trên profile "dev" để tránh làm hỏng checksum schema history trên Production.</p>
 */
@Configuration
@Profile("dev")
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy repairThenMigrate() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
