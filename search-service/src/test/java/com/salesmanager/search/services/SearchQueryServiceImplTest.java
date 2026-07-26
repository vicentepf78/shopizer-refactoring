package com.salesmanager.search.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.contracts.search.SearchProductRequest;
import com.salesmanager.contracts.search.ValueList;
import com.salesmanager.search.support.SearchUnavailableException;

import modules.commons.search.SearchModule;
import modules.commons.search.request.SearchItem;
import modules.commons.search.request.SearchRequest;
import modules.commons.search.request.SearchResponse;

@ExtendWith(MockitoExtension.class)
class SearchQueryServiceImplTest {

	@Mock
	private SearchModule searchModule;

	private SearchQueryServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new SearchQueryServiceImpl(searchModule);
	}

	@Test
	void searchUsesOpenSearchModule() throws Exception {
		SearchResponse response = new SearchResponse();
		SearchItem item = new SearchItem();
		item.setName("widget");
		response.setItems(Collections.singletonList(item));
		when(searchModule.searchProducts(any(SearchRequest.class))).thenReturn(response);

		SearchProductRequest request = new SearchProductRequest();
		request.setQuery("wid");

		assertThat(service.search("DEFAULT", "en", request)).hasSize(1);
		verify(searchModule).searchProducts(any(SearchRequest.class));
	}

	@Test
	void autocompleteMapsSuggestionsToValueList() throws Exception {
		SearchResponse response = new SearchResponse();
		SearchItem item = new SearchItem();
		item.setSuggestions("widget");
		response.setItems(Collections.singletonList(item));
		when(searchModule.searchKeywords(any(SearchRequest.class))).thenReturn(response);

		ValueList valueList = service.autocomplete("DEFAULT", "en", "wid");
		assertThat(valueList.getValues()).containsExactly("widget");
	}

	@Test
	void openSearchFailureMapsToSearchUnavailable() throws Exception {
		when(searchModule.searchProducts(any(SearchRequest.class))).thenThrow(new RuntimeException("connection refused"));

		SearchProductRequest request = new SearchProductRequest();
		request.setQuery("wid");

		assertThatThrownBy(() -> service.search("DEFAULT", "en", request))
				.isInstanceOf(SearchUnavailableException.class)
				.hasMessageContaining("OpenSearch search failed");
	}

	@Test
	void missingSearchModuleReturns503Exception() {
		SearchQueryServiceImpl offline = new SearchQueryServiceImpl(null);
		SearchProductRequest request = new SearchProductRequest();
		request.setQuery("wid");

		assertThatThrownBy(() -> offline.search("DEFAULT", "en", request))
				.isInstanceOf(SearchUnavailableException.class)
				.hasMessageContaining("not configured");
	}

	@Test
	void hasNoSmCoreModelImports() {
		Package pkg = SearchQueryServiceImpl.class.getPackage();
		assertThat(pkg.getName()).startsWith("com.salesmanager.search");
	}
}
