package com.salesmanager.reference.facade;

import java.util.List;

import com.salesmanager.contracts.reference.ReadableCurrency;

public interface CurrencyFacade {

	List<ReadableCurrency> getList();
}
