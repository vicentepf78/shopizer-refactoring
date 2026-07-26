package com.salesmanager.contracts.order;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderLineSnapshot implements Serializable {

	private static final long serialVersionUID = 1L;

	private String sku;
	private String productName;
	private int quantity;
	private BigDecimal oneTimeCharge;

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getOneTimeCharge() {
		return oneTimeCharge;
	}

	public void setOneTimeCharge(BigDecimal oneTimeCharge) {
		this.oneTimeCharge = oneTimeCharge;
	}

}
