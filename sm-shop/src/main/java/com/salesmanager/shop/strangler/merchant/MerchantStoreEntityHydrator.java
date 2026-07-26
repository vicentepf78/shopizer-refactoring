package com.salesmanager.shop.strangler.merchant;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.salesmanager.contracts.merchant.MerchantStoreSnapshot;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;

@Component
public class MerchantStoreEntityHydrator {

	public MerchantStore hydrate(MerchantStoreSnapshot snapshot) {
		if (snapshot == null) {
			return null;
		}
		MerchantStore store = new MerchantStore();
		store.setId(snapshot.getId());
		store.setCode(snapshot.getCode());
		store.setStorename(snapshot.getName());
		store.setRetailer(snapshot.isRetailer());
		store.setStoreEmailAddress(snapshot.getEmail());
		store.setStorephone(snapshot.getPhone());
		store.setStoreTemplate(snapshot.getTemplate());
		store.setUseCache(snapshot.isUseCache());
		store.setCurrencyFormatNational(snapshot.isCurrencyFormatNational());
		if (StringUtils.isNotBlank(snapshot.getDefaultLanguage())) {
			Language defaultLanguage = new Language(snapshot.getDefaultLanguage());
			store.setDefaultLanguage(defaultLanguage);
		}
		if (StringUtils.isNotBlank(snapshot.getParentCode())) {
			MerchantStore parent = new MerchantStore();
			parent.setCode(snapshot.getParentCode());
			store.setParent(parent);
		}
		return store;
	}
}
