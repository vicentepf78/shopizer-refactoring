package com.salesmanager.reference.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.contracts.reference.ReadableZone;
import com.salesmanager.core.business.services.reference.zone.ZoneService;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.reference.zone.Zone;

@ExtendWith(MockitoExtension.class)
class ZoneFacadeImplTest {

	@Mock
	private ZoneService zoneService;

	@InjectMocks
	private ZoneFacadeImpl zoneFacade;

	@Test
	void getZones_unknownCountry_returnsEmptyList() throws Exception {
		Language language = new Language("en");
		language.setId(1);
		when(zoneService.getZones("XX", language)).thenReturn(Collections.emptyList());

		List<ReadableZone> result = zoneFacade.getZones("XX", language);

		assertTrue(result.isEmpty());
	}

	@Test
	void getZones_nullFromService_returnsEmptyList() throws Exception {
		Language language = new Language("en");
		language.setId(1);
		when(zoneService.getZones("XX", language)).thenReturn(null);

		assertTrue(zoneFacade.getZones("XX", language).isEmpty());
	}

	@Test
	void getZones_mapsDto() throws Exception {
		Language language = new Language("en");
		language.setId(1);
		Country country = new Country("BR");
		Zone zone = new Zone();
		zone.setId(1L);
		zone.setCode("SP");
		zone.setCountry(country);
		when(zoneService.getZones("BR", language)).thenReturn(Collections.singletonList(zone));

		List<ReadableZone> result = zoneFacade.getZones("BR", language);

		assertEquals(1, result.size());
		assertEquals("SP", result.get(0).getCode());
		assertEquals("BR", result.get(0).getCountryCode());
	}
}
