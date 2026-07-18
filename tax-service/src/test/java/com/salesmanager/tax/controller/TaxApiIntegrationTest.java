package com.salesmanager.tax.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.contracts.client.ReferenceServiceClient;
import com.salesmanager.contracts.reference.ReadableCountry;
import com.salesmanager.contracts.reference.ReadableLanguage;
import com.salesmanager.contracts.reference.ReadableZone;
import com.salesmanager.contracts.tax.PersistableTaxClass;
import com.salesmanager.contracts.tax.PersistableTaxRate;
import com.salesmanager.contracts.tax.TaxRateDescription;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.zone.Zone;
import com.salesmanager.tax.security.JWTTokenUtil;
import com.salesmanager.tax.support.TestDataFactory;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestDataFactory.class)
class TaxApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private JWTTokenUtil jwtTokenUtil;
	@Autowired
	private TestDataFactory testDataFactory;

	@MockBean
	private ReferenceServiceClient referenceServiceClient;

	private String bearer;
	private TestDataFactory.Seed seed;

	@BeforeEach
	void setUp() {
		seed = testDataFactory.ensureDefaultAdmin();
		bearer = "Bearer " + jwtTokenUtil.generateToken("admin");
	}

	@Test
	void taxClassCrud_withValidJwt_succeeds() throws Exception {
		String code = "C" + UUID.randomUUID().toString().substring(0, 8);
		PersistableTaxClass body = new PersistableTaxClass();
		body.setCode(code);
		body.setName("Tax Class 1");

		MvcResult created = mockMvc.perform(post("/api/v1/private/tax/class")
						.header("Authorization", bearer)
						.param("store", "DEFAULT")
						.param("lang", "en")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").isNumber())
				.andReturn();

		long id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

		mockMvc.perform(get("/api/v1/private/tax/class")
						.header("Authorization", bearer)
						.param("store", "DEFAULT")
						.param("lang", "en"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[?(@.code=='" + code + "')]").exists());

		mockMvc.perform(get("/api/v1/private/tax/class/" + code)
						.header("Authorization", bearer)
						.param("store", "DEFAULT")
						.param("lang", "en"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(code));

		mockMvc.perform(delete("/api/v1/private/tax/class/" + id)
						.header("Authorization", bearer)
						.param("store", "DEFAULT")
						.param("lang", "en"))
				.andExpect(status().isOk());
	}

	@Test
	void taxRateCrud_withValidCodesAndDescriptions() throws Exception {
		String classCode = "R" + UUID.randomUUID().toString().substring(0, 8);
		PersistableTaxClass taxClass = new PersistableTaxClass();
		taxClass.setCode(classCode);
		taxClass.setName("Rate Class");
		mockMvc.perform(post("/api/v1/private/tax/class")
						.header("Authorization", bearer)
						.param("store", "DEFAULT")
						.param("lang", "en")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(taxClass)))
				.andExpect(status().isOk());

		Zone zone = testDataFactory.seedZone("QC", seed.country);

		ReadableCountry country = new ReadableCountry();
		country.setId(seed.country.getId().longValue());
		country.setCode("CA");
		ReadableZone readableZone = new ReadableZone();
		readableZone.setId(zone.getId());
		readableZone.setCode("QC");
		readableZone.setCountryCode("CA");
		ReadableLanguage language = new ReadableLanguage();
		language.setId(seed.language.getId());
		language.setCode("en");

		when(referenceServiceClient.getCountryByCode(eq("CA"), any())).thenReturn(country);
		when(referenceServiceClient.getZoneByCode(eq("CA"), eq("QC"), any())).thenReturn(readableZone);
		when(referenceServiceClient.getLanguageByCode("en")).thenReturn(language);

		TaxRateDescription desc = new TaxRateDescription();
		desc.setLanguage("en");
		desc.setName("GST");
		desc.setDescription("Goods and Services Tax");

		String rateCode = "G" + UUID.randomUUID().toString().substring(0, 8);
		PersistableTaxRate rate = new PersistableTaxRate();
		rate.setCode(rateCode);
		rate.setCountry("CA");
		rate.setZone("QC");
		rate.setTaxClass(classCode);
		rate.setRate(new BigDecimal("5.0000"));
		rate.setPriority(1);
		rate.setDescriptions(Collections.singletonList(desc));

		MvcResult created = mockMvc.perform(post("/api/v1/private/tax/rate")
						.header("Authorization", bearer)
						.param("store", "DEFAULT")
						.param("lang", "en")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(rate)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").isNumber())
				.andReturn();

		long id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

		mockMvc.perform(get("/api/v1/private/tax/rate/" + id)
						.header("Authorization", bearer)
						.param("store", "DEFAULT")
						.param("lang", "en"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(rateCode))
				.andExpect(jsonPath("$.description.name").value("GST"));
	}

	@Test
	void uniqueTaxRate_missingCode_returnsExistsFalse() throws Exception {
		mockMvc.perform(get("/api/v1/private/tax/rate/unique")
						.header("Authorization", bearer)
						.param("store", "DEFAULT")
						.param("lang", "en")
						.param("code", "MISSING"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.exists").value(false));
	}

	@Test
	void storeMismatch_returns403() throws Exception {
		MerchantStore other = testDataFactory.seedOtherStore("OTHER", seed);

		mockMvc.perform(get("/api/v1/private/tax/class")
						.header("Authorization", bearer)
						.param("store", other.getCode())
						.param("lang", "en"))
				.andExpect(status().isForbidden());
	}

	@Test
	void createTaxRate_invalidCountry_returns400() throws Exception {
		String classCode = "I" + UUID.randomUUID().toString().substring(0, 8);
		PersistableTaxClass taxClass = new PersistableTaxClass();
		taxClass.setCode(classCode);
		taxClass.setName("Invalid");
		mockMvc.perform(post("/api/v1/private/tax/class")
						.header("Authorization", bearer)
						.param("store", "DEFAULT")
						.param("lang", "en")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(taxClass)))
				.andExpect(status().isOk());

		when(referenceServiceClient.getCountryByCode(eq("ZZ"), any())).thenReturn(null);

		PersistableTaxRate rate = new PersistableTaxRate();
		rate.setCode("B" + UUID.randomUUID().toString().substring(0, 8));
		rate.setCountry("ZZ");
		rate.setZone("XX");
		rate.setTaxClass(classCode);
		rate.setRate(BigDecimal.ONE);

		mockMvc.perform(post("/api/v1/private/tax/rate")
						.header("Authorization", bearer)
						.param("store", "DEFAULT")
						.param("lang", "en")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(rate)))
				.andExpect(status().isBadRequest());
	}
}
