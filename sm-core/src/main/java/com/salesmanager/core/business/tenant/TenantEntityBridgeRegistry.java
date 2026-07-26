package com.salesmanager.core.business.tenant;

import com.salesmanager.contracts.tenant.LanguageCode;
import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.business.exception.ConversionException;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;

public final class TenantEntityBridgeRegistry {

	public interface Bridge {

		MerchantStore resolveStore(MerchantStoreId storeId) throws ConversionException;

		Language resolveLanguage(LanguageCode languageCode) throws ConversionException;

	}

	private static Bridge bridge;

	private TenantEntityBridgeRegistry() {
	}

	public static void register(Bridge tenantBridge) {
		bridge = tenantBridge;
	}

	public static Bridge require() {
		if (bridge == null) {
			throw new IllegalStateException("TenantEntityBridge is not registered");
		}
		return bridge;
	}

	public static void clear() {
		bridge = null;
	}

}
