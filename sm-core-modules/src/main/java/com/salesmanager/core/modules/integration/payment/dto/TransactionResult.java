package com.salesmanager.core.modules.integration.payment.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TransactionResult implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private BigDecimal amount;
	private Date transactionDate;
	private String transactionType;
	private String paymentType;
	private String details;
	private Map<String, String> transactionDetails = new HashMap<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Date getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public Map<String, String> getTransactionDetails() {
		return transactionDetails;
	}

	public void setTransactionDetails(Map<String, String> transactionDetails) {
		this.transactionDetails = transactionDetails;
	}

}
