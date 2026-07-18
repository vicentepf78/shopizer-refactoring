package com.salesmanager.reference.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.contracts.reference.ReadableCountry;
import com.salesmanager.core.business.services.reference.country.CountryService;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.reference.country.CountryDescription;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.reference.zone.Zone;
import com.salesmanager.core.model.reference.zone.ZoneDescription;

@ExtendWith(MockitoExtension.class)
class CountryFacadeImplTest {

	@Mock
	private CountryService countryService;

	@InjectMocks
	private CountryFacadeImpl countryFacade;

	@Test
	void getListCountryZones_includesNestedZonesInDto() throws Exception {
		Language language = new Language("en");
		language.setId(1);

		ZoneDescription zoneDescription = new ZoneDescription();
		zoneDescription.setName("São Paulo");
		zoneDescription.setLanguage(language);

		Zone zone = new Zone();
		zone.setId(10L);
		zone.setCode("SP");
		zone.setDescriptons(Collections.singletonList(zoneDescription));

		CountryDescription countryDescription = new CountryDescription();
		countryDescription.setName("Brazil");

		Country country = new Country();
		country.setId(55);
		country.setIsoCode("BR");
		country.setSupported(true);
		country.setDescriptions(new HashSet<>(Collections.singletonList(countryDescription)));
		country.setZones(new HashSet<>(Collections.singletonList(zone)));

		when(countryService.listCountryZones(language)).thenReturn(Collections.singletonList(country));

		List<ReadableCountry> result = countryFacade.getListCountryZones(language);

		assertEquals(1, result.size());
		ReadableCountry dto = result.get(0);
		assertEquals("BR", dto.getCode());
		assertEquals("Brazil", dto.getName());
		assertFalse(dto.getZones().isEmpty());
		assertEquals("SP", dto.getZones().get(0).getCode());
		assertEquals("São Paulo", dto.getZones().get(0).getName());
		assertEquals("BR", dto.getZones().get(0).getCountryCode());
	}
}
