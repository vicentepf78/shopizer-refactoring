package com.salesmanager.shop.strangler.search;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.salesmanager.contracts.search.ProductIndexPayload;
import com.salesmanager.core.business.services.search.index.SearchIndexProducer;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.shop.strangler.config.Wave2Properties;
import com.salesmanager.shop.strangler.search.SearchIndexClientRestTemplateImpl;

@ExtendWith(MockitoExtension.class)
class SearchIndexProducerHttpTest {

	private static final String BASE_URL = "http://search-test:8084";
	private static final String TOKEN = "test-token";

	@Mock
	private com.salesmanager.core.business.services.search.index.ProductIndexPayloadBuilder payloadBuilder;

	private RestTemplate restTemplate;
	private MockRestServiceServer server;
	private SearchIndexProducer producer;

	@BeforeEach
	void setUp() throws Exception {
		restTemplate = new RestTemplate();
		server = MockRestServiceServer.createServer(restTemplate);

		Wave2Properties properties = new Wave2Properties();
		properties.getSearchService().setBaseUrl(BASE_URL);
		properties.getSearchService().setInternalToken(TOKEN);

		SearchIndexClientRestTemplateImpl client = new SearchIndexClientRestTemplateImpl(restTemplate, properties);
		producer = new SearchIndexProducerHttp(client, payloadBuilder);
	}

	@Test
	void index_emitsDeleteThenBulkPostWithInternalToken() throws Exception {
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");

		Product product = new Product();
		product.setId(99L);

		ProductIndexPayload payload = new ProductIndexPayload();
		payload.setId(99L);
		payload.setStore("default");
		payload.setLanguage("en");
		payload.setSchemaVersion(1);
		when(payloadBuilder.buildAll(store, product)).thenReturn(Collections.singletonList(payload));

		server.expect(requestTo(BASE_URL + "/internal/v1/index/99?store=default&languages=en"))
				.andExpect(method(HttpMethod.DELETE))
				.andExpect(header(SearchIndexClientRestTemplateImpl.INTERNAL_TOKEN_HEADER, TOKEN))
				.andRespond(withSuccess());

		server.expect(requestTo(BASE_URL + "/internal/v1/index/bulk"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header(SearchIndexClientRestTemplateImpl.INTERNAL_TOKEN_HEADER, TOKEN))
				.andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

		producer.index(store, product);

		server.verify();
	}

	@Test
	void deleteDocument_emitsDeleteWithInternalToken() {
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");

		server.expect(requestTo(BASE_URL + "/internal/v1/index/5?store=default&languages=en,fr"))
				.andExpect(method(HttpMethod.DELETE))
				.andExpect(header(SearchIndexClientRestTemplateImpl.INTERNAL_TOKEN_HEADER, TOKEN))
				.andRespond(withSuccess());

		producer.deleteDocument(store, 5L, List.of("en", "fr"));

		server.verify();
	}

}
