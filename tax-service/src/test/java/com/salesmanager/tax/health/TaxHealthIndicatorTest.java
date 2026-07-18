package com.salesmanager.tax.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

class TaxHealthIndicatorTest {

	@Test
	void reportsDownWhenReferenceHttpFailsEvenIfDatabaseUp() throws Exception {
		DataSource dataSource = mock(DataSource.class);
		Connection connection = mock(Connection.class);
		when(dataSource.getConnection()).thenReturn(connection);
		when(connection.isValid(anyInt())).thenReturn(true);

		RestTemplate restTemplate = mock(RestTemplate.class);
		when(restTemplate.getForEntity(anyString(), eq(String.class)))
				.thenThrow(new RestClientException("reference down"));

		Health health = new TaxHealthIndicator(dataSource, restTemplate, "http://reference:8081").health();

		assertThat(health.getStatus()).isEqualTo(Status.DOWN);
		assertThat(health.getDetails()).containsEntry("database", "available");
		assertThat(health.getDetails()).containsEntry("reference", "unavailable");
	}

	@Test
	void reportsUpWhenDatabaseAndReferenceOk() throws Exception {
		DataSource dataSource = mock(DataSource.class);
		Connection connection = mock(Connection.class);
		when(dataSource.getConnection()).thenReturn(connection);
		when(connection.isValid(anyInt())).thenReturn(true);

		RestTemplate restTemplate = mock(RestTemplate.class);
		when(restTemplate.getForEntity(eq("http://reference:8081/actuator/health"), eq(String.class)))
				.thenReturn(new ResponseEntity<>("{\"status\":\"UP\"}", HttpStatus.OK));

		Health health = new TaxHealthIndicator(dataSource, restTemplate, "http://reference:8081").health();

		assertThat(health.getStatus()).isEqualTo(Status.UP);
		assertThat(health.getDetails()).containsEntry("reference", "available");
	}
}
