package com.salesmanager.core.business.repositories.merchant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import com.salesmanager.core.model.merchant.MerchantStore;

@DataJpaTest
@ContextConfiguration(classes = MerchantRepositoryTest.Config.class)
class MerchantRepositoryTest {

	@EnableJpaRepositories(basePackageClasses = { MerchantRepository.class, PageableMerchantRepository.class })
	@EntityScan(basePackages = "com.salesmanager.core.model")
	static class Config {
	}

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void findByCode_returnsPersistedStore() {
		MerchantTestData.persistStore(entityManager, "ALPHA");

		MerchantStore found = merchantRepository.findByCode("ALPHA");

		assertNotNull(found);
		assertEquals("ALPHA", found.getCode());
	}

	@Test
	void existsByCode_reflectsPersistence() {
		MerchantTestData.persistStore(entityManager, "BETA");

		assertTrue(merchantRepository.existsByCode("BETA"));
		assertFalse(merchantRepository.existsByCode("MISSING"));
	}

	@Test
	void listByGroup_includesParentAndChildViaParentId() {
		MerchantStore parent = MerchantTestData.persistStore(entityManager, "PARENT");
		MerchantTestData.persistStore(entityManager, "CHILD", parent);

		assertEquals(2, merchantRepository.listByGroup("PARENT", parent.getId()).size());
	}
}
