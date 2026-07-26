package com.salesmanager.search.services;

import java.util.List;

import com.salesmanager.contracts.search.SearchItem;
import com.salesmanager.contracts.search.SearchProductRequest;
import com.salesmanager.contracts.search.ValueList;

public interface SearchQueryService {

	List<SearchItem> search(String store, String lang, SearchProductRequest request);

	ValueList autocomplete(String store, String lang, String query);
}
