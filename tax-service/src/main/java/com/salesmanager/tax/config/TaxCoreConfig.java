package com.salesmanager.tax.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableCaching
@EntityScan(basePackages = "com.salesmanager.core.model")
@EnableJpaRepositories(basePackages = {
		"com.salesmanager.core.business.repositories.tax",
		"com.salesmanager.core.business.repositories.reference.language",
		"com.salesmanager.tax.security"
})
@ComponentScan(basePackages = {
		"com.salesmanager.core.business.services.tax",
		"com.salesmanager.core.business.services.common",
		"com.salesmanager.core.business.utils",
		"com.salesmanager.core.business.services.reference.language"
})
public class TaxCoreConfig {
}
