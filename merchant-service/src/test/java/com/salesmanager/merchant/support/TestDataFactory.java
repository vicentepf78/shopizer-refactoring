package com.salesmanager.merchant.support;

import java.util.Collections;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.user.Group;
import com.salesmanager.core.model.user.GroupType;
import com.salesmanager.core.model.user.User;
import com.salesmanager.merchant.security.AdminUserRepository;

@Component
public class TestDataFactory {

	@PersistenceContext
	private EntityManager entityManager;

	private final AdminUserRepository adminUserRepository;

	public TestDataFactory(AdminUserRepository adminUserRepository) {
		this.adminUserRepository = adminUserRepository;
	}

	@Transactional
	public void ensureDefaultAdmin() {
		if (adminUserRepository.findByUserName("admin") != null) {
			return;
		}
		seedDefaultAdmin("admin", "DEFAULT");
	}

	@Transactional
	public void seedDefaultAdmin(String username, String storeCode) {
		Language language = new Language("en");
		entityManager.persist(language);

		Country country = new Country("CA");
		country.setSupported(true);
		entityManager.persist(country);

		Currency currency = new Currency();
		currency.setCurrency(java.util.Currency.getInstance("CAD"));
		currency.setName("Canadian Dollar");
		currency.setSupported(true);
		entityManager.persist(currency);

		MerchantStore store = new MerchantStore();
		store.setCode(storeCode);
		store.setStorename(storeCode + " Store");
		store.setStorephone("555-0100");
		store.setStorecity("Montreal");
		store.setStorepostalcode("H2X1Y4");
		store.setStoreEmailAddress("admin@" + storeCode.toLowerCase() + ".test");
		store.setCountry(country);
		store.setCurrency(currency);
		store.setDefaultLanguage(language);
		store.setLanguages(Collections.singletonList(language));
		entityManager.persist(store);

		Group adminGroup = new Group("ADMIN");
		adminGroup.setGroupType(GroupType.ADMIN);
		entityManager.persist(adminGroup);

		User user = new User(username, "{noop}password", username + "@test.local");
		user.setFirstName("Test");
		user.setLastName("Admin");
		user.setMerchantStore(store);
		user.setDefaultLanguage(language);
		user.getGroups().add(adminGroup);
		entityManager.persist(user);

		entityManager.flush();
	}
}
