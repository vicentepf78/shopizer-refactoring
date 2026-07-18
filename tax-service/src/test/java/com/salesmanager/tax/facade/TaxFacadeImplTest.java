package com.salesmanager.tax.facade;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.core.business.exception.TaxClassInUseException;
import com.salesmanager.core.business.services.tax.TaxClassService;
import com.salesmanager.core.business.services.tax.TaxRateService;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.tax.taxclass.TaxClass;
import com.salesmanager.tax.mapper.PersistableTaxClassMapper;
import com.salesmanager.tax.mapper.PersistableTaxRateMapper;
import com.salesmanager.tax.mapper.ReadableTaxClassMapper;
import com.salesmanager.tax.mapper.ReadableTaxRateMapper;

@ExtendWith(MockitoExtension.class)
class TaxFacadeImplTest {

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

	@BeforeEach
	void setUp() {
		facade = new TaxFacadeImpl(
				taxClassService,
				taxRateService,
				persistableTaxClassMapper,
				readableTaxClassMapper,
				persistableTaxRateMapper,
				readableTaxRateMapper);
	}

	@Test
	void deleteTaxClass_inUse_propagatesConflict() throws Exception {
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		Language language = new Language("en");

		TaxClass model = new TaxClass("DEFAULT");
		model.setId(9L);
		model.setMerchantStore(store);

		when(taxClassService.getById(9L)).thenReturn(model);
		doThrow(new TaxClassInUseException(9L, 3L)).when(taxClassService).delete(any(TaxClass.class));

		assertThatThrownBy(() -> facade.deleteTaxClass(9L, store, language))
				.isInstanceOf(TaxClassInUseException.class)
				.extracting(ex -> ((TaxClassInUseException) ex).getErrorCode())
				.isEqualTo(TaxClassInUseException.ERROR_CODE);
	}
}
