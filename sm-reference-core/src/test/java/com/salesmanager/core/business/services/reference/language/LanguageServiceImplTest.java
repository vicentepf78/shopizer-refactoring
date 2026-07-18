package com.salesmanager.core.business.services.reference.language;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.core.business.repositories.reference.language.LanguageRepository;
import com.salesmanager.core.business.utils.CacheUtils;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.reference.language.Language;

@ExtendWith(MockitoExtension.class)
class LanguageServiceImplTest {

	@Mock
	private LanguageRepository languageRepository;

	@Mock
	private CacheUtils cache;

	private LanguageServiceImpl languageService;

	@BeforeEach
	void setUp() throws Exception {
		languageService = new LanguageServiceImpl(languageRepository);
		Field cacheField = LanguageServiceImpl.class.getDeclaredField("cache");
		cacheField.setAccessible(true);
		cacheField.set(languageService, cache);
	}

	@Test
	void toLocale_withLanguageAndCountryCode_buildsExpectedLocale() {
		Language language = new Language("en");

		Locale locale = languageService.toLocale(language, "US");

		assertEquals("en", locale.getLanguage());
		assertEquals("US", locale.getCountry());
	}

	@Test
	void toLocale_withNullCountryCode_usesLanguageOnly() {
		Language language = new Language("fr");

		Locale locale = languageService.toLocale(language, (String) null);

		assertEquals("fr", locale.getLanguage());
		assertEquals("", locale.getCountry());
	}

	@Test
	void toLocale_newSignature_doesNotRequireMerchantStore() throws Exception {
		Method overload = LanguageService.class.getMethod("toLocale", Language.class, String.class);
		assertEquals(Locale.class, overload.getReturnType());

		for (Class<?> param : overload.getParameterTypes()) {
			assertFalse(param.getName().contains("MerchantStore"),
					"REF-08 overload must not take MerchantStore: " + param.getName());
		}
	}

	@Test
	void toLocale_withMerchantStore_delegatesToCountryCodeOverload() {
		Language language = new Language("en");
		Country country = new Country();
		country.setIsoCode("CA");
		MerchantStore store = new MerchantStore();
		store.setCountry(country);

		Locale locale = languageService.toLocale(language, store);

		assertEquals("en", locale.getLanguage());
		assertEquals("CA", locale.getCountry());
	}

	@Test
	void toLocale_withNullStore_usesLanguageOnly() {
		Language language = new Language("pt");

		Locale locale = languageService.toLocale(language, (MerchantStore) null);

		assertEquals("pt", locale.getLanguage());
		assertEquals("", locale.getCountry());
	}

	@Test
	void getByCode_delegatesToRepository() throws Exception {
		Language expected = new Language("en");
		when(languageRepository.findByCode("en")).thenReturn(expected);

		assertEquals(expected, languageService.getByCode("en"));
	}

	@Test
	void getLanguages_usesCacheThenRepositoryList() throws Exception {
		Language en = new Language("en");
		Language fr = new Language("fr");
		List<Language> langs = Arrays.asList(en, fr);
		when(cache.getFromCache("LANGUAGES")).thenReturn(null);
		when(languageRepository.findAll()).thenReturn(langs);

		List<Language> result = languageService.getLanguages();

		assertEquals(2, result.size());
	}

	@Test
	void getLanguagesMap_indexesByCode() throws Exception {
		Language en = new Language("en");
		when(cache.getFromCache("LANGUAGES")).thenReturn(Arrays.asList(en));

		Map<String, Language> map = languageService.getLanguagesMap();

		assertTrue(map.containsKey("en"));
		assertEquals(en, map.get("en"));
	}

	@Test
	void toLanguage_resolvesFromMap() throws Exception {
		Language en = new Language("en");
		when(cache.getFromCache("LANGUAGES")).thenReturn(Arrays.asList(en));

		Language resolved = languageService.toLanguage(Locale.ENGLISH);

		assertEquals("en", resolved.getCode());
	}

	@Test
	void defaultLanguage_returnsEnglish() throws Exception {
		Language en = new Language("en");
		when(cache.getFromCache("LANGUAGES")).thenReturn(Arrays.asList(en));

		assertNotNull(languageService.defaultLanguage());
		assertEquals("en", languageService.defaultLanguage().getCode());
	}

	@Test
	void getLanguages_whenCacheHit_skipsRepository() throws Exception {
		Language en = new Language("en");
		when(cache.getFromCache(eq("LANGUAGES"))).thenReturn(Arrays.asList(en));

		List<Language> result = languageService.getLanguages();

		assertEquals(1, result.size());
		assertEquals("en", result.get(0).getCode());
	}

	@Test
	void toLanguage_whenMissing_fallsBackToDefault() throws Exception {
		when(cache.getFromCache(anyString())).thenReturn(Arrays.asList());

		Language resolved = languageService.toLanguage(Locale.JAPANESE);

		assertEquals("en", resolved.getCode());
	}

	@Test
	void getLanguages_whenCacheThrows_wrapsServiceException() throws Exception {
		when(cache.getFromCache("LANGUAGES")).thenThrow(new Exception("cache down"));

		try {
			languageService.getLanguages();
			org.junit.jupiter.api.Assertions.fail("expected ServiceException");
		} catch (Exception e) {
			assertTrue(e.getClass().getSimpleName().contains("ServiceException"));
		}
	}

	@Test
	void toLanguage_whenMapLookupFails_fallsBackToDefault() throws Exception {
		when(cache.getFromCache("LANGUAGES")).thenThrow(new Exception("boom"));

		Language resolved = languageService.toLanguage(Locale.FRENCH);

		assertEquals("en", resolved.getCode());
	}
}
