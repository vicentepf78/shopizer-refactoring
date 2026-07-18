package com.salesmanager.shop.model.references;

import java.io.Serializable;

/**
 * @deprecated Use {@link com.salesmanager.contracts.reference.ReadableLanguage} from shopizer-api-contracts.
 * Kept as a compile-compatible legacy alias for the monolith.
 */
@Deprecated
public class ReadableLanguage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String code;
	private int id;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

}
