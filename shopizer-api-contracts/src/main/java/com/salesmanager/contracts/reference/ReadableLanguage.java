package com.salesmanager.contracts.reference;

import java.io.Serializable;

public class ReadableLanguage implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;
	private String code;
	private int sortOrder;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

}
