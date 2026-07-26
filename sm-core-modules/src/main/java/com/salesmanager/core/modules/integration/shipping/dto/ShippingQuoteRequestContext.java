package com.salesmanager.core.modules.integration.shipping.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.salesmanager.core.modules.integration.common.dto.IntegrationModuleDto;
import com.salesmanager.core.modules.integration.common.dto.IntegrationStoreContext;

public class ShippingQuoteRequestContext implements Serializable {

	private static final long serialVersionUID = 1L;

	private IntegrationStoreContext store;
	private List<PackageDetailsDto> packages = new ArrayList<>();
	private BigDecimal orderTotal;
	private ShippingAddressDto delivery;
	private ShippingAddressDto origin;
	private String locale;
	private IntegrationConfiguration configuration;
	private IntegrationModuleDto module;

	public IntegrationStoreContext getStore() {
		return store;
	}

	public void setStore(IntegrationStoreContext store) {
		this.store = store;
	}

	public List<PackageDetailsDto> getPackages() {
		return packages;
	}

	public void setPackages(List<PackageDetailsDto> packages) {
		this.packages = packages;
	}

	public BigDecimal getOrderTotal() {
		return orderTotal;
	}

	public void setOrderTotal(BigDecimal orderTotal) {
		this.orderTotal = orderTotal;
	}

	public ShippingAddressDto getDelivery() {
		return delivery;
	}

	public void setDelivery(ShippingAddressDto delivery) {
		this.delivery = delivery;
	}

	public ShippingAddressDto getOrigin() {
		return origin;
	}

	public void setOrigin(ShippingAddressDto origin) {
		this.origin = origin;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
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
