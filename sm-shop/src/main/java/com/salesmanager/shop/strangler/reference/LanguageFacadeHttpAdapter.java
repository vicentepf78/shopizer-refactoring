package com.salesmanager.shop.strangler.reference;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.salesmanager.contracts.reference.ReadableLanguage;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.store.api.exception.ResourceNotFoundException;
import com.salesmanager.shop.store.controller.language.facade.LanguageFacade;
import com.salesmanager.shop.strangler.support.StranglerRestClient;

@Service
@ConditionalOnProperty(name = "wave1.strangler.enabled", havingValue = "true")
public class LanguageFacadeHttpAdapter implements LanguageFacade {

	private final StranglerRestClient restClient;
	private final String baseUrl;

	public LanguageFacadeHttpAdapter(
			StranglerRestClient restClient,
			@Value("${wave1.reference-service.base-url}") String baseUrl) {
		this.restClient = restClient;
		this.baseUrl = StringUtils.removeEnd(baseUrl, "/");
	}

	@Override
	public List<Language> getLanguages() {
		List<ReadableLanguage> body = restClient.exchange(
				baseUrl + "/api/v1/languages",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<ReadableLanguage>>() {},
				false);
		if (body == null || body.isEmpty()) {
			throw new ResourceNotFoundException("No languages found");
		}
		return body.stream().map(this::toLanguage).collect(Collectors.toList());
	}

	private Language toLanguage(ReadableLanguage dto) {
		Language language = new Language();
		language.setId(dto.getId());
		language.setCode(dto.getCode());
		language.setSortOrder(dto.getSortOrder());
		return language;
	}
}
