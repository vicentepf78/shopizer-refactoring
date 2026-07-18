package com.salesmanager.reference.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.salesmanager.contracts.reference.ReadableCountry;
import com.salesmanager.contracts.reference.ReadableCurrency;
import com.salesmanager.contracts.reference.ReadableLanguage;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.reference.facade.CountryFacade;
import com.salesmanager.reference.facade.CurrencyFacade;
import com.salesmanager.reference.facade.LanguageFacade;
import com.salesmanager.reference.facade.ZoneFacade;
import com.salesmanager.reference.support.LanguageResolver;

@ExtendWith(MockitoExtension.class)
class ReferencesControllerTest {

	@Mock
	private LanguageFacade languageFacade;
	@Mock
	private CountryFacade countryFacade;
	@Mock
	private ZoneFacade zoneFacade;
	@Mock
	private CurrencyFacade currencyFacade;
	@Mock
	private LanguageResolver languageResolver;

	private MockMvc mockMvc;
	private Language language;

	@BeforeEach
	void setUp() {
		language = new Language("en");
		language.setId(1);
		mockMvc = MockMvcBuilders
				.standaloneSetup(new ReferencesController(
						languageFacade, countryFacade, zoneFacade, currencyFacade, languageResolver))
				.build();
	}

	@Test
	void getCountry_returnsReadableCountryList() throws Exception {
		when(languageResolver.resolve(any())).thenReturn(language);
		ReadableCountry country = new ReadableCountry();
		country.setId(1L);
		country.setCode("BR");
		country.setName("Brazil");
		when(countryFacade.getListCountryZones(language)).thenReturn(Collections.singletonList(country));

		mockMvc.perform(get("/api/v1/country").param("lang", "en").param("store", "DEFAULT")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].code").value("BR"))
				.andExpect(jsonPath("$[0].name").value("Brazil"))
				.andExpect(content().string(not(containsString("hibernateLazyInitializer"))));
	}

	@Test
	void getZones_unknownCode_returnsEmptyArray() throws Exception {
		when(languageResolver.resolve(any())).thenReturn(language);
		when(zoneFacade.getZones(eq("XX"), any(Language.class))).thenReturn(Collections.emptyList());

		mockMvc.perform(get("/api/v1/zones").param("code", "XX").param("lang", "en")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().json("[]"));
	}

	@Test
	void getLanguages_returnsDtos() throws Exception {
		ReadableLanguage lang = new ReadableLanguage();
		lang.setId(1);
		lang.setCode("en");
		lang.setSortOrder(0);
		when(languageFacade.getLanguages()).thenReturn(Collections.singletonList(lang));

		mockMvc.perform(get("/api/v1/languages").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("en"))
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].sortOrder").value(0))
				.andExpect(content().string(not(containsString("hibernateLazyInitializer"))));
	}

	@Test
	void getCurrency_returnsDtos() throws Exception {
		ReadableCurrency currency = new ReadableCurrency();
		currency.setId(1L);
		currency.setCode("USD");
		currency.setName("US Dollar");
		currency.setSupported(true);
		when(currencyFacade.getList()).thenReturn(Collections.singletonList(currency));

		mockMvc.perform(get("/api/v1/currency").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("USD"))
				.andExpect(content().string(not(containsString("hibernateLazyInitializer"))));
	}

	@Test
	void getMeasures_returnsSizeReferences() throws Exception {
		mockMvc.perform(get("/api/v1/measures").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.measures", hasSize(2)))
				.andExpect(jsonPath("$.weights", hasSize(2)));
	}
}
