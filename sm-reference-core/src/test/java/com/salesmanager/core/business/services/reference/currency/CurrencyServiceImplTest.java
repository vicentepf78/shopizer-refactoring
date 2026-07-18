package com.salesmanager.core.business.services.reference.currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.core.business.repositories.reference.currency.CurrencyRepository;
import com.salesmanager.core.model.reference.currency.Currency;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceImplTest {

	@Mock
	private CurrencyRepository currencyRepository;

	private CurrencyServiceImpl currencyService;

	@BeforeEach
	void setUp() {
		currencyService = new CurrencyServiceImpl(currencyRepository);
	}

	@Test
	void getByCode_delegatesToRepository() {
		Currency currency = new Currency();
		when(currencyRepository.getByCode("USD")).thenReturn(currency);

		assertEquals(currency, currencyService.getByCode("USD"));
	}
}
