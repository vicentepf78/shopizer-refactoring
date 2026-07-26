package com.salesmanager.shop.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.contracts.tenant.LanguageCode;
import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.business.exception.ConversionException;
import com.salesmanager.core.business.services.merchant.MerchantStoreService;
import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;

@ExtendWith(MockitoExtension.class)
class TenantEntityBridgeImplTest {

	@Mock
	private MerchantStoreService merchantStoreService;

	@Mock
	private LanguageService languageService;

	@InjectMocks
	private TenantEntityBridgeImpl bridge;

	@Test
	void resolveStoreReturnsStoreForValidCode() throws Exception {
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		when(merchantStoreService.getByCode("DEFAULT")).thenReturn(store);

		assertSame(store, bridge.resolveStore(MerchantStoreId.of("DEFAULT")));
	}

	@Test
	void resolveStoreThrowsConversionExceptionForUnknownCode() throws Exception {
		when(merchantStoreService.getByCode("MISSING")).thenReturn(null);

		ConversionException ex = assertThrows(ConversionException.class,
				() -> bridge.resolveStore(MerchantStoreId.of("MISSING")));
		assertEquals("Unknown store: MISSING", ex.getMessage());
	}

	@Test
	void resolveLanguageReturnsLanguageForValidCode() throws Exception {
		Language language = new Language();
		language.setCode("en");
		when(languageService.getByCode("en")).thenReturn(language);

		assertSame(language, bridge.resolveLanguage(LanguageCode.of("en")));
	}

	@Test
	void resolveLanguageThrowsConversionExceptionForUnknownCode() throws Exception {
		when(languageService.getByCode("xx")).thenReturn(null);

		ConversionException ex = assertThrows(ConversionException.class,
				() -> bridge.resolveLanguage(LanguageCode.of("xx")));
		assertEquals("Unknown language: xx", ex.getMessage());
	}

}
