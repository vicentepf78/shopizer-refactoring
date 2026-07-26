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
import com.salesmanager.core.model.system.MerchantConfiguration;
import com.salesmanager.core.model.system.MerchantConfigurationType;
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
	public Seed ensureDefaultAdmin() {
		User existing = adminUserRepository.findByUserName("admin");
		if (existing != null) {
			ensureGroup(existing, "ADMIN_RETAILER");
			MerchantStore store = existing.getMerchantStore();
			Language language = existing.getDefaultLanguage();
			Country country = store.getCountry();
			Currency currency = store.getCurrency();
			country.getId();
			currency.getId();
			language.getId();
			entityManager.flush();
			return new Seed(store, existing, language, country, currency);
		}
		return seedDefaultAdmin("admin", "DEFAULT");
	}

	private void ensureGroup(User user, String groupName) {
		boolean hasGroup = user.getGroups().stream()
				.anyMatch(g -> groupName.equals(g.getGroupName()));
		if (hasGroup) {
			return;
		}
		Group group = entityManager
				.createQuery("select g from Group g where g.groupName = :name", Group.class)
				.setParameter("name", groupName)
				.getResultStream()
				.findFirst()
				.orElse(null);
		if (group == null) {
			group = new Group(groupName);
			group.setGroupType(GroupType.ADMIN);
			entityManager.persist(group);
		}
		user.getGroups().add(group);
		entityManager.merge(user);
	}

	@Transactional
	public Seed seedDefaultAdmin(String username, String storeCode) {
		Language language = new Language("en");
		entityManager.persist(language);

		Country country = new Country("CA");
		country.setSupported(true);
		entityManager.persist(country);

		Currency cad = new Currency();
		cad.setCurrency(java.util.Currency.getInstance("CAD"));
		cad.setName("Canadian Dollar");
		cad.setSupported(true);
		entityManager.persist(cad);

		Currency usd = new Currency();
		usd.setCurrency(java.util.Currency.getInstance("USD"));
		usd.setName("US Dollar");
		usd.setSupported(true);
		entityManager.persist(usd);

		MerchantStore store = new MerchantStore();
		store.setCode(storeCode);
		store.setStorename(storeCode + " Store");
		store.setStorephone("555-0100");
		store.setStorecity("Montreal");
		store.setStorepostalcode("H2X1Y4");
		store.setStoreEmailAddress("admin@" + storeCode.toLowerCase() + ".test");
		store.setCountry(country);
		store.setCurrency(cad);
		store.setDefaultLanguage(language);
		store.setLanguages(Collections.singletonList(language));
		store.setRetailer(true);
		entityManager.persist(store);

		Group adminGroup = new Group("ADMIN");
		adminGroup.setGroupType(GroupType.ADMIN);
		entityManager.persist(adminGroup);

		Group retailGroup = new Group("ADMIN_RETAILER");
		retailGroup.setGroupType(GroupType.ADMIN);
		entityManager.persist(retailGroup);

		User user = new User(username, "{noop}password", username + "@test.local");
		user.setFirstName("Test");
		user.setLastName("Admin");
		user.setMerchantStore(store);
		user.setDefaultLanguage(language);
		user.getGroups().add(adminGroup);
		user.getGroups().add(retailGroup);
		entityManager.persist(user);

		entityManager.flush();
		return new Seed(store, user, language, country, cad);
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
	public void seedSocialConfig(MerchantStore store, String key, String value) {
		MerchantConfiguration config = new MerchantConfiguration();
		config.setKey(key);
		config.setValue(value);
		config.setActive(true);
		config.setMerchantConfigurationType(MerchantConfigurationType.SOCIAL);
		config.setMerchantStore(store);
		entityManager.persist(config);
		entityManager.flush();
	}

	@Transactional
	public Language ensureLanguage(String code) {
		Language existing = entityManager
				.createQuery("select l from Language l where l.code = :code", Language.class)
				.setParameter("code", code)
				.getResultStream()
				.findFirst()
				.orElse(null);
		if (existing != null) {
			return existing;
		}
		Language language = new Language(code);
		entityManager.persist(language);
		entityManager.flush();
		return language;
	}

	public static final class Seed {
		public final MerchantStore store;
		public final User user;
		public final Language language;
		public final Country country;
		public final Currency currency;

		public Seed(MerchantStore store, User user, Language language, Country country, Currency currency) {
			this.store = store;
			this.user = user;
			this.language = language;
			this.country = country;
			this.currency = currency;
		}
	}
}
