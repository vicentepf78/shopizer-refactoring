package com.salesmanager.core.business.repositories.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import com.salesmanager.core.business.repositories.merchant.MerchantTestData;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.system.MerchantConfiguration;
import com.salesmanager.core.model.system.MerchantConfigurationType;

@DataJpaTest
@ContextConfiguration(classes = MerchantConfigurationRepositoryTest.Config.class)
class MerchantConfigurationRepositoryTest {

	@EnableJpaRepositories(basePackageClasses = MerchantConfigurationRepository.class)
	@EntityScan(basePackages = "com.salesmanager.core.model")
	static class Config {
	}

	@Autowired
	private MerchantConfigurationRepository merchantConfigurationRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void findByMerchantStoreAndKey_returnsPersistedConfig() {
		MerchantStore store = MerchantTestData.persistStore(entityManager, "CFG1");
		MerchantConfiguration config = new MerchantConfiguration();
		config.setMerchantStore(store);
		config.setKey("SHIPPING");
		config.setValue("flat");
		config.setMerchantConfigurationType(MerchantConfigurationType.INTEGRATION);
		merchantConfigurationRepository.saveAndFlush(config);

		MerchantConfiguration found = merchantConfigurationRepository.findByMerchantStoreAndKey(store.getId(), "SHIPPING");

		assertNotNull(found);
		assertEquals("flat", found.getValue());
	}
}
