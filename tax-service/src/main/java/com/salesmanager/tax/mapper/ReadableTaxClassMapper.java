package com.salesmanager.tax.mapper;

import org.springframework.stereotype.Component;

import com.salesmanager.contracts.tax.ReadableTaxClass;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.tax.taxclass.TaxClass;

@Component
public class ReadableTaxClassMapper {

	public ReadableTaxClass convert(TaxClass source, MerchantStore store, Language language) {
		ReadableTaxClass taxClass = new ReadableTaxClass();
		taxClass.setId(source.getId());
		taxClass.setCode(source.getCode());
		taxClass.setName(source.getTitle());
		taxClass.setStore(store.getCode());
		return taxClass;
	}
}
