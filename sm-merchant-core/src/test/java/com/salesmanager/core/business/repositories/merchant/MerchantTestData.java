package com.salesmanager.core.business.repositories.merchant;

import java.util.Collections;

import javax.persistence.EntityManager;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.core.model.reference.language.Language;

public final class MerchantTestData {

	private MerchantTestData() {
	}

	public static MerchantStore persistStore(EntityManager em, String code) {
		return persistStore(em, code, null);
	}

	public static MerchantStore persistStore(EntityManager em, String code, MerchantStore parent) {
		Language language = findOrCreateLanguage(em);
		Country country = findOrCreateCountry(em);
		Currency currency = findOrCreateCurrency(em);

		MerchantStore store = new MerchantStore();
		store.setCode(code);
		store.setStorename(code + " Store");
		store.setStorephone("555-0100");
		store.setStorecity("Montreal");
		store.setStorepostalcode("H2X1Y4");
		store.setStoreEmailAddress("admin@" + code.toLowerCase() + ".test");
		store.setCountry(country);
		store.setCurrency(currency);
		store.setDefaultLanguage(language);
		store.setLanguages(Collections.singletonList(language));
		if (parent != null) {
			store.setParent(parent);
		}
		em.persist(store);
		em.flush();
		return store;
	}

	private static Language findOrCreateLanguage(EntityManager em) {
		return em.createQuery("select l from Language l where l.code = :code", Language.class)
				.setParameter("code", "en")
				.getResultStream()
				.findFirst()
				.orElseGet(() -> {
					Language language = new Language("en");
					em.persist(language);
					return language;
				});
	}

	private static Country findOrCreateCountry(EntityManager em) {
		return em.createQuery("select c from Country c where c.isoCode = :iso", Country.class)
				.setParameter("iso", "CA")
				.getResultStream()
				.findFirst()
				.orElseGet(() -> {
					Country country = new Country("CA");
					country.setSupported(true);
					em.persist(country);
					return country;
				});
	}

	private static Currency findOrCreateCurrency(EntityManager em) {
		return em.createQuery("select c from Currency c where c.name = :name", Currency.class)
				.setParameter("name", "Canadian Dollar")
				.getResultStream()
				.findFirst()
				.orElseGet(() -> {
					Currency currency = new Currency();
					currency.setCurrency(java.util.Currency.getInstance("CAD"));
					currency.setName("Canadian Dollar");
					currency.setSupported(true);
					em.persist(currency);
					return currency;
				});
	}
}
