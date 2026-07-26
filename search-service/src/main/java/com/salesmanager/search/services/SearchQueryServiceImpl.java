package com.salesmanager.search.services;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.contracts.search.SearchItem;
import com.salesmanager.contracts.search.SearchProductRequest;
import com.salesmanager.contracts.search.ValueList;
import com.salesmanager.search.support.SearchUnavailableException;

import modules.commons.search.SearchModule;
import modules.commons.search.request.SearchRequest;
import modules.commons.search.request.SearchResponse;

@Service
public class SearchQueryServiceImpl implements SearchQueryService {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final SearchModule searchModule;

	@Autowired
	public SearchQueryServiceImpl(@Autowired(required = false) SearchModule searchModule) {
		this.searchModule = searchModule;
	}

	@Override
	public List<SearchItem> search(String store, String lang, SearchProductRequest request) {
		Validate.notNull(request, "Search request cannot be null");
		Validate.notNull(request.getQuery(), "Search keyword must not be null");
		Validate.notNull(lang, "Language cannot be null");
		Validate.notNull(store, "Store cannot be null");
		ensureAvailable();

		try {
			SearchRequest searchRequest = new SearchRequest();
			searchRequest.setLanguage(lang);
			searchRequest.setSearchString(request.getQuery());
			searchRequest.setStore(store.toLowerCase());
			SearchResponse response = searchModule.searchProducts(searchRequest);
			return response.getItems().stream().map(SearchQueryServiceImpl::toContractItem).collect(Collectors.toList());
		} catch (SearchUnavailableException e) {
			throw e;
		} catch (Exception e) {
			throw new SearchUnavailableException("OpenSearch search failed", e);
		}
	}

	@Override
	public ValueList autocomplete(String store, String lang, String query) {
		Validate.notNull(query, "Search keyword must not be null");
		Validate.notNull(lang, "Language cannot be null");
		Validate.notNull(store, "Store cannot be null");
		ensureAvailable();

		try {
			SearchRequest req = new SearchRequest();
			req.setLanguage(lang);
			req.setStore(store.toLowerCase());
			req.setSearchString(query);
			SearchResponse response = searchModule.searchKeywords(req);
			List<String> keywords = response.getItems().stream()
					.map(modules.commons.search.request.SearchItem::getSuggestions)
					.collect(Collectors.toList());
			ValueList valueList = new ValueList();
			valueList.setValues(keywords);
			return valueList;
		} catch (SearchUnavailableException e) {
			throw e;
		} catch (Exception e) {
			throw new SearchUnavailableException("OpenSearch autocomplete failed", e);
		}
	}

	private void ensureAvailable() {
		if (searchModule == null) {
			throw new SearchUnavailableException("OpenSearch is not configured");
		}
	}

	SearchModule getSearchModule() {
		return searchModule;
	}

	// ponytail: Jackson convertValue — commons SearchItem shares field names with contract DTO
	private static SearchItem toContractItem(modules.commons.search.request.SearchItem source) {
		return MAPPER.convertValue(source, SearchItem.class);
	}
}
