package com.salesmanager.shop.populator.references;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.salesmanager.contracts.reference.ReadableCurrency;
import com.salesmanager.contracts.reference.ReadableLanguage;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.core.model.reference.language.Language;

class ReferenceDtoMappersTest {

	@Test
	void readableLanguageMapper_mapsEntityToContractDto() {
		Language source = new Language("en");
		source.setId(1);
		source.setSortOrder(5);

		ReadableLanguage dto = ReadableLanguageMapper.toDto(source);

		assertThat(dto.getId()).isEqualTo(1);
		assertThat(dto.getCode()).isEqualTo("en");
		assertThat(dto.getSortOrder()).isEqualTo(5);
	}

	@Test
	void readableCurrencyMapper_mapsEntityToContractDto() {
		Currency source = new Currency();
		source.setId(2L);
		source.setCurrency(java.util.Currency.getInstance("EUR"));
		source.setName("Euro");
		source.setSupported(true);

		ReadableCurrency dto = ReadableCurrencyMapper.toDto(source);

		assertThat(dto.getId()).isEqualTo(2L);
		assertThat(dto.getCode()).isEqualTo("EUR");
		assertThat(dto.getName()).isEqualTo("Euro");
		assertThat(dto.getSymbol()).isEqualTo("€");
		assertThat(dto.isSupported()).isTrue();
	}
}
