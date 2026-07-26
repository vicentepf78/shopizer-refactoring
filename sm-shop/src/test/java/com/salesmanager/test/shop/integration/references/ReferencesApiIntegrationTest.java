package com.salesmanager.test.shop.integration.references;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.shop.application.ShopApplication;
import com.salesmanager.test.shop.common.ServicesTestSupport;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ShopApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class ReferencesApiIntegrationTest extends ServicesTestSupport {

	@Inject
	private TestRestTemplate testRestTemplate;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void getLanguages_returnsReadableLanguageShape() throws Exception {
		HttpEntity<String> httpEntity = new HttpEntity<>(getHeader());

		ResponseEntity<String> response = testRestTemplate.exchange(
				"/api/v1/languages",
				HttpMethod.GET,
				httpEntity,
				String.class);

		assertThat(response.getStatusCode(), notNullValue());
		assertThat(response.getStatusCode(), is(HttpStatus.OK));
		assertNotNull(response.getBody());

		JsonNode languages = objectMapper.readTree(response.getBody());
		assertThat(languages.size(), greaterThanOrEqualTo(1));

		JsonNode first = languages.get(0);
		assertThat(first.has("id"), is(true));
		assertThat(first.has("code"), is(true));
		assertThat(first.has("sortOrder"), is(true));
		assertThat(first.size(), is(3));
		assertThat(response.getBody(), not(containsString("hibernateLazyInitializer")));
		assertThat(response.getBody(), not(containsString("com.salesmanager.core.model")));
	}

	@Test
	public void getCurrency_returnsReadableCurrencyShape() throws Exception {
		HttpEntity<String> httpEntity = new HttpEntity<>(getHeader());

		ResponseEntity<String> response = testRestTemplate.exchange(
				"/api/v1/currency",
				HttpMethod.GET,
				httpEntity,
				String.class);

		assertThat(response.getStatusCode(), notNullValue());
		assertThat(response.getStatusCode(), is(HttpStatus.OK));
		assertNotNull(response.getBody());

		JsonNode currencies = objectMapper.readTree(response.getBody());
		assertThat(currencies.size(), greaterThanOrEqualTo(1));

		JsonNode first = currencies.get(0);
		assertThat(first.has("id"), is(true));
		assertThat(first.has("code"), is(true));
		assertThat(first.has("name"), is(true));
		assertThat(first.has("symbol"), is(true));
		assertThat(first.has("supported"), is(true));
		assertThat(response.getBody(), not(containsString("hibernateLazyInitializer")));
	}
}
