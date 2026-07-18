package com.salesmanager.tax.health;

import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Tax health: shared DB + reference-service HTTP (Wave 1 STR-05).
 */
@Component
public class TaxHealthIndicator implements HealthIndicator {

	private final DataSource dataSource;
	private final RestTemplate referenceRestTemplate;
	private final String referenceHealthUrl;

	public TaxHealthIndicator(
			DataSource dataSource,
			RestTemplate referenceRestTemplate,
			@Value("${wave1.reference-service.base-url}") String referenceBaseUrl) {
		this.dataSource = dataSource;
		this.referenceRestTemplate = referenceRestTemplate;
		this.referenceHealthUrl = StringUtils.removeEnd(referenceBaseUrl, "/") + "/actuator/health";
	}

	@Override
	public Health health() {
		Health.Builder builder = Health.up();
		boolean dbUp = checkDatabase(builder);
		boolean referenceUp = checkReference(builder);
		if (dbUp && referenceUp) {
			return builder.up().build();
		}
		return builder.down().build();
	}

	boolean checkDatabase(Health.Builder builder) {
		try (Connection connection = dataSource.getConnection()) {
			if (connection.isValid(2)) {
				builder.withDetail("database", "available");
				return true;
			}
			builder.withDetail("database", "invalid");
			return false;
		} catch (Exception ex) {
			builder.withDetail("database", "unavailable");
			builder.withException(ex);
			return false;
		}
	}

	boolean checkReference(Health.Builder builder) {
		try {
			ResponseEntity<String> response =
					referenceRestTemplate.getForEntity(referenceHealthUrl, String.class);
			if (response.getStatusCode().is2xxSuccessful()) {
				builder.withDetail("reference", "available");
				return true;
			}
			builder.withDetail("reference", "status=" + response.getStatusCodeValue());
			return false;
		} catch (RestClientException ex) {
			builder.withDetail("reference", "unavailable");
			builder.withException(ex);
			return false;
		}
	}
}
