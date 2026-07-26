package com.salesmanager.shop.store.api.v1.references;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.store.controller.country.facade.CountryFacade;
import com.salesmanager.shop.store.controller.currency.facade.CurrencyFacade;
import com.salesmanager.shop.store.controller.language.facade.LanguageFacade;
import com.salesmanager.shop.store.controller.store.facade.StoreFacade;
import com.salesmanager.shop.store.controller.zone.facade.ZoneFacade;
import com.salesmanager.shop.utils.LanguageUtils;

@ExtendWith(MockitoExtension.class)
class ReferencesApiTest {

	@Mock
	private StoreFacade storeFacade;
	@Mock
	private LanguageUtils languageUtils;
	@Mock
	private LanguageFacade languageFacade;
	@Mock
	private CountryFacade countryFacade;
	@Mock
	private ZoneFacade zoneFacade;
	@Mock
	private CurrencyFacade currencyFacade;

	@InjectMocks
	private ReferencesApi referencesApi;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(referencesApi).build();
	}

	@Test
	void getLanguages_returnsReadableLanguageShapeWithoutJpaEntityFields() throws Exception {
		Language language = new Language("en");
		language.setId(1);
		language.setSortOrder(0);
		when(languageFacade.getLanguages()).thenReturn(Collections.singletonList(language));

		mockMvc.perform(get("/api/v1/languages").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].code").value("en"))
				.andExpect(jsonPath("$[0].sortOrder").value(0))
				.andExpect(content().string(not(containsString("hibernateLazyInitializer"))))
				.andExpect(content().string(not(containsString("auditSection"))))
				.andExpect(content().string(not(containsString("com.salesmanager.core.model"))));
	}

	@Test
	void getCountry_withNullLanguage_resolvesDefaultLanguageBeforeFacade() throws Exception {
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		Language defaultLanguage = new Language("en");
		defaultLanguage.setId(1);
		HttpServletRequest request = mock(HttpServletRequest.class);

		when(storeFacade.getByCode(request)).thenReturn(store);
		when(languageUtils.getServiceLanguage(isNull())).thenReturn(defaultLanguage);
		when(countryFacade.getListCountryZones(defaultLanguage, store)).thenReturn(Collections.emptyList());

		referencesApi.getCountry(null, request);

		verify(languageUtils).getServiceLanguage(isNull());
		verify(countryFacade).getListCountryZones(eq(defaultLanguage), eq(store));
	}

	@Test
	void getZones_withNullLanguage_resolvesDefaultLanguageBeforeFacade() throws Exception {
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		Language defaultLanguage = new Language("en");
		defaultLanguage.setId(1);
		HttpServletRequest request = mock(HttpServletRequest.class);

		when(storeFacade.getByCode(request)).thenReturn(store);
		when(languageUtils.getServiceLanguage(isNull())).thenReturn(defaultLanguage);
		when(zoneFacade.getZones("US", defaultLanguage, store)).thenReturn(Collections.emptyList());

		referencesApi.getZones("US", null, request);

		verify(languageUtils).getServiceLanguage(isNull());
		verify(zoneFacade).getZones(eq("US"), eq(defaultLanguage), eq(store));
	}

	@Test
	void getCurrency_returnsReadableCurrencyShapeWithoutJpaEntityFields() throws Exception {
		Currency currency = new Currency();
		currency.setId(1L);
		currency.setCurrency(java.util.Currency.getInstance("USD"));
		currency.setName("US Dollar");
		currency.setSupported(true);
		when(currencyFacade.getList()).thenReturn(Collections.singletonList(currency));

		mockMvc.perform(get("/api/v1/currency").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].code").value("USD"))
				.andExpect(jsonPath("$[0].name").value("US Dollar"))
				.andExpect(jsonPath("$[0].symbol").value("US$"))
				.andExpect(jsonPath("$[0].supported").value(true))
				.andExpect(content().string(not(containsString("hibernateLazyInitializer"))))
				.andExpect(content().string(not(containsString("auditSection"))))
				.andExpect(content().string(not(containsString("com.salesmanager.core.model"))));
	}
}
