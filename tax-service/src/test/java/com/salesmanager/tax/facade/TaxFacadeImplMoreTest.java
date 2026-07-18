package com.salesmanager.tax.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.contracts.common.Entity;
import com.salesmanager.contracts.common.ReadableEntityList;
import com.salesmanager.contracts.tax.PersistableTaxClass;
import com.salesmanager.contracts.tax.PersistableTaxRate;
import com.salesmanager.contracts.tax.ReadableTaxClass;
import com.salesmanager.contracts.tax.ReadableTaxRate;
import com.salesmanager.core.business.services.tax.TaxClassService;
import com.salesmanager.core.business.services.tax.TaxRateService;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.tax.taxclass.TaxClass;
import com.salesmanager.core.model.tax.taxrate.TaxRate;
import com.salesmanager.tax.mapper.PersistableTaxClassMapper;
import com.salesmanager.tax.mapper.PersistableTaxRateMapper;
import com.salesmanager.tax.mapper.ReadableTaxClassMapper;
import com.salesmanager.tax.mapper.ReadableTaxRateMapper;
import com.salesmanager.tax.support.OperationNotAllowedException;
import com.salesmanager.tax.support.ResourceNotFoundException;
import com.salesmanager.tax.support.StoreForbiddenException;

@ExtendWith(MockitoExtension.class)
class TaxFacadeImplMoreTest {

	@Mock
	private TaxClassService taxClassService;
	@Mock
	private TaxRateService taxRateService;
	@Mock
	private PersistableTaxClassMapper persistableTaxClassMapper;
	@Mock
	private ReadableTaxClassMapper readableTaxClassMapper;
	@Mock
	private PersistableTaxRateMapper persistableTaxRateMapper;
	@Mock
	private ReadableTaxRateMapper readableTaxRateMapper;

	private TaxFacadeImpl facade;
	private MerchantStore store;
	private Language language;

	@BeforeEach
	void setUp() {
		facade = new TaxFacadeImpl(
				taxClassService, taxRateService, persistableTaxClassMapper, readableTaxClassMapper,
				persistableTaxRateMapper, readableTaxRateMapper);
		store = new MerchantStore();
		store.setCode("DEFAULT");
		language = new Language("en");
	}

	@Test
	void createTaxClass_andList_andGet_andExists() throws Exception {
		PersistableTaxClass dto = new PersistableTaxClass();
		dto.setCode("TC");
		TaxClass model = new TaxClass("TC");
		model.setId(1L);
		model.setMerchantStore(store);
		ReadableTaxClass readable = new ReadableTaxClass();
		readable.setCode("TC");

		when(taxClassService.exists("TC", store)).thenReturn(false);
		when(persistableTaxClassMapper.convert(dto, store, language)).thenReturn(model);
		when(taxClassService.saveOrUpdate(model)).thenReturn(model);
		when(taxClassService.listByStore(store)).thenReturn(Collections.singletonList(model));
		when(readableTaxClassMapper.convert(model, store, language)).thenReturn(readable);
		when(taxClassService.getByCode("TC", store)).thenReturn(model);

		Entity id = facade.createTaxClass(dto, store, language);
		assertThat(id.getId()).isEqualTo(1L);

		ReadableEntityList<ReadableTaxClass> list = facade.taxClasses(store, language);
		assertThat(list.getItems()).hasSize(1);
		assertThat(facade.taxClass("TC", store, language).getCode()).isEqualTo("TC");
		assertThat(facade.existsTaxClass("TC", store, language)).isFalse();
	}

	@Test
	void createTaxClass_duplicate_throws() throws Exception {
		PersistableTaxClass dto = new PersistableTaxClass();
		dto.setCode("TC");
		when(taxClassService.exists("TC", store)).thenReturn(true);
		assertThatThrownBy(() -> facade.createTaxClass(dto, store, language))
				.isInstanceOf(OperationNotAllowedException.class);
	}

	@Test
	void deleteTaxClass_wrongStore_forbidden() throws Exception {
		MerchantStore other = new MerchantStore();
		other.setCode("OTHER");
		TaxClass model = new TaxClass("TC");
		model.setId(1L);
		model.setMerchantStore(other);
		when(taxClassService.getById(1L)).thenReturn(model);
		assertThatThrownBy(() -> facade.deleteTaxClass(1L, store, language))
				.isInstanceOf(StoreForbiddenException.class);
	}

	@Test
	void taxClass_missing_notFound() throws Exception {
		when(taxClassService.getByCode("X", store)).thenReturn(null);
		assertThatThrownBy(() -> facade.taxClass("X", store, language))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void taxRate_crud_andExistsFalse() throws Exception {
		PersistableTaxRate dto = new PersistableTaxRate();
		dto.setCode("R1");
		TaxRate model = new TaxRate();
		model.setId(5L);
		model.setCode("R1");
		ReadableTaxRate readable = new ReadableTaxRate();
		readable.setCode("R1");

		when(taxRateService.getByCode("R1", store)).thenReturn(null);
		when(persistableTaxRateMapper.convert(dto, store, language)).thenReturn(model);
		when(taxRateService.saveOrUpdate(model)).thenReturn(model);
		when(taxRateService.getById(5L, store)).thenReturn(model);
		when(readableTaxRateMapper.convert(model, store, language)).thenReturn(readable);
		when(taxRateService.listByStore(store, language)).thenReturn(Collections.singletonList(model));
		when(taxRateService.exists("MISSING", store)).thenReturn(false);

		assertThat(facade.createTaxRate(dto, store, language).getId()).isEqualTo(5L);
		assertThat(facade.taxRate(5L, store, language).getCode()).isEqualTo("R1");
		assertThat(facade.taxRates(store, language).getItems()).hasSize(1);
		assertThat(facade.existsTaxRate("MISSING", store, language)).isFalse();

		facade.deleteTaxRate(5L, store, language);
		verify(taxRateService).delete(model);

		when(persistableTaxRateMapper.merge(any(), any(), any(), any())).thenReturn(model);
		facade.updateTaxRate(5L, dto, store, language);
		verify(taxRateService, org.mockito.Mockito.times(2)).saveOrUpdate(model);
	}

	@Test
	void updateTaxClass_ok() throws Exception {
		PersistableTaxClass dto = new PersistableTaxClass();
		dto.setCode("TC");
		TaxClass model = new TaxClass("TC");
		model.setId(1L);
		model.setMerchantStore(store);
		when(taxClassService.getById(1L)).thenReturn(model);
		when(persistableTaxClassMapper.convert(dto, store, language)).thenReturn(model);
		facade.updateTaxClass(1L, dto, store, language);
		verify(taxClassService).saveOrUpdate(model);
	}
}
