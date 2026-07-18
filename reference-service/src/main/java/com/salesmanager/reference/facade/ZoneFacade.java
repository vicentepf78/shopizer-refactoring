package com.salesmanager.reference.facade;

import java.util.List;

import com.salesmanager.contracts.reference.ReadableZone;
import com.salesmanager.core.model.reference.language.Language;

public interface ZoneFacade {

	List<ReadableZone> getZones(String countryCode, Language language);
}
