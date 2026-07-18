package com.salesmanager.contracts.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.salesmanager.contracts.common.Entity;
import com.salesmanager.contracts.common.ReadableEntityList;
import com.salesmanager.contracts.reference.ReadableCountry;
import com.salesmanager.contracts.reference.ReadableLanguage;
import com.salesmanager.contracts.reference.ReadableZone;
import com.salesmanager.contracts.tax.PersistableTaxClass;
import com.salesmanager.contracts.tax.PersistableTaxRate;
import com.salesmanager.contracts.tax.ReadableTaxClass;
import com.salesmanager.contracts.tax.ReadableTaxRate;
import com.salesmanager.contracts.tax.TaxRateDescription;

class ServiceClientContractTest {

	@Test
	void referenceServiceClientAcceptsOnlyStringCodes() {
		for (Method method : ReferenceServiceClient.class.getDeclaredMethods()) {
			for (Parameter parameter : method.getParameters()) {
				assertEquals(String.class, parameter.getType(),
						method.getName() + " must use String codes, not JPA entities");
				assertFalse(parameter.getType().getName().contains("MerchantStore"));
				assertFalse(parameter.getType().getName().contains("core.model"));
			}
			assertFalse(method.getReturnType().getName().startsWith("com.salesmanager.core.model"));
		}

		ReferenceServiceClient client = new ReferenceServiceClient() {
			@Override
			public ReadableCountry getCountryByCode(String isoCode, String langCode) {
				ReadableCountry country = new ReadableCountry();
				country.setCode(isoCode);
				country.setName(langCode);
				return country;
			}

			@Override
			public ReadableZone getZoneByCode(String countryCode, String zoneCode, String langCode) {
				ReadableZone zone = new ReadableZone();
				zone.setCountryCode(countryCode);
				zone.setCode(zoneCode);
				zone.setName(langCode);
				return zone;
			}

			@Override
			public ReadableLanguage getLanguageByCode(String code) {
				ReadableLanguage language = new ReadableLanguage();
				language.setCode(code);
				return language;
			}
		};

		assertEquals("CA", client.getCountryByCode("CA", "en").getCode());
		assertEquals("QC", client.getZoneByCode("CA", "QC", "en").getCode());
		assertEquals("en", client.getLanguageByCode("en").getCode());
	}

	@Test
	void taxServiceClientUsesStoreAndLangStringCodes() {
		for (Method method : TaxServiceClient.class.getDeclaredMethods()) {
			for (Class<?> type : method.getParameterTypes()) {
				assertFalse(type.getName().contains("MerchantStore"), method.getName());
				assertFalse(type.getName().contains("core.model"), method.getName());
				assertFalse(type.getName().equals("com.salesmanager.core.model.reference.language.Language"),
						method.getName());
			}
			String returnName = method.getReturnType().getName();
			assertFalse(returnName.startsWith("com.salesmanager.core.model"), method.getName());
		}

		TaxServiceClient client = new TaxServiceClient() {
			@Override
			public Entity createTaxClass(PersistableTaxClass taxClass, String storeCode, String langCode) {
				return new Entity(1L);
			}

			@Override
			public void updateTaxClass(Long id, PersistableTaxClass taxClass, String storeCode, String langCode) {
			}

			@Override
			public void deleteTaxClass(Long id, String storeCode, String langCode) {
			}

			@Override
			public boolean existsTaxClass(String code, String storeCode, String langCode) {
				return true;
			}

			@Override
			public ReadableEntityList<ReadableTaxClass> taxClasses(String storeCode, String langCode) {
				ReadableEntityList<ReadableTaxClass> list = new ReadableEntityList<>();
				list.setItems(Arrays.asList(new ReadableTaxClass()));
				return list;
			}

			@Override
			public ReadableTaxClass taxClass(String code, String storeCode, String langCode) {
				ReadableTaxClass taxClass = new ReadableTaxClass();
				taxClass.setCode(code);
				taxClass.setStore(storeCode);
				return taxClass;
			}

			@Override
			public Entity createTaxRate(PersistableTaxRate taxRate, String storeCode, String langCode) {
				return new Entity(2L);
			}

			@Override
			public void updateTaxRate(Long id, PersistableTaxRate taxRate, String storeCode, String langCode) {
			}

			@Override
			public void deleteTaxRate(Long id, String storeCode, String langCode) {
			}

			@Override
			public boolean existsTaxRate(String code, String storeCode, String langCode) {
				return false;
			}

			@Override
			public ReadableEntityList<ReadableTaxRate> taxRates(String storeCode, String langCode) {
				return new ReadableEntityList<>();
			}

			@Override
			public ReadableTaxRate taxRate(Long id, String storeCode, String langCode) {
				ReadableTaxRate rate = new ReadableTaxRate();
				rate.setId(id);
				rate.setStore(storeCode);
				return rate;
			}
		};

		PersistableTaxClass taxClass = new PersistableTaxClass();
		taxClass.setCode("DEFAULT");
		assertEquals(Long.valueOf(1L), client.createTaxClass(taxClass, "DEFAULT", "en").getId());
		assertTrue(client.existsTaxClass("DEFAULT", "DEFAULT", "en"));
		assertEquals("DEFAULT", client.taxClass("DEFAULT", "DEFAULT", "en").getCode());
		assertEquals(1, client.taxClasses("DEFAULT", "en").getItems().size());

		PersistableTaxRate taxRate = new PersistableTaxRate();
		taxRate.setCode("GST");
		taxRate.setRate(new BigDecimal("5.00"));
		TaxRateDescription description = new TaxRateDescription();
		description.setName("GST");
		taxRate.setDescriptions(Arrays.asList(description));
		assertEquals(Long.valueOf(2L), client.createTaxRate(taxRate, "DEFAULT", "en").getId());
		assertFalse(client.existsTaxRate("MISSING", "DEFAULT", "en"));
		assertEquals(Long.valueOf(9L), client.taxRate(9L, "DEFAULT", "en").getId());

		client.updateTaxClass(1L, taxClass, "DEFAULT", "en");
		client.deleteTaxClass(1L, "DEFAULT", "en");
		client.updateTaxRate(2L, taxRate, "DEFAULT", "en");
		client.deleteTaxRate(2L, "DEFAULT", "en");
		assertEquals(0, client.taxRates("DEFAULT", "en").getItems() == null ? 0
				: client.taxRates("DEFAULT", "en").getItems().size());
	}

}
