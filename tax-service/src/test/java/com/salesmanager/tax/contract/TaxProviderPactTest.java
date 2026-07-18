package com.salesmanager.tax.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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

import com.salesmanager.contracts.common.Entity;
import com.salesmanager.contracts.common.ReadableEntityList;
import com.salesmanager.contracts.tax.PersistableTaxClass;
import com.salesmanager.contracts.tax.PersistableTaxRate;
import com.salesmanager.contracts.tax.ReadableTaxClass;
import com.salesmanager.contracts.tax.ReadableTaxRate;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.tax.controller.TaxClassController;
import com.salesmanager.tax.controller.TaxRatesController;
import com.salesmanager.tax.facade.TaxFacade;
import com.salesmanager.tax.support.RestErrorHandler;
import com.salesmanager.tax.web.LanguageArgumentResolver;
import com.salesmanager.tax.web.MerchantStoreArgumentResolver;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;

/**
 * Pact provider verification for Wave 1 tax class + tax rate endpoints (TAX-08).
 */
@Provider("tax-service")
@PactFolder("../pacts")
@ExtendWith(MockitoExtension.class)
class TaxProviderPactTest {

	@Mock
	private TaxFacade taxFacade;
	@Mock
	private MerchantStoreArgumentResolver storeResolver;
	@Mock
	private LanguageArgumentResolver languageResolver;

	private MerchantStore store;
	private Language language;

	@BeforeEach
	void setUp(PactVerificationContext context) throws Exception {
		store = new MerchantStore();
		store.setCode("DEFAULT");
		language = new Language("en");

		when(storeResolver.supportsParameter(any())).thenAnswer(inv ->
				inv.getArgument(0, org.springframework.core.MethodParameter.class)
						.getParameterType().equals(MerchantStore.class));
		when(languageResolver.supportsParameter(any())).thenAnswer(inv ->
				inv.getArgument(0, org.springframework.core.MethodParameter.class)
						.getParameterType().equals(Language.class));
		when(storeResolver.resolveArgument(any(), any(), any(), any())).thenReturn(store);
		when(languageResolver.resolveArgument(any(), any(), any(), any())).thenReturn(language);

		MockMvcTestTarget target = new MockMvcTestTarget(MockMvcBuilders
				.standaloneSetup(new TaxClassController(taxFacade), new TaxRatesController(taxFacade))
				.setCustomArgumentResolvers(storeResolver, languageResolver)
				.setControllerAdvice(new RestErrorHandler())
				.setMessageConverters(new MappingJackson2HttpMessageConverter())
				.build());
		context.setTarget(target);
	}

	@TestTemplate
	@ExtendWith(PactVerificationInvocationContextProvider.class)
	void verifyInteraction(PactVerificationContext context) {
		context.verifyInteraction();
	}

	@State("store DEFAULT accepts tax class create")
	void acceptTaxClassCreate() {
		Entity id = new Entity();
		id.setId(5L);
		when(taxFacade.createTaxClass(any(PersistableTaxClass.class), any(), any())).thenReturn(id);
	}

	@State("tax classes exist for store DEFAULT")
	void taxClassesExist() {
		ReadableTaxClass item = new ReadableTaxClass();
		item.setId(5L);
		item.setCode("T1");
		item.setName("Standard");
		item.setStore("DEFAULT");
		ReadableEntityList<ReadableTaxClass> list = new ReadableEntityList<>();
		list.setItems(Collections.singletonList(item));
		list.setTotalPages(1);
		list.setNumber(0);
		list.setRecordsTotal(1);
		list.setRecordsFiltered(1);
		when(taxFacade.taxClasses(any(), any())).thenReturn(list);
	}

	@State("tax class T1 exists for store DEFAULT")
	void taxClassT1Exists() {
		ReadableTaxClass item = new ReadableTaxClass();
		item.setId(5L);
		item.setCode("T1");
		item.setName("Standard");
		item.setStore("DEFAULT");
		when(taxFacade.taxClass(eq("T1"), any(), any())).thenReturn(item);
	}

	@State("tax class code T1 uniqueness check")
	void taxClassUnique() {
		when(taxFacade.existsTaxClass(eq("T1"), any(), any())).thenReturn(true);
	}

	@State("tax class id 5 exists for store DEFAULT")
	void taxClassId5Exists() {
		doNothing().when(taxFacade).updateTaxClass(eq(5L), any(PersistableTaxClass.class), any(), any());
	}

	@State("tax class id 5 deletable for store DEFAULT")
	void taxClassId5Deletable() {
		doNothing().when(taxFacade).deleteTaxClass(eq(5L), any(), any());
	}

	@State("store DEFAULT accepts tax rate create")
	void acceptTaxRateCreate() {
		Entity id = new Entity();
		id.setId(9L);
		when(taxFacade.createTaxRate(any(PersistableTaxRate.class), any(), any())).thenReturn(id);
	}

	@State("tax rates exist for store DEFAULT")
	void taxRatesExist() {
		ReadableTaxRate item = new ReadableTaxRate();
		item.setId(9L);
		item.setCode("TR1");
		item.setRate("10.00");
		item.setCountry("BR");
		item.setStore("DEFAULT");
		ReadableEntityList<ReadableTaxRate> list = new ReadableEntityList<>();
		list.setItems(Collections.singletonList(item));
		list.setTotalPages(1);
		list.setRecordsTotal(1);
		when(taxFacade.taxRates(any(), any())).thenReturn(list);
	}

	@State("tax rate id 9 exists for store DEFAULT")
	void taxRateId9Exists() {
		ReadableTaxRate item = new ReadableTaxRate();
		item.setId(9L);
		item.setCode("TR1");
		item.setRate("10.00");
		item.setCountry("BR");
		item.setStore("DEFAULT");
		// Shared state for GET and PUT — only one stub is used per interaction.
		lenient().when(taxFacade.taxRate(eq(9L), any(), any())).thenReturn(item);
		lenient().doNothing().when(taxFacade).updateTaxRate(eq(9L), any(PersistableTaxRate.class), any(), any());
	}

	@State("tax rate code TR1 uniqueness check")
	void taxRateUnique() {
		when(taxFacade.existsTaxRate(eq("TR1"), any(), any())).thenReturn(false);
	}

	@State("tax rate id 9 deletable for store DEFAULT")
	void taxRateId9Deletable() {
		doNothing().when(taxFacade).deleteTaxRate(eq(9L), any(), any());
	}
}
