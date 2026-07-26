package com.salesmanager.merchant.populator;

import org.springframework.stereotype.Component;

import com.salesmanager.contracts.merchant.MerchantStoreSnapshot;
import com.salesmanager.contracts.merchant.ReadableMerchantStore;

@Component
public class MerchantStoreSnapshotPopulator {

	public MerchantStoreSnapshot toSnapshot(ReadableMerchantStore store) {
		MerchantStoreSnapshot snapshot = new MerchantStoreSnapshot();
		snapshot.setId(store.getId());
		snapshot.setCode(store.getCode());
		snapshot.setName(store.getName());
		snapshot.setDefaultLanguage(store.getDefaultLanguage());
		snapshot.setCurrency(store.getCurrency());
		snapshot.setInBusinessSince(store.getInBusinessSince());
		snapshot.setEmail(store.getEmail());
		snapshot.setPhone(store.getPhone());
		snapshot.setTemplate(store.getTemplate());
		snapshot.setUseCache(store.isUseCache());
		snapshot.setCurrencyFormatNational(store.isCurrencyFormatNational());
		snapshot.setRetailer(store.isRetailer());
		if (store.getDimension() != null) {
			snapshot.setDimension(store.getDimension().name());
		}
		if (store.getWeight() != null) {
			snapshot.setWeight(store.getWeight().name());
		}
		snapshot.setAddress(store.getAddress());
		snapshot.setLogo(store.getLogo());
		if (store.getParent() != null) {
			snapshot.setParentCode(store.getParent().getCode());
		}
		snapshot.setSupportedLanguages(store.getSupportedLanguages());
		return snapshot;
	}
}
