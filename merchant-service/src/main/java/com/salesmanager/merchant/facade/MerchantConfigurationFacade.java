package com.salesmanager.merchant.facade;

import com.salesmanager.contracts.merchant.Configs;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;

public interface MerchantConfigurationFacade {

	Configs getMerchantConfig(MerchantStore merchantStore, Language language);
}
