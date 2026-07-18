package com.salesmanager.reference.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.salesmanager.contracts.reference.ReadableCountry;
import com.salesmanager.contracts.reference.ReadableCurrency;
import com.salesmanager.contracts.reference.ReadableLanguage;
import com.salesmanager.contracts.reference.ReadableZone;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.reference.controller.ReferencesController;
import com.salesmanager.reference.facade.CountryFacade;
import com.salesmanager.reference.facade.CurrencyFacade;
import com.salesmanager.reference.facade.LanguageFacade;
import com.salesmanager.reference.facade.ZoneFacade;
import com.salesmanager.reference.support.LanguageResolver;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;

/**
 * Pact provider verification for Wave 1 public reference endpoints (STR-02).
 */
@Provider("reference-service")
@PactFolder("../pacts")
@ExtendWith(MockitoExtension.class)
class ReferenceProviderPactTest {

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

	private Language language;

	@BeforeEach
	void setUp(PactVerificationContext context) {
		language = new Language("en");
		language.setId(1);
		// Not every interaction resolves language (e.g. measures/currency).
		lenient().when(languageResolver.resolve(any())).thenReturn(language);

		MockMvcTestTarget target = new MockMvcTestTarget(MockMvcBuilders
				.standaloneSetup(new ReferencesController(
						languageFacade, countryFacade, zoneFacade, currencyFacade, languageResolver))
				.setMessageConverters(new MappingJackson2HttpMessageConverter())
				.build());
		context.setTarget(target);
	}

	@TestTemplate
	@ExtendWith(PactVerificationInvocationContextProvider.class)
	void verifyInteraction(PactVerificationContext context) {
		context.verifyInteraction();
	}

	@State("languages exist")
	void languagesExist() {
		ReadableLanguage lang = new ReadableLanguage();
		lang.setId(1);
		lang.setCode("en");
		lang.setSortOrder(0);
		when(languageFacade.getLanguages()).thenReturn(Collections.singletonList(lang));
	}

	@State("countries exist")
	void countriesExist() {
		ReadableCountry country = new ReadableCountry();
		country.setId(1L);
		country.setCode("BR");
		country.setName("Brazil");
		when(countryFacade.getListCountryZones(language)).thenReturn(Collections.singletonList(country));
	}

	@State("zones for country BR exist")
	void zonesExist() {
		ReadableZone zone = new ReadableZone();
		zone.setId(10L);
		zone.setCode("SP");
		zone.setName("Sao Paulo");
		zone.setCountryCode("BR");
		when(zoneFacade.getZones(eq("BR"), any())).thenReturn(Collections.singletonList(zone));
	}

	@State("currencies exist")
	void currenciesExist() {
		ReadableCurrency currency = new ReadableCurrency();
		currency.setId(1L);
		currency.setCode("BRL");
		currency.setName("Brazilian Real");
		currency.setSymbol("R$");
		currency.setSupported(true);
		when(currencyFacade.getList()).thenReturn(Collections.singletonList(currency));
	}

	@State("measures available")
	void measuresAvailable() {
		// controller builds SizeReferences from enums — no facade stub required
	}
}
