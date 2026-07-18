package com.salesmanager.shop.strangler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wave1")
public class Wave1Properties {

	private final Strangler strangler = new Strangler();
	private final ServiceEndpoint referenceService = new ServiceEndpoint();
	private final ServiceEndpoint taxService = new ServiceEndpoint();
	private final Http http = new Http();

	public Strangler getStrangler() {
		return strangler;
	}

	public ServiceEndpoint getReferenceService() {
		return referenceService;
	}

	public ServiceEndpoint getTaxService() {
		return taxService;
	}

	public Http getHttp() {
		return http;
	}

	public static class Strangler {
		private boolean enabled = false;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}

	public static class ServiceEndpoint {
		private String baseUrl;

		public String getBaseUrl() {
			return baseUrl;
		}

		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}
	}

	public static class Http {
		private final Client client = new Client();

		public Client getClient() {
			return client;
		}

		public static class Client {
			private long timeoutMs = 5000L;

			public long getTimeoutMs() {
				return timeoutMs;
			}

			public void setTimeoutMs(long timeoutMs) {
				this.timeoutMs = timeoutMs;
			}
		}
	}
}
