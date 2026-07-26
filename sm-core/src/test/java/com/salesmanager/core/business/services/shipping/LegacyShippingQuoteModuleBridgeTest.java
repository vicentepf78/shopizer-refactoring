package com.salesmanager.core.business.services.shipping;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.salesmanager.core.business.services.payments.IntegrationContextMapper;
import com.salesmanager.core.model.common.Delivery;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.shipping.PackageDetails;
import com.salesmanager.core.model.shipping.ShippingConfiguration;
import com.salesmanager.core.model.shipping.ShippingOption;
import com.salesmanager.core.model.shipping.ShippingOrigin;
import com.salesmanager.core.model.shipping.ShippingQuote;
import com.salesmanager.core.model.system.CustomIntegrationConfiguration;
import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.salesmanager.core.model.system.IntegrationModule;
import com.salesmanager.core.model.system.ModuleConfig;
import com.salesmanager.core.modules.integration.IntegrationException;
import com.salesmanager.core.modules.integration.shipping.dto.ShippingQuoteRequestContext;
import com.salesmanager.core.modules.integration.shipping.model.ShippingQuoteModule;
import com.salesmanager.core.modules.integration.shipping.model.ShippingQuoteModuleV2;

class LegacyShippingQuoteModuleBridgeTest {

	@Test
	void getShippingQuotesPrefersHydratedIntegrationModuleFromEntityBundle() throws Exception {
		MerchantStore store = sampleStore();
		IntegrationModule hydratedModule = sampleHydratedModule();
		CapturingShippingQuoteModule delegate = new CapturingShippingQuoteModule();
		LegacyShippingEntityBundle entities = new LegacyShippingEntityBundle(new ShippingQuote(),
				Collections.emptyList(), BigDecimal.TEN, new Delivery(), new ShippingOrigin(), store,
				new ShippingConfiguration(), Locale.US, hydratedModule);
		ShippingQuoteModuleV2 bridge = new LegacyShippingQuoteModuleBridge(delegate, entities);
		ShippingQuoteRequestContext context = IntegrationContextMapper.toShippingQuoteRequestContext(store,
				Collections.emptyList(), BigDecimal.TEN, new Delivery(), new ShippingOrigin(),
				new IntegrationConfiguration(), hydratedModule, Locale.US);

		bridge.getShippingQuotes(context);

		assertThat(delegate.getCapturedModule()).isSameAs(hydratedModule);
		assertThat(delegate.getCapturedModule().getModuleConfigs()).containsKey("PROD");
		assertThat(delegate.getCapturedModule().getModuleConfigs().get("PROD").getHost()).isEqualTo("shipping.example");
	}

	private static MerchantStore sampleStore() {
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		return store;
	}

	private static IntegrationModule sampleHydratedModule() {
		ModuleConfig prodConfig = new ModuleConfig();
		prodConfig.setHost("shipping.example");
		prodConfig.setScheme("https");
		Map<String, ModuleConfig> moduleConfigs = new HashMap<>();
		moduleConfigs.put("PROD", prodConfig);

		IntegrationModule module = new IntegrationModule();
		module.setCode("usps");
		module.setModule("usps");
		module.setModuleConfigs(moduleConfigs);
		return module;
	}

	private static final class CapturingShippingQuoteModule implements ShippingQuoteModule {

		private IntegrationModule capturedModule;

		IntegrationModule getCapturedModule() {
			return capturedModule;
		}

		@Override
		public void validateModuleConfiguration(IntegrationConfiguration integrationConfiguration, MerchantStore store)
				throws IntegrationException {
		}

		@Override
		public CustomIntegrationConfiguration getCustomModuleConfiguration(MerchantStore store)
				throws IntegrationException {
			return null;
		}

		@Override
		public List<ShippingOption> getShippingQuotes(ShippingQuote quote, List<PackageDetails> packages,
				BigDecimal orderTotal, Delivery delivery, ShippingOrigin origin, MerchantStore store,
				IntegrationConfiguration configuration, IntegrationModule module,
				ShippingConfiguration shippingConfiguration, Locale locale) throws IntegrationException {
			this.capturedModule = module;
			return Collections.emptyList();
		}
	}

}
