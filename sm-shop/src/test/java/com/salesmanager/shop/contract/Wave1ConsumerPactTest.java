package com.salesmanager.shop.contract;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonArray;
import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactDirectory;

/**
 * Wave 1 Pact consumer for strangler HTTP adapters (reference + tax).
 * Writes contracts under repo {@code pacts/} for provider verification.
 */
@ExtendWith(PactConsumerTestExt.class)
@PactDirectory("../pacts")
@PactTestFor(pactVersion = PactSpecVersion.V3)
class Wave1ConsumerPactTest {

	private static final String CONSUMER = "sm-shop-wave1";
	private static final String AUTH = "Bearer test-token";

	private final RestTemplate restTemplate = new RestTemplate();

	@Pact(consumer = CONSUMER, provider = "reference-service")
	RequestResponsePact referenceServicePact(PactDslWithProvider builder) {
		return builder
				.given("languages exist")
				.uponReceiving("get languages")
					.path("/api/v1/languages")
					.method("GET")
				.willRespondWith()
					.status(200)
					.headers(jsonHeaders())
					.body(newJsonArray(a -> a.object(o -> {
						o.numberType("id", 1);
						o.stringType("code", "en");
						o.numberType("sortOrder", 0);
					})).build())

				.given("countries exist")
				.uponReceiving("get countries")
					.path("/api/v1/country")
					.method("GET")
					.query("lang=en&store=DEFAULT")
				.willRespondWith()
					.status(200)
					.headers(jsonHeaders())
					.body(newJsonArray(a -> a.object(o -> {
						o.numberType("id", 1);
						o.stringType("code", "BR");
						o.stringType("name", "Brazil");
					})).build())

				.given("zones for country BR exist")
				.uponReceiving("get zones by country code")
					.path("/api/v1/zones")
					.method("GET")
					.query("code=BR&lang=en&store=DEFAULT")
				.willRespondWith()
					.status(200)
					.headers(jsonHeaders())
					.body(newJsonArray(a -> a.object(o -> {
						o.numberType("id", 10);
						o.stringType("code", "SP");
						o.stringType("name", "Sao Paulo");
						o.stringType("countryCode", "BR");
					})).build())

				.given("currencies exist")
				.uponReceiving("get currencies")
					.path("/api/v1/currency")
					.method("GET")
				.willRespondWith()
					.status(200)
					.headers(jsonHeaders())
					.body(newJsonArray(a -> a.object(o -> {
						o.numberType("id", 1);
						o.stringType("code", "BRL");
						o.stringType("name", "Brazilian Real");
						o.stringType("symbol", "R$");
						o.booleanType("supported", true);
					})).build())

				.given("measures available")
				.uponReceiving("get measures")
					.path("/api/v1/measures")
					.method("GET")
				.willRespondWith()
					.status(200)
					.headers(jsonHeaders())
					.body(newJsonBody(o -> {
						o.array("measures", a -> {
							a.stringValue("CM");
							a.stringValue("IN");
						});
						o.array("weights", a -> {
							a.stringValue("LB");
							a.stringValue("KG");
						});
					}).build())
				.toPact();
	}

	@Pact(consumer = CONSUMER, provider = "tax-service")
	RequestResponsePact taxServicePact(PactDslWithProvider builder) {
		return builder
				.given("store DEFAULT accepts tax class create")
				.uponReceiving("create tax class")
					.path("/api/v1/private/tax/class")
					.method("POST")
					.headers(authJsonHeaders())
					.query("store=DEFAULT&lang=en")
					.body(newJsonBody(o -> {
						o.stringType("code", "T1");
						o.stringType("name", "Standard");
					}).build())
				.willRespondWith()
					.status(200)
					.headers(jsonHeaders())
					.body(newJsonBody(o -> o.numberType("id", 5)).build())

				.given("tax classes exist for store DEFAULT")
				.uponReceiving("list tax classes")
					.path("/api/v1/private/tax/class")
					.method("GET")
					.headers(authHeaders())
					.query("store=DEFAULT&lang=en")
				.willRespondWith()
					.status(200)
					.headers(jsonHeaders())
					.body(newJsonBody(o -> {
						o.numberType("totalPages", 1);
						o.numberType("number", 0);
						o.numberType("recordsTotal", 1);
						o.numberType("recordsFiltered", 1);
						o.array("items", a -> a.object(item -> {
							item.numberType("id", 5);
							item.stringType("code", "T1");
							item.stringType("name", "Standard");
							item.stringType("store", "DEFAULT");
						}));
					}).build())

				.given("tax class T1 exists for store DEFAULT")
				.uponReceiving("get tax class by code")
					.path("/api/v1/private/tax/class/T1")
					.method("GET")
					.headers(authHeaders())
					.query("store=DEFAULT&lang=en")
				.willRespondWith()
					.status(200)
					.headers(jsonHeaders())
					.body(newJsonBody(o -> {
						o.numberType("id", 5);
						o.stringType("code", "T1");
						o.stringType("name", "Standard");
						o.stringType("store", "DEFAULT");
					}).build())

				.given("tax class code T1 uniqueness check")
				.uponReceiving("tax class unique")
					.path("/api/v1/private/tax/class/unique")
					.method("GET")
					.headers(authHeaders())
					.query("code=T1&store=DEFAULT&lang=en")
				.willRespondWith()
					.status(200)
					.headers(jsonHeaders())
					.body(newJsonBody(o -> o.booleanType("exists", true)).build())

				.given("tax class id 5 exists for store DEFAULT")
				.uponReceiving("update tax class")
					.path("/api/v1/private/tax/class/5")
					.method("PUT")
					.headers(authJsonHeaders())
					.query("store=DEFAULT&lang=en")
					.body(newJsonBody(o -> {
						o.stringType("code", "T1");
						o.stringType("name", "Updated");
					}).build())
				.willRespondWith()
					.status(200)

				.given("tax class id 5 deletable for store DEFAULT")
				.uponReceiving("delete tax class")
					.path("/api/v1/private/tax/class/5")
					.method("DELETE")
					.headers(authHeaders())
					.query("store=DEFAULT&lang=en")
				.willRespondWith()
					.status(200)

				.given("store DEFAULT accepts tax rate create")
				.uponReceiving("create tax rate")
					.path("/api/v1/private/tax/rate")
					.method("POST")
					.headers(authJsonHeaders())
					.query("store=DEFAULT&lang=en")
					.body(newJsonBody(o -> {
						o.stringType("code", "TR1");
						o.numberType("priority", 0);
						o.numberType("rate", 10.0);
						o.stringType("country", "BR");
						o.stringType("taxClass", "T1");
					}).build())
				.willRespondWith()
					.status(200)
					.headers(jsonHeaders())
					.body(newJsonBody(o -> o.numberType("id", 9)).build())

				.given("tax rates exist for store DEFAULT")
				.uponReceiving("list tax rates")
					.path("/api/v1/private/tax/rates")
					.method("GET")
					.headers(authHeaders())
					.query("store=DEFAULT&lang=en")
				.willRespondWith()
					.status(200)
					.headers(jsonHeaders())
					.body(newJsonBody(o -> {
						o.numberType("totalPages", 1);
						o.numberType("recordsTotal", 1);
						o.array("items", a -> a.object(item -> {
							item.numberType("id", 9);
							item.stringType("code", "TR1");
							item.stringType("rate", "10.00");
							item.stringType("country", "BR");
							item.stringType("store", "DEFAULT");
						}));
					}).build())

				.given("tax rate id 9 exists for store DEFAULT")
				.uponReceiving("get tax rate by id")
					.path("/api/v1/private/tax/rate/9")
					.method("GET")
					.headers(authHeaders())
					.query("store=DEFAULT&lang=en")
				.willRespondWith()
					.status(200)
					.headers(jsonHeaders())
					.body(newJsonBody(o -> {
						o.numberType("id", 9);
						o.stringType("code", "TR1");
						o.stringType("rate", "10.00");
						o.stringType("country", "BR");
						o.stringType("store", "DEFAULT");
					}).build())

				.given("tax rate code TR1 uniqueness check")
				.uponReceiving("tax rate unique")
					.path("/api/v1/private/tax/rate/unique")
					.method("GET")
					.headers(authHeaders())
					.query("code=TR1&store=DEFAULT&lang=en")
				.willRespondWith()
					.status(200)
					.headers(jsonHeaders())
					.body(newJsonBody(o -> o.booleanType("exists", false)).build())

				.given("tax rate id 9 exists for store DEFAULT")
				.uponReceiving("update tax rate")
					.path("/api/v1/private/tax/rate/9")
					.method("PUT")
					.headers(authJsonHeaders())
					.query("store=DEFAULT&lang=en")
					.body(newJsonBody(o -> {
						o.stringType("code", "TR1");
						o.numberType("priority", 1);
						o.numberType("rate", 12.0);
						o.stringType("country", "BR");
						o.stringType("taxClass", "T1");
					}).build())
				.willRespondWith()
					.status(200)

				.given("tax rate id 9 deletable for store DEFAULT")
				.uponReceiving("delete tax rate")
					.path("/api/v1/private/tax/rate/9")
					.method("DELETE")
					.headers(authHeaders())
					.query("store=DEFAULT&lang=en")
				.willRespondWith()
					.status(200)
				.toPact();
	}

	@Test
	@PactTestFor(providerName = "reference-service", pactMethod = "referenceServicePact")
	void referenceEndpoints_matchStranglerExpectations(MockServer mockServer) {
		String base = mockServer.getUrl();

		ResponseEntity<List<Map<String, Object>>> languages = restTemplate.exchange(
				base + "/api/v1/languages",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<Map<String, Object>>>() {});
		assertThat(languages.getStatusCodeValue()).isEqualTo(200);
		assertThat(languages.getBody()).isNotEmpty();
		assertThat(languages.getBody().get(0)).containsKeys("id", "code", "sortOrder");

		ResponseEntity<List<Map<String, Object>>> countries = restTemplate.exchange(
				base + "/api/v1/country?lang=en&store=DEFAULT",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<Map<String, Object>>>() {});
		assertThat(countries.getStatusCodeValue()).isEqualTo(200);
		assertThat(countries.getBody().get(0)).containsKeys("id", "code", "name");

		ResponseEntity<List<Map<String, Object>>> zones = restTemplate.exchange(
				base + "/api/v1/zones?code=BR&lang=en&store=DEFAULT",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<Map<String, Object>>>() {});
		assertThat(zones.getStatusCodeValue()).isEqualTo(200);
		assertThat(zones.getBody().get(0)).containsKeys("id", "code", "name", "countryCode");

		ResponseEntity<List<Map<String, Object>>> currencies = restTemplate.exchange(
				base + "/api/v1/currency",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<Map<String, Object>>>() {});
		assertThat(currencies.getStatusCodeValue()).isEqualTo(200);
		assertThat(currencies.getBody().get(0)).containsKeys("id", "code", "name", "symbol", "supported");

		ResponseEntity<Map<String, Object>> measures = restTemplate.exchange(
				base + "/api/v1/measures",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<Map<String, Object>>() {});
		assertThat(measures.getStatusCodeValue()).isEqualTo(200);
		assertThat(measures.getBody()).containsKeys("measures", "weights");
	}

	@Test
	@PactTestFor(providerName = "tax-service", pactMethod = "taxServicePact")
	void taxEndpoints_matchStranglerExpectations(MockServer mockServer) {
		String base = mockServer.getUrl();
		HttpHeaders auth = new HttpHeaders();
		auth.set(HttpHeaders.AUTHORIZATION, AUTH);
		HttpHeaders authJson = new HttpHeaders();
		authJson.set(HttpHeaders.AUTHORIZATION, AUTH);
		authJson.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<Map<String, Object>> createdClass = restTemplate.exchange(
				base + "/api/v1/private/tax/class?store=DEFAULT&lang=en",
				HttpMethod.POST,
				new HttpEntity<>("{\"code\":\"T1\",\"name\":\"Standard\"}", authJson),
				new ParameterizedTypeReference<Map<String, Object>>() {});
		assertThat(createdClass.getStatusCodeValue()).isEqualTo(200);
		assertThat(createdClass.getBody()).containsKey("id");

		ResponseEntity<Map<String, Object>> classList = restTemplate.exchange(
				base + "/api/v1/private/tax/class?store=DEFAULT&lang=en",
				HttpMethod.GET,
				new HttpEntity<>(auth),
				new ParameterizedTypeReference<Map<String, Object>>() {});
		assertThat(classList.getStatusCodeValue()).isEqualTo(200);
		assertThat(classList.getBody()).containsKey("items");

		ResponseEntity<Map<String, Object>> taxClass = restTemplate.exchange(
				base + "/api/v1/private/tax/class/T1?store=DEFAULT&lang=en",
				HttpMethod.GET,
				new HttpEntity<>(auth),
				new ParameterizedTypeReference<Map<String, Object>>() {});
		assertThat(taxClass.getStatusCodeValue()).isEqualTo(200);
		assertThat(taxClass.getBody()).containsKeys("id", "code", "name", "store");

		ResponseEntity<Map<String, Object>> classUnique = restTemplate.exchange(
				base + "/api/v1/private/tax/class/unique?code=T1&store=DEFAULT&lang=en",
				HttpMethod.GET,
				new HttpEntity<>(auth),
				new ParameterizedTypeReference<Map<String, Object>>() {});
		assertThat(classUnique.getStatusCodeValue()).isEqualTo(200);
		assertThat(classUnique.getBody()).containsKey("exists");

		assertThat(restTemplate.exchange(
				base + "/api/v1/private/tax/class/5?store=DEFAULT&lang=en",
				HttpMethod.PUT,
				new HttpEntity<>("{\"code\":\"T1\",\"name\":\"Updated\"}", authJson),
				Void.class).getStatusCodeValue()).isEqualTo(200);

		assertThat(restTemplate.exchange(
				base + "/api/v1/private/tax/class/5?store=DEFAULT&lang=en",
				HttpMethod.DELETE,
				new HttpEntity<>(auth),
				Void.class).getStatusCodeValue()).isEqualTo(200);

		ResponseEntity<Map<String, Object>> createdRate = restTemplate.exchange(
				base + "/api/v1/private/tax/rate?store=DEFAULT&lang=en",
				HttpMethod.POST,
				new HttpEntity<>(
						"{\"code\":\"TR1\",\"priority\":0,\"rate\":10.0,\"country\":\"BR\",\"taxClass\":\"T1\"}",
						authJson),
				new ParameterizedTypeReference<Map<String, Object>>() {});
		assertThat(createdRate.getStatusCodeValue()).isEqualTo(200);
		assertThat(createdRate.getBody()).containsKey("id");

		ResponseEntity<Map<String, Object>> rateList = restTemplate.exchange(
				base + "/api/v1/private/tax/rates?store=DEFAULT&lang=en",
				HttpMethod.GET,
				new HttpEntity<>(auth),
				new ParameterizedTypeReference<Map<String, Object>>() {});
		assertThat(rateList.getStatusCodeValue()).isEqualTo(200);
		assertThat(rateList.getBody()).containsKey("items");

		ResponseEntity<Map<String, Object>> taxRate = restTemplate.exchange(
				base + "/api/v1/private/tax/rate/9?store=DEFAULT&lang=en",
				HttpMethod.GET,
				new HttpEntity<>(auth),
				new ParameterizedTypeReference<Map<String, Object>>() {});
		assertThat(taxRate.getStatusCodeValue()).isEqualTo(200);
		assertThat(taxRate.getBody()).containsKeys("id", "code", "rate", "country", "store");

		ResponseEntity<Map<String, Object>> rateUnique = restTemplate.exchange(
				base + "/api/v1/private/tax/rate/unique?code=TR1&store=DEFAULT&lang=en",
				HttpMethod.GET,
				new HttpEntity<>(auth),
				new ParameterizedTypeReference<Map<String, Object>>() {});
		assertThat(rateUnique.getStatusCodeValue()).isEqualTo(200);
		assertThat(rateUnique.getBody()).containsKey("exists");

		assertThat(restTemplate.exchange(
				base + "/api/v1/private/tax/rate/9?store=DEFAULT&lang=en",
				HttpMethod.PUT,
				new HttpEntity<>(
						"{\"code\":\"TR1\",\"priority\":1,\"rate\":12.0,\"country\":\"BR\",\"taxClass\":\"T1\"}",
						authJson),
				Void.class).getStatusCodeValue()).isEqualTo(200);

		assertThat(restTemplate.exchange(
				base + "/api/v1/private/tax/rate/9?store=DEFAULT&lang=en",
				HttpMethod.DELETE,
				new HttpEntity<>(auth),
				Void.class).getStatusCodeValue()).isEqualTo(200);
	}

	private static Map<String, String> jsonHeaders() {
		Map<String, String> headers = new HashMap<>();
		headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		return headers;
	}

	private static Map<String, String> authHeaders() {
		Map<String, String> headers = new HashMap<>();
		headers.put(HttpHeaders.AUTHORIZATION, AUTH);
		return headers;
	}

	private static Map<String, String> authJsonHeaders() {
		Map<String, String> headers = authHeaders();
		headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		return headers;
	}
}
