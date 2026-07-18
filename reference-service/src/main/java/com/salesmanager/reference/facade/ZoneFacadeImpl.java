package com.salesmanager.reference.facade;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.salesmanager.contracts.reference.ReadableZone;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.reference.zone.ZoneService;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.reference.zone.Zone;
import com.salesmanager.reference.populator.ReadableZonePopulator;
import com.salesmanager.reference.support.ServiceRuntimeException;

@Service
public class ZoneFacadeImpl implements ZoneFacade {

	private final ZoneService zoneService;
	private final ReadableZonePopulator zonePopulator = new ReadableZonePopulator();

	public ZoneFacadeImpl(ZoneService zoneService) {
		this.zoneService = zoneService;
	}

	@Override
	public List<ReadableZone> getZones(String countryCode, Language language) {
		List<Zone> listZones = getListZones(countryCode, language);
		// OQ-02: unknown country code → empty list (not 404)
		if (listZones == null || listZones.isEmpty()) {
			return Collections.emptyList();
		}
		return listZones.stream()
				.map(zone -> zonePopulator.populate(zone, language))
				.collect(Collectors.toList());
	}

	private List<Zone> getListZones(String countryCode, Language language) {
		try {
			return zoneService.getZones(countryCode, language);
		} catch (ServiceException e) {
			throw new ServiceRuntimeException(e);
		}
	}
}
