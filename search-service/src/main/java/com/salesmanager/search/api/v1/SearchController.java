package com.salesmanager.search.api.v1;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.salesmanager.contracts.search.ValueList;
import com.salesmanager.search.services.SearchQueryService;

import modules.commons.search.request.SearchItem;

@RestController
@RequestMapping("/api/v1")
public class SearchController {

	private final SearchQueryService searchQueryService;

	public SearchController(SearchQueryService searchQueryService) {
		this.searchQueryService = searchQueryService;
	}

	@PostMapping("/search")
	public @ResponseBody List<SearchItem> search(
			@RequestBody SearchProductRequest searchRequest,
			@RequestParam(defaultValue = "DEFAULT") String store,
			@RequestParam(defaultValue = "en") String lang) {
		return searchQueryService.search(store, lang, searchRequest);
	}

	@PostMapping("/search/autocomplete")
	public @ResponseBody ValueList autocomplete(
			@RequestBody SearchProductRequest searchRequest,
			@RequestParam(defaultValue = "DEFAULT") String store,
			@RequestParam(defaultValue = "en") String lang) {
		return searchQueryService.autocomplete(store, lang, searchRequest.getQuery());
	}
}
