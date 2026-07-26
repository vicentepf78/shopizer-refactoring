package com.salesmanager.shop.tenant;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.salesmanager.contracts.tenant.LanguageCode;
import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.business.exception.ConversionException;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.merchant.MerchantStoreService;
import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.business.tenant.TenantEntityBridgeRegistry;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;

@Component
public class TenantEntityBridgeImpl implements TenantEntityBridge {

	private final MerchantStoreService merchantStoreService;
	private final LanguageService languageService;

	public TenantEntityBridgeImpl(MerchantStoreService merchantStoreService, LanguageService languageService) {
		this.merchantStoreService = merchantStoreService;
		this.languageService = languageService;
	}

	@PostConstruct
	void registerBridge() {
		TenantEntityBridgeRegistry.register(this);
	}

	@Override
	public MerchantStore resolveStore(MerchantStoreId storeId) throws ConversionException {
		try {
			return merchantStoreService.getByCode(storeId.getCode());
		} catch (ServiceException e) {
			throw new ConversionException(e);
		}
	}

	@Override
	public Language resolveLanguage(LanguageCode languageCode) throws ConversionException {
		try {
			return languageService.getByCode(languageCode.getCode());
		} catch (ServiceException e) {
			throw new ConversionException(e);
		}
	}

}
