package com.salesmanager.contracts.catalog;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProductSnapshotVariant implements Serializable {

	private static final long serialVersionUID = 1L;

	private Map<String, String> options = new LinkedHashMap<>();
	private String sku;

	public Map<String, String> getOptions() {
		return options;
	}

	public void setOptions(Map<String, String> options) {
		this.options = options;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

}
