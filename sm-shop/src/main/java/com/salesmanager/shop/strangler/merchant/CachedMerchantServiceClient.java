package com.salesmanager.shop.strangler.merchant;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.salesmanager.contracts.client.MerchantServiceClient;
import com.salesmanager.contracts.merchant.MerchantStoreSnapshot;

/**
 * ponytail: in-memory TTL cache; upgrade to Caffeine if eviction pressure grows.
 */
public class CachedMerchantServiceClient implements MerchantServiceClient {

	private final MerchantServiceClient delegate;
	private final long ttlMillis;
	private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

	public CachedMerchantServiceClient(MerchantServiceClient delegate, long ttlSeconds) {
		this.delegate = delegate;
		this.ttlMillis = ttlSeconds * 1000L;
	}

	@Override
	public MerchantStoreSnapshot getStoreSnapshot(String code) {
		long now = System.currentTimeMillis();
		CacheEntry entry = cache.get(code);
		if (entry != null && now - entry.loadedAt < ttlMillis) {
			return entry.snapshot;
		}
		MerchantStoreSnapshot snapshot = delegate.getStoreSnapshot(code);
		if (snapshot != null) {
			cache.put(code, new CacheEntry(snapshot, now));
		}
		return snapshot;
	}

	private static final class CacheEntry {
		private final MerchantStoreSnapshot snapshot;
		private final long loadedAt;

		private CacheEntry(MerchantStoreSnapshot snapshot, long loadedAt) {
			this.snapshot = snapshot;
			this.loadedAt = loadedAt;
		}
	}
}
