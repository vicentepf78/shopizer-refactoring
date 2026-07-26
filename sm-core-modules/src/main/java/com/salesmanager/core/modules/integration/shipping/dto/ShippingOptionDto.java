package com.salesmanager.core.modules.integration.shipping.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class ShippingOptionDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private BigDecimal optionPrice;
	private String optionName;
	private String optionCode;
	private String optionDeliveryDate;
	private String optionShippingDate;
	private String optionPriceText;
	private String optionId;
	private String description;
	private String shippingModuleCode;
	private String note;
	private String estimatedNumberOfDays;

	public BigDecimal getOptionPrice() {
		return optionPrice;
	}

	public void setOptionPrice(BigDecimal optionPrice) {
		this.optionPrice = optionPrice;
	}

	public String getOptionName() {
		return optionName;
	}

	public void setOptionName(String optionName) {
		this.optionName = optionName;
	}

	public String getOptionCode() {
		return optionCode;
	}

	public void setOptionCode(String optionCode) {
		this.optionCode = optionCode;
	}

	public String getOptionDeliveryDate() {
		return optionDeliveryDate;
	}

	public void setOptionDeliveryDate(String optionDeliveryDate) {
		this.optionDeliveryDate = optionDeliveryDate;
	}

	public String getOptionShippingDate() {
		return optionShippingDate;
	}

	public void setOptionShippingDate(String optionShippingDate) {
		this.optionShippingDate = optionShippingDate;
	}

	public String getOptionPriceText() {
		return optionPriceText;
	}

	public void setOptionPriceText(String optionPriceText) {
		this.optionPriceText = optionPriceText;
	}

	public String getOptionId() {
		return optionId;
	}

	public void setOptionId(String optionId) {
		this.optionId = optionId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getShippingModuleCode() {
		return shippingModuleCode;
	}

	public void setShippingModuleCode(String shippingModuleCode) {
		this.shippingModuleCode = shippingModuleCode;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getEstimatedNumberOfDays() {
		return estimatedNumberOfDays;
	}

	public void setEstimatedNumberOfDays(String estimatedNumberOfDays) {
		this.estimatedNumberOfDays = estimatedNumberOfDays;
	}

}
