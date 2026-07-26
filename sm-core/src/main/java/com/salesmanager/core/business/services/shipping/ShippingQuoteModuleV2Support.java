package com.salesmanager.core.business.services.shipping;

import com.salesmanager.core.modules.integration.shipping.model.ShippingQuoteModule;
import com.salesmanager.core.modules.integration.shipping.model.ShippingQuoteModuleV2;

final class ShippingQuoteModuleV2Support {

	private ShippingQuoteModuleV2Support() {
	}

	static ShippingQuoteModuleV2 resolve(ShippingQuoteModule module, LegacyShippingEntityBundle entities) {
		if (module instanceof ShippingQuoteModuleV2) {
			return (ShippingQuoteModuleV2) module;
		}
		return new LegacyShippingQuoteModuleBridge(module, entities);
	}

}
