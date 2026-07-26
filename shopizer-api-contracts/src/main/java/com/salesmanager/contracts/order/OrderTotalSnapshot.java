package com.salesmanager.contracts.order;

import java.io.Serializable;
import java.math.BigDecimal;

public class OrderTotalSnapshot implements Serializable {

	private static final long serialVersionUID = 1L;

	private String code;
	private String title;
	private String text;
	private BigDecimal value;
	private String module;
	private String orderTotalType;
	private int sortOrder;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getOrderTotalType() {
		return orderTotalType;
	}

	public void setOrderTotalType(String orderTotalType) {
		this.orderTotalType = orderTotalType;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

}
