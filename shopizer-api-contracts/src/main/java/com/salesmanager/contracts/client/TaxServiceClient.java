package com.salesmanager.contracts.client;

import com.salesmanager.contracts.common.Entity;
import com.salesmanager.contracts.common.ReadableEntityList;
import com.salesmanager.contracts.tax.PersistableTaxClass;
import com.salesmanager.contracts.tax.PersistableTaxRate;
import com.salesmanager.contracts.tax.ReadableTaxClass;
import com.salesmanager.contracts.tax.ReadableTaxRate;

public interface TaxServiceClient {

	Entity createTaxClass(PersistableTaxClass taxClass, String storeCode, String langCode);

	void updateTaxClass(Long id, PersistableTaxClass taxClass, String storeCode, String langCode);

	void deleteTaxClass(Long id, String storeCode, String langCode);

	boolean existsTaxClass(String code, String storeCode, String langCode);

	ReadableEntityList<ReadableTaxClass> taxClasses(String storeCode, String langCode);

	ReadableTaxClass taxClass(String code, String storeCode, String langCode);

	Entity createTaxRate(PersistableTaxRate taxRate, String storeCode, String langCode);

	void updateTaxRate(Long id, PersistableTaxRate taxRate, String storeCode, String langCode);

	void deleteTaxRate(Long id, String storeCode, String langCode);

	boolean existsTaxRate(String code, String storeCode, String langCode);

	ReadableEntityList<ReadableTaxRate> taxRates(String storeCode, String langCode);

	ReadableTaxRate taxRate(Long id, String storeCode, String langCode);

}
