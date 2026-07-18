package com.salesmanager.core.business.repositories.tax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import com.salesmanager.core.model.tax.taxclass.TaxClass;

@DataJpaTest
@ContextConfiguration(classes = TaxClassRepositoryTest.Config.class)
class TaxClassRepositoryTest {

	@EnableJpaRepositories(basePackageClasses = TaxClassRepository.class)
	@EntityScan(basePackages = "com.salesmanager.core.model")
	static class Config {
	}

	@Autowired
	private TaxClassRepository taxClassRepository;

	@Test
	void findByCode_returnsPersistedTaxClass() {
		TaxClass taxClass = new TaxClass("DEFAULT");
		taxClass.setTitle("Default");
		taxClassRepository.saveAndFlush(taxClass);

		TaxClass found = taxClassRepository.findByCode("DEFAULT");

		assertNotNull(found);
		assertEquals("DEFAULT", found.getCode());
	}
}
