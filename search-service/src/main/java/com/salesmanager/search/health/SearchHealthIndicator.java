package com.salesmanager.search.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import modules.commons.search.SearchModule;

@Component
public class SearchHealthIndicator implements HealthIndicator {

	private final SearchModule searchModule;

	public SearchHealthIndicator(@Autowired(required = false) SearchModule searchModule) {
		this.searchModule = searchModule;
	}

	@Override
	public Health health() {
		if (searchModule == null) {
			return Health.down().withDetail("openSearch", "not configured").build();
		}
		try {
			Object connection = searchModule.getConnection();
			if (connection == null) {
				return Health.down().withDetail("openSearch", "no connection").build();
			}
			return Health.up().withDetail("openSearch", "available").build();
		} catch (Exception ex) {
			return Health.down().withDetail("openSearch", "unavailable").withException(ex).build();
		}
	}
}
