package com.salesmanager.contracts.merchant;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.salesmanager.contracts.content.ReadableImage;
import com.salesmanager.contracts.reference.ReadableAddress;
import com.salesmanager.contracts.reference.ReadableLanguage;

/**
 * Internal store snapshot for {@code GET /internal/v1/store/{code}} and BFF hydrator.
 */
public class MerchantStoreSnapshot implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;
	private String code;
	private String name;
	private String defaultLanguage;
	private String currency;
	private String inBusinessSince;
	private String email;
	private String phone;
	private String template;
	private boolean useCache;
	private boolean currencyFormatNational;
	private boolean retailer;
	private String dimension;
	private String weight;
	private ReadableAddress address;
	private ReadableImage logo;
	private String parentCode;
	private List<ReadableLanguage> supportedLanguages = new ArrayList<>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getInBusinessSince() {
		return inBusinessSince;
	}

	public void setInBusinessSince(String inBusinessSince) {
		this.inBusinessSince = inBusinessSince;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public boolean isUseCache() {
		return useCache;
	}

	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}

	public boolean isCurrencyFormatNational() {
		return currencyFormatNational;
	}

	public void setCurrencyFormatNational(boolean currencyFormatNational) {
		this.currencyFormatNational = currencyFormatNational;
	}

	public boolean isRetailer() {
		return retailer;
	}

	public void setRetailer(boolean retailer) {
		this.retailer = retailer;
	}

	public String getDimension() {
		return dimension;
	}

	public void setDimension(String dimension) {
		this.dimension = dimension;
	}

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}

	public ReadableAddress getAddress() {
		return address;
	}

	public void setAddress(ReadableAddress address) {
		this.address = address;
	}

	public ReadableImage getLogo() {
		return logo;
	}

	public void setLogo(ReadableImage logo) {
		this.logo = logo;
	}

	public String getParentCode() {
		return parentCode;
	}

	public void setParentCode(String parentCode) {
		this.parentCode = parentCode;
	}

	public List<ReadableLanguage> getSupportedLanguages() {
		return supportedLanguages;
	}

	public void setSupportedLanguages(List<ReadableLanguage> supportedLanguages) {
		this.supportedLanguages = supportedLanguages;
	}

}
