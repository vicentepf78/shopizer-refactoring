package com.salesmanager.search.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.salesmanager.contracts.search.ValueList;
import com.salesmanager.search.api.internal.InternalIndexController;
import com.salesmanager.search.api.v1.SearchController;
import com.salesmanager.search.services.SearchIndexService;
import com.salesmanager.search.services.SearchQueryService;
import com.salesmanager.search.web.RestErrorHandler;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import modules.commons.search.request.SearchItem;

/**
 * Pact provider verification for Wave 2 search query + internal index endpoints (STR-02).
 */
@Provider("search-service")
@PactFolder("../pacts")
@ExtendWith(MockitoExtension.class)
class SearchProviderPactTest {

	@Mock
	private SearchQueryService searchQueryService;
	@Mock
	private SearchIndexService searchIndexService;

	@BeforeEach
	void setUp(PactVerificationContext context) {
		MockMvcTestTarget target = new MockMvcTestTarget(MockMvcBuilders
				.standaloneSetup(
						new SearchController(searchQueryService),
						new InternalIndexController(searchIndexService))
				.setControllerAdvice(new RestErrorHandler())
				.setMessageConverters(new MappingJackson2HttpMessageConverter())
				.build());
		context.setTarget(target);
	}

	@TestTemplate
	@ExtendWith(PactVerificationInvocationContextProvider.class)
	void verifyInteraction(PactVerificationContext context) {
		context.verifyInteraction();
	}

	@State("search results exist for store DEFAULT")
	void searchResultsExist() {
		SearchItem item = new SearchItem();
		item.setId(1L);
		item.setName("Phone");
		item.setDescription("Smart phone");
		org.mockito.Mockito.when(searchQueryService.search(anyString(), anyString(), any()))
				.thenReturn(Collections.singletonList(item));
	}

	@State("autocomplete suggestions exist for store DEFAULT")
	void autocompleteSuggestionsExist() {
		ValueList values = new ValueList();
		values.setValues(java.util.Arrays.asList("phone", "phones"));
		org.mockito.Mockito.when(searchQueryService.autocomplete(anyString(), anyString(), anyString()))
				.thenReturn(values);
	}

	@State("index accepts schema version 1")
	void indexAcceptsSchemaVersion1() {
		doNothing().when(searchIndexService).index(any());
	}

	@State("index accepts schema version 2")
	void indexAcceptsSchemaVersion2() {
		doNothing().when(searchIndexService).index(any());
	}

	@State("index rejects unsupported schema version")
	void indexRejectsUnsupportedSchema() {
		// validation happens in controller before service call
	}
}
