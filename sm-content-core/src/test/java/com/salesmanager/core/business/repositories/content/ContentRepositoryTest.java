package com.salesmanager.core.business.repositories.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import com.salesmanager.core.model.content.Content;
import com.salesmanager.core.model.content.ContentType;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.core.model.reference.language.Language;

@DataJpaTest
@ContextConfiguration(classes = ContentRepositoryTest.Config.class)
class ContentRepositoryTest {

	@EnableJpaRepositories(basePackageClasses = { ContentRepository.class, PageContentRepository.class })
	@EntityScan(basePackages = "com.salesmanager.core.model")
	static class Config {
	}

	@Autowired
	private ContentRepository contentRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void findByCode_returnsPersistedContent() {
		Country country = new Country();
		country.setIsoCode("CA");
		country.setSupported(true);
		entityManager.persist(country);

		Currency currency = new Currency();
		currency.setCurrency(java.util.Currency.getInstance("CAD"));
		currency.setSupported(true);
		entityManager.persist(currency);

		Language language = new Language("en");
		language.setCode("en");
		entityManager.persist(language);

		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		store.setStorename("Default");
		store.setStoreEmailAddress("store@test.com");
		store.setCountry(country);
		store.setCurrency(currency);
		store.setDefaultLanguage(language);
		entityManager.persist(store);
		entityManager.flush();

		Content content = new Content();
		content.setCode("home-page");
		content.setContentType(ContentType.PAGE);
		content.setMerchantStore(store);
		content.setVisible(true);
		contentRepository.saveAndFlush(content);

		Content found = contentRepository.findByCode("home-page", store.getId());

		assertNotNull(found);
		assertEquals("home-page", found.getCode());
		assertEquals(ContentType.PAGE, found.getContentType());
	}
}
