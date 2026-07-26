package com.salesmanager.shop.strangler.search;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.salesmanager.contracts.client.SearchIndexClient;
import com.salesmanager.contracts.search.ProductIndexBulkPayload;
import com.salesmanager.contracts.search.ProductIndexPayload;
import com.salesmanager.shop.strangler.config.Wave2Properties;

/**
 * ponytail: stub HTTP producer — logs failures only; full error mapping in T24.
 */
public class SearchIndexClientRestTemplateImpl implements SearchIndexClient {

	static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

	private final RestTemplate restTemplate;
	private final Wave2Properties properties;

	public SearchIndexClientRestTemplateImpl(RestTemplate restTemplate, Wave2Properties properties) {
		this.restTemplate = restTemplate;
		this.properties = properties;
	}

	@Override
	public void index(ProductIndexPayload payload) {
		String url = properties.getSearchService().getBaseUrl() + "/internal/v1/index";
		restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(payload, internalHeaders()), Void.class);
	}

	@Override
	public void indexBulk(ProductIndexBulkPayload bulk) {
		String url = properties.getSearchService().getBaseUrl() + "/internal/v1/index/bulk";
		restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(bulk, internalHeaders()), Void.class);
	}

	@Override
	public void deleteDocument(Long productId, String store, List<String> languages) {
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(properties.getSearchService().getBaseUrl() + "/internal/v1/index/" + productId)
				.queryParam("store", store);
		if (!CollectionUtils.isEmpty(languages)) {
			builder.queryParam("languages", languages.stream().collect(Collectors.joining(",")));
		}
		restTemplate.exchange(builder.toUriString(), HttpMethod.DELETE, new HttpEntity<>(internalHeaders()), Void.class);
	}

	private HttpHeaders internalHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.set(INTERNAL_TOKEN_HEADER, properties.getSearchService().getInternalToken());
		return headers;
	}
}
