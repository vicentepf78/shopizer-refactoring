package com.salesmanager.core.modules.integration.payment.dto;

import java.io.Serializable;

import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.salesmanager.core.modules.integration.common.dto.IntegrationModuleDto;
import com.salesmanager.core.modules.integration.common.dto.IntegrationStoreContext;

public class PaymentCaptureContext implements Serializable {

	private static final long serialVersionUID = 1L;

	private IntegrationStoreContext store;
	private Long customerId;
	private String customerEmail;
	private Long orderId;
	private TransactionResult capturableTransaction;
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

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public TransactionResult getCapturableTransaction() {
		return capturableTransaction;
	}

	public void setCapturableTransaction(TransactionResult capturableTransaction) {
		this.capturableTransaction = capturableTransaction;
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
