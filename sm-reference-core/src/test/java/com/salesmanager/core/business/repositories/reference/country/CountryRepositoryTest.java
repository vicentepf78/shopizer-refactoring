package com.salesmanager.core.business.repositories.reference.country;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import com.salesmanager.core.model.reference.country.Country;

@DataJpaTest
@ContextConfiguration(classes = CountryRepositoryTest.Config.class)
class CountryRepositoryTest {

	@EnableJpaRepositories(basePackageClasses = CountryRepository.class)
	@EntityScan(basePackages = "com.salesmanager.core.model")
	static class Config {
	}

	@Autowired
	private CountryRepository countryRepository;

	@Test
	void findByIsoCode_returnsPersistedCountry() {
		Country country = new Country();
		country.setIsoCode("BR");
		country.setSupported(true);
		countryRepository.saveAndFlush(country);

		Country found = countryRepository.findByIsoCode("BR");

		assertNotNull(found);
		assertEquals("BR", found.getIsoCode());
	}
}
