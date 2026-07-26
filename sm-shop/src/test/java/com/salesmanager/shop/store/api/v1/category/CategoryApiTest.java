package com.salesmanager.shop.store.api.v1.category;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.shop.model.catalog.category.ReadableCategory;
import com.salesmanager.shop.store.controller.category.facade.CategoryFacade;
import com.salesmanager.shop.store.controller.user.facade.UserFacade;

@ExtendWith(MockitoExtension.class)
class CategoryApiTest {

	@Mock
	private CategoryFacade categoryFacade;

	@Mock
	private UserFacade userFacade;

	@InjectMocks
	private CategoryApi categoryApi;

	@Test
	void getById_withNullLanguage_passesNullLanguageCodeForAllLanguages() throws Exception {
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");

		ReadableCategory category = new ReadableCategory();
		when(categoryFacade.getById(MerchantStoreId.of("DEFAULT"), 1L, null)).thenReturn(category);

		categoryApi.get(1L, store, null);

		verify(categoryFacade).getById(eq(MerchantStoreId.of("DEFAULT")), eq(1L), isNull());
	}

}
