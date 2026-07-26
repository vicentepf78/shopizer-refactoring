package com.salesmanager.contracts.order;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Checkout-relevant order projection for {@code CheckoutCommand} and outbox JSON payloads.
 * <p>
 * Outbox stage field usage is documented in
 * {@code com.salesmanager.core.business.services.checkout.CheckoutOutboxSnapshotDesign}.
 */
public class OrderSnapshot implements Serializable {

	private static final long serialVersionUID = 1L;

	private int schemaVersion = 1;
	private Long id;
	private String status;
	private Long customerId;
	private String customerEmailAddress;
	private String storeCode;
	private String currencyCode;
	private BigDecimal currencyValue;
	private BigDecimal total;
	private String paymentType;
	private String paymentModuleCode;
	private String shippingModuleCode;
	private String shoppingCartCode;
	private List<OrderLineSnapshot> lines = new ArrayList<>();
	private List<OrderTotalSnapshot> totals = new ArrayList<>();

	public int getSchemaVersion() {
		return schemaVersion;
	}

	public void setSchemaVersion(int schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public String getCustomerEmailAddress() {
		return customerEmailAddress;
	}

	public void setCustomerEmailAddress(String customerEmailAddress) {
		this.customerEmailAddress = customerEmailAddress;
	}

	public String getStoreCode() {
		return storeCode;
	}

	public void setStoreCode(String storeCode) {
		this.storeCode = storeCode;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public BigDecimal getCurrencyValue() {
		return currencyValue;
	}

	public void setCurrencyValue(BigDecimal currencyValue) {
		this.currencyValue = currencyValue;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getPaymentModuleCode() {
		return paymentModuleCode;
	}

	public void setPaymentModuleCode(String paymentModuleCode) {
		this.paymentModuleCode = paymentModuleCode;
	}

	public String getShippingModuleCode() {
		return shippingModuleCode;
	}

	public void setShippingModuleCode(String shippingModuleCode) {
		this.shippingModuleCode = shippingModuleCode;
	}

	public String getShoppingCartCode() {
		return shoppingCartCode;
	}

	public void setShoppingCartCode(String shoppingCartCode) {
		this.shoppingCartCode = shoppingCartCode;
	}

	public List<OrderLineSnapshot> getLines() {
		return lines;
	}

	public void setLines(List<OrderLineSnapshot> lines) {
		this.lines = lines;
	}

	public List<OrderTotalSnapshot> getTotals() {
		return totals;
	}

	public void setTotals(List<OrderTotalSnapshot> totals) {
		this.totals = totals;
	}

}
