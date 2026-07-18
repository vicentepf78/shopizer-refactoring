package com.salesmanager.reference.populator;

import com.salesmanager.contracts.reference.ReadableZone;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.reference.zone.Zone;
import com.salesmanager.core.model.reference.zone.ZoneDescription;

public class ReadableZonePopulator {

	public ReadableZone populate(Zone source, Language language) {
		ReadableZone target = new ReadableZone();
		target.setId(source.getId());
		target.setCode(source.getCode());
		if (source.getCountry() != null) {
			target.setCountryCode(source.getCountry().getIsoCode());
		}
		if (source.getDescriptions() != null) {
			for (ZoneDescription d : source.getDescriptions()) {
				if (d.getLanguage() != null && language != null
						&& d.getLanguage().getId() != null
						&& d.getLanguage().getId().equals(language.getId())) {
					target.setName(d.getName());
					break;
				}
			}
		}
		return target;
	}
}
