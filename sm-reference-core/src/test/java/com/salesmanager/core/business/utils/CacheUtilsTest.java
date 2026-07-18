package com.salesmanager.core.business.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

@ExtendWith(MockitoExtension.class)
class CacheUtilsTest {

	@Mock
	private Cache cache;

	private CacheUtils cacheUtils;

	@BeforeEach
	void setUp() throws Exception {
		cacheUtils = new CacheUtils();
		Field field = CacheUtils.class.getDeclaredField("cache");
		field.setAccessible(true);
		field.set(cacheUtils, cache);
	}

	@Test
	void putAndGetRoundTrip() throws Exception {
		when(cache.get("K")).thenReturn(new SimpleValueWrapper("V"));

		cacheUtils.putInCache("V", "K");
		verify(cache).put("K", "V");

		assertEquals("V", cacheUtils.getFromCache("K"));
	}

	@Test
	void getFromCache_whenMissing_returnsNull() throws Exception {
		when(cache.get("missing")).thenReturn(null);
		assertNull(cacheUtils.getFromCache("missing"));
	}

	@Test
	void removeFromCache_evictsKey() throws Exception {
		cacheUtils.removeFromCache("K");
		verify(cache).evict("K");
	}

	@Test
	void shutDownCache_isNoOp() throws Exception {
		cacheUtils.shutDownCache();
	}
}
