package com.salesmanager.shop.store.controller.shipping.facade;

import java.util.List;

import com.salesmanager.contracts.tenant.LanguageCode;
import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.model.shipping.PackageDetails;
import com.salesmanager.shop.model.references.PersistableAddress;
import com.salesmanager.shop.model.references.ReadableAddress;
import com.salesmanager.shop.model.references.ReadableCountry;
import com.salesmanager.shop.model.shipping.ExpeditionConfiguration;

public interface ShippingFacade {
	
	ExpeditionConfiguration getExpeditionConfiguration(MerchantStoreId storeId, LanguageCode language);
	void saveExpeditionConfiguration(ExpeditionConfiguration expedition, MerchantStoreId storeId);
	
	
	ReadableAddress getShippingOrigin(MerchantStoreId storeId);
	void saveShippingOrigin(PersistableAddress address, MerchantStoreId storeId);
	

	void createPackage(PackageDetails packaging, MerchantStoreId storeId);
	
	PackageDetails getPackage(String code, MerchantStoreId storeId);
	
	/**
	 * List of configured ShippingCountry for a given store
	 * @param storeId
	 * @param language
	 * @return
	 */
	List<ReadableCountry> shipToCountry(MerchantStoreId storeId, LanguageCode language);
	
	List<PackageDetails> listPackages(MerchantStoreId storeId);
	
	void updatePackage(String code, PackageDetails packaging, MerchantStoreId storeId);
	
	void deletePackage(String code, MerchantStoreId storeId);

}
