package com.salesmanager.content.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

class ContentHealthIndicatorTest {

	@TempDir
	java.nio.file.Path tempDir;

	@Test
	void reportsUpWhenDatabaseCmsAndReferenceOk() throws Exception {
		DataSource dataSource = mock(DataSource.class);
		Connection connection = mock(Connection.class);
		when(dataSource.getConnection()).thenReturn(connection);
		when(connection.isValid(anyInt())).thenReturn(true);

		RestTemplate restTemplate = mock(RestTemplate.class);
		when(restTemplate.getForEntity(eq("http://reference:8081/actuator/health"), eq(String.class)))
				.thenReturn(new ResponseEntity<>("{\"status\":\"UP\"}", HttpStatus.OK));

		Health health = new ContentHealthIndicator(
				dataSource,
				restTemplate,
				"http://reference:8081",
				"default",
				tempDir.toString()).health();

		assertThat(health.getStatus()).isEqualTo(Status.UP);
		assertThat(health.getDetails()).containsEntry("database", "available");
		assertThat(health.getDetails()).containsEntry("cms", "available");
		assertThat(health.getDetails()).containsEntry("referenceService", "available");
	}

	@Test
	void reportsDownWhenCmsBackendInaccessible() throws Exception {
		DataSource dataSource = mock(DataSource.class);
		Connection connection = mock(Connection.class);
		when(dataSource.getConnection()).thenReturn(connection);
		when(connection.isValid(anyInt())).thenReturn(true);

		RestTemplate restTemplate = mock(RestTemplate.class);
		when(restTemplate.getForEntity(anyString(), eq(String.class)))
				.thenReturn(new ResponseEntity<>("{\"status\":\"UP\"}", HttpStatus.OK));

		java.nio.file.Path blockingFile = tempDir.resolve("cms-blocked");
		Files.writeString(blockingFile, "not-a-directory");
		String cmsPath = blockingFile.resolve("nested").toString();

		Health health = new ContentHealthIndicator(
				dataSource,
				restTemplate,
				"http://reference:8081",
				"default",
				cmsPath).health();

		assertThat(health.getStatus()).isEqualTo(Status.DOWN);
		assertThat(health.getDetails()).containsEntry("cms", "unavailable");
	}

	@Test
	void reportsDownWhenReferenceHttpFails() throws Exception {
		DataSource dataSource = mock(DataSource.class);
		Connection connection = mock(Connection.class);
		when(dataSource.getConnection()).thenReturn(connection);
		when(connection.isValid(anyInt())).thenReturn(true);

		RestTemplate restTemplate = mock(RestTemplate.class);
		when(restTemplate.getForEntity(anyString(), eq(String.class)))
				.thenThrow(new RestClientException("reference down"));

		Health health = new ContentHealthIndicator(
				dataSource,
				restTemplate,
				"http://reference:8081",
				"default",
				tempDir.toString()).health();

		assertThat(health.getStatus()).isEqualTo(Status.DOWN);
		assertThat(health.getDetails()).containsEntry("referenceService", "unavailable");
	}
}
