package com.salesmanager.merchant.facade;

import java.util.List;

import com.salesmanager.contracts.merchant.MerchantStoreSnapshot;
import com.salesmanager.contracts.merchant.PersistableBrand;
import com.salesmanager.contracts.merchant.PersistableMerchantStore;
import com.salesmanager.contracts.merchant.ReadableBrand;
import com.salesmanager.contracts.merchant.ReadableMerchantStore;
import com.salesmanager.contracts.merchant.ReadableMerchantStoreList;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.merchant.MerchantStoreCriteria;
import com.salesmanager.core.model.reference.language.Language;

public interface StoreFacade {

	MerchantStore get(String code);

	MerchantStore getByCode(String code);

	List<Language> supportedLanguages(MerchantStore store);

	ReadableMerchantStore getByCode(String code, String lang);

	ReadableMerchantStore getFullByCode(String code, String lang);

	ReadableMerchantStore getByCode(String code, Language language);

	ReadableMerchantStore getFullByCode(String code, Language language);

	boolean existByCode(String code);

	ReadableMerchantStoreList getByCriteria(MerchantStoreCriteria criteria, Language lang);

	ReadableMerchantStoreList getChildStores(Language language, String code, int page, int count);

	void create(PersistableMerchantStore store);

	void update(PersistableMerchantStore store);

	void delete(String code);

	ReadableBrand getBrand(String code);

	void createBrand(String merchantStoreCode, PersistableBrand brand);

	void deleteLogo(String code);

	void addStoreLogo(String code, String fileName, byte[] content, String contentType);

	List<ReadableMerchantStore> getMerchantStoreNames(MerchantStoreCriteria criteria);

	ReadableMerchantStoreList findAll(MerchantStoreCriteria criteria, Language language, int page, int count);

	MerchantStoreSnapshot getSnapshot(String code, Language language);
}
