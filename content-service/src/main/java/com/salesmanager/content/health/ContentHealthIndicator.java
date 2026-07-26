package com.salesmanager.content.health;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
public class ContentHealthIndicator implements HealthIndicator {

	private final DataSource dataSource;
	private final RestTemplate referenceRestTemplate;
	private final String referenceHealthUrl;
	private final String cmsMethod;
	private final String cmsFilesLocation;

	public ContentHealthIndicator(
			DataSource dataSource,
			RestTemplate referenceRestTemplate,
			@Value("${wave1.reference-service.base-url}") String referenceBaseUrl,
			@Value("${config.cms.method:default}") String cmsMethod,
			@Value("${config.cms.files.location:./files}") String cmsFilesLocation) {
		this.dataSource = dataSource;
		this.referenceRestTemplate = referenceRestTemplate;
		this.referenceHealthUrl = StringUtils.removeEnd(referenceBaseUrl, "/") + "/actuator/health";
		this.cmsMethod = cmsMethod;
		this.cmsFilesLocation = cmsFilesLocation;
	}

	@Override
	public Health health() {
		Health.Builder builder = Health.up();
		boolean dbUp = checkDatabase(builder);
		boolean cmsUp = checkCms(builder);
		boolean referenceUp = checkReference(builder);
		if (dbUp && cmsUp && referenceUp) {
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

	boolean checkCms(Health.Builder builder) {
		if (!"default".equalsIgnoreCase(cmsMethod) && !"httpd".equalsIgnoreCase(cmsMethod)) {
			builder.withDetail("cms", "skipped(method=" + cmsMethod + ")");
			return true;
		}
		try {
			Path path = Paths.get(cmsFilesLocation);
			if (!Files.exists(path)) {
				Files.createDirectories(path);
			}
			builder.withDetail("cms", "available");
			return true;
		} catch (IOException ex) {
			builder.withDetail("cms", "unavailable");
			builder.withException(ex);
			return false;
		}
	}

	boolean checkReference(Health.Builder builder) {
		try {
			ResponseEntity<String> response =
					referenceRestTemplate.getForEntity(referenceHealthUrl, String.class);
			if (response.getStatusCode().is2xxSuccessful()) {
				builder.withDetail("referenceService", "available");
				return true;
			}
			builder.withDetail("referenceService", "status=" + response.getStatusCodeValue());
			return false;
		} catch (RestClientException ex) {
			builder.withDetail("referenceService", "unavailable");
			builder.withException(ex);
			return false;
		}
	}
}
