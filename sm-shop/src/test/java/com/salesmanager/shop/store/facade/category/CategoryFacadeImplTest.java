package com.salesmanager.shop.store.facade.category;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.business.services.catalog.category.CategoryService;
import com.salesmanager.core.business.services.merchant.MerchantStoreService;
import com.salesmanager.core.model.catalog.category.Category;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.shop.mapper.catalog.ReadableCategoryMapper;
import com.salesmanager.shop.model.catalog.category.ReadableCategory;
import com.salesmanager.shop.model.catalog.category.ReadableCategoryList;
import com.salesmanager.shop.tenant.TenantEntityBridge;

@ExtendWith(MockitoExtension.class)
class CategoryFacadeImplTest {

	@Mock
	private CategoryService categoryService;

	@Mock
	private MerchantStoreService merchantStoreService;

	@Mock
	private ReadableCategoryMapper readableCategoryMapper;

	@Mock
	private TenantEntityBridge tenantEntityBridge;

	@InjectMocks
	private CategoryFacadeImpl categoryFacade;

	@Test
	void getCategoryHierarchy_withNullLanguageCode_usesDepthQueryWithoutLanguage() throws Exception {
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		store.setId(1);
		MerchantStore parent = new MerchantStore();
		parent.setCode("DEFAULT");
		parent.setId(1);
		Category category = new Category();
		category.setId(1L);
		category.setDepth(0);
		ReadableCategory readableCategory = new ReadableCategory();
		readableCategory.setId(1L);
		readableCategory.setDepth(0);

		when(tenantEntityBridge.resolveStore(MerchantStoreId.of("DEFAULT"))).thenReturn(store);
		when(merchantStoreService.getParent("DEFAULT")).thenReturn(parent);
		when(categoryService.getListByDepth(parent, 0)).thenReturn(List.of(category));
		when(readableCategoryMapper.convert(eq(category), eq(store), isNull())).thenReturn(readableCategory);

		ReadableCategoryList result = categoryFacade.getCategoryHierarchy(MerchantStoreId.of("DEFAULT"), null, 0, null,
				null, 0, 10);

		assertNotNull(result);
		verify(categoryService).getListByDepth(parent, 0);
		verify(readableCategoryMapper).convert(eq(category), eq(store), isNull());
	}

	@Test
	void getCategoryByFriendlyUrl_withNullLanguageCode_usesListBySeUrlAndAllLangMapper() throws Exception {
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		Category category = new Category();
		category.setId(2L);
		ReadableCategory readableCategory = new ReadableCategory();
		readableCategory.setId(2L);

		when(tenantEntityBridge.resolveStore(MerchantStoreId.of("DEFAULT"))).thenReturn(store);
		when(categoryService.listBySeUrl(store, "shoes")).thenReturn(Collections.singletonList(category));
		when(readableCategoryMapper.convert(eq(category), eq(store), isNull())).thenReturn(readableCategory);

		ReadableCategory result = categoryFacade.getCategoryByFriendlyUrl(MerchantStoreId.of("DEFAULT"), "shoes", null);

		assertNotNull(result);
		verify(categoryService).listBySeUrl(store, "shoes");
		verify(readableCategoryMapper).convert(eq(category), eq(store), isNull());
	}
}
