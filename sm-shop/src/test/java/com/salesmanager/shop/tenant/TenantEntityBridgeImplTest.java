package com.salesmanager.shop.tenant;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.business.services.merchant.MerchantStoreService;
import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.model.merchant.MerchantStore;

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

}
