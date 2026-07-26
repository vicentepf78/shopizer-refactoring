package com.salesmanager.core.business.services.shipping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import com.salesmanager.core.model.common.Delivery;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.shipping.PackageDetails;
import com.salesmanager.core.model.shipping.ShippingConfiguration;
import com.salesmanager.core.model.shipping.ShippingOrigin;
import com.salesmanager.core.model.shipping.ShippingQuote;
import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.salesmanager.core.model.system.IntegrationModule;

final class LegacyShippingEntityBundle {

	private final ShippingQuote quote;
	private final List<PackageDetails> packages;
	private final BigDecimal orderTotal;
	private final Delivery delivery;
	private final ShippingOrigin origin;
	private final MerchantStore store;
	private final ShippingConfiguration shippingConfiguration;
	private final Locale locale;
	private final IntegrationModule integrationModule;

	LegacyShippingEntityBundle(ShippingQuote quote, List<PackageDetails> packages, BigDecimal orderTotal,
			Delivery delivery, ShippingOrigin origin, MerchantStore store, ShippingConfiguration shippingConfiguration,
			Locale locale, IntegrationModule integrationModule) {
		this.quote = quote;
		this.packages = packages;
		this.orderTotal = orderTotal;
		this.delivery = delivery;
		this.origin = origin;
		this.store = store;
		this.shippingConfiguration = shippingConfiguration;
		this.locale = locale;
		this.integrationModule = integrationModule;
	}

	ShippingQuote getQuote() {
		return quote;
	}

	List<PackageDetails> getPackages() {
		return packages;
	}

	BigDecimal getOrderTotal() {
		return orderTotal;
	}

	Delivery getDelivery() {
		return delivery;
	}

	ShippingOrigin getOrigin() {
		return origin;
	}

	MerchantStore getStore() {
		return store;
	}

	ShippingConfiguration getShippingConfiguration() {
		return shippingConfiguration;
	}

	Locale getLocale() {
		return locale;
	}

	IntegrationModule getIntegrationModule() {
		return integrationModule;
	}

}
