package com.salesmanager.shop.strangler.reference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.ConnectException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.references.ReadableCountry;
import com.salesmanager.shop.model.references.ReadableZone;
import com.salesmanager.shop.strangler.support.ServiceUnavailableException;
import com.salesmanager.shop.strangler.support.StranglerRestClient;

class ReferenceFacadeHttpAdapterTest {

	private RestTemplate restTemplate;
	private MockRestServiceServer server;
	private StranglerRestClient restClient;
	private CountryFacadeHttpAdapter countryAdapter;
	private ZoneFacadeHttpAdapter zoneAdapter;
	private LanguageFacadeHttpAdapter languageAdapter;
	private CurrencyFacadeHttpAdapter currencyAdapter;

	@BeforeEach
	void setUp() {
		restTemplate = new RestTemplate();
		server = MockRestServiceServer.createServer(restTemplate);
		restClient = new StranglerRestClient(restTemplate);
		countryAdapter = new CountryFacadeHttpAdapter(restClient, "http://reference-test:8081");
		zoneAdapter = new ZoneFacadeHttpAdapter(restClient, "http://reference-test:8081");
		languageAdapter = new LanguageFacadeHttpAdapter(restClient, "http://reference-test:8081");
		currencyAdapter = new CurrencyFacadeHttpAdapter(restClient, "http://reference-test:8081");

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(StranglerRestClient.CORRELATION_HEADER, "corr-ref-1");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
	}

	@AfterEach
	void tearDown() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	void countryAdapter_propagatesCorrelationId() {
		server.expect(requestTo("http://reference-test:8081/api/v1/country?lang=en&store=DEFAULT"))
				.andExpect(method(HttpMethod.GET))
				.andExpect(header(StranglerRestClient.CORRELATION_HEADER, "corr-ref-1"))
				.andRespond(withSuccess(
						"[{\"id\":1,\"code\":\"CA\",\"name\":\"Canada\",\"zones\":[]}]",
						MediaType.APPLICATION_JSON));

		Language language = new Language("en");
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");

		List<ReadableCountry> countries = countryAdapter.getListCountryZones(language, store);

		assertThat(countries).hasSize(1);
		assertThat(countries.get(0).getCode()).isEqualTo("CA");
		server.verify();
	}

	@Test
	void zoneAdapter_returnsEmptyListForUnknownCountry() {
		server.expect(requestTo("http://reference-test:8081/api/v1/zones?code=XX&lang=en&store=DEFAULT"))
				.andExpect(method(HttpMethod.GET))
				.andExpect(header(StranglerRestClient.CORRELATION_HEADER, "corr-ref-1"))
				.andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

		Language language = new Language("en");
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");

		List<ReadableZone> zones = zoneAdapter.getZones("XX", language, store);

		assertThat(zones).isEmpty();
		server.verify();
	}

	@Test
	void languageAdapter_mapsReadableLanguageToEntity() {
		server.expect(requestTo("http://reference-test:8081/api/v1/languages"))
				.andExpect(method(HttpMethod.GET))
				.andExpect(header(StranglerRestClient.CORRELATION_HEADER, "corr-ref-1"))
				.andRespond(withSuccess(
						"[{\"id\":1,\"code\":\"en\",\"sortOrder\":0}]",
						MediaType.APPLICATION_JSON));

		List<Language> languages = languageAdapter.getLanguages();

		assertThat(languages).hasSize(1);
		assertThat(languages.get(0).getCode()).isEqualTo("en");
		assertThat(languages.get(0).getId()).isEqualTo(1);
		server.verify();
	}

	@Test
	void currencyAdapter_mapsReadableCurrencyToEntity() {
		server.expect(requestTo("http://reference-test:8081/api/v1/currency"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(
						"[{\"id\":1,\"code\":\"USD\",\"name\":\"US Dollar\",\"symbol\":\"$\",\"supported\":true}]",
						MediaType.APPLICATION_JSON));

		List<Currency> currencies = currencyAdapter.getList();

		assertThat(currencies).hasSize(1);
		assertThat(currencies.get(0).getCode()).isEqualTo("USD");
		server.verify();
	}

	@Test
	void countryAdapter_mapsConnectFailureTo503() {
		server.expect(requestTo("http://reference-test:8081/api/v1/country?lang=en&store=DEFAULT"))
				.andRespond(withException(new ConnectException("Connection refused")));

		Language language = new Language("en");
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");

		assertThatThrownBy(() -> countryAdapter.getListCountryZones(language, store))
				.isInstanceOf(ServiceUnavailableException.class)
				.hasFieldOrPropertyWithValue("errorCode", "503");
	}
}
