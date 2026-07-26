package com.salesmanager.core.modules.integration.common.dto;

import java.io.Serializable;

import com.salesmanager.contracts.tenant.LanguageCode;
import com.salesmanager.contracts.tenant.MerchantStoreId;

public class IntegrationStoreContext implements Serializable {

	private static final long serialVersionUID = 1L;

	private MerchantStoreId storeId;
	private String currencyCode;
	private LanguageCode defaultLanguage;

	public MerchantStoreId getStoreId() {
		return storeId;
	}

	public void setStoreId(MerchantStoreId storeId) {
		this.storeId = storeId;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public LanguageCode getDefaultLanguage() {
		return defaultLanguage;
	}

	public void setDefaultLanguage(LanguageCode defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}

}
