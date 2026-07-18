package com.salesmanager.reference.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.contracts.reference.ReadableCurrency;
import com.salesmanager.core.business.services.reference.currency.CurrencyService;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.reference.support.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class CurrencyFacadeImplTest {

	@Mock
	private CurrencyService currencyService;

	@InjectMocks
	private CurrencyFacadeImpl currencyFacade;

	@Test
	void getList_returnsReadableCurrencyNotJpaEntity() {
		Currency usd = currency("USD", "US Dollar", true);
		Currency brl = currency("BRL", "Brazilian Real", true);
		when(currencyService.list()).thenReturn(Arrays.asList(usd, brl));

		List<ReadableCurrency> result = currencyFacade.getList();

		assertEquals(2, result.size());
		assertEquals("BRL", result.get(0).getCode());
		assertEquals("USD", result.get(1).getCode());
		assertTrue(result.get(0) instanceof ReadableCurrency);
		assertFalse(result.get(0).getClass().getName().contains("core.model"));
		assertEquals("Brazilian Real", result.get(0).getName());
		assertEquals(usd.getSymbol(), result.get(1).getSymbol());
	}

	@Test
	void getList_empty_throwsNotFound() {
		when(currencyService.list()).thenReturn(Collections.emptyList());
		assertThrows(ResourceNotFoundException.class, () -> currencyFacade.getList());
	}

	private static Currency currency(String code, String name, boolean supported) {
		Currency c = new Currency();
		c.setId(1L);
		c.setCurrency(java.util.Currency.getInstance(code));
		c.setName(name);
		c.setSupported(supported);
		return c;
	}
}
