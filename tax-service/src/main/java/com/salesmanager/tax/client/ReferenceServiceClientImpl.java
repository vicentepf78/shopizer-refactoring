package com.salesmanager.tax.client;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.salesmanager.contracts.client.ReferenceServiceClient;
import com.salesmanager.contracts.reference.ReadableCountry;
import com.salesmanager.contracts.reference.ReadableLanguage;
import com.salesmanager.contracts.reference.ReadableZone;

@Component
public class ReferenceServiceClientImpl implements ReferenceServiceClient {

	private final RestTemplate referenceRestTemplate;
	private final String baseUrl;

	public ReferenceServiceClientImpl(
			RestTemplate referenceRestTemplate,
			@Value("${wave1.reference-service.base-url}") String baseUrl) {
		this.referenceRestTemplate = referenceRestTemplate;
		this.baseUrl = StringUtils.removeEnd(baseUrl, "/");
	}

	@Override
	public ReadableCountry getCountryByCode(String isoCode, String langCode) {
		if (StringUtils.isBlank(isoCode)) {
			return null;
		}
		List<ReadableCountry> countries = getCountries(langCode);
		return countries.stream()
				.filter(c -> isoCode.equalsIgnoreCase(c.getCode()))
				.findFirst()
				.orElse(null);
	}

	@Override
	public ReadableZone getZoneByCode(String countryCode, String zoneCode, String langCode) {
		if (StringUtils.isBlank(countryCode) || StringUtils.isBlank(zoneCode)) {
			return null;
		}
		List<ReadableZone> zones = getZones(countryCode, langCode);
		return zones.stream()
				.filter(z -> zoneCode.equalsIgnoreCase(z.getCode()))
				.findFirst()
				.orElse(null);
	}

	@Override
	public ReadableLanguage getLanguageByCode(String code) {
		if (StringUtils.isBlank(code)) {
			return null;
		}
		List<ReadableLanguage> languages = getLanguages();
		return languages.stream()
				.filter(l -> code.equalsIgnoreCase(l.getCode()))
				.findFirst()
				.orElse(null);
	}

	List<ReadableCountry> getCountries(String langCode) {
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(baseUrl + "/api/v1/country");
		if (StringUtils.isNotBlank(langCode)) {
			builder.queryParam("lang", langCode);
		}
		return exchangeList(builder.toUriString(), new ParameterizedTypeReference<List<ReadableCountry>>() {});
	}

	List<ReadableZone> getZones(String countryCode, String langCode) {
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(baseUrl + "/api/v1/zones")
				.queryParam("code", countryCode);
		if (StringUtils.isNotBlank(langCode)) {
			builder.queryParam("lang", langCode);
		}
		return exchangeList(builder.toUriString(), new ParameterizedTypeReference<List<ReadableZone>>() {});
	}

	List<ReadableLanguage> getLanguages() {
		return exchangeList(baseUrl + "/api/v1/languages",
				new ParameterizedTypeReference<List<ReadableLanguage>>() {});
	}

	private <T> List<T> exchangeList(String url, ParameterizedTypeReference<List<T>> type) {
		try {
			ResponseEntity<List<T>> response = referenceRestTemplate.exchange(
					url, HttpMethod.GET, null, type);
			List<T> body = response.getBody();
			return body != null ? body : Collections.emptyList();
		} catch (RestClientException e) {
			throw new RestClientException("Reference service call failed: " + url, e);
		}
	}
}
