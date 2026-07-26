package com.salesmanager.core.business.repositories.merchant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import com.salesmanager.core.model.common.CriteriaOrderBy;
import com.salesmanager.core.model.common.GenericEntityList;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.merchant.MerchantStoreCriteria;

@DataJpaTest
@ContextConfiguration(classes = MerchantRepositoryImplTest.Config.class)
class MerchantRepositoryImplTest {

	@EnableJpaRepositories(basePackageClasses = MerchantRepository.class)
	@EntityScan(basePackages = "com.salesmanager.core.model")
	static class Config {
	}

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void listByCriteria_filtersByCode() throws Exception {
		MerchantTestData.persistStore(entityManager, "FINDME");
		MerchantTestData.persistStore(entityManager, "OTHER");

		MerchantStoreCriteria criteria = new MerchantStoreCriteria();
		criteria.setCode("find");
		criteria.setPageSize(10);
		criteria.setStartPage(0);

		GenericEntityList<MerchantStore> result = merchantRepository.listByCriteria(criteria);

		assertEquals(1, result.getTotalCount());
		assertEquals("FINDME", result.getList().get(0).getCode());
	}

	@Test
	void listByCriteria_filtersByCodeAndName() throws Exception {
		MerchantTestData.persistStore(entityManager, "BOTH");

		MerchantStoreCriteria criteria = new MerchantStoreCriteria();
		criteria.setCode("both");
		criteria.setName("both");
		criteria.setPageSize(10);
		criteria.setStartPage(0);

		GenericEntityList<MerchantStore> result = merchantRepository.listByCriteria(criteria);

		assertNotNull(result);
	}

	@Test
	void listByCriteria_ordersByRequestedField() throws Exception {
		MerchantTestData.persistStore(entityManager, "ORDA");
		MerchantTestData.persistStore(entityManager, "ORDB");

		MerchantStoreCriteria criteria = new MerchantStoreCriteria();
		criteria.setCriteriaOrderByField("code");
		criteria.setOrderBy(CriteriaOrderBy.ASC);
		criteria.setPageSize(10);
		criteria.setStartPage(0);

		GenericEntityList<MerchantStore> result = merchantRepository.listByCriteria(criteria);

		assertEquals(2, result.getTotalCount());
	}
}
