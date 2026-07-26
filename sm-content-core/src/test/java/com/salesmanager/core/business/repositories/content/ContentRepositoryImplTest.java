package com.salesmanager.core.business.repositories.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import com.salesmanager.core.model.content.Content;
import com.salesmanager.core.model.content.ContentDescription;
import com.salesmanager.core.model.content.ContentType;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.core.model.reference.language.Language;

@DataJpaTest
@ContextConfiguration(classes = ContentRepositoryImplTest.Config.class)
class ContentRepositoryImplTest {

	@EnableJpaRepositories(basePackageClasses = ContentRepository.class)
	@EntityScan(basePackages = "com.salesmanager.core.model")
	static class Config {
	}

	@Autowired
	private ContentRepository contentRepository;

	@Autowired
	private EntityManager entityManager;

	private MerchantStore store;
	private Language language;

	@BeforeEach
	void seedStore() {
		Country country = new Country();
		country.setIsoCode("CA");
		country.setSupported(true);
		entityManager.persist(country);

		Currency currency = new Currency();
		currency.setCurrency(java.util.Currency.getInstance("CAD"));
		currency.setSupported(true);
		entityManager.persist(currency);

		language = new Language("en");
		language.setCode("en");
		entityManager.persist(language);

		store = new MerchantStore();
		store.setCode("DEFAULT");
		store.setStorename("Default");
		store.setStoreEmailAddress("store@test.com");
		store.setCountry(country);
		store.setCurrency(currency);
		store.setDefaultLanguage(language);
		entityManager.persist(store);
		entityManager.flush();
	}

	@Test
	void listNameByType_returnsVisibleContentDescriptions() throws Exception {
		Content content = visibleContent("footer-box", ContentType.BOX, "Footer", "/footer");
		contentRepository.saveAndFlush(content);

		List<ContentDescription> names = contentRepository.listNameByType(
				Collections.singletonList(ContentType.BOX), store, language);

		assertEquals(1, names.size());
		assertEquals("Footer", names.get(0).getName());
		assertEquals("/footer", names.get(0).getSeUrl());
	}

	@Test
	void getBySeUrl_returnsMatchingDescription() {
		contentRepository.saveAndFlush(visibleContent("about-page", ContentType.PAGE, "About", "/about-us"));

		ContentDescription description = contentRepository.getBySeUrl(store, "/about-us");

		assertNotNull(description);
		assertEquals("About", description.getName());
	}

	@Test
	void getBySeUrl_whenMissing_throwsEmptyResult() {
		assertThrows(Exception.class, () -> contentRepository.getBySeUrl(store, "/missing"));
	}

	private Content visibleContent(String code, ContentType type, String name, String seUrl) {
		Content content = new Content();
		content.setCode(code);
		content.setContentType(type);
		content.setMerchantStore(store);
		content.setVisible(true);

		ContentDescription description = new ContentDescription();
		description.setLanguage(language);
		description.setName(name);
		description.setSeUrl(seUrl);
		description.setContent(content);
		content.getDescriptions().add(description);
		return content;
	}
}
