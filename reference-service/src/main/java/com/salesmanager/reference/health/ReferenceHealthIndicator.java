package com.salesmanager.reference.health;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom DB health for reference-service (Wave 1 STR-05).
 */
@Component
public class ReferenceHealthIndicator implements HealthIndicator {

	private final DataSource dataSource;

	public ReferenceHealthIndicator(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public Health health() {
		try (Connection connection = dataSource.getConnection()) {
			if (connection.isValid(2)) {
				return Health.up().withDetail("database", "available").build();
			}
			return Health.down().withDetail("database", "invalid").build();
		} catch (Exception ex) {
			return Health.down(ex).withDetail("database", "unavailable").build();
		}
	}
}
