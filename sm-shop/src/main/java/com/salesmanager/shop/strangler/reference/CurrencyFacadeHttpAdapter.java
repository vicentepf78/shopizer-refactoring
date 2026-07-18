package com.salesmanager.shop.strangler.reference;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.salesmanager.contracts.reference.ReadableCurrency;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.shop.store.api.exception.ResourceNotFoundException;
import com.salesmanager.shop.store.controller.currency.facade.CurrencyFacade;
import com.salesmanager.shop.strangler.support.StranglerRestClient;

@Service
@ConditionalOnProperty(name = "wave1.strangler.enabled", havingValue = "true")
public class CurrencyFacadeHttpAdapter implements CurrencyFacade {

	private final StranglerRestClient restClient;
	private final String baseUrl;

	public CurrencyFacadeHttpAdapter(
			StranglerRestClient restClient,
			@Value("${wave1.reference-service.base-url}") String baseUrl) {
		this.restClient = restClient;
		this.baseUrl = StringUtils.removeEnd(baseUrl, "/");
	}

	@Override
	public List<Currency> getList() {
		List<ReadableCurrency> body = restClient.exchange(
				baseUrl + "/api/v1/currency",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<ReadableCurrency>>() {},
				false);
		if (body == null || body.isEmpty()) {
			throw new ResourceNotFoundException("No currencies found");
		}
		return body.stream()
				.map(this::toCurrency)
				.sorted(Comparator.comparing(Currency::getCode, Comparator.nullsLast(String::compareTo)))
				.collect(Collectors.toList());
	}

	private Currency toCurrency(ReadableCurrency dto) {
		Currency currency = new Currency();
		if (dto.getId() != null) {
			currency.setId(dto.getId());
		}
		if (StringUtils.isNotBlank(dto.getCode())) {
			currency.setCurrency(java.util.Currency.getInstance(dto.getCode()));
		}
		currency.setName(dto.getName());
		currency.setSupported(dto.isSupported());
		return currency;
	}
}
