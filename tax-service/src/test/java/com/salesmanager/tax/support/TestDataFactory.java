package com.salesmanager.tax.support;

import java.util.Collections;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.reference.zone.Zone;
import com.salesmanager.core.model.user.Group;
import com.salesmanager.core.model.user.GroupType;
import com.salesmanager.core.model.user.User;
import com.salesmanager.tax.security.AdminUserRepository;

@Component
public class TestDataFactory {

	@PersistenceContext
	private EntityManager entityManager;

	private final AdminUserRepository adminUserRepository;

	public TestDataFactory(AdminUserRepository adminUserRepository) {
		this.adminUserRepository = adminUserRepository;
	}

	@Transactional
	public Seed ensureDefaultAdmin() {
		User existing = adminUserRepository.findByUserName("admin");
		if (existing != null) {
			MerchantStore store = existing.getMerchantStore();
			Language language = existing.getDefaultLanguage();
			Country country = store.getCountry();
			Currency currency = store.getCurrency();
			// force-init lazy associations while session is open
			country.getId();
			currency.getId();
			language.getId();
			return new Seed(store, existing, language, country, currency);
		}
		return seedDefaultAdmin("admin", "DEFAULT");
	}

	@Transactional
	public Seed seedDefaultAdmin(String username, String storeCode) {
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
		return new Seed(store, user, language, country, currency);
	}

	@Transactional
	public MerchantStore seedOtherStore(String storeCode, Seed seed) {
		MerchantStore existing = entityManager
				.createQuery("select m from MerchantStore m where m.code = :code", MerchantStore.class)
				.setParameter("code", storeCode)
				.getResultStream()
				.findFirst()
				.orElse(null);
		if (existing != null) {
			return existing;
		}

		MerchantStore store = new MerchantStore();
		store.setCode(storeCode);
		store.setStorename(storeCode + " Store");
		store.setStorephone("555-0200");
		store.setStorecity("Toronto");
		store.setStorepostalcode("M5V1A1");
		store.setStoreEmailAddress("admin@" + storeCode.toLowerCase() + ".test");
		store.setCountry(seed.country);
		store.setCurrency(seed.currency);
		store.setDefaultLanguage(seed.language);
		store.setLanguages(Collections.singletonList(seed.language));
		entityManager.persist(store);
		entityManager.flush();
		return store;
	}

	@Transactional
	public Zone seedZone(String code, Country country) {
		Zone existing = entityManager
				.createQuery("select z from Zone z where z.code = :code", Zone.class)
				.setParameter("code", code)
				.getResultStream()
				.findFirst()
				.orElse(null);
		if (existing != null) {
			return existing;
		}
		Zone zone = new Zone();
		zone.setCode(code);
		zone.setCountry(country);
		entityManager.persist(zone);
		entityManager.flush();
		return zone;
	}

	public static final class Seed {
		public final MerchantStore store;
		public final User user;
		public final Language language;
		public final Country country;
		public final Currency currency;

		public Seed(MerchantStore store, User user, Language language, Country country) {
			this(store, user, language, country, store.getCurrency());
		}

		public Seed(MerchantStore store, User user, Language language, Country country, Currency currency) {
			this.store = store;
			this.user = user;
			this.language = language;
			this.country = country;
			this.currency = currency;
		}
	}
}
