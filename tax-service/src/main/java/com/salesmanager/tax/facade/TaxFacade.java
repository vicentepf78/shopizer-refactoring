package com.salesmanager.tax.facade;

import com.salesmanager.contracts.common.Entity;
import com.salesmanager.contracts.common.ReadableEntityList;
import com.salesmanager.contracts.tax.PersistableTaxClass;
import com.salesmanager.contracts.tax.PersistableTaxRate;
import com.salesmanager.contracts.tax.ReadableTaxClass;
import com.salesmanager.contracts.tax.ReadableTaxRate;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;

public interface TaxFacade {

	Entity createTaxClass(PersistableTaxClass taxClass, MerchantStore store, Language language);

	void updateTaxClass(Long id, PersistableTaxClass taxClass, MerchantStore store, Language language);

	void deleteTaxClass(Long id, MerchantStore store, Language language);

	ReadableTaxClass taxClass(String code, MerchantStore store, Language language);

	ReadableEntityList<ReadableTaxClass> taxClasses(MerchantStore store, Language language);

	boolean existsTaxClass(String code, MerchantStore store, Language language);

	Entity createTaxRate(PersistableTaxRate taxRate, MerchantStore store, Language language);

	void updateTaxRate(Long id, PersistableTaxRate taxRate, MerchantStore store, Language language);

	void deleteTaxRate(Long id, MerchantStore store, Language language);

	ReadableTaxRate taxRate(Long id, MerchantStore store, Language language);

	ReadableEntityList<ReadableTaxRate> taxRates(MerchantStore store, Language language);

	boolean existsTaxRate(String code, MerchantStore store, Language language);
}
