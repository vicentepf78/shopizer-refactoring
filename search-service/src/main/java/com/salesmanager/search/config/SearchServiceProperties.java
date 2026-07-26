package com.salesmanager.search.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "search-service")
public class SearchServiceProperties {

	private final Internal internal = new Internal();

	public Internal getInternal() {
		return internal;
	}

	public static class Internal {
		private String token = "dev-search-token";

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}
	}
}
