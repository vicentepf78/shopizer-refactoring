package com.salesmanager.core.business.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.salesmanager.contracts.tenant.LanguageCode;
import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.business.exception.ConversionException;
import com.salesmanager.core.business.tenant.TenantEntityBridgeRegistry;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;

class AbstractDataPopulatorTenantOverloadTest {

	@AfterEach
	void tearDown() {
		TenantEntityBridgeRegistry.clear();
	}

	@Test
	void tenantPrimitiveOverloadDelegatesToEntityPopulate() throws ConversionException {
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		Language language = new Language("en");

		TenantEntityBridgeRegistry.register(new TenantEntityBridgeRegistry.Bridge() {
			@Override
			public MerchantStore resolveStore(MerchantStoreId storeId) {
				assertEquals("DEFAULT", storeId.getCode());
				return store;
			}

			@Override
			public Language resolveLanguage(LanguageCode languageCode) {
				assertEquals("en", languageCode.getCode());
				return language;
			}
		});

		TestPopulator populator = new TestPopulator();
		String result = populator.populate("input", MerchantStoreId.of("DEFAULT"), LanguageCode.of("en"));

		assertEquals("input:DEFAULT:en", result);
	}

	private static final class TestPopulator extends AbstractDataPopulator<String, String> {

		@Override
		public String populate(String source, String target, MerchantStore store, Language language)
				throws ConversionException {
			return source + ":" + store.getCode() + ":" + language.getCode();
		}

		@Override
		protected String createTarget() {
			return "";
		}

	}

}
