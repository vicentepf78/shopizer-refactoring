package com.salesmanager.shop.strangler.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.catalog.SearchProductRequest;
import com.salesmanager.shop.model.entity.ValueList;
import com.salesmanager.shop.strangler.support.ServiceUnavailableException;
import com.salesmanager.shop.strangler.support.StranglerRestClient;

@ExtendWith(MockitoExtension.class)
class SearchFacadeHttpAdapterTest {

	private static final String BASE_URL = "http://search-test:8084";

	private MockRestServiceServer server;
	private SearchFacadeHttpAdapter adapter;

	@Mock
	private SearchBulkIndexOrchestrator bulkIndexOrchestrator;

	private MerchantStore store;
	private Language language;

	@BeforeEach
	void setUp() {
		RestTemplate restTemplate = new RestTemplate();
		server = MockRestServiceServer.createServer(restTemplate);
		adapter = new SearchFacadeHttpAdapter(restTemplate, BASE_URL, bulkIndexOrchestrator);

		store = new MerchantStore();
		store.setCode("DEFAULT");
		language = new Language("en");

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(StranglerRestClient.CORRELATION_HEADER, "corr-search-1");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
	}

	@AfterEach
	void tearDown() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	void search_forwardsQueryWithStoreAndLangHeaders() {
		server.expect(requestTo("http://search-test:8084/api/v1/search?store=DEFAULT&lang=en"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header(StranglerRestClient.CORRELATION_HEADER, "corr-search-1"))
				.andRespond(withSuccess(
						"[{\"id\":1,\"name\":\"Phone\",\"description\":\"Smart phone\"}]",
						MediaType.APPLICATION_JSON));

		SearchProductRequest searchRequest = new SearchProductRequest();
		searchRequest.setQuery("phone");
		searchRequest.setCount(10);
		searchRequest.setStart(0);

		List<modules.commons.search.request.SearchItem> items = adapter.search(store, language, searchRequest);

		assertThat(items).hasSize(1);
		assertThat(items.get(0).getName()).isEqualTo("Phone");
		server.verify();
		verifyNoInteractions(bulkIndexOrchestrator);
	}

	@Test
	void autocomplete_forwardsQueryWithStoreAndLang() {
		server.expect(requestTo("http://search-test:8084/api/v1/search/autocomplete?store=DEFAULT&lang=en"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header(StranglerRestClient.CORRELATION_HEADER, "corr-search-1"))
				.andRespond(withSuccess("{\"values\":[\"phone\",\"phones\"]}", MediaType.APPLICATION_JSON));

		ValueList valueList = adapter.autocompleteRequest("ph", store, language);

		assertThat(valueList.getValues()).containsExactly("phone", "phones");
		server.verify();
	}

	@Test
	void connectFailure_mapsTo503WithoutCallingInProcessFacade() {
		server.expect(requestTo("http://search-test:8084/api/v1/search?store=DEFAULT&lang=en"))
				.andRespond(withException(new ConnectException("Connection refused")));

		SearchProductRequest searchRequest = new SearchProductRequest();
		searchRequest.setQuery("phone");

		assertThatThrownBy(() -> adapter.search(store, language, searchRequest))
				.isInstanceOf(ServiceUnavailableException.class);
		verifyNoInteractions(bulkIndexOrchestrator);
	}

	@Test
	void autocompleteConnectFailure_mapsTo503() {
		server.expect(requestTo("http://search-test:8084/api/v1/search/autocomplete?store=DEFAULT&lang=en"))
				.andRespond(withException(new ConnectException("Connection refused")));

		assertThatThrownBy(() -> adapter.autocompleteRequest("ph", store, language))
				.isInstanceOf(ServiceUnavailableException.class);
	}

	@Test
	void indexAllData_delegatesToBulkOrchestrator() throws Exception {
		adapter.indexAllData(store);

		verify(bulkIndexOrchestrator).indexAllData(store);
		server.verify();
	}
}
