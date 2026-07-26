package com.salesmanager.merchant.api.v1.system;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.salesmanager.contracts.merchant.Configs;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.merchant.facade.MerchantConfigurationFacade;

@RestController
@RequestMapping("/api/v1")
public class PublicConfigsController {

	private final MerchantConfigurationFacade configurationFacade;

	public PublicConfigsController(MerchantConfigurationFacade configurationFacade) {
		this.configurationFacade = configurationFacade;
	}

	@GetMapping("/config")
	public Configs getConfig(MerchantStore merchantStore, Language language) {
		return configurationFacade.getMerchantConfig(merchantStore, language);
	}
}
