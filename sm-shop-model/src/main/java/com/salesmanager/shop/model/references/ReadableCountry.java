package com.salesmanager.shop.model.references;

import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated Use {@link com.salesmanager.contracts.reference.ReadableCountry} from shopizer-api-contracts.
 * Kept as a compile-compatible legacy alias for the monolith.
 */
@Deprecated
public class ReadableCountry extends CountryEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String name;
	private List<ReadableZone> zones = new ArrayList<ReadableZone>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ReadableZone> getZones() {
		return zones;
	}

	public void setZones(List<ReadableZone> zones) {
		this.zones = zones;
	}

}
