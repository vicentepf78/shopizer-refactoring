package com.salesmanager.content.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.salesmanager.contracts.client.ReferenceServiceClient;
import com.salesmanager.content.support.TestDataFactory;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestDataFactory.class)
class ContentSecurityIntegrationTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private TestDataFactory testDataFactory;

	@MockBean
	private ReferenceServiceClient referenceServiceClient;

	@BeforeEach
	void setUp() {
		testDataFactory.ensureDefaultAdmin();
	}

	@Test
	void privateContentWithoutJwt_returns401() throws Exception {
		mockMvc.perform(get("/api/v1/private/content/pages").param("store", "DEFAULT"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void publicContentWithoutJwt_isOpen() throws Exception {
		mockMvc.perform(get("/api/v1/content/pages").param("store", "DEFAULT").param("page", "0").param("count", "10"))
				.andExpect(status().isOk());
	}
}
