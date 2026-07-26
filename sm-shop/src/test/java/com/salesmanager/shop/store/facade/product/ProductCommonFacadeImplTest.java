package com.salesmanager.shop.store.facade.product;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.business.services.catalog.pricing.PricingService;
import com.salesmanager.core.business.services.catalog.product.ProductService;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.catalog.product.ReadableProduct;
import com.salesmanager.shop.tenant.TenantEntityBridge;
import com.salesmanager.shop.utils.ImageFilePath;

@ExtendWith(MockitoExtension.class)
class ProductCommonFacadeImplTest {

	@Mock
	private ProductService productService;

	@Mock
	private PricingService pricingService;

	@Mock
	private ImageFilePath imageUtils;

	@Mock
	private TenantEntityBridge tenantEntityBridge;

	@InjectMocks
	private ProductCommonFacadeImpl productCommonFacade;

	@Test
	void getProduct_withNullLanguageCode_doesNotCallBridgeResolveLanguage() throws Exception {
		MerchantStore store = new MerchantStore();
		store.setId(1);
		store.setCode("DEFAULT");
		store.setDefaultLanguage(new Language("en"));

		Product product = new Product();
		product.setId(1L);
		product.setMerchantStore(store);

		when(tenantEntityBridge.resolveStore(MerchantStoreId.of("DEFAULT"))).thenReturn(store);
		when(productService.findOne(1L, store)).thenReturn(product);

		ReadableProduct result = productCommonFacade.getProduct(MerchantStoreId.of("DEFAULT"), 1L, null);

		assertNotNull(result);
		verify(tenantEntityBridge, never()).resolveLanguage(any());
	}
}
