package com.salesmanager.merchant.health;

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

@Component
public class MerchantHealthIndicator implements HealthIndicator {

	private final DataSource dataSource;
	private final RestTemplate referenceRestTemplate;
	private final RestTemplate contentRestTemplate;
	private final String referenceHealthUrl;
	private final String contentHealthUrl;

	public MerchantHealthIndicator(
			DataSource dataSource,
			RestTemplate referenceRestTemplate,
			RestTemplate contentRestTemplate,
			@Value("${wave1.reference-service.base-url}") String referenceBaseUrl,
			@Value("${wave2.content-service.base-url}") String contentBaseUrl) {
		this.dataSource = dataSource;
		this.referenceRestTemplate = referenceRestTemplate;
		this.contentRestTemplate = contentRestTemplate;
		this.referenceHealthUrl = StringUtils.removeEnd(referenceBaseUrl, "/") + "/actuator/health";
		this.contentHealthUrl = StringUtils.removeEnd(contentBaseUrl, "/") + "/actuator/health";
	}

	@Override
	public Health health() {
		Health.Builder builder = Health.up();
		boolean dbUp = checkDatabase(builder);
		boolean referenceUp = checkReference(builder);
		boolean contentUp = checkContent(builder);
		if (dbUp && referenceUp && contentUp) {
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
		return checkRemote(builder, referenceRestTemplate, referenceHealthUrl, "referenceService");
	}

	boolean checkContent(Health.Builder builder) {
		return checkRemote(builder, contentRestTemplate, contentHealthUrl, "contentService");
	}

	private boolean checkRemote(
			Health.Builder builder,
			RestTemplate restTemplate,
			String healthUrl,
			String detailKey) {
		try {
			ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
			if (response.getStatusCode().is2xxSuccessful()) {
				builder.withDetail(detailKey, "available");
				return true;
			}
			builder.withDetail(detailKey, "status=" + response.getStatusCodeValue());
			return false;
		} catch (RestClientException ex) {
			builder.withDetail(detailKey, "unavailable");
			builder.withException(ex);
			return false;
		}
	}
}
