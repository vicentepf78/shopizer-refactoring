package com.salesmanager.core.modules.integration.shipping.model;

import java.util.List;

import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.salesmanager.core.modules.integration.IntegrationException;
import com.salesmanager.core.modules.integration.common.dto.IntegrationStoreContext;
import com.salesmanager.core.modules.integration.shipping.dto.ShippingOptionDto;
import com.salesmanager.core.modules.integration.shipping.dto.ShippingQuoteRequestContext;

public interface ShippingQuoteModuleV2 {

	void validateModuleConfiguration(IntegrationConfiguration configuration, IntegrationStoreContext store)
			throws IntegrationException;

	List<ShippingOptionDto> getShippingQuotes(ShippingQuoteRequestContext context) throws IntegrationException;

}
