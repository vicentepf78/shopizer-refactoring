package com.salesmanager.shop.model.entity;

import java.util.List;

/**
 * @deprecated Use {@link com.salesmanager.contracts.common.ReadableEntityList} from shopizer-api-contracts.
 * Kept as a compile-compatible legacy alias for the monolith.
 */
@Deprecated
public class ReadableEntityList<T> extends ReadableList {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<T> items;

	public List<T> getItems() {
		return items;
	}

	public void setItems(List<T> items) {
		this.items = items;
	}

}
