package com.salesmanager.content.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.salesmanager.contracts.common.Entity;
import com.salesmanager.contracts.common.ReadableEntityList;
import com.salesmanager.contracts.content.ContentFolder;
import com.salesmanager.contracts.content.box.ReadableContentBox;
import com.salesmanager.contracts.content.page.ReadableContentPage;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.content.api.v1.content.ContentApi;
import com.salesmanager.content.facade.content.ContentFacade;
import com.salesmanager.content.support.RestErrorHandler;
import com.salesmanager.content.util.ContentImagePath;
import com.salesmanager.content.web.LanguageArgumentResolver;
import com.salesmanager.content.web.MerchantStoreArgumentResolver;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;

/**
 * Pact provider verification for Wave 2 content P1 endpoints (STR-02).
 */
@Provider("content-service")
@PactFolder("../pacts")
@ExtendWith(MockitoExtension.class)
class ContentProviderPactTest {

	@Mock
	private ContentFacade contentFacade;
	@Mock
	private ContentImagePath imageUtils;
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
		lenient().when(storeResolver.resolveArgument(any(), any(), any(), any())).thenReturn(store);
		lenient().when(languageResolver.resolveArgument(any(), any(), any(), any())).thenReturn(language);

		ContentApi contentApi = new ContentApi();
		ReflectionTestUtils.setField(contentApi, "contentFacade", contentFacade);
		ReflectionTestUtils.setField(contentApi, "imageUtils", imageUtils);

		MockMvcTestTarget target = new MockMvcTestTarget(MockMvcBuilders
				.standaloneSetup(contentApi)
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

	@State("content pages exist for store DEFAULT")
	void contentPagesExist() {
		ReadableContentPage item = new ReadableContentPage();
		item.setId(1L);
		item.setCode("home");
		item.setVisible(true);
		item.setContentType("PAGE");
		ReadableEntityList<ReadableContentPage> list = new ReadableEntityList<>();
		list.setItems(Collections.singletonList(item));
		list.setTotalPages(1);
		list.setNumber(0);
		list.setRecordsTotal(1);
		list.setRecordsFiltered(1);
		when(contentFacade.getContentPages(any(), any(), anyInt(), anyInt())).thenReturn(list);
	}

	@State("content page home exists for store DEFAULT")
	void contentPageHomeExists() {
		ReadableContentPage page = new ReadableContentPage();
		page.setId(1L);
		page.setCode("home");
		page.setPath("/home");
		when(contentFacade.getContentPage(eq("home"), any(), any())).thenReturn(page);
	}

	@State("content boxes exist for store DEFAULT")
	void contentBoxesExist() {
		ReadableContentBox item = new ReadableContentBox();
		item.setId(2L);
		item.setCode("sidebar");
		ReadableEntityList<ReadableContentBox> list = new ReadableEntityList<>();
		list.setItems(Collections.singletonList(item));
		list.setTotalPages(1);
		when(contentFacade.getContentBoxes(any(), any(), any(), anyInt(), anyInt())).thenReturn(list);
	}

	@State("content box sidebar exists for store DEFAULT")
	void contentBoxSidebarExists() {
		ReadableContentBox box = new ReadableContentBox();
		box.setId(2L);
		box.setCode("sidebar");
		when(contentFacade.getContentBox(eq("sidebar"), any(), any())).thenReturn(box);
	}

	@State("store DEFAULT accepts content page create")
	void acceptContentPageCreate() {
		when(contentFacade.saveContentPage(any(), any(), any())).thenReturn(3L);
	}

	@State("store DEFAULT accepts content box create")
	void acceptContentBoxCreate() {
		when(contentFacade.saveContentBox(any(), any(), any())).thenReturn(4L);
	}

	@State("content images folder available for store DEFAULT")
	void contentImagesFolderAvailable() throws Exception {
		ContentFolder folder = new ContentFolder();
		folder.setPath("/");
		folder.setContent(Collections.emptyList());
		when(contentFacade.getContentFolder(any(), any())).thenReturn(folder);
	}
}
