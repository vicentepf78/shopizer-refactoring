package com.salesmanager.shop.strangler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wave2")
public class Wave2Properties {

	private final Strangler strangler = new Strangler();
	private final ServiceEndpoint contentService = new ServiceEndpoint();
	private final SearchServiceEndpoint searchService = new SearchServiceEndpoint();
	private final MerchantServiceEndpoint merchantService = new MerchantServiceEndpoint();
	private final Http http = new Http();
	private final Search search = new Search();

	public Strangler getStrangler() {
		return strangler;
	}

	public ServiceEndpoint getContentService() {
		return contentService;
	}

	public SearchServiceEndpoint getSearchService() {
		return searchService;
	}

	public MerchantServiceEndpoint getMerchantService() {
		return merchantService;
	}

	public Http getHttp() {
		return http;
	}

	public Search getSearch() {
		return search;
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

	public static class SearchServiceEndpoint extends ServiceEndpoint {
		private String internalToken;

		public String getInternalToken() {
			return internalToken;
		}

		public void setInternalToken(String internalToken) {
			this.internalToken = internalToken;
		}
	}

	public static class MerchantServiceEndpoint extends ServiceEndpoint {
		private final Cache cache = new Cache();

		public Cache getCache() {
			return cache;
		}

		public static class Cache {
			private long ttlSeconds = 60L;

			public long getTtlSeconds() {
				return ttlSeconds;
			}

			public void setTtlSeconds(long ttlSeconds) {
				this.ttlSeconds = ttlSeconds;
			}
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

	public static class Search {
		private final Index index = new Index();

		public Index getIndex() {
			return index;
		}

		public static class Index {
			private long reindexDelayMs = 0L;

			public long getReindexDelayMs() {
				return reindexDelayMs;
			}

			public void setReindexDelayMs(long reindexDelayMs) {
				this.reindexDelayMs = reindexDelayMs;
			}
		}
	}
}
