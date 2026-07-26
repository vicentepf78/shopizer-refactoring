package com.salesmanager.shop.store.api.v1.search;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.contracts.search.SearchProductRequest;
import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.shop.model.entity.ValueList;
import com.salesmanager.shop.store.controller.search.facade.SearchFacade;

@ExtendWith(MockitoExtension.class)
class SearchApiTest {

	@Mock
	private SearchFacade searchFacade;

	@InjectMocks
	private SearchApi searchApi;

	@Test
	void search_withNullLanguage_passesNullLanguageCodeForAllLanguages() {
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");

		SearchProductRequest searchRequest = new SearchProductRequest();
		searchRequest.setQuery("phone");

		when(searchFacade.search(MerchantStoreId.of("DEFAULT"), null, searchRequest))
				.thenReturn(Collections.emptyList());

		searchApi.search(searchRequest, store, null);

		verify(searchFacade).search(eq(MerchantStoreId.of("DEFAULT")), isNull(), eq(searchRequest));
	}

	@Test
	void autocomplete_withNullLanguage_passesNullLanguageCodeForAllLanguages() {
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");

		SearchProductRequest searchRequest = new SearchProductRequest();
		searchRequest.setQuery("phone");

		ValueList valueList = new ValueList();
		when(searchFacade.autocompleteRequest("phone", MerchantStoreId.of("DEFAULT"), null)).thenReturn(valueList);

		searchApi.autocomplete(searchRequest, store, null);

		verify(searchFacade).autocompleteRequest(eq("phone"), eq(MerchantStoreId.of("DEFAULT")), isNull());
	}

}
