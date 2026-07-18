package com.salesmanager.core.business.services.common.generic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;

import com.salesmanager.core.model.reference.currency.Currency;

@ExtendWith(MockitoExtension.class)
class SalesManagerEntityServiceImplTest {

	@Mock
	private JpaRepository<Currency, Long> repository;

	private SalesManagerEntityServiceImpl<Long, Currency> service;

	@BeforeEach
	void setUp() {
		service = new SalesManagerEntityServiceImpl<Long, Currency>(repository) {
		};
	}

	@Test
	void list_delegatesToFindAll() {
		Currency currency = new Currency();
		when(repository.findAll()).thenReturn(Collections.singletonList(currency));

		List<Currency> result = service.list();

		assertEquals(1, result.size());
		assertEquals(currency, result.get(0));
	}

	@Test
	void count_delegatesToRepository() {
		when(repository.count()).thenReturn(3L);
		assertEquals(3L, service.count());
	}

	@Test
	void getById_delegatesToGetOne() {
		Currency currency = new Currency();
		when(repository.getOne(1L)).thenReturn(currency);
		assertEquals(currency, service.getById(1L));
	}

	@Test
	void delete_delegatesToRepository() throws Exception {
		Currency currency = new Currency();
		service.delete(currency);
		verify(repository).delete(currency);
	}

	@Test
	void flush_delegatesToRepository() {
		service.flush();
		verify(repository).flush();
	}

	@Test
	void save_create_update_and_saveAll_delegate() throws Exception {
		Currency currency = new Currency();
		when(repository.saveAndFlush(currency)).thenReturn(currency);

		service.save(currency);
		service.create(currency);
		service.update(currency);
		service.saveAll(Collections.singletonList(currency));

		verify(repository, org.mockito.Mockito.atLeast(3)).saveAndFlush(currency);
		verify(repository).saveAll(Collections.singletonList(currency));
	}

	@Test
	void getObjectClass_returnsCurrency() {
		assertEquals(Currency.class, service.getObjectClass());
	}
}
