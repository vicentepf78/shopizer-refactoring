package com.salesmanager.core.business.repositories.tax;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ContextConfiguration(classes = TaxRateRepositoryTest.Config.class)
class TaxRateRepositoryTest {

	@EnableJpaRepositories(basePackageClasses = TaxRateRepository.class)
	@EntityScan(basePackages = "com.salesmanager.core.model")
	static class Config {
	}

	@Autowired
	private TaxRateRepository taxRateRepository;

	@Test
	void repositoryIsWired() {
		assertNotNull(taxRateRepository);
	}
}
