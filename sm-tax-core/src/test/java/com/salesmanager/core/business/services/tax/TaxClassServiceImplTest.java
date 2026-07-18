package com.salesmanager.core.business.services.tax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.core.business.exception.TaxClassInUseException;
import com.salesmanager.core.business.repositories.tax.ProductTaxClassCountRepository;
import com.salesmanager.core.business.repositories.tax.TaxClassRepository;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.tax.taxclass.TaxClass;

@ExtendWith(MockitoExtension.class)
class TaxClassServiceImplTest {

	@Mock
	private TaxClassRepository taxClassRepository;

	@Mock
	private ProductTaxClassCountRepository productTaxClassCountRepository;

	private TaxClassServiceImpl taxClassService;

	@BeforeEach
	void setUp() {
		taxClassService = new TaxClassServiceImpl(taxClassRepository, productTaxClassCountRepository);
	}

	@Test
	void delete_withProducts_throwsTaxClassInUseException() throws Exception {
		TaxClass taxClass = taxClass(10L, "DEFAULT");
		when(taxClassRepository.getOne(10L)).thenReturn(taxClass);
		when(productTaxClassCountRepository.countByTaxClassId(10L)).thenReturn(3L);

		TaxClassInUseException ex = assertThrows(TaxClassInUseException.class,
				() -> taxClassService.delete(taxClass));

		assertEquals(10L, ex.getTaxClassId());
		assertEquals(3L, ex.getProductCount());
		assertEquals(TaxClassInUseException.ERROR_CODE, ex.getErrorCode());
		verify(taxClassRepository, never()).delete(any(TaxClass.class));
	}

	@Test
	void delete_withoutProducts_removesSuccessfully() throws Exception {
		TaxClass taxClass = taxClass(10L, "DEFAULT");
		when(taxClassRepository.getOne(10L)).thenReturn(taxClass);
		when(productTaxClassCountRepository.countByTaxClassId(10L)).thenReturn(0L);

		taxClassService.delete(taxClass);

		verify(taxClassRepository).delete(taxClass);
	}

	@Test
	void listByStore_delegatesToRepository() throws Exception {
		MerchantStore store = store(1);
		List<TaxClass> expected = Collections.singletonList(taxClass(1L, "A"));
		when(taxClassRepository.findByStore(1)).thenReturn(expected);

		assertSame(expected, taxClassService.listByStore(store));
	}

	@Test
	void getByCode_delegatesToRepository() throws Exception {
		TaxClass expected = taxClass(1L, "A");
		when(taxClassRepository.findByCode("A")).thenReturn(expected);

		assertSame(expected, taxClassService.getByCode("A"));
	}

	@Test
	void getByCode_withStore_delegatesToRepository() throws Exception {
		MerchantStore store = store(1);
		TaxClass expected = taxClass(1L, "A");
		when(taxClassRepository.findByStoreAndCode(1, "A")).thenReturn(expected);

		assertSame(expected, taxClassService.getByCode("A", store));
	}

	@Test
	void exists_whenPresent_returnsTrue() throws Exception {
		MerchantStore store = store(1);
		when(taxClassRepository.findByStoreAndCode(1, "A")).thenReturn(taxClass(1L, "A"));

		assertTrue(taxClassService.exists("A", store));
	}

	@Test
	void exists_whenAbsent_returnsFalse() throws Exception {
		MerchantStore store = store(1);
		when(taxClassRepository.findByStoreAndCode(1, "MISSING")).thenReturn(null);

		assertFalse(taxClassService.exists("MISSING", store));
	}

	@Test
	void saveOrUpdate_newEntity_savesAndFlushes() throws Exception {
		TaxClass taxClass = taxClass(null, "NEW");
		TaxClass saved = taxClass(5L, "NEW");
		when(taxClassRepository.saveAndFlush(taxClass)).thenReturn(saved);

		assertSame(saved, taxClassService.saveOrUpdate(taxClass));
	}

	@Test
	void saveOrUpdate_existingEntity_updates() throws Exception {
		TaxClass taxClass = taxClass(5L, "OLD");
		when(taxClassRepository.saveAndFlush(taxClass)).thenReturn(taxClass);

		assertSame(taxClass, taxClassService.saveOrUpdate(taxClass));
		verify(taxClassRepository).saveAndFlush(taxClass);
	}

	private static TaxClass taxClass(Long id, String code) {
		TaxClass taxClass = new TaxClass(code);
		taxClass.setId(id);
		return taxClass;
	}

	private static MerchantStore store(Integer id) {
		MerchantStore store = new MerchantStore();
		store.setId(id);
		store.setCode("DEFAULT");
		return store;
	}
}
