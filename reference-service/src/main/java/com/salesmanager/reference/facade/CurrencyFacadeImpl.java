package com.salesmanager.reference.facade;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.salesmanager.contracts.reference.ReadableCurrency;
import com.salesmanager.core.business.services.reference.currency.CurrencyService;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.reference.populator.ReadableCurrencyMapper;
import com.salesmanager.reference.support.ResourceNotFoundException;

@Service
public class CurrencyFacadeImpl implements CurrencyFacade {

	private final CurrencyService currencyService;

	public CurrencyFacadeImpl(CurrencyService currencyService) {
		this.currencyService = currencyService;
	}

	@Override
	public List<ReadableCurrency> getList() {
		List<Currency> currencyList = currencyService.list();
		if (currencyList == null || currencyList.isEmpty()) {
			throw new ResourceNotFoundException("No currencies found");
		}
		return currencyList.stream()
				.sorted(Comparator.comparing(Currency::getCode, Comparator.nullsLast(String::compareTo)))
				.map(ReadableCurrencyMapper::toDto)
				.collect(Collectors.toList());
	}
}
