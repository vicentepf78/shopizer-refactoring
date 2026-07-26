package com.salesmanager.merchant.api.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.salesmanager.contracts.client.ContentServiceClient;
import com.salesmanager.contracts.client.ReferenceServiceClient;
import com.salesmanager.contracts.merchant.PersistableMerchantStore;
import com.salesmanager.contracts.reference.PersistableAddress;
import com.salesmanager.contracts.reference.ReadableCountry;
import com.salesmanager.contracts.reference.ReadableLanguage;
import com.salesmanager.merchant.security.JWTTokenUtil;
import com.salesmanager.merchant.support.MerchantConstants;
import com.salesmanager.merchant.support.TestDataFactory;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestDataFactory.class)
class MerchantStoreIntegrationTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private RequestMappingHandlerMapping handlerMapping;
	@Autowired
	private TestDataFactory testDataFactory;
	@Autowired
	private JWTTokenUtil jwtTokenUtil;

	@MockBean
	private ReferenceServiceClient referenceServiceClient;

	@MockBean
	private ContentServiceClient contentServiceClient;

	private String bearer;

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
		bearer = "Bearer " + jwtTokenUtil.generateToken("admin");
	}

	@Test
	void noProductTypeRoutesRegistered() {
		boolean hasProductType = handlerMapping.getHandlerMethods().keySet().stream()
				.anyMatch(info -> info.getPatternsCondition().getPatterns().stream()
						.anyMatch(p -> p.toLowerCase().contains("producttype")));
		assertThat(hasProductType).isFalse();
	}

	@Test
	void getPublicStore_returnsReadableDto() throws Exception {
		mockMvc.perform(get("/api/v1/store/DEFAULT").param("lang", "en"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("DEFAULT"));
	}

	@Test
	void deleteDefaultStore_returnsBadRequest() throws Exception {
		mockMvc.perform(delete("/api/v1/private/store/DEFAULT")
						.header("Authorization", bearer))
				.andExpect(status().isBadRequest());
	}

	@Test
	void getConfig_returnsFlagsAndSocial() throws Exception {
		TestDataFactory.Seed seed = testDataFactory.ensureDefaultAdmin();
		testDataFactory.seedSocialConfig(seed.store, MerchantConstants.KEY_FACEBOOK_PAGE_URL, "https://facebook.test");

		mockMvc.perform(get("/api/v1/config").param("store", "DEFAULT").param("lang", "en"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.displayShipping").value(true))
				.andExpect(jsonPath("$.facebook").value("https://facebook.test"));
	}

	@Test
	void createAndUpdateStore_viaPrivateApi() throws Exception {
		String code = "shop" + System.currentTimeMillis() % 100000;
		PersistableMerchantStore created = new PersistableMerchantStore();
		created.setCode(code);
		created.setCurrency("CAD");
		created.setDefaultLanguage("en");
		created.setEmail("new@test.local");
		created.setName(code);
		created.setPhone("555-9999");
		created.setSupportedLanguages(java.util.Collections.singletonList("en"));
		PersistableAddress address = new PersistableAddress();
		address.setAddress("99 King");
		address.setCity("Montreal");
		address.setCountry("CA");
		address.setPostalCode("H2X1Y4");
		address.setStateProvince("QC");
		created.setAddress(address);

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/private/store")
						.header("Authorization", bearer)
						.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
						.content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(created)))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/store/" + code).param("lang", "en"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(code));

		PersistableMerchantStore updatedDefault = new PersistableMerchantStore();
		updatedDefault.setCode("DEFAULT");
		updatedDefault.setCurrency("CAD");
		updatedDefault.setDefaultLanguage("en");
		updatedDefault.setEmail("admin@default.test");
		updatedDefault.setName("DEFAULT-updated");
		updatedDefault.setPhone("555-0100");
		updatedDefault.setSupportedLanguages(java.util.Collections.singletonList("en"));
		updatedDefault.setAddress(address);

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/private/store/DEFAULT")
						.header("Authorization", bearer)
						.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
						.content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(updatedDefault)))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/store/DEFAULT").param("lang", "en"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("DEFAULT-updated"));
	}

	@Test
	void getMarketing_returnsBrand() throws Exception {
		testDataFactory.seedSocialConfig(testDataFactory.ensureDefaultAdmin().store,
				MerchantConstants.KEY_PINTEREST_PAGE_URL, "https://pinterest.test");
		org.mockito.Mockito.doNothing().when(contentServiceClient)
				.uploadLogo(org.mockito.ArgumentMatchers.eq("DEFAULT"), org.mockito.ArgumentMatchers.anyString(),
						org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString());
		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.multipart("/api/v1/private/store/DEFAULT/marketing/logo")
						.file(new org.springframework.mock.web.MockMultipartFile("file", "logo.png",
								"image/png", new byte[] { 1, 2, 3 }))
						.header("Authorization", bearer))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/v1/private/store/DEFAULT/marketing").header("Authorization", bearer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.logo.name").value("logo.png"));
	}
}
