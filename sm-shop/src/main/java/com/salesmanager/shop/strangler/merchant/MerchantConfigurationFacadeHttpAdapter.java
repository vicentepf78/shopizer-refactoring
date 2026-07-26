package com.salesmanager.shop.strangler.merchant;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.system.Configs;
import com.salesmanager.shop.store.controller.system.MerchantConfigurationFacade;
import com.salesmanager.shop.strangler.support.StranglerRestClient;

@Service
@ConditionalOnProperty(name = "wave2.strangler.enabled", havingValue = "true")
public class MerchantConfigurationFacadeHttpAdapter implements MerchantConfigurationFacade {

	private final StranglerRestClient restClient;
	private final String baseUrl;

	public MerchantConfigurationFacadeHttpAdapter(
			RestTemplate wave2RestTemplate,
			@Value("${wave2.merchant-service.base-url}") String baseUrl) {
		this.restClient = new StranglerRestClient(wave2RestTemplate);
		this.baseUrl = StringUtils.removeEnd(baseUrl, "/");
	}

	@Override
	public Configs getMerchantConfig(MerchantStore merchantStore, Language language) {
		Validate.notNull(merchantStore, "MerchantStore cannot be null");
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1/config");
		if (StringUtils.isNotBlank(merchantStore.getCode())) {
			builder.queryParam("store", merchantStore.getCode());
		}
		if (language != null && StringUtils.isNotBlank(language.getCode())) {
			builder.queryParam("lang", language.getCode());
		}
		return restClient.exchange(
				builder.toUriString(),
				HttpMethod.GET,
				null,
				Configs.class,
				false);
	}
}
