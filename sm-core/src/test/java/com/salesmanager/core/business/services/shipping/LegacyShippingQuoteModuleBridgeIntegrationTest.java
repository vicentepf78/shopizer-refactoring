package com.salesmanager.core.business.services.shipping;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.salesmanager.core.business.modules.integration.shipping.impl.PriceByDistanceShippingQuoteRules;
import com.salesmanager.core.business.services.payments.IntegrationContextMapper;
import com.salesmanager.core.model.common.Delivery;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.shipping.PackageDetails;
import com.salesmanager.core.model.shipping.ShippingConfiguration;
import com.salesmanager.core.model.shipping.ShippingOrigin;
import com.salesmanager.core.model.shipping.ShippingQuote;
import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.salesmanager.core.model.system.IntegrationModule;
import com.salesmanager.core.modules.constants.Constants;
import com.salesmanager.core.modules.integration.shipping.dto.ShippingOptionDto;
import com.salesmanager.core.modules.integration.shipping.dto.ShippingQuoteRequestContext;
import com.salesmanager.core.modules.integration.shipping.model.ShippingQuoteModuleV2;

class LegacyShippingQuoteModuleBridgeIntegrationTest {

	@Test
	void priceByDistanceGetShippingQuotesViaLegacyBridgeReturnsOptions() throws Exception {
		MerchantStore store = sampleStore();
		ShippingQuote quote = sampleQuoteWithDistance(10D);
		PackageDetails packageDetails = samplePackage();
		Delivery delivery = sampleDelivery();
		IntegrationConfiguration configuration = sampleConfiguration();
		IntegrationModule module = sampleModule();

		LegacyShippingEntityBundle entities = new LegacyShippingEntityBundle(quote,
				Collections.singletonList(packageDetails), new BigDecimal("49.99"), delivery, new ShippingOrigin(),
				store, new ShippingConfiguration(), Locale.US, module);
		ShippingQuoteModuleV2 moduleV2 = new LegacyShippingQuoteModuleBridge(new PriceByDistanceShippingQuoteRules(),
				entities);
		ShippingQuoteRequestContext context = IntegrationContextMapper.toShippingQuoteRequestContext(store,
				Collections.singletonList(packageDetails), new BigDecimal("49.99"), delivery, new ShippingOrigin(),
				configuration, module, Locale.US);

		List<ShippingOptionDto> options = moduleV2.getShippingQuotes(context);

		assertThat(options).hasSize(1);
		assertThat(options.get(0).getShippingModuleCode())
				.isEqualTo(PriceByDistanceShippingQuoteRules.MODULE_CODE);
		assertThat(options.get(0).getOptionPrice()).isEqualByComparingTo("20");
	}

	private static MerchantStore sampleStore() {
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		return store;
	}

	private static ShippingQuote sampleQuoteWithDistance(double distanceKm) {
		ShippingQuote quote = new ShippingQuote();
		Map<String, Object> quoteInformations = new HashMap<>();
		quoteInformations.put(Constants.DISTANCE_KEY, distanceKm);
		quote.setQuoteInformations(quoteInformations);
		return quote;
	}

	private static PackageDetails samplePackage() {
		PackageDetails packageDetails = new PackageDetails();
		packageDetails.setShippingWeight(1D);
		packageDetails.setShippingHeight(10D);
		packageDetails.setShippingWidth(10D);
		packageDetails.setShippingLength(10D);
		return packageDetails;
	}

	private static Delivery sampleDelivery() {
		Delivery delivery = new Delivery();
		delivery.setPostalCode("90210");
		Country country = new Country();
		country.setIsoCode("US");
		country.setName("United States");
		delivery.setCountry(country);
		return delivery;
	}

	private static IntegrationConfiguration sampleConfiguration() {
		IntegrationConfiguration configuration = new IntegrationConfiguration();
		configuration.setModuleCode(PriceByDistanceShippingQuoteRules.MODULE_CODE);
		configuration.setActive(true);
		return configuration;
	}

	private static IntegrationModule sampleModule() {
		IntegrationModule module = new IntegrationModule();
		module.setCode(PriceByDistanceShippingQuoteRules.MODULE_CODE);
		module.setModule(PriceByDistanceShippingQuoteRules.MODULE_CODE);
		return module;
	}

}
