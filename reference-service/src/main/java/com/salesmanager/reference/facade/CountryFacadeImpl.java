package com.salesmanager.reference.facade;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.salesmanager.contracts.reference.ReadableCountry;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.reference.country.CountryService;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.reference.populator.ReadableCountryPopulator;
import com.salesmanager.reference.support.ServiceRuntimeException;

@Service
public class CountryFacadeImpl implements CountryFacade {

	private final CountryService countryService;
	private final ReadableCountryPopulator countryPopulator = new ReadableCountryPopulator();

	public CountryFacadeImpl(CountryService countryService) {
		this.countryService = countryService;
	}

	@Override
	public List<ReadableCountry> getListCountryZones(Language language) {
		return getListOfCountryZones(language).stream()
				.map(country -> countryPopulator.populate(country, language))
				.collect(Collectors.toList());
	}

	private List<Country> getListOfCountryZones(Language language) {
		try {
			return countryService.listCountryZones(language);
		} catch (ServiceException e) {
			throw new ServiceRuntimeException(e);
		}
	}
}
