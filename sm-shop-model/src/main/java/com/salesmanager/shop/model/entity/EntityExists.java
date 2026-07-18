package com.salesmanager.shop.model.entity;

import java.io.Serializable;

/**
 * @deprecated Use {@link com.salesmanager.contracts.common.EntityExists} from shopizer-api-contracts.
 * Kept as a compile-compatible legacy alias for the monolith.
 */
@Deprecated
public class EntityExists implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean exists = false;
	
	public EntityExists() {
		
	}

	public EntityExists(boolean exists) {
		this.exists = exists;
	}

	public boolean isExists() {
		return exists;
	}

	public void setExists(boolean exists) {
		this.exists = exists;
	}

}
