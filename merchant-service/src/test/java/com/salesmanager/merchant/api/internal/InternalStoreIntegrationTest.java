package com.salesmanager.merchant.api.internal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

import com.salesmanager.contracts.client.ContentServiceClient;
import com.salesmanager.contracts.client.ReferenceServiceClient;
import com.salesmanager.contracts.reference.ReadableCountry;
import com.salesmanager.contracts.reference.ReadableLanguage;
import com.salesmanager.merchant.support.TestDataFactory;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestDataFactory.class)
class InternalStoreIntegrationTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private TestDataFactory testDataFactory;
	@Autowired
	private com.salesmanager.merchant.security.MerchantStoreRepository merchantStoreRepository;

	@MockBean
	private ReferenceServiceClient referenceServiceClient;

	@MockBean
	private ContentServiceClient contentServiceClient;

	@BeforeEach
	void setUp() {
		TestDataFactory.Seed seed = testDataFactory.ensureDefaultAdmin();
		ReadableLanguage readableEn = new ReadableLanguage();
		readableEn.setId(seed.language.getId());
		readableEn.setCode("en");
		org.mockito.Mockito.when(referenceServiceClient.getLanguageByCode(org.mockito.ArgumentMatchers.eq("en")))
				.thenReturn(readableEn);
		ReadableCountry country = new ReadableCountry();
		country.setCode("CA");
		org.mockito.Mockito.when(referenceServiceClient.getCountryByCode(
				org.mockito.ArgumentMatchers.eq("CA"), org.mockito.ArgumentMatchers.any()))
				.thenReturn(country);
	}

	@Test
	void getSnapshot_returnsMerchantStoreSnapshot() throws Exception {
		String storeName = merchantStoreRepository.findByCode("DEFAULT").getStorename();
		mockMvc.perform(get("/internal/v1/store/DEFAULT").param("lang", "en"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("DEFAULT"))
				.andExpect(jsonPath("$.name").value(storeName))
				.andExpect(jsonPath("$.defaultLanguage").value("en"));
	}
}
