package com.salesmanager.tax.mapper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.contracts.client.ReferenceServiceClient;
import com.salesmanager.contracts.tax.PersistableTaxRate;
import com.salesmanager.core.business.services.tax.TaxClassService;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.tax.support.ValidationException;

@ExtendWith(MockitoExtension.class)
class PersistableTaxRateMapperTest {

	@Mock
	private ReferenceServiceClient referenceServiceClient;
	@Mock
	private TaxClassService taxClassService;

	private PersistableTaxRateMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = new PersistableTaxRateMapper(referenceServiceClient, taxClassService);
	}

	@Test
	void merge_invalidCountry_throwsValidationException() {
		when(referenceServiceClient.getCountryByCode(eq("XX"), any())).thenReturn(null);

		PersistableTaxRate source = new PersistableTaxRate();
		source.setCode("RATE1");
		source.setCountry("XX");
		source.setZone("QC");
		source.setTaxClass("DEFAULT");
		source.setRate(BigDecimal.ONE);

		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		Language language = new Language("en");

		assertThatThrownBy(() -> mapper.merge(source, new com.salesmanager.core.model.tax.taxrate.TaxRate(), store, language))
				.isInstanceOf(ValidationException.class)
				.hasMessageContaining("Invalid country");
	}
}
