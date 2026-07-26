package com.salesmanager.shop.tenant;

import com.salesmanager.contracts.tenant.LanguageCode;
import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.business.exception.ConversionException;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;

public interface TenantEntityBridge {

	MerchantStore resolveStore(MerchantStoreId storeId) throws ConversionException;

	Language resolveLanguage(LanguageCode languageCode) throws ConversionException;

}
