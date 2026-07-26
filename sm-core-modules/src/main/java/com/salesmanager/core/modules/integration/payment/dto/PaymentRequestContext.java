package com.salesmanager.core.modules.integration.payment.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.salesmanager.core.modules.integration.common.dto.IntegrationModuleDto;
import com.salesmanager.core.modules.integration.common.dto.IntegrationStoreContext;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentRequestContext implements Serializable {

	private static final long serialVersionUID = 1L;

	private IntegrationStoreContext store;
	private Long customerId;
	private String customerEmail;
	private List<PaymentLineItemDto> lineItems = new ArrayList<>();
	private BigDecimal amount;
	private String paymentModuleCode;
	private String paymentType;
	private String transactionType;
	private String currencyCode;
	private Map<String, String> paymentMetaData = new HashMap<>();
	private IntegrationConfiguration configuration;
	private IntegrationModuleDto module;

	public IntegrationStoreContext getStore() {
		return store;
	}

	public void setStore(IntegrationStoreContext store) {
		this.store = store;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public String getCustomerEmail() {
		return customerEmail;
	}

	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}

	public List<PaymentLineItemDto> getLineItems() {
		return lineItems;
	}

	public void setLineItems(List<PaymentLineItemDto> lineItems) {
		this.lineItems = lineItems;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getPaymentModuleCode() {
		return paymentModuleCode;
	}

	public void setPaymentModuleCode(String paymentModuleCode) {
		this.paymentModuleCode = paymentModuleCode;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public Map<String, String> getPaymentMetaData() {
		return paymentMetaData;
	}

	public void setPaymentMetaData(Map<String, String> paymentMetaData) {
		this.paymentMetaData = paymentMetaData;
	}

	public IntegrationConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(IntegrationConfiguration configuration) {
		this.configuration = configuration;
	}

	public IntegrationModuleDto getModule() {
		return module;
	}

	public void setModule(IntegrationModuleDto module) {
		this.module = module;
	}

}
