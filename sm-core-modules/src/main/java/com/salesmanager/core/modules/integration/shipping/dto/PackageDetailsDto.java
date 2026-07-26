package com.salesmanager.core.modules.integration.shipping.dto;

import java.io.Serializable;

public class PackageDetailsDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private String code;
	private double shippingWeight;
	private double shippingMaxWeight;
	private double shippingLength;
	private double shippingHeight;
	private double shippingWidth;
	private int shippingQuantity;
	private int treshold;
	private String type;
	private String itemName = "";

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public double getShippingWeight() {
		return shippingWeight;
	}

	public void setShippingWeight(double shippingWeight) {
		this.shippingWeight = shippingWeight;
	}

	public double getShippingMaxWeight() {
		return shippingMaxWeight;
	}

	public void setShippingMaxWeight(double shippingMaxWeight) {
		this.shippingMaxWeight = shippingMaxWeight;
	}

	public double getShippingLength() {
		return shippingLength;
	}

	public void setShippingLength(double shippingLength) {
		this.shippingLength = shippingLength;
	}

	public double getShippingHeight() {
		return shippingHeight;
	}

	public void setShippingHeight(double shippingHeight) {
		this.shippingHeight = shippingHeight;
	}

	public double getShippingWidth() {
		return shippingWidth;
	}

	public void setShippingWidth(double shippingWidth) {
		this.shippingWidth = shippingWidth;
	}

	public int getShippingQuantity() {
		return shippingQuantity;
	}

	public void setShippingQuantity(int shippingQuantity) {
		this.shippingQuantity = shippingQuantity;
	}

	public int getTreshold() {
		return treshold;
	}

	public void setTreshold(int treshold) {
		this.treshold = treshold;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

}
