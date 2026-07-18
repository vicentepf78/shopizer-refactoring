package com.salesmanager.tax.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.salesmanager.contracts.client.ReferenceServiceClient;
import com.salesmanager.tax.support.TestDataFactory;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestDataFactory.class)
class TaxSecurityIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ReferenceServiceClient referenceServiceClient;

	@Test
	void privateTaxClassWithoutJwt_returns401() throws Exception {
		mockMvc.perform(get("/api/v1/private/tax/class").param("store", "DEFAULT"))
				.andExpect(status().isUnauthorized());
	}
}
