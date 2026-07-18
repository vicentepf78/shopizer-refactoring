package com.salesmanager.core.business.services.reference.country;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.core.business.repositories.reference.country.CountryRepository;
import com.salesmanager.core.business.utils.CacheUtils;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.reference.country.CountryDescription;
import com.salesmanager.core.model.reference.language.Language;

@ExtendWith(MockitoExtension.class)
class CountryServiceImplTest {

	@Mock
	private CountryRepository countryRepository;

	@Mock
	private CacheUtils cache;

	private CountryServiceImpl countryService;

	@BeforeEach
	void setUp() throws Exception {
		countryService = new CountryServiceImpl(countryRepository);
		Field cacheField = CountryServiceImpl.class.getDeclaredField("cache");
		cacheField.setAccessible(true);
		cacheField.set(countryService, cache);
	}

	@Test
	void getByCode_delegatesToRepository() throws Exception {
		Country country = new Country();
		country.setIsoCode("BR");
		when(countryRepository.findByIsoCode("BR")).thenReturn(country);

		assertEquals(country, countryService.getByCode("BR"));
	}

	@Test
	void getCountries_populatesNameFromDescription() throws Exception {
		Language language = new Language("en");
		language.setId(1);
		Country country = new Country();
		country.setIsoCode("BR");
		CountryDescription description = new CountryDescription();
		description.setName("Brazil");
		country.setDescriptions(new HashSet<>(Collections.singletonList(description)));

		when(cache.getFromCache("COUNTRIES_en")).thenReturn(null);
		when(countryRepository.listByLanguage(1)).thenReturn(Collections.singletonList(country));

		List<Country> result = countryService.getCountries(language);

		assertEquals(1, result.size());
		assertEquals("Brazil", result.get(0).getName());
		verify(cache).putInCache(eq(result), eq("COUNTRIES_en"));
	}

	@Test
	void getCountriesMap_indexesByIsoCode() throws Exception {
		Language language = new Language("en");
		language.setId(1);
		Country country = new Country();
		country.setIsoCode("US");
		CountryDescription description = new CountryDescription();
		description.setName("United States");
		country.setDescriptions(new HashSet<>(Collections.singletonList(description)));
		when(cache.getFromCache(anyString())).thenReturn(Collections.singletonList(country));

		Map<String, Country> map = countryService.getCountriesMap(language);

		assertTrue(map.containsKey("US"));
	}

	@Test
	void getCountries_filtersByIsoCodes() throws Exception {
		Language language = new Language("en");
		language.setId(1);
		Country br = new Country();
		br.setIsoCode("BR");
		Country us = new Country();
		us.setIsoCode("US");
		when(cache.getFromCache(anyString())).thenReturn(Arrays.asList(br, us));

		List<Country> result = countryService.getCountries(Collections.singletonList("BR"), language);

		assertEquals(1, result.size());
		assertEquals("BR", result.get(0).getIsoCode());
	}

	@Test
	void listCountryZones_delegatesToRepository() throws Exception {
		Language language = new Language("en");
		language.setId(1);
		when(countryRepository.listCountryZonesByLanguage(1)).thenReturn(Collections.emptyList());

		assertTrue(countryService.listCountryZones(language).isEmpty());
	}

	@Test
	void addCountryDescription_updatesCountry() throws Exception {
		Country country = new Country();
		country.setDescriptions(new HashSet<>());
		CountryDescription description = new CountryDescription();
		when(countryRepository.saveAndFlush(country)).thenReturn(country);

		countryService.addCountryDescription(country, description);

		assertTrue(country.getDescriptions().contains(description));
		assertEquals(country, description.getCountry());
	}

	@Test
	void getCountries_whenCacheHit_returnsCached() throws Exception {
		Language language = new Language("en");
		language.setId(1);
		Country country = new Country();
		country.setIsoCode("CA");
		when(cache.getFromCache("COUNTRIES_en")).thenReturn(Collections.singletonList(country));

		List<Country> result = countryService.getCountries(language);

		assertEquals(1, result.size());
		assertEquals("CA", result.get(0).getIsoCode());
	}

	@Test
	void listCountryZones_wrapsRepositoryFailure() {
		Language language = new Language("en");
		language.setId(1);
		when(countryRepository.listCountryZonesByLanguage(1)).thenThrow(new RuntimeException("db"));

		try {
			countryService.listCountryZones(language);
			org.junit.jupiter.api.Assertions.fail("expected ServiceException");
		} catch (Exception e) {
			assertTrue(e.getClass().getSimpleName().contains("ServiceException")
					|| e.getCause() != null);
		}
	}
}
