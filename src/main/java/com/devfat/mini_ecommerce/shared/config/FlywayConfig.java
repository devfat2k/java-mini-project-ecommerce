package com.devfat.mini_ecommerce.shared.config;











import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flyway migration strategy: repair trước khi migrate để tự động đồng bộ
 * checksum trong flyway_schema_history với file migration hiện tại.
 *
 * <p>Lý do cần bean này: Spring Boot không expose property "repair-on-migrate"
 * — property đó chỉ tồn tại ở Flyway Teams/Enterprise edition, không phải
 * Community. Nếu chỉ dùng application.yaml thì repair sẽ bị bỏ qua hoàn toàn.</p>
 *
 * <p>repair() chỉ cập nhật checksum trong bảng flyway_schema_history cho các
 * migration đã apply — nó KHÔNG chạy lại migration, KHÔNG xoá dữ liệu.</p>
 */
@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy repairThenMigrate() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
