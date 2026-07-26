package com.salesmanager.core.modules.integration.payment.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.salesmanager.core.modules.integration.common.dto.IntegrationModuleDto;
import com.salesmanager.core.modules.integration.common.dto.IntegrationStoreContext;

public class PaymentRefundContext implements Serializable {

	private static final long serialVersionUID = 1L;

	private IntegrationStoreContext store;
	private Long customerId;
	private Long orderId;
	private boolean partial;
	private BigDecimal amount;
	private TransactionResult refundableTransaction;
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

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public boolean isPartial() {
		return partial;
	}

	public void setPartial(boolean partial) {
		this.partial = partial;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public TransactionResult getRefundableTransaction() {
		return refundableTransaction;
	}

	public void setRefundableTransaction(TransactionResult refundableTransaction) {
		this.refundableTransaction = refundableTransaction;
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
