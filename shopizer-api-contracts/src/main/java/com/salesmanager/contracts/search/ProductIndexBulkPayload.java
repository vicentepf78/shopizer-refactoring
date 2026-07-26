package com.salesmanager.contracts.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;

public class ProductIndexBulkPayload implements Serializable {

	public static final int MAX_BATCH_SIZE = 50;

	private static final long serialVersionUID = 1L;

	@Size(max = MAX_BATCH_SIZE)
	private List<ProductIndexPayload> payloads = new ArrayList<>();

	public List<ProductIndexPayload> getPayloads() {
		return payloads;
	}

	public void setPayloads(List<ProductIndexPayload> payloads) {
		this.payloads = payloads;
	}

}
