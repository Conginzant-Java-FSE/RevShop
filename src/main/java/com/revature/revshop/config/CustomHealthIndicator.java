package com.revature.revshop.config;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class CustomHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public CustomHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        boolean dbUp = checkDatabase();
        if (dbUp) {
            return Health.up()
                    .withDetail("app", "RevShop is running")
                    .withDetail("database", "Connected")
                    .build();
        }
        return Health.down()
                .withDetail("app", "RevShop")
                .withDetail("error", "Database connectivity issue")
                .build();
    }

    private boolean checkDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }
}
