package com.salesmanager.merchant.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableCaching
@EntityScan(basePackages = "com.salesmanager.core.model")
@EnableJpaRepositories(basePackages = {
		"com.salesmanager.core.business.repositories.merchant",
		"com.salesmanager.core.business.repositories.system",
		"com.salesmanager.core.business.repositories.reference.language",
		"com.salesmanager.merchant.security"
})
@ComponentScan(basePackages = {
		"com.salesmanager.core.business.services.merchant",
		"com.salesmanager.core.business.services.system",
		"com.salesmanager.core.business.services.common",
		"com.salesmanager.core.business.utils",
		"com.salesmanager.core.business.services.reference.language"
})
public class MerchantCoreConfig {
}
