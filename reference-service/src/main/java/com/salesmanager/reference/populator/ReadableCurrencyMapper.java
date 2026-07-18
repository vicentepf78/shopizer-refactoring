package com.salesmanager.reference.populator;

import com.salesmanager.contracts.reference.ReadableCurrency;
import com.salesmanager.core.model.reference.currency.Currency;

public final class ReadableCurrencyMapper {

	private ReadableCurrencyMapper() {
	}

	public static ReadableCurrency toDto(Currency source) {
		ReadableCurrency target = new ReadableCurrency();
		if (source.getId() != null) {
			target.setId(source.getId());
		}
		target.setCode(source.getCode());
		target.setName(source.getName());
		if (source.getCurrency() != null) {
			target.setSymbol(source.getSymbol());
		}
		target.setSupported(Boolean.TRUE.equals(source.getSupported()));
		return target;
	}
}
