package com.salesmanager.shop.strangler.reference;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.references.ReadableZone;
import com.salesmanager.shop.store.controller.zone.facade.ZoneFacade;
import com.salesmanager.shop.strangler.support.StranglerRestClient;

@Service
@ConditionalOnProperty(name = "wave1.strangler.enabled", havingValue = "true")
public class ZoneFacadeHttpAdapter implements ZoneFacade {

	private final StranglerRestClient restClient;
	private final String baseUrl;

	public ZoneFacadeHttpAdapter(
			StranglerRestClient restClient,
			@Value("${wave1.reference-service.base-url}") String baseUrl) {
		this.restClient = restClient;
		this.baseUrl = StringUtils.removeEnd(baseUrl, "/");
	}

	@Override
	public List<ReadableZone> getZones(String countryCode, Language language, MerchantStore merchantStore) {
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(baseUrl + "/api/v1/zones")
				.queryParam("code", countryCode);
		if (language != null && StringUtils.isNotBlank(language.getCode())) {
			builder.queryParam("lang", language.getCode());
		}
		if (merchantStore != null && StringUtils.isNotBlank(merchantStore.getCode())) {
			builder.queryParam("store", merchantStore.getCode());
		}
		List<ReadableZone> body = restClient.exchange(
				builder.toUriString(),
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<ReadableZone>>() {},
				false);
		return body != null ? body : Collections.emptyList();
	}
}
