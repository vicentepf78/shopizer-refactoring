package com.salesmanager.shop.strangler.tax;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.entity.Entity;
import com.salesmanager.shop.model.entity.EntityExists;
import com.salesmanager.shop.model.entity.ReadableEntityList;
import com.salesmanager.shop.model.tax.PersistableTaxClass;
import com.salesmanager.shop.model.tax.PersistableTaxRate;
import com.salesmanager.shop.model.tax.ReadableTaxClass;
import com.salesmanager.shop.model.tax.ReadableTaxRate;
import com.salesmanager.shop.store.controller.tax.facade.TaxFacade;
import com.salesmanager.shop.strangler.support.StranglerRestClient;

@Service
@ConditionalOnProperty(name = "wave1.strangler.enabled", havingValue = "true")
public class TaxFacadeHttpAdapter implements TaxFacade {

	private final StranglerRestClient restClient;
	private final String baseUrl;

	public TaxFacadeHttpAdapter(
			StranglerRestClient restClient,
			@Value("${wave1.tax-service.base-url}") String baseUrl) {
		this.restClient = restClient;
		this.baseUrl = StringUtils.removeEnd(baseUrl, "/");
	}

	@Override
	public Entity createTaxClass(PersistableTaxClass taxClass, MerchantStore store, Language language) {
		Validate.notNull(taxClass, "TaxClass cannot be null");
		return restClient.exchange(
				taxUrl("/private/tax/class", store, language),
				HttpMethod.POST,
				taxClass,
				Entity.class,
				true);
	}

	@Override
	public void updateTaxClass(Long id, PersistableTaxClass taxClass, MerchantStore store, Language language) {
		Validate.notNull(id, "TaxClass id cannot be null");
		Validate.notNull(taxClass, "TaxClass cannot be null");
		taxClass.setId(id);
		restClient.exchangeVoid(
				taxUrl("/private/tax/class/" + id, store, language),
				HttpMethod.PUT,
				taxClass,
				true);
	}

	@Override
	public void deleteTaxClass(Long id, MerchantStore store, Language language) {
		Validate.notNull(id, "TaxClass id cannot be null");
		restClient.exchangeVoid(
				taxUrl("/private/tax/class/" + id, store, language),
				HttpMethod.DELETE,
				null,
				true);
	}

	@Override
	public boolean existsTaxClass(String code, MerchantStore store, Language language) {
		Validate.notNull(code, "TaxClass code cannot be null");
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(baseUrl + "/api/v1/private/tax/class/unique")
				.queryParam("code", code);
		appendStoreLang(builder, store, language);
		EntityExists exists = restClient.exchange(
				builder.toUriString(),
				HttpMethod.GET,
				null,
				EntityExists.class,
				true);
		return exists != null && exists.isExists();
	}

	@Override
	public ReadableEntityList<ReadableTaxClass> taxClasses(MerchantStore store, Language language) {
		return restClient.exchange(
				taxUrl("/private/tax/class", store, language),
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<ReadableEntityList<ReadableTaxClass>>() {},
				true);
	}

	@Override
	public ReadableTaxClass taxClass(String code, MerchantStore store, Language language) {
		Validate.notNull(code, "TaxClass code cannot be null");
		return restClient.exchange(
				taxUrl("/private/tax/class/" + code, store, language),
				HttpMethod.GET,
				null,
				ReadableTaxClass.class,
				true);
	}

	@Override
	public Entity createTaxRate(PersistableTaxRate taxRate, MerchantStore store, Language language) {
		Validate.notNull(taxRate, "TaxRate cannot be null");
		return restClient.exchange(
				taxUrl("/private/tax/rate", store, language),
				HttpMethod.POST,
				taxRate,
				Entity.class,
				true);
	}

	@Override
	public void updateTaxRate(Long id, PersistableTaxRate taxRate, MerchantStore store, Language language) {
		Validate.notNull(id, "TaxRate id cannot be null");
		Validate.notNull(taxRate, "TaxRate cannot be null");
		taxRate.setId(id);
		restClient.exchangeVoid(
				taxUrl("/private/tax/rate/" + id, store, language),
				HttpMethod.PUT,
				taxRate,
				true);
	}

	@Override
	public void deleteTaxRate(Long id, MerchantStore store, Language language) {
		Validate.notNull(id, "TaxRate id cannot be null");
		restClient.exchangeVoid(
				taxUrl("/private/tax/rate/" + id, store, language),
				HttpMethod.DELETE,
				null,
				true);
	}

	@Override
	public boolean existsTaxRate(String code, MerchantStore store, Language language) {
		Validate.notNull(code, "TaxRate code cannot be null");
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(baseUrl + "/api/v1/private/tax/rate/unique")
				.queryParam("code", code);
		appendStoreLang(builder, store, language);
		EntityExists exists = restClient.exchange(
				builder.toUriString(),
				HttpMethod.GET,
				null,
				EntityExists.class,
				true);
		return exists != null && exists.isExists();
	}

	@Override
	public ReadableEntityList<ReadableTaxRate> taxRates(MerchantStore store, Language language) {
		return restClient.exchange(
				taxUrl("/private/tax/rates", store, language),
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<ReadableEntityList<ReadableTaxRate>>() {},
				true);
	}

	@Override
	public ReadableTaxRate taxRate(Long id, MerchantStore store, Language language) {
		Validate.notNull(id, "TaxRate id cannot be null");
		return restClient.exchange(
				taxUrl("/private/tax/rate/" + id, store, language),
				HttpMethod.GET,
				null,
				ReadableTaxRate.class,
				true);
	}

	private String taxUrl(String path, MerchantStore store, Language language) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1" + path);
		appendStoreLang(builder, store, language);
		return builder.toUriString();
	}

	private void appendStoreLang(UriComponentsBuilder builder, MerchantStore store, Language language) {
		if (store != null && StringUtils.isNotBlank(store.getCode())) {
			builder.queryParam("store", store.getCode());
		}
		if (language != null && StringUtils.isNotBlank(language.getCode())) {
			builder.queryParam("lang", language.getCode());
		}
	}
}
