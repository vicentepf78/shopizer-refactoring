package com.salesmanager.contracts.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ValueList implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<String> values = new ArrayList<>();

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

}
