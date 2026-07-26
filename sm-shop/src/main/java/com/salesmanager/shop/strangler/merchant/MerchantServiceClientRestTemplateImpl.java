package com.salesmanager.shop.strangler.merchant;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.salesmanager.contracts.client.MerchantServiceClient;
import com.salesmanager.contracts.merchant.MerchantStoreSnapshot;
import com.salesmanager.shop.strangler.config.Wave2Properties;
import com.salesmanager.shop.strangler.support.StranglerRestClient;

public class MerchantServiceClientRestTemplateImpl implements MerchantServiceClient {

	private final StranglerRestClient restClient;
	private final String baseUrl;

	public MerchantServiceClientRestTemplateImpl(RestTemplate wave2RestTemplate, Wave2Properties properties) {
		this.restClient = new StranglerRestClient(wave2RestTemplate);
		this.baseUrl = StringUtils.removeEnd(properties.getMerchantService().getBaseUrl(), "/");
	}

	@Override
	public MerchantStoreSnapshot getStoreSnapshot(String code) {
		String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/internal/v1/store/" + code)
				.queryParam("lang", "en")
				.toUriString();
		return restClient.exchange(url, HttpMethod.GET, null, MerchantStoreSnapshot.class, false);
	}
}
