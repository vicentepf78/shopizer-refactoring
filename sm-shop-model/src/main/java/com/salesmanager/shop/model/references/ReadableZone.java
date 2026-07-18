package com.salesmanager.shop.model.references;

/**
 * @deprecated Use {@link com.salesmanager.contracts.reference.ReadableZone} from shopizer-api-contracts.
 * Kept as a compile-compatible legacy alias for the monolith.
 */
@Deprecated
public class ReadableZone extends ZoneEntity {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
