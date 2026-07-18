package com.salesmanager.core.business.services.reference.zone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.core.business.repositories.reference.zone.ZoneRepository;
import com.salesmanager.core.business.utils.CacheUtils;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.reference.zone.Zone;
import com.salesmanager.core.model.reference.zone.ZoneDescription;

@ExtendWith(MockitoExtension.class)
class ZoneServiceImplTest {

	@Mock
	private ZoneRepository zoneRepository;

	@Mock
	private CacheUtils cache;

	private ZoneServiceImpl zoneService;

	@BeforeEach
	void setUp() throws Exception {
		zoneService = new ZoneServiceImpl(zoneRepository);
		Field cacheField = ZoneServiceImpl.class.getDeclaredField("cache");
		cacheField.setAccessible(true);
		cacheField.set(zoneService, cache);
	}

	@Test
	void getByCode_delegatesToRepository() {
		Zone zone = new Zone();
		when(zoneRepository.findByCode("QC")).thenReturn(zone);
		assertEquals(zone, zoneService.getByCode("QC"));
	}

	@Test
	void getZones_byCountryCode_populatesName() throws Exception {
		Language language = new Language("en");
		language.setId(1);
		Zone zone = new Zone();
		zone.setCode("QC");
		ZoneDescription description = new ZoneDescription();
		description.setName("Quebec");
		zone.setDescriptons(Collections.singletonList(description));

		when(cache.getFromCache(anyString())).thenReturn(null);
		when(zoneRepository.listByLanguageAndCountry("CA", 1)).thenReturn(Collections.singletonList(zone));

		List<Zone> result = zoneService.getZones("CA", language);

		assertEquals(1, result.size());
		assertEquals("Quebec", result.get(0).getName());
	}

	@Test
	void getZones_byCountryEntity_usesIsoCode() throws Exception {
		Language language = new Language("en");
		language.setId(1);
		Country country = new Country();
		country.setIsoCode("CA");
		Zone zone = new Zone();
		zone.setCode("ON");
		ZoneDescription description = new ZoneDescription();
		description.setName("Ontario");
		zone.setDescriptons(Collections.singletonList(description));

		when(cache.getFromCache(eq("ZONES_CA_en"))).thenReturn(null);
		when(zoneRepository.listByLanguageAndCountry("CA", 1)).thenReturn(Collections.singletonList(zone));

		List<Zone> result = zoneService.getZones(country, language);

		assertEquals("ON", result.get(0).getCode());
	}

	@Test
	void getZones_byLanguage_returnsMap() throws Exception {
		Language language = new Language("en");
		language.setId(1);
		Zone zone = new Zone();
		zone.setCode("QC");
		ZoneDescription description = new ZoneDescription();
		description.setName("Quebec");
		zone.setDescriptons(Collections.singletonList(description));

		when(cache.getFromCache("ZONES_en")).thenReturn(null);
		when(zoneRepository.listByLanguage(1)).thenReturn(Collections.singletonList(zone));

		Map<String, Zone> result = zoneService.getZones(language);

		assertTrue(result.containsKey("QC"));
		assertEquals("Quebec", result.get("QC").getName());
	}

	@Test
	void addDescription_whenDescriptionsNull_createsList() throws Exception {
		Zone zone = new Zone();
		zone.setDescriptons(null);
		ZoneDescription description = new ZoneDescription();
		when(zoneRepository.saveAndFlush(zone)).thenReturn(zone);

		zoneService.addDescription(zone, description);

		assertEquals(1, zone.getDescriptions().size());
	}

	@Test
	void addDescription_whenMissing_appendsAndUpdates() throws Exception {
		Zone zone = new Zone();
		ZoneDescription existing = new ZoneDescription();
		zone.setDescriptons(new java.util.ArrayList<>(Collections.singletonList(existing)));
		ZoneDescription description = new ZoneDescription();
		when(zoneRepository.saveAndFlush(zone)).thenReturn(zone);

		zoneService.addDescription(zone, description);

		assertEquals(2, zone.getDescriptions().size());
	}

	@Test
	void getZones_withNullCountry_usesDefaultCountry() throws Exception {
		Language language = new Language("en");
		language.setId(1);
		when(cache.getFromCache("ZONES_CA_en")).thenReturn(Collections.emptyList());

		List<Zone> result = zoneService.getZones((Country) null, language);

		assertTrue(result.isEmpty());
	}

	@Test
	void getZones_byLanguage_cacheHit() throws Exception {
		Language language = new Language("en");
		language.setId(1);
		Map<String, Zone> cached = Collections.singletonMap("QC", new Zone());
		when(cache.getFromCache("ZONES_en")).thenReturn(cached);

		assertEquals(cached, zoneService.getZones(language));
	}
}
