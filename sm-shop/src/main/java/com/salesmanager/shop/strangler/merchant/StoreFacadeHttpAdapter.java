package com.salesmanager.shop.strangler.merchant;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.salesmanager.contracts.client.MerchantServiceClient;
import com.salesmanager.contracts.merchant.MerchantStoreSnapshot;
import com.salesmanager.core.model.content.InputContentFile;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.merchant.MerchantStoreCriteria;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.entity.EntityExists;
import com.salesmanager.shop.model.store.PersistableBrand;
import com.salesmanager.shop.model.store.PersistableMerchantStore;
import com.salesmanager.shop.model.store.ReadableBrand;
import com.salesmanager.shop.model.store.ReadableMerchantStore;
import com.salesmanager.shop.model.store.ReadableMerchantStoreList;
import com.salesmanager.shop.store.api.exception.ResourceNotFoundException;
import com.salesmanager.shop.store.controller.store.facade.StoreFacade;
import com.salesmanager.shop.strangler.support.DownstreamHttpException;
import com.salesmanager.shop.strangler.support.ServiceUnavailableException;
import com.salesmanager.shop.strangler.support.StranglerRestClient;

@Service("storeFacade")
@ConditionalOnProperty(name = "wave2.strangler.enabled", havingValue = "true")
public class StoreFacadeHttpAdapter implements StoreFacade {

	private final StranglerRestClient restClient;
	private final RestTemplate wave2RestTemplate;
	private final String baseUrl;
	private final MerchantServiceClient merchantServiceClient;
	private final MerchantStoreEntityHydrator merchantStoreEntityHydrator;

	public StoreFacadeHttpAdapter(
			RestTemplate wave2RestTemplate,
			@Value("${wave2.merchant-service.base-url}") String baseUrl,
			MerchantServiceClient merchantServiceClient,
			MerchantStoreEntityHydrator merchantStoreEntityHydrator) {
		this.restClient = new StranglerRestClient(wave2RestTemplate);
		this.wave2RestTemplate = wave2RestTemplate;
		this.baseUrl = StringUtils.removeEnd(baseUrl, "/");
		this.merchantServiceClient = merchantServiceClient;
		this.merchantStoreEntityHydrator = merchantStoreEntityHydrator;
	}

	@Override
	public MerchantStore getByCode(HttpServletRequest request) {
		String code = request.getParameter("store");
		if (StringUtils.isBlank(code)) {
			code = MerchantStore.DEFAULT_STORE;
		}
		return get(code);
	}

	@Override
	public MerchantStore get(String code) {
		MerchantStoreSnapshot snapshot = merchantServiceClient.getStoreSnapshot(code);
		return merchantStoreEntityHydrator.hydrate(snapshot);
	}

	@Override
	public MerchantStore getByCode(String code) {
		MerchantStore store = get(code);
		if (store == null) {
			throw new ResourceNotFoundException("Merchant store code [" + code + "] not found");
		}
		return store;
	}

	@Override
	public List<Language> supportedLanguages(MerchantStore store) {
		Validate.notNull(store, "MerchantStore cannot be null");
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1/store/languages");
		appendStore(builder, store);
		List<Language> languages = restClient.exchange(
				builder.toUriString(),
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<Language>>() {},
				false);
		return languages != null ? languages : Collections.emptyList();
	}

	@Override
	public ReadableMerchantStore getByCode(String code, String lang) {
		return restClient.exchange(
				publicStoreUrl("/store/" + code, lang),
				HttpMethod.GET,
				null,
				ReadableMerchantStore.class,
				false);
	}

	@Override
	public ReadableMerchantStore getFullByCode(String code, String lang) {
		return restClient.exchange(
				privateStoreUrl("/private/store/" + code, lang),
				HttpMethod.GET,
				null,
				ReadableMerchantStore.class,
				true);
	}

	@Override
	public ReadableMerchantStoreList findAll(MerchantStoreCriteria criteria, Language language, int page, int count) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1/private/stores")
				.queryParam("page", page)
				.queryParam("count", count);
		appendCriteria(builder, criteria);
		appendLang(builder, language);
		return restClient.exchange(
				builder.toUriString(),
				HttpMethod.GET,
				null,
				ReadableMerchantStoreList.class,
				true);
	}

	@Override
	public ReadableMerchantStoreList getChildStores(Language language, String code, int start, int count) {
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(baseUrl + "/api/v1/private/merchant/" + code + "/stores")
				.queryParam("page", start)
				.queryParam("count", count);
		appendLang(builder, language);
		return restClient.exchange(
				builder.toUriString(),
				HttpMethod.GET,
				null,
				ReadableMerchantStoreList.class,
				true);
	}

	@Override
	public ReadableMerchantStore getByCode(String code, Language lang) {
		return restClient.exchange(
				publicStoreUrl("/store/" + code, lang),
				HttpMethod.GET,
				null,
				ReadableMerchantStore.class,
				false);
	}

	@Override
	public ReadableMerchantStore getFullByCode(String code, Language language) {
		return restClient.exchange(
				privateStoreUrl("/private/store/" + code, language),
				HttpMethod.GET,
				null,
				ReadableMerchantStore.class,
				true);
	}

	@Override
	public boolean existByCode(String code) {
		Validate.notNull(code, "Store code cannot be null");
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1/store/unique")
				.queryParam("code", code);
		EntityExists exists = restClient.exchange(
				builder.toUriString(),
				HttpMethod.GET,
				null,
				EntityExists.class,
				false);
		return exists != null && exists.isExists();
	}

	@Override
	public ReadableMerchantStoreList getByCriteria(MerchantStoreCriteria criteria, Language lang) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1/private/stores");
		appendCriteria(builder, criteria);
		appendLang(builder, lang);
		return restClient.exchange(
				builder.toUriString(),
				HttpMethod.GET,
				null,
				ReadableMerchantStoreList.class,
				true);
	}

	@Override
	public void create(PersistableMerchantStore store) {
		Validate.notNull(store, "PersistableMerchantStore must not be null");
		restClient.exchangeVoid(
				baseUrl + "/api/v1/private/store",
				HttpMethod.POST,
				store,
				true);
	}

	@Override
	public void update(PersistableMerchantStore store) {
		Validate.notNull(store, "PersistableMerchantStore must not be null");
		Validate.notNull(store.getCode(), "PersistableMerchantStore.code must not be null");
		restClient.exchangeVoid(
				baseUrl + "/api/v1/private/store/" + store.getCode(),
				HttpMethod.PUT,
				store,
				true);
	}

	@Override
	public void delete(String code) {
		Validate.notNull(code, "Store code cannot be null");
		restClient.exchangeVoid(
				baseUrl + "/api/v1/private/store/" + code,
				HttpMethod.DELETE,
				null,
				true);
	}

	@Override
	public ReadableBrand getBrand(String code) {
		Validate.notNull(code, "Store code cannot be null");
		return restClient.exchange(
				baseUrl + "/api/v1/private/store/" + code + "/marketing",
				HttpMethod.GET,
				null,
				ReadableBrand.class,
				true);
	}

	@Override
	public void createBrand(String merchantStoreCode, PersistableBrand brand) {
		Validate.notNull(merchantStoreCode, "Store code cannot be null");
		Validate.notNull(brand, "PersistableBrand must not be null");
		restClient.exchangeVoid(
				baseUrl + "/api/v1/private/store/" + merchantStoreCode + "/marketing",
				HttpMethod.POST,
				brand,
				true);
	}

	@Override
	public void deleteLogo(String code) {
		Validate.notNull(code, "Store code cannot be null");
		restClient.exchangeVoid(
				baseUrl + "/api/v1/private/store/" + code + "/marketing/logo",
				HttpMethod.DELETE,
				null,
				true);
	}

	@Override
	public void addStoreLogo(String code, InputContentFile cmsContentImage) {
		Validate.notNull(code, "Store code cannot be null");
		Validate.notNull(cmsContentImage, "InputContentFile cannot be null");
		byte[] bytes = readBytes(cmsContentImage);
		String fileName = cmsContentImage.getFileName();
		String url = baseUrl + "/api/v1/private/store/" + code + "/marketing/logo";

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", new ByteArrayResource(bytes) {
			@Override
			public String getFilename() {
				return fileName;
			}
		});

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.set(StranglerRestClient.CORRELATION_HEADER, resolveCorrelationId());
		String authorization = currentHeader(HttpHeaders.AUTHORIZATION);
		if (StringUtils.isNotBlank(authorization)) {
			headers.set(HttpHeaders.AUTHORIZATION, authorization);
		}

		try {
			HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
			ResponseEntity<Void> response = wave2RestTemplate.postForEntity(url, entity, Void.class);
			if (!response.getStatusCode().is2xxSuccessful()) {
				throw new DownstreamHttpException(response.getStatusCode(), null);
			}
		} catch (HttpStatusCodeException e) {
			org.springframework.http.HttpStatus status =
					org.springframework.http.HttpStatus.resolve(e.getRawStatusCode());
			if (status == null) {
				status = org.springframework.http.HttpStatus.BAD_GATEWAY;
			}
			throw new DownstreamHttpException(status, e.getResponseBodyAsString());
		} catch (ResourceAccessException e) {
			throw new ServiceUnavailableException("Downstream service unavailable: " + url, e);
		} catch (RestClientException e) {
			throw new ServiceUnavailableException("Downstream service call failed: " + url, e);
		}
	}

	@Override
	public List<ReadableMerchantStore> getMerchantStoreNames(MerchantStoreCriteria criteria) {
		Validate.notNull(criteria, "MerchantStoreCriteria must not be null");
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1/private/stores/names");
		if (StringUtils.isNotBlank(criteria.getStoreCode())) {
			builder.queryParam("store", criteria.getStoreCode());
		}
		if (StringUtils.isNotBlank(criteria.getLanguage())) {
			builder.queryParam("lang", criteria.getLanguage());
		}
		List<ReadableMerchantStore> names = restClient.exchange(
				builder.toUriString(),
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<ReadableMerchantStore>>() {},
				true);
		return names != null ? names : Collections.emptyList();
	}

	private String publicStoreUrl(String path, String lang) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1" + path);
		if (StringUtils.isNotBlank(lang)) {
			builder.queryParam("lang", lang);
		}
		return builder.toUriString();
	}

	private String publicStoreUrl(String path, Language language) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1" + path);
		appendLang(builder, language);
		return builder.toUriString();
	}

	private String privateStoreUrl(String path, String lang) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1" + path);
		if (StringUtils.isNotBlank(lang)) {
			builder.queryParam("lang", lang);
		}
		return builder.toUriString();
	}

	private String privateStoreUrl(String path, Language language) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/v1" + path);
		appendLang(builder, language);
		return builder.toUriString();
	}

	private void appendCriteria(UriComponentsBuilder builder, MerchantStoreCriteria criteria) {
		if (criteria == null) {
			return;
		}
		if (StringUtils.isNotBlank(criteria.getCode())) {
			builder.queryParam("code", criteria.getCode());
		}
		if (StringUtils.isNotBlank(criteria.getName())) {
			builder.queryParam("name", criteria.getName());
		}
		if (StringUtils.isNotBlank(criteria.getStoreCode())) {
			builder.queryParam("store", criteria.getStoreCode());
		}
		if (criteria.isRetailers()) {
			builder.queryParam("retailers", true);
		}
	}

	private void appendLang(UriComponentsBuilder builder, Language language) {
		if (language != null && StringUtils.isNotBlank(language.getCode())) {
			builder.queryParam("lang", language.getCode());
		}
	}

	private void appendStore(UriComponentsBuilder builder, MerchantStore store) {
		if (store != null && StringUtils.isNotBlank(store.getCode())) {
			builder.queryParam("store", store.getCode());
		}
	}

	private static byte[] readBytes(InputContentFile file) {
		try (InputStream in = file.getFile()) {
			return in != null ? IOUtils.toByteArray(in) : new byte[0];
		} catch (IOException e) {
			throw new IllegalStateException("Cannot read logo content", e);
		}
	}

	private static String resolveCorrelationId() {
		String existing = currentHeader(StranglerRestClient.CORRELATION_HEADER);
		return StringUtils.isNotBlank(existing) ? existing : UUID.randomUUID().toString();
	}

	private static String currentHeader(String name) {
		org.springframework.web.context.request.RequestAttributes attrs =
				org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
		if (!(attrs instanceof org.springframework.web.context.request.ServletRequestAttributes)) {
			return null;
		}
		HttpServletRequest request = ((org.springframework.web.context.request.ServletRequestAttributes) attrs).getRequest();
		return request != null ? request.getHeader(name) : null;
	}
}
