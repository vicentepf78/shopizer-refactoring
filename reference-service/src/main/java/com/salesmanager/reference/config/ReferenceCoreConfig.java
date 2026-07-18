package com.salesmanager.reference.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableCaching
@EntityScan(basePackages = "com.salesmanager.core.model")
@EnableJpaRepositories(basePackages = "com.salesmanager.core.business.repositories.reference")
@ComponentScan(basePackages = {
		"com.salesmanager.core.business.services.reference",
		"com.salesmanager.core.business.services.common",
		"com.salesmanager.core.business.utils"
})
public class ReferenceCoreConfig {
}
