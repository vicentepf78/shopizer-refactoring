package com.salesmanager.tax.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.salesmanager.contracts.common.Entity;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.tax.facade.TaxFacade;
import com.salesmanager.tax.support.RestErrorHandler;
import com.salesmanager.tax.web.LanguageArgumentResolver;
import com.salesmanager.tax.web.MerchantStoreArgumentResolver;

@ExtendWith(MockitoExtension.class)
class TaxRatesControllerTest {

	@Mock
	private TaxFacade taxFacade;
	@Mock
	private MerchantStoreArgumentResolver storeResolver;
	@Mock
	private LanguageArgumentResolver languageResolver;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() throws Exception {
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		Language language = new Language("en");
		when(storeResolver.supportsParameter(any())).thenAnswer(inv ->
				inv.getArgument(0, org.springframework.core.MethodParameter.class)
						.getParameterType().equals(MerchantStore.class));
		when(languageResolver.supportsParameter(any())).thenAnswer(inv ->
				inv.getArgument(0, org.springframework.core.MethodParameter.class)
						.getParameterType().equals(Language.class));
		when(storeResolver.resolveArgument(any(), any(), any(), any())).thenReturn(store);
		when(languageResolver.resolveArgument(any(), any(), any(), any())).thenReturn(language);

		mockMvc = MockMvcBuilders.standaloneSetup(new TaxRatesController(taxFacade))
				.setCustomArgumentResolvers(storeResolver, languageResolver)
				.setControllerAdvice(new RestErrorHandler())
				.build();
	}

	@Test
	void create_andUnique() throws Exception {
		Entity id = new Entity();
		id.setId(9L);
		when(taxFacade.createTaxRate(any(), any(), any())).thenReturn(id);
		when(taxFacade.existsTaxRate(eq("R1"), any(), any())).thenReturn(false);

		mockMvc.perform(post("/api/v1/private/tax/rate")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"code\":\"R1\",\"country\":\"CA\",\"zone\":\"QC\",\"taxClass\":\"DEFAULT\",\"rate\":5}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(9));

		mockMvc.perform(get("/api/v1/private/tax/rate/unique").param("code", "R1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.exists").value(false));
	}
}
