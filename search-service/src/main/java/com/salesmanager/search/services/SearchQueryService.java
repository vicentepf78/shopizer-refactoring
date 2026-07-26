package com.salesmanager.search.services;

import java.util.List;

import com.salesmanager.contracts.search.ValueList;
import com.salesmanager.search.api.v1.SearchProductRequest;

import modules.commons.search.request.SearchItem;

public interface SearchQueryService {

	List<SearchItem> search(String store, String lang, SearchProductRequest request);

	ValueList autocomplete(String store, String lang, String query);
}
