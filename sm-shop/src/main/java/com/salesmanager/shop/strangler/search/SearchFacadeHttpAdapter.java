package com.salesmanager.shop.strangler.search;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.salesmanager.contracts.tenant.LanguageCode;
import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.shop.model.catalog.SearchProductRequest;
import com.salesmanager.shop.model.entity.ValueList;
import com.salesmanager.shop.store.controller.search.facade.SearchFacade;
import com.salesmanager.shop.strangler.support.StranglerRestClient;
import com.salesmanager.shop.tenant.TenantEntityBridge;

import modules.commons.search.request.SearchItem;

@Service("searchFacade")
@ConditionalOnProperty(name = "wave2.strangler.enabled", havingValue = "true")
public class SearchFacadeHttpAdapter implements SearchFacade {

	private final StranglerRestClient restClient;
	private final String baseUrl;
	private final SearchBulkIndexOrchestrator bulkIndexOrchestrator;
	private final TenantEntityBridge tenantEntityBridge;

	public SearchFacadeHttpAdapter(
			RestTemplate wave2RestTemplate,
			@Value("${wave2.search-service.base-url}") String baseUrl,
			SearchBulkIndexOrchestrator bulkIndexOrchestrator,
			TenantEntityBridge tenantEntityBridge) {
		this.restClient = new StranglerRestClient(wave2RestTemplate);
		this.baseUrl = StringUtils.removeEnd(baseUrl, "/");
		this.bulkIndexOrchestrator = bulkIndexOrchestrator;
		this.tenantEntityBridge = tenantEntityBridge;
	}

	@Override
	@Async
	public void indexAllData(MerchantStoreId storeId) throws Exception {
		MerchantStore store = tenantEntityBridge.resolveStore(storeId);
		bulkIndexOrchestrator.indexAllData(store);
	}

	@Override
	public List<SearchItem> search(MerchantStoreId storeId, LanguageCode language, SearchProductRequest searchRequest) {
		Validate.notNull(searchRequest, "SearchProductRequest cannot be null");
		Validate.notNull(searchRequest.getQuery(), "Search Keyword must not be null");
		Validate.notNull(language, "Language cannot be null");
		Validate.notNull(storeId, "MerchantStoreId cannot be null");

		List<SearchItem> items = restClient.exchange(
				searchUrl("/search", storeId, language),
				HttpMethod.POST,
				searchRequest,
				new ParameterizedTypeReference<List<SearchItem>>() {},
				false);
		return items != null ? items : Collections.emptyList();
	}

	@Override
	public ValueList autocompleteRequest(String word, MerchantStoreId storeId, LanguageCode language) {
		Validate.notNull(word, "Search Keyword must not be null");
		Validate.notNull(language, "Language cannot be null");
		Validate.notNull(storeId, "MerchantStoreId cannot be null");

		SearchProductRequest request = new SearchProductRequest();
		request.setQuery(word);

		com.salesmanager.contracts.search.ValueList remote = restClient.exchange(
				searchUrl("/search/autocomplete", storeId, language),
				HttpMethod.POST,
				request,
				com.salesmanager.contracts.search.ValueList.class,
				false);

		ValueList valueList = new ValueList();
		if (remote != null && remote.getValues() != null) {
			valueList.setValues(remote.getValues());
		}
		return valueList;
	}

	private String searchUrl(String path, MerchantStoreId storeId, LanguageCode language) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1" + path);
		if (storeId != null && StringUtils.isNotBlank(storeId.getCode())) {
			builder.queryParam("store", storeId.getCode());
		}
		if (language != null && StringUtils.isNotBlank(language.getCode())) {
			builder.queryParam("lang", language.getCode());
		}
		return builder.toUriString();
	}
}
