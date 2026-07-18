package com.salesmanager.reference.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.model.reference.language.Language;

@ExtendWith(MockitoExtension.class)
class LanguageResolverTest {

	@Mock
	private LanguageService languageService;

	@InjectMocks
	private LanguageResolver languageResolver;

	@Test
	void resolve_usesLangCodeWhenPresent() throws Exception {
		Language fr = new Language("fr");
		fr.setId(2);
		when(languageService.getByCode("fr")).thenReturn(fr);

		assertEquals("fr", languageResolver.resolve("fr").getCode());
	}

	@Test
	void resolve_fallsBackToDefaultWhenBlank() {
		Language en = new Language("en");
		en.setId(1);
		when(languageService.defaultLanguage()).thenReturn(en);

		assertEquals("en", languageResolver.resolve(null).getCode());
	}
}
