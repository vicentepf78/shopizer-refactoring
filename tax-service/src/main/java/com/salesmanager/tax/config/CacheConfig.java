package com.salesmanager.tax.config;

import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class CacheConfig {

	@Bean
	public EhCacheManagerFactoryBean springCacheManager() {
		EhCacheManagerFactoryBean factory = new EhCacheManagerFactoryBean();
		factory.setConfigLocation(new ClassPathResource("ehcache.xml"));
		// reuse named CacheManager across Spring test contexts in the same JVM
		factory.setShared(true);
		return factory;
	}

	@Bean
	public EhCacheCacheManager cacheManager(net.sf.ehcache.CacheManager springCacheManager) {
		return new EhCacheCacheManager(springCacheManager);
	}

	@Bean(name = "serviceCache")
	public Cache serviceCache(EhCacheCacheManager cacheManager) {
		return cacheManager.getCache("com.shopizer.OBJECT_CACHE");
	}
}
