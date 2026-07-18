package com.salesmanager.reference.populator;

import com.salesmanager.contracts.reference.ReadableCountry;
import com.salesmanager.contracts.reference.ReadableZone;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.reference.zone.Zone;
import com.salesmanager.core.model.reference.zone.ZoneDescription;

public class ReadableCountryPopulator {

	public ReadableCountry populate(Country source, Language language) {
		ReadableCountry target = new ReadableCountry();
		if (source.getId() != null) {
			target.setId(source.getId().longValue());
		}
		target.setCode(source.getIsoCode());
		target.setSupported(source.getSupported());
		if (source.getDescriptions() != null && !source.getDescriptions().isEmpty()) {
			target.setName(source.getDescriptions().iterator().next().getName());
		}
		if (source.getZones() != null) {
			for (Zone z : source.getZones()) {
				ReadableZone readableZone = new ReadableZone();
				readableZone.setCountryCode(target.getCode());
				if (z.getId() != null) {
					readableZone.setId(z.getId());
				}
				readableZone.setCode(z.getCode());
				if (z.getDescriptions() != null) {
					for (ZoneDescription d : z.getDescriptions()) {
						if (d.getLanguage() != null && language != null
								&& d.getLanguage().getId() != null
								&& d.getLanguage().getId().equals(language.getId())) {
							readableZone.setName(d.getName());
							break;
						}
					}
				}
				target.getZones().add(readableZone);
			}
		}
		return target;
	}
}
