package com.salesmanager.reference.facade;

import java.util.List;

import com.salesmanager.contracts.reference.ReadableCountry;
import com.salesmanager.core.model.reference.language.Language;

public interface CountryFacade {

	List<ReadableCountry> getListCountryZones(Language language);
}
