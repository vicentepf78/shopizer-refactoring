package com.salesmanager.search.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import modules.commons.search.SearchModule;

class SearchHealthIndicatorTest {

	@Test
	void reportsDownWhenOpenSearchClientFails() throws Exception {
		SearchModule searchModule = mock(SearchModule.class);
		when(searchModule.getConnection()).thenThrow(new RuntimeException("connection refused"));

		Health health = new SearchHealthIndicator(searchModule).health();

		assertThat(health.getStatus()).isEqualTo(Status.DOWN);
		assertThat(health.getDetails()).containsEntry("openSearch", "unavailable");
	}

	@Test
	void reportsUpWhenOpenSearchConnectionAvailable() throws Exception {
		SearchModule searchModule = mock(SearchModule.class);
		when(searchModule.getConnection()).thenReturn(new Object());

		Health health = new SearchHealthIndicator(searchModule).health();

		assertThat(health.getStatus()).isEqualTo(Status.UP);
		assertThat(health.getDetails()).containsEntry("openSearch", "available");
	}

	@Test
	void reportsDownWhenSearchModuleNotConfigured() {
		Health health = new SearchHealthIndicator(null).health();

		assertThat(health.getStatus()).isEqualTo(Status.DOWN);
		assertThat(health.getDetails()).containsEntry("openSearch", "not configured");
	}
}
