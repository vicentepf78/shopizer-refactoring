package com.salesmanager.core.business.services.shipping;

import java.util.List;

import com.salesmanager.core.business.services.payments.IntegrationContextMapper;
import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.salesmanager.core.model.system.IntegrationModule;
import com.salesmanager.core.modules.integration.common.dto.IntegrationModuleDto;
import com.salesmanager.core.modules.integration.IntegrationException;
import com.salesmanager.core.modules.integration.common.dto.IntegrationStoreContext;
import com.salesmanager.core.modules.integration.shipping.dto.ShippingOptionDto;
import com.salesmanager.core.modules.integration.shipping.dto.ShippingQuoteRequestContext;
import com.salesmanager.core.modules.integration.shipping.model.ShippingQuoteModule;
import com.salesmanager.core.modules.integration.shipping.model.ShippingQuoteModuleV2;

public class LegacyShippingQuoteModuleBridge implements ShippingQuoteModuleV2 {

	private final ShippingQuoteModule delegate;
	private final LegacyShippingEntityBundle entities;

	public LegacyShippingQuoteModuleBridge(ShippingQuoteModule delegate, LegacyShippingEntityBundle entities) {
		this.delegate = delegate;
		this.entities = entities;
	}

	@Override
	public void validateModuleConfiguration(IntegrationConfiguration configuration, IntegrationStoreContext store)
			throws IntegrationException {
		delegate.validateModuleConfiguration(configuration, entities.getStore());
	}

	@Override
	public List<ShippingOptionDto> getShippingQuotes(ShippingQuoteRequestContext context) throws IntegrationException {
		return IntegrationContextMapper.toShippingOptionDtos(delegate.getShippingQuotes(entities.getQuote(),
				entities.getPackages(), entities.getOrderTotal(), entities.getDelivery(), entities.getOrigin(),
				entities.getStore(), context.getConfiguration(), resolveModule(context.getModule()),
				entities.getShippingConfiguration(), entities.getLocale()));
	}

	private IntegrationModule resolveModule(IntegrationModuleDto module) {
		if (entities.getIntegrationModule() != null) {
			return entities.getIntegrationModule();
		}
		return IntegrationContextMapper.toModule(module);
	}

}
