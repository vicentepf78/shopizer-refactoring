package com.salesmanager.contracts.customer;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Checkout-relevant customer projection without lazy JPA collections.
 * <p>
 * Used by {@code CheckoutCommand} and checkout outbox payloads (ADR-005).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerSnapshot implements Serializable {

	private static final long serialVersionUID = 1L;

	private int schemaVersion = 1;
	private Long id;
	private String emailAddress;
	private String nick;
	private String company;
	private boolean anonymous;
	private String language;
	private AddressSnapshot billing;
	private AddressSnapshot delivery;

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

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public boolean isAnonymous() {
		return anonymous;
	}

	public void setAnonymous(boolean anonymous) {
		this.anonymous = anonymous;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public AddressSnapshot getBilling() {
		return billing;
	}

	public void setBilling(AddressSnapshot billing) {
		this.billing = billing;
	}

	public AddressSnapshot getDelivery() {
		return delivery;
	}

	public void setDelivery(AddressSnapshot delivery) {
		this.delivery = delivery;
	}

}
