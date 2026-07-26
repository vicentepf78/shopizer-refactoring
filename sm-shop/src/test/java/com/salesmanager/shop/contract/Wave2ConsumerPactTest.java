package com.salesmanager.shop.contract;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonArray;
import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
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
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import com.salesmanager.contracts.merchant.MerchantStoreSnapshot;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactDirectory;

/**
 * Wave 2 Pact consumer for strangler HTTP adapters (content + search + merchant).
 * Writes contracts under repo {@code pacts/} for provider verification.
 */
@ExtendWith(PactConsumerTestExt.class)
@PactDirectory("../pacts")
@PactTestFor(pactVersion = PactSpecVersion.V3)
class Wave2ConsumerPactTest {

	private static final String CONSUMER = "sm-shop-wave2";
	private static final String AUTH = "Bearer test-token";
	private static final String INTERNAL_TOKEN = "test-internal-token";
	private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

	private final RestTemplate restTemplate = new RestTemplate();

	@Pact(consumer = CONSUMER, provider = "content-service")
	RequestResponsePact contentServicePact(PactDslWithProvider builder) {
		return builder
				.given("content pages exist for store DEFAULT")
				.uponReceiving("list content pages")
					.path("/api/v1/content/pages")
					.method("GET")
					.query("store=DEFAULT&lang=en&page=0&count=10")
				.willRespondWith()
					.status(200)
					.headers(jsonHeaders())
					.body(newJsonBody(o -> {
						o.numberType("totalPages", 1);
						o.numberType("number", 0);
						o.numberType("recordsTotal", 1);
						o.numberType("recordsFiltered", 1);
						o.array("items", a -> a.object(item -> {
							item.numberType("id", 1);
							item.stringType("code", "home");
							item.booleanType("visible", true);
							item.stringType("contentType", "PAGE");
						}));
					}).build())

				.given("content page home exists for store DEFAULT")
				.uponReceiving("get content page by code")
					.path("/api/v1/content/pages/home")
					.method("GET")
					.query("store=DEFAULT&lang=en")
				.willRespondWith()
					.status(200)
					.headers(jsonHeaders())
					.body(newJsonBody(o -> {
						o.numberType("id", 1);
						o.stringType("code", "home");
						o.stringType("path", "/home");
					}).build())

				.given("content boxes exist for store DEFAULT")
				.uponReceiving("list content boxes")
					.path("/api/v1/content/boxes")
					.method("GET")
					.query("store=DEFAULT&lang=en&page=0&count=10")
				.willRespondWith()
					.status(200)
					.headers(jsonHeaders())
					.body(newJsonBody(o -> {
						o.numberType("totalPages", 1);
						o.array("items", a -> a.object(item -> {
							item.numberType("id", 2);
							item.stringType("code", "sidebar");
							item.stringType("contentType", "BOX");
						}));
					}).build())

				.given("content box sidebar exists for store DEFAULT")
				.uponReceiving("get content box by code")
					.path("/api/v1/content/boxes/sidebar")
					.method("GET")
					.query("store=DEFAULT&lang=en")
				.willRespondWith()
					.status(200)
					.headers(jsonHeaders())
					.body(newJsonBody(o -> {
						o.numberType("id", 2);
						o.stringType("code", "sidebar");
						o.stringType("contentType", "BOX");
					}).build())

				.given("store DEFAULT accepts content page create")
				.uponReceiving("create content page")
					.path("/api/v1/private/content/page")
					.method("POST")
					.headers(authJsonHeaders())
					.query("store=DEFAULT&lang=en")
					.body(newJsonBody(o -> {
						o.stringType("code", "about");
						o.booleanType("visible", true);
						o.stringType("contentType", "PAGE");
					}).build())
				.willRespondWith()
					.status(201)
					.headers(jsonHeaders())
					.body(newJsonBody(o -> o.numberType("id", 3)).build())

				.given("store DEFAULT accepts content box create")
				.uponReceiving("create content box")
					.path("/api/v1/private/content/box")
					.method("POST")
					.headers(authJsonHeaders())
					.query("store=DEFAULT&lang=en")
					.body(newJsonBody(o -> {
						o.stringType("code", "promo");
						o.booleanType("visible", true);
					}).build())
				.willRespondWith()
					.status(201)
					.headers(jsonHeaders())
					.body(newJsonBody(o -> o.numberType("id", 4)).build())

				.given("content images folder available for store DEFAULT")
				.uponReceiving("get content images folder")
					.path("/api/v1/content/images")
					.method("GET")
					.query("store=DEFAULT&lang=en")
				.willRespondWith()
					.status(200)
					.headers(jsonHeaders())
					.body(newJsonBody(o -> {
						o.stringType("path", "/");
						o.array("content", a -> {});
					}).build())
				.toPact();
	}

	@Pact(consumer = CONSUMER, provider = "search-service")
	RequestResponsePact searchQueryPact(PactDslWithProvider builder) {
		return builder
				.given("search results exist for store DEFAULT")
				.uponReceiving("product search")
					.path("/api/v1/search")
					.method("POST")
					.headers(jsonHeaders())
					.query("store=DEFAULT&lang=en")
					.body(newJsonBody(o -> o.stringType("query", "phone")).build())
				.willRespondWith()
					.status(200)
					.headers(jsonHeaders())
					.body(newJsonArray(a -> a.object(o -> {
						o.numberType("id", 1);
						o.stringType("name", "Phone");
						o.stringType("description", "Smart phone");
					})).build())

				.given("autocomplete suggestions exist for store DEFAULT")
				.uponReceiving("search autocomplete")
					.path("/api/v1/search/autocomplete")
					.method("POST")
					.headers(jsonHeaders())
					.query("store=DEFAULT&lang=en")
					.body(newJsonBody(o -> o.stringType("query", "ph")).build())
				.willRespondWith()
					.status(200)
					.headers(jsonHeaders())
					.body(newJsonBody(o -> o.array("values", a -> {
						a.stringValue("phone");
						a.stringValue("phones");
					})).build())
				.toPact();
	}

	@Pact(consumer = CONSUMER, provider = "search-service")
	RequestResponsePact searchIndexValidPact(PactDslWithProvider builder) {
		return builder
				.given("index accepts schema version 1")
				.uponReceiving("index product document")
					.path("/internal/v1/index")
					.method("POST")
					.headers(internalJsonHeaders())
					.body(newJsonBody(o -> {
						o.numberType("schemaVersion", 1);
						o.numberType("id", 99);
						o.stringType("store", "default");
						o.stringType("language", "en");
						o.stringType("name", "Sample");
					}).build())
				.willRespondWith()
					.status(204)
				.toPact();
	}

	@Pact(consumer = CONSUMER, provider = "search-service")
	RequestResponsePact searchIndexSchemaVersionTwoPact(PactDslWithProvider builder) {
		return builder
				.given("index accepts schema version 2")
				.uponReceiving("index product document schema version 2")
					.path("/internal/v1/index")
					.method("POST")
					.headers(internalJsonHeaders())
					.body(newJsonBody(o -> {
						o.numberType("schemaVersion", 2);
						o.numberType("id", 99);
						o.stringType("store", "default");
						o.stringType("language", "en");
						o.stringType("name", "Sample");
					}).build())
				.willRespondWith()
					.status(204)
				.toPact();
	}

	@Pact(consumer = CONSUMER, provider = "search-service")
	RequestResponsePact searchIndexInvalidPact(PactDslWithProvider builder) {
		return builder
				.given("index rejects unsupported schema version")
				.uponReceiving("index rejects bad schema version")
					.path("/internal/v1/index")
					.method("POST")
					.headers(internalJsonHeaders())
					.body(newJsonBody(o -> {
						o.numberType("schemaVersion", 3);
						o.numberType("id", 99);
					}).build())
				.willRespondWith()
					.status(422)
					.headers(jsonHeaders())
					.body(newJsonBody(o -> {
						o.stringType("error", "Unsupported schemaVersion: 3");
						o.numberType("schemaVersion", 3);
					}).build())
				.toPact();
	}

	@Pact(consumer = CONSUMER, provider = "merchant-service")
	RequestResponsePact merchantServicePact(PactDslWithProvider builder) {
		return builder
				.given("store DEFAULT exists")
				.uponReceiving("get public store")
					.path("/api/v1/store/DEFAULT")
					.method("GET")
					.query("lang=en")
				.willRespondWith()
					.status(200)
					.headers(jsonHeaders())
					.body(newJsonBody(o -> {
						o.numberType("id", 1);
						o.stringType("code", "DEFAULT");
						o.stringType("name", "Default store");
					}).build())

				.given("merchant config exists for store DEFAULT")
				.uponReceiving("get merchant config")
					.path("/api/v1/config")
					.method("GET")
					.query("store=DEFAULT&lang=en")
				.willRespondWith()
					.status(200)
					.headers(jsonHeaders())
					.body(newJsonBody(o -> {
						o.booleanType("displaySearchBox", true);
						o.booleanType("displayShipping", true);
						o.stringType("facebook", "https://facebook.test");
					}).build())

				.given("store snapshot DEFAULT exists")
				.uponReceiving("get store snapshot")
					.path("/internal/v1/store/DEFAULT")
					.method("GET")
					.query("lang=en")
				.willRespondWith()
					.status(200)
					.headers(jsonHeaders())
					.body(newJsonBody(o -> {
						o.numberType("id", 1);
						o.stringType("code", "DEFAULT");
						o.stringType("name", "Default store");
						o.stringType("defaultLanguage", "en");
					}).build())

				.given("private store DEFAULT exists")
				.uponReceiving("get private store")
					.path("/api/v1/private/store/DEFAULT")
					.method("GET")
					.headers(authHeaders())
					.query("lang=en")
				.willRespondWith()
					.status(200)
					.headers(jsonHeaders())
					.body(newJsonBody(o -> {
						o.numberType("id", 1);
						o.stringType("code", "DEFAULT");
						o.stringType("name", "Default store");
					}).build())

				.given("store create accepted")
				.uponReceiving("create merchant store")
					.path("/api/v1/private/store")
					.method("POST")
					.headers(authJsonHeaders())
					.body(newJsonBody(o -> {
						o.stringType("code", "NEWSTORE");
						o.stringType("name", "New Store");
						o.stringType("email", "new@store.test");
						o.stringType("phone", "555-0100");
					}).build())
				.willRespondWith()
					.status(200)
				.toPact();
	}

	@Test
	@PactTestFor(providerName = "content-service", pactMethod = "contentServicePact")
	void contentEndpoints_matchStranglerExpectations(MockServer mockServer) {
		String base = mockServer.getUrl();
		HttpHeaders auth = authEntityHeaders();
		HttpHeaders authJson = authJsonEntityHeaders();

		ResponseEntity<Map<String, Object>> pages = restTemplate.exchange(
				base + "/api/v1/content/pages?store=DEFAULT&lang=en&page=0&count=10",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<Map<String, Object>>() {});
		assertThat(pages.getStatusCodeValue()).isEqualTo(200);
		assertThat(pages.getBody()).containsKey("items");

		ResponseEntity<Map<String, Object>> page = restTemplate.exchange(
				base + "/api/v1/content/pages/home?store=DEFAULT&lang=en",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<Map<String, Object>>() {});
		assertThat(page.getStatusCodeValue()).isEqualTo(200);
		assertThat(page.getBody()).containsKeys("code", "path");

		ResponseEntity<Map<String, Object>> boxes = restTemplate.exchange(
				base + "/api/v1/content/boxes?store=DEFAULT&lang=en&page=0&count=10",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<Map<String, Object>>() {});
		assertThat(boxes.getStatusCodeValue()).isEqualTo(200);
		assertThat(boxes.getBody()).containsKey("items");

		ResponseEntity<Map<String, Object>> box = restTemplate.exchange(
				base + "/api/v1/content/boxes/sidebar?store=DEFAULT&lang=en",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<Map<String, Object>>() {});
		assertThat(box.getStatusCodeValue()).isEqualTo(200);
		assertThat(box.getBody()).containsKeys("code", "contentType");

		ResponseEntity<Map<String, Object>> createdPage = restTemplate.exchange(
				base + "/api/v1/private/content/page?store=DEFAULT&lang=en",
				HttpMethod.POST,
				new HttpEntity<>("{\"code\":\"about\",\"visible\":true,\"contentType\":\"PAGE\"}", authJson),
				new ParameterizedTypeReference<Map<String, Object>>() {});
		assertThat(createdPage.getStatusCodeValue()).isEqualTo(201);
		assertThat(createdPage.getBody()).containsKey("id");

		ResponseEntity<Map<String, Object>> createdBox = restTemplate.exchange(
				base + "/api/v1/private/content/box?store=DEFAULT&lang=en",
				HttpMethod.POST,
				new HttpEntity<>("{\"code\":\"promo\",\"visible\":true}", authJson),
				new ParameterizedTypeReference<Map<String, Object>>() {});
		assertThat(createdBox.getStatusCodeValue()).isEqualTo(201);
		assertThat(createdBox.getBody()).containsKey("id");

		ResponseEntity<Map<String, Object>> folder = restTemplate.exchange(
				base + "/api/v1/content/images?store=DEFAULT&lang=en",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<Map<String, Object>>() {});
		assertThat(folder.getStatusCodeValue()).isEqualTo(200);
		assertThat(folder.getBody()).containsKeys("path", "content");
	}

	@Test
	@PactTestFor(providerName = "search-service", pactMethod = "searchQueryPact")
	void searchQueryEndpoints_matchStranglerExpectations(MockServer mockServer) {
		String base = mockServer.getUrl();
		HttpHeaders json = jsonEntityHeaders();

		ResponseEntity<List<Map<String, Object>>> search = restTemplate.exchange(
				base + "/api/v1/search?store=DEFAULT&lang=en",
				HttpMethod.POST,
				new HttpEntity<>("{\"query\":\"phone\"}", json),
				new ParameterizedTypeReference<List<Map<String, Object>>>() {});
		assertThat(search.getStatusCodeValue()).isEqualTo(200);
		assertThat(search.getBody()).isNotEmpty();
		assertThat(search.getBody().get(0)).containsKeys("id", "name");

		ResponseEntity<Map<String, Object>> autocomplete = restTemplate.exchange(
				base + "/api/v1/search/autocomplete?store=DEFAULT&lang=en",
				HttpMethod.POST,
				new HttpEntity<>("{\"query\":\"ph\"}", json),
				new ParameterizedTypeReference<Map<String, Object>>() {});
		assertThat(autocomplete.getStatusCodeValue()).isEqualTo(200);
		assertThat(autocomplete.getBody()).containsKey("values");
	}

	@Test
	@PactTestFor(providerName = "search-service", pactMethod = "searchIndexValidPact")
	void searchIndexValid_matchStranglerExpectations(MockServer mockServer) {
		assertThat(restTemplate.exchange(
				mockServer.getUrl() + "/internal/v1/index",
				HttpMethod.POST,
				new HttpEntity<>(
						"{\"schemaVersion\":1,\"id\":99,\"store\":\"default\",\"language\":\"en\",\"name\":\"Sample\"}",
						internalJsonEntityHeaders()),
				Void.class).getStatusCodeValue()).isEqualTo(204);
	}

	@Test
	@PactTestFor(providerName = "search-service", pactMethod = "searchIndexSchemaVersionTwoPact")
	void searchIndexSchemaVersionTwo_matchStranglerExpectations(MockServer mockServer) {
		assertThat(restTemplate.exchange(
				mockServer.getUrl() + "/internal/v1/index",
				HttpMethod.POST,
				new HttpEntity<>(
						"{\"schemaVersion\":2,\"id\":99,\"store\":\"default\",\"language\":\"en\",\"name\":\"Sample\"}",
						internalJsonEntityHeaders()),
				Void.class).getStatusCodeValue()).isEqualTo(204);
	}

	@Test
	@PactTestFor(providerName = "search-service", pactMethod = "searchIndexInvalidPact")
	void searchIndexInvalid_matchStranglerExpectations(MockServer mockServer) {
		ResponseEntity<Map<String, Object>> rejected = tolerantRestTemplate().exchange(
				mockServer.getUrl() + "/internal/v1/index",
				HttpMethod.POST,
				new HttpEntity<>("{\"schemaVersion\":3,\"id\":99}", internalJsonEntityHeaders()),
				new ParameterizedTypeReference<Map<String, Object>>() {});
		assertThat(rejected.getStatusCodeValue()).isEqualTo(422);
		assertThat(rejected.getBody()).containsKeys("error", "schemaVersion");
	}

	@Test
	@PactTestFor(providerName = "merchant-service", pactMethod = "merchantServicePact")
	void merchantEndpoints_matchStranglerExpectations(MockServer mockServer) {
		String base = mockServer.getUrl();
		HttpHeaders auth = authEntityHeaders();
		HttpHeaders authJson = authJsonEntityHeaders();

		ResponseEntity<Map<String, Object>> store = restTemplate.exchange(
				base + "/api/v1/store/DEFAULT?lang=en",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<Map<String, Object>>() {});
		assertThat(store.getStatusCodeValue()).isEqualTo(200);
		assertThat(store.getBody()).containsKeys("code", "name");

		ResponseEntity<Map<String, Object>> config = restTemplate.exchange(
				base + "/api/v1/config?store=DEFAULT&lang=en",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<Map<String, Object>>() {});
		assertThat(config.getStatusCodeValue()).isEqualTo(200);
		assertThat(config.getBody()).containsKeys("displaySearchBox", "displayShipping");

		ResponseEntity<MerchantStoreSnapshot> snapshot = restTemplate.exchange(
				base + "/internal/v1/store/DEFAULT?lang=en",
				HttpMethod.GET,
				null,
				MerchantStoreSnapshot.class);
		assertThat(snapshot.getStatusCodeValue()).isEqualTo(200);
		assertThat(snapshot.getBody()).isNotNull();
		assertThat(snapshot.getBody().getCode()).isEqualTo("DEFAULT");

		ResponseEntity<Map<String, Object>> privateStore = restTemplate.exchange(
				base + "/api/v1/private/store/DEFAULT?lang=en",
				HttpMethod.GET,
				new HttpEntity<>(auth),
				new ParameterizedTypeReference<Map<String, Object>>() {});
		assertThat(privateStore.getStatusCodeValue()).isEqualTo(200);
		assertThat(privateStore.getBody()).containsKeys("code", "name");

		assertThat(restTemplate.exchange(
				base + "/api/v1/private/store",
				HttpMethod.POST,
				new HttpEntity<>(
						"{\"code\":\"NEWSTORE\",\"name\":\"New Store\",\"email\":\"new@store.test\",\"phone\":\"555-0100\"}",
						authJson),
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

	private static Map<String, String> internalJsonHeaders() {
		Map<String, String> headers = jsonHeaders();
		headers.put(INTERNAL_TOKEN_HEADER, INTERNAL_TOKEN);
		return headers;
	}

	private static HttpHeaders jsonEntityHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}

	private static HttpHeaders authEntityHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, AUTH);
		return headers;
	}

	private static HttpHeaders authJsonEntityHeaders() {
		HttpHeaders headers = authEntityHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}

	private static HttpHeaders internalJsonEntityHeaders() {
		HttpHeaders headers = jsonEntityHeaders();
		headers.set(INTERNAL_TOKEN_HEADER, INTERNAL_TOKEN);
		return headers;
	}

	private static RestTemplate tolerantRestTemplate() {
		RestTemplate client = new RestTemplate();
		client.setErrorHandler(new DefaultResponseErrorHandler() {
			@Override
			public boolean hasError(ClientHttpResponse response) throws IOException {
				return false;
			}
		});
		return client;
	}
}
