package com.salesmanager.content.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableCaching
@EntityScan(basePackages = "com.salesmanager.core.model")
@EnableJpaRepositories(basePackages = {
		"com.salesmanager.core.business.repositories.content",
		"com.salesmanager.core.business.repositories.merchant",
		"com.salesmanager.core.business.repositories.reference.language",
		"com.salesmanager.content.security"
})
@ComponentScan(basePackages = {
		"com.salesmanager.core.business.services.content",
		"com.salesmanager.core.business.services.common",
		"com.salesmanager.core.business.utils",
		"com.salesmanager.core.business.services.reference.language",
		"com.salesmanager.core.business.modules.cms.content.gcp",
		"com.salesmanager.core.business.modules.cms.impl"
})
public class ContentCoreConfig {
}
