package com.salesmanager.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.contracts.search.ProductIndexPayload;
import com.salesmanager.search.api.v1.SearchProductRequest;
import com.salesmanager.search.web.InternalTokenFilter;

import modules.commons.search.SearchModule;
import modules.commons.search.request.SearchRequest;
import modules.commons.search.request.SearchResponse;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SearchServiceIntegrationTest {

	private static final String TOKEN = "test-search-token";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private SearchModule searchModule;

	@BeforeEach
	void stubOpenSearch() throws Exception {
		when(searchModule.getConnection()).thenReturn(new Object());
		SearchResponse empty = new SearchResponse();
		empty.setItems(java.util.Collections.emptyList());
		when(searchModule.searchProducts(any(SearchRequest.class))).thenReturn(empty);
		when(searchModule.searchKeywords(any(SearchRequest.class))).thenReturn(empty);
	}

	@Test
	void contextLoadsWithoutDataSource(@Autowired(required = false) DataSource dataSource) {
		assertThat(dataSource).isNull();
	}

	@Test
	void healthEndpointIsReachable() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk());
	}

	@Test
	void invalidInternalTokenReturns401() throws Exception {
		ProductIndexPayload payload = validPayload();
		mockMvc.perform(post("/internal/v1/index")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(payload)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").exists());
	}

	@Test
	void unsupportedSchemaVersionReturns422() throws Exception {
		ProductIndexPayload payload = validPayload();
		payload.setSchemaVersion(2);
		mockMvc.perform(post("/internal/v1/index")
						.header(InternalTokenFilter.INTERNAL_TOKEN_HEADER, TOKEN)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(payload)))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.schemaVersion").value(2));
	}

	@Test
	void validIndexWithTokenReturns204() throws Exception {
		ProductIndexPayload payload = validPayload();
		mockMvc.perform(post("/internal/v1/index")
						.header(InternalTokenFilter.INTERNAL_TOKEN_HEADER, TOKEN)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(payload)))
				.andExpect(status().isNoContent());
	}

	@Test
	void searchEndpointIsRegistered() throws Exception {
		SearchProductRequest request = new SearchProductRequest();
		request.setQuery("test");
		mockMvc.perform(post("/api/v1/search")
						.param("store", "DEFAULT")
						.param("lang", "en")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk());
	}

	@Test
	void autocompleteEndpointIsRegistered() throws Exception {
		SearchProductRequest request = new SearchProductRequest();
		request.setQuery("te");
		mockMvc.perform(post("/api/v1/search/autocomplete")
						.param("store", "DEFAULT")
						.param("lang", "en")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk());
	}

	@Test
	void reindexAdminEndpointReturns501() throws Exception {
		mockMvc.perform(post("/api/v1/private/system/search/index"))
				.andExpect(status().isNotImplemented());
	}

	@Test
	void deleteIndexWithTokenReturns204() throws Exception {
		mockMvc.perform(delete("/internal/v1/index/99")
						.header(InternalTokenFilter.INTERNAL_TOKEN_HEADER, TOKEN)
						.param("store", "default")
						.param("languages", "en,fr"))
				.andExpect(status().isNoContent());
	}

	private ProductIndexPayload validPayload() {
		ProductIndexPayload payload = new ProductIndexPayload();
		payload.setSchemaVersion(1);
		payload.setId(1L);
		payload.setStore("default");
		payload.setLanguage("en");
		payload.setName("Sample");
		return payload;
	}
}
