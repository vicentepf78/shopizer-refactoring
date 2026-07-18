package com.salesmanager.core.business.services.tax;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.core.business.repositories.tax.TaxRateRepository;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.reference.zone.Zone;
import com.salesmanager.core.model.tax.taxclass.TaxClass;
import com.salesmanager.core.model.tax.taxrate.TaxRate;

@ExtendWith(MockitoExtension.class)
class TaxRateServiceImplTest {

	@Mock
	private TaxRateRepository taxRateRepository;

	private TaxRateServiceImpl taxRateService;

	@BeforeEach
	void setUp() {
		taxRateService = new TaxRateServiceImpl(taxRateRepository);
	}

	@Test
	void exists_whenCodeAbsent_returnsFalseWithoutException() throws Exception {
		MerchantStore store = store(1);
		when(taxRateRepository.findByStoreAndCode(1, "MISSING")).thenReturn(null);

		assertFalse(taxRateService.exists("MISSING", store));
	}

	@Test
	void exists_whenCodePresent_returnsTrue() throws Exception {
		MerchantStore store = store(1);
		when(taxRateRepository.findByStoreAndCode(1, "GST")).thenReturn(rate(2L, "GST"));

		assertTrue(taxRateService.exists("GST", store));
	}

	@Test
	void getByCode_delegatesToRepository() throws Exception {
		MerchantStore store = store(1);
		TaxRate expected = rate(2L, "GST");
		when(taxRateRepository.findByStoreAndCode(1, "GST")).thenReturn(expected);

		assertSame(expected, taxRateService.getByCode("GST", store));
	}

	@Test
	void listByStore_delegatesToRepository() throws Exception {
		MerchantStore store = store(1);
		List<TaxRate> expected = Collections.singletonList(rate(1L, "GST"));
		when(taxRateRepository.findByStore(1)).thenReturn(expected);

		assertSame(expected, taxRateService.listByStore(store));
	}

	@Test
	void listByStore_withLanguage_delegatesToRepository() throws Exception {
		MerchantStore store = store(1);
		Language language = language(1);
		List<TaxRate> expected = Collections.singletonList(rate(1L, "GST"));
		when(taxRateRepository.findByStoreAndLanguage(1, 1)).thenReturn(expected);

		assertSame(expected, taxRateService.listByStore(store, language));
	}

	@Test
	void listByCountryZoneAndTaxClass_delegatesToRepository() throws Exception {
		MerchantStore store = store(1);
		Country country = country(10);
		Zone zone = zone(20L);
		Language language = language(1);
		List<TaxRate> expected = Collections.singletonList(rate(1L, "GST"));
		when(taxRateRepository.findByMerchantAndZoneAndCountryAndLanguage(1, 20L, 10, 1))
				.thenReturn(expected);

		assertSame(expected, taxRateService.listByCountryZoneAndTaxClass(
				country, zone, new TaxClass("DEFAULT"), store, language));
	}

	@Test
	void listByCountryStateProvinceAndTaxClass_delegatesToRepository() throws Exception {
		MerchantStore store = store(1);
		Country country = country(10);
		Language language = language(1);
		List<TaxRate> expected = Collections.singletonList(rate(1L, "GST"));
		when(taxRateRepository.findByMerchantAndProvinceAndCountryAndLanguage(1, "ON", 10, 1))
				.thenReturn(expected);

		assertSame(expected, taxRateService.listByCountryStateProvinceAndTaxClass(
				country, "ON", new TaxClass("DEFAULT"), store, language));
	}

	@Test
	void getById_withStore_delegatesToRepository() throws Exception {
		MerchantStore store = store(1);
		TaxRate expected = rate(9L, "GST");
		when(taxRateRepository.findByStoreAndId(1, 9L)).thenReturn(expected);

		assertSame(expected, taxRateService.getById(9L, store));
	}

	@Test
	void delete_delegatesToRepository() throws Exception {
		TaxRate taxRate = rate(1L, "GST");

		taxRateService.delete(taxRate);

		verify(taxRateRepository).delete(taxRate);
	}

	@Test
	void saveOrUpdate_newEntity_savesAndFlushes() throws Exception {
		TaxRate taxRate = rate(null, "NEW");
		TaxRate saved = rate(5L, "NEW");
		when(taxRateRepository.saveAndFlush(taxRate)).thenReturn(saved);

		assertSame(saved, taxRateService.saveOrUpdate(taxRate));
	}

	@Test
	void saveOrUpdate_existingEntity_updates() throws Exception {
		TaxRate taxRate = rate(5L, "OLD");
		when(taxRateRepository.saveAndFlush(taxRate)).thenReturn(taxRate);

		assertSame(taxRate, taxRateService.saveOrUpdate(taxRate));
		verify(taxRateRepository).saveAndFlush(taxRate);
	}

	private static TaxRate rate(Long id, String code) {
		TaxRate rate = new TaxRate();
		rate.setId(id);
		rate.setCode(code);
		return rate;
	}

	private static MerchantStore store(Integer id) {
		MerchantStore store = new MerchantStore();
		store.setId(id);
		store.setCode("DEFAULT");
		return store;
	}

	private static Language language(Integer id) {
		Language language = new Language("en");
		language.setId(id);
		return language;
	}

	private static Country country(Integer id) {
		Country country = new Country();
		country.setId(id);
		return country;
	}

	private static Zone zone(Long id) {
		Zone zone = new Zone();
		zone.setId(id);
		return zone;
	}
}
