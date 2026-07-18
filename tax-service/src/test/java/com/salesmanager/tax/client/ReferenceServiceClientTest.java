package com.salesmanager.tax.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.salesmanager.contracts.reference.ReadableCountry;
import com.salesmanager.contracts.reference.ReadableLanguage;
import com.salesmanager.contracts.reference.ReadableZone;

class ReferenceServiceClientTest {

	private RestTemplate restTemplate;
	private MockRestServiceServer server;
	private ReferenceServiceClientImpl client;

	@BeforeEach
	void setUp() {
		restTemplate = new RestTemplate();
		server = MockRestServiceServer.createServer(restTemplate);
		client = new ReferenceServiceClientImpl(restTemplate, "http://reference-test:8081");
	}

	@Test
	void getCountryByCode_resolvesFromCountryList() {
		server.expect(requestTo("http://reference-test:8081/api/v1/country?lang=en"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(
						"[{\"id\":1,\"code\":\"CA\",\"name\":\"Canada\",\"supported\":true,\"zones\":[]}]",
						MediaType.APPLICATION_JSON));

		ReadableCountry country = client.getCountryByCode("CA", "en");

		assertThat(country).isNotNull();
		assertThat(country.getCode()).isEqualTo("CA");
		assertThat(country.getId()).isEqualTo(1L);
		server.verify();
	}

	@Test
	void getZoneByCode_resolvesFromZonesList() {
		server.expect(requestTo("http://reference-test:8081/api/v1/zones?code=CA&lang=en"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(
						"[{\"id\":10,\"code\":\"QC\",\"name\":\"Quebec\",\"countryCode\":\"CA\"}]",
						MediaType.APPLICATION_JSON));

		ReadableZone zone = client.getZoneByCode("CA", "QC", "en");

		assertThat(zone).isNotNull();
		assertThat(zone.getCode()).isEqualTo("QC");
		assertThat(zone.getId()).isEqualTo(10L);
		server.verify();
	}

	@Test
	void getLanguageByCode_resolvesFromLanguagesList() {
		server.expect(requestTo("http://reference-test:8081/api/v1/languages"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(
						"[{\"id\":1,\"code\":\"en\",\"sortOrder\":0}]",
						MediaType.APPLICATION_JSON));

		ReadableLanguage language = client.getLanguageByCode("en");

		assertThat(language).isNotNull();
		assertThat(language.getCode()).isEqualTo("en");
		server.verify();
	}

	@Test
	void getCountryByCode_unknown_returnsNull() {
		server.expect(requestTo("http://reference-test:8081/api/v1/country?lang=en"))
				.andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

		assertThat(client.getCountryByCode("XX", "en")).isNull();
		server.verify();
	}
}
