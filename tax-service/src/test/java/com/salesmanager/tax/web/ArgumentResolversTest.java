package com.salesmanager.tax.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.tax.security.MerchantStoreRepository;
import com.salesmanager.tax.security.StoreAuthorizationService;
import com.salesmanager.tax.support.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class ArgumentResolversTest {

	@Mock
	private MerchantStoreRepository merchantStoreRepository;
	@Mock
	private StoreAuthorizationService storeAuthorizationService;
	@Mock
	private LanguageService languageService;
	@Mock
	private MethodParameter storeParam;
	@Mock
	private MethodParameter langParam;

	@Test
	void merchantStoreResolver_loadsAndAuthorizes() throws Exception {
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		when(storeParam.getParameterType()).thenReturn((Class) MerchantStore.class);
		when(merchantStoreRepository.findByCode("DEFAULT")).thenReturn(store);
		doNothing().when(storeAuthorizationService).authorize(eq(store), any());

		MerchantStoreArgumentResolver resolver =
				new MerchantStoreArgumentResolver(merchantStoreRepository, storeAuthorizationService);
		assertThat(resolver.supportsParameter(storeParam)).isTrue();

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/api/v1/private/tax/class");
		Object resolved = resolver.resolveArgument(storeParam, null, new ServletWebRequest(request), null);
		assertThat(resolved).isSameAs(store);
		verify(storeAuthorizationService).authorize(eq(store), eq("/api/v1/private/tax/class"));
	}

	@Test
	void merchantStoreResolver_missingStore_notFound() {
		when(merchantStoreRepository.findByCode("DEFAULT")).thenReturn(null);
		MerchantStoreArgumentResolver resolver =
				new MerchantStoreArgumentResolver(merchantStoreRepository, storeAuthorizationService);
		MockHttpServletRequest request = new MockHttpServletRequest();
		assertThatThrownBy(() -> resolver.resolveArgument(storeParam, null, new ServletWebRequest(request), null))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void languageResolver_usesLangParam() throws Exception {
		Language language = new Language("fr");
		when(langParam.getParameterType()).thenReturn((Class) Language.class);
		when(languageService.getByCode("fr")).thenReturn(language);

		LanguageArgumentResolver resolver = new LanguageArgumentResolver(languageService);
		assertThat(resolver.supportsParameter(langParam)).isTrue();

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("lang", "fr");
		assertThat(resolver.resolveArgument(langParam, null, new ServletWebRequest(request), null))
				.isSameAs(language);
	}

	@Test
	void languageResolver_defaultsWhenMissing() throws Exception {
		Language language = new Language("en");
		when(languageService.defaultLanguage()).thenReturn(language);
		LanguageArgumentResolver resolver = new LanguageArgumentResolver(languageService);
		MockHttpServletRequest request = new MockHttpServletRequest();
		assertThat(resolver.resolveArgument(langParam, null, new ServletWebRequest(request), null))
				.isSameAs(language);
	}
}
