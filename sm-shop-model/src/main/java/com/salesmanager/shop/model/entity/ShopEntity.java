package com.salesmanager.shop.model.entity;

import java.io.Serializable;

/**
 * @deprecated Use {@link com.salesmanager.contracts.common.ShopEntity} from shopizer-api-contracts.
 * Kept as a compile-compatible legacy alias for the monolith.
 */
@Deprecated
public abstract class ShopEntity extends Entity implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String language;
	
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getLanguage() {
		return language;
	}


}
