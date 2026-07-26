package com.salesmanager.merchant.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.salesmanager.contracts.merchant.Configs;
import com.salesmanager.contracts.merchant.MerchantStoreSnapshot;
import com.salesmanager.contracts.merchant.ReadableMerchantStore;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.merchant.api.internal.InternalStoreController;
import com.salesmanager.merchant.api.v1.store.MerchantStoreController;
import com.salesmanager.merchant.api.v1.system.PublicConfigsController;
import com.salesmanager.merchant.facade.MerchantConfigurationFacade;
import com.salesmanager.merchant.facade.StoreFacade;
import com.salesmanager.merchant.security.UserAuthorizationService;
import com.salesmanager.merchant.support.RestErrorHandler;
import com.salesmanager.merchant.web.LanguageArgumentResolver;
import com.salesmanager.merchant.web.MerchantStoreArgumentResolver;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;

/**
 * Pact provider verification for Wave 2 merchant P1 endpoints (STR-02, ADR-007 no ProductType).
 */
@Provider("merchant-service")
@PactFolder("../pacts")
@ExtendWith(MockitoExtension.class)
class MerchantProviderPactTest {

	@Mock
	private StoreFacade storeFacade;
	@Mock
	private MerchantConfigurationFacade configurationFacade;
	@Mock
	private UserAuthorizationService userAuthorizationService;
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

		lenient().when(storeResolver.supportsParameter(any())).thenAnswer(inv ->
				inv.getArgument(0, org.springframework.core.MethodParameter.class)
						.getParameterType().equals(MerchantStore.class));
		lenient().when(languageResolver.supportsParameter(any())).thenAnswer(inv ->
				inv.getArgument(0, org.springframework.core.MethodParameter.class)
						.getParameterType().equals(Language.class));
		lenient().when(storeResolver.resolveArgument(any(), any(), any(), any())).thenReturn(store);
		lenient().when(languageResolver.resolveArgument(any(), any(), any(), any())).thenReturn(language);

		lenient().when(userAuthorizationService.authenticatedUser()).thenReturn("admin");
		lenient().doNothing().when(userAuthorizationService).authorizedGroup(anyString(), any());

		MockMvcTestTarget target = new MockMvcTestTarget(MockMvcBuilders
				.standaloneSetup(
						new MerchantStoreController(storeFacade, userAuthorizationService),
						new PublicConfigsController(configurationFacade),
						new InternalStoreController(storeFacade))
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

	@State("store DEFAULT exists")
	void storeDefaultExists() {
		ReadableMerchantStore readable = new ReadableMerchantStore();
		readable.setId(1);
		readable.setCode("DEFAULT");
		readable.setName("Default store");
		when(storeFacade.getByCode(eq("DEFAULT"), anyString())).thenReturn(readable);
	}

	@State("merchant config exists for store DEFAULT")
	void merchantConfigExists() {
		Configs configs = new Configs();
		configs.setDisplaySearchBox(true);
		configs.setDisplayShipping(true);
		configs.setFacebook("https://facebook.test");
		when(configurationFacade.getMerchantConfig(any(), any())).thenReturn(configs);
	}

	@State("store snapshot DEFAULT exists")
	void storeSnapshotExists() {
		MerchantStoreSnapshot snapshot = new MerchantStoreSnapshot();
		snapshot.setId(1);
		snapshot.setCode("DEFAULT");
		snapshot.setName("Default store");
		snapshot.setDefaultLanguage("en");
		when(storeFacade.getSnapshot(eq("DEFAULT"), any())).thenReturn(snapshot);
	}

	@State("private store DEFAULT exists")
	void privateStoreDefaultExists() {
		ReadableMerchantStore readable = new ReadableMerchantStore();
		readable.setId(1);
		readable.setCode("DEFAULT");
		readable.setName("Default store");
		when(storeFacade.getFullByCode(eq("DEFAULT"), any(Language.class))).thenReturn(readable);
	}

	@State("store create accepted")
	void storeCreateAccepted() {
		doNothing().when(storeFacade).create(any());
	}
}
