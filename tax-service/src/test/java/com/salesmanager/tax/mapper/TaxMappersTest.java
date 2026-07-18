package com.salesmanager.tax.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.contracts.client.ReferenceServiceClient;
import com.salesmanager.contracts.reference.ReadableCountry;
import com.salesmanager.contracts.reference.ReadableLanguage;
import com.salesmanager.contracts.reference.ReadableZone;
import com.salesmanager.contracts.tax.PersistableTaxClass;
import com.salesmanager.contracts.tax.PersistableTaxRate;
import com.salesmanager.contracts.tax.ReadableTaxClass;
import com.salesmanager.contracts.tax.ReadableTaxRate;
import com.salesmanager.contracts.tax.TaxRateDescription;
import com.salesmanager.core.business.services.tax.TaxClassService;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.reference.zone.Zone;
import com.salesmanager.core.model.tax.taxclass.TaxClass;
import com.salesmanager.core.model.tax.taxrate.TaxRate;
import com.salesmanager.tax.support.ValidationException;

@ExtendWith(MockitoExtension.class)
class TaxMappersTest {

	@Mock
	private ReferenceServiceClient referenceServiceClient;
	@Mock
	private TaxClassService taxClassService;

	private PersistableTaxRateMapper rateMapper;
	private PersistableTaxClassMapper classMapper;
	private ReadableTaxClassMapper readableClassMapper;
	private ReadableTaxRateMapper readableRateMapper;

	private MerchantStore store;
	private Language language;

	@BeforeEach
	void setUp() {
		rateMapper = new PersistableTaxRateMapper(referenceServiceClient, taxClassService);
		classMapper = new PersistableTaxClassMapper();
		readableClassMapper = new ReadableTaxClassMapper();
		readableRateMapper = new ReadableTaxRateMapper();
		store = new MerchantStore();
		store.setCode("DEFAULT");
		language = new Language("en");
		language.setId(1);
	}

	@Test
	void persistableTaxClass_convertAndMerge() {
		PersistableTaxClass source = new PersistableTaxClass();
		source.setCode("TC");
		source.setName("Class");
		source.setId(3L);

		TaxClass converted = classMapper.convert(source, store, language);
		assertThat(converted.getCode()).isEqualTo("TC");
		assertThat(converted.getTitle()).isEqualTo("Class");
		assertThat(converted.getMerchantStore()).isSameAs(store);

		TaxClass merged = classMapper.merge(source, new TaxClass(), store, language);
		assertThat(merged.getId()).isEqualTo(3L);
	}

	@Test
	void readableTaxClass_convert() {
		TaxClass source = new TaxClass("TC");
		source.setId(2L);
		source.setTitle("Title");
		ReadableTaxClass readable = readableClassMapper.convert(source, store, language);
		assertThat(readable.getCode()).isEqualTo("TC");
		assertThat(readable.getName()).isEqualTo("Title");
		assertThat(readable.getStore()).isEqualTo("DEFAULT");
	}

	@Test
	void persistableTaxRate_happyPath_withDescriptions() throws Exception {
		ReadableCountry country = new ReadableCountry();
		country.setId(7L);
		country.setCode("CA");
		ReadableZone zone = new ReadableZone();
		zone.setId(8L);
		zone.setCode("QC");
		ReadableLanguage lang = new ReadableLanguage();
		lang.setId(1);
		lang.setCode("en");
		TaxClass taxClass = new TaxClass("DEFAULT");
		taxClass.setId(1L);

		when(referenceServiceClient.getCountryByCode(eq("CA"), any())).thenReturn(country);
		when(referenceServiceClient.getZoneByCode(eq("CA"), eq("QC"), any())).thenReturn(zone);
		when(referenceServiceClient.getLanguageByCode("en")).thenReturn(lang);
		when(taxClassService.getByCode("DEFAULT", store)).thenReturn(taxClass);

		TaxRateDescription desc = new TaxRateDescription();
		desc.setLanguage("en");
		desc.setName("GST");
		desc.setDescription("desc");
		desc.setTitle("t");

		PersistableTaxRate source = new PersistableTaxRate();
		source.setCode("GST");
		source.setCountry("CA");
		source.setZone("QC");
		source.setTaxClass("DEFAULT");
		source.setRate(new BigDecimal("5.00"));
		source.setPriority(1);
		source.setDescriptions(Collections.singletonList(desc));

		TaxRate result = rateMapper.convert(source, store, language);
		assertThat(result.getCode()).isEqualTo("GST");
		assertThat(result.getCountry().getIsoCode()).isEqualTo("CA");
		assertThat(result.getZone().getCode()).isEqualTo("QC");
		assertThat(result.getDescriptions()).hasSize(1);
	}

	@Test
	void persistableTaxRate_invalidZone_throws() {
		ReadableCountry country = new ReadableCountry();
		country.setId(7L);
		country.setCode("CA");
		when(referenceServiceClient.getCountryByCode(eq("CA"), any())).thenReturn(country);
		when(referenceServiceClient.getZoneByCode(eq("CA"), eq("XX"), any())).thenReturn(null);

		PersistableTaxRate source = new PersistableTaxRate();
		source.setCode("GST");
		source.setCountry("CA");
		source.setZone("XX");
		source.setTaxClass("DEFAULT");
		source.setRate(BigDecimal.ONE);

		assertThatThrownBy(() -> rateMapper.convert(source, store, language))
				.isInstanceOf(ValidationException.class)
				.hasMessageContaining("Invalid zone");
	}

	@Test
	void persistableTaxRate_invalidLanguage_throws() throws Exception {
		ReadableCountry country = new ReadableCountry();
		country.setId(7L);
		country.setCode("CA");
		ReadableZone zone = new ReadableZone();
		zone.setId(8L);
		zone.setCode("QC");
		when(referenceServiceClient.getCountryByCode(eq("CA"), any())).thenReturn(country);
		when(referenceServiceClient.getZoneByCode(eq("CA"), eq("QC"), any())).thenReturn(zone);
		when(referenceServiceClient.getLanguageByCode("zz")).thenReturn(null);
		when(taxClassService.getByCode(any(), any())).thenReturn(new TaxClass("DEFAULT"));

		TaxRateDescription desc = new TaxRateDescription();
		desc.setLanguage("zz");
		desc.setName("x");

		PersistableTaxRate source = new PersistableTaxRate();
		source.setCode("GST");
		source.setCountry("CA");
		source.setZone("QC");
		source.setTaxClass("DEFAULT");
		source.setRate(BigDecimal.ONE);
		source.setDescriptions(Collections.singletonList(desc));

		assertThatThrownBy(() -> rateMapper.convert(source, store, language))
				.isInstanceOf(ValidationException.class)
				.hasMessageContaining("Invalid language");
	}

	@Test
	void readableTaxRate_convertWithMatchingDescription() {
		Country country = new Country("CA");
		Zone zone = new Zone();
		zone.setCode("QC");
		Language lang = new Language("en");
		lang.setId(1);

		com.salesmanager.core.model.tax.taxrate.TaxRateDescription desc =
				new com.salesmanager.core.model.tax.taxrate.TaxRateDescription();
		desc.setId(1L);
		desc.setName("GST");
		desc.setDescription("d");
		desc.setTitle("t");
		desc.setLanguage(lang);

		TaxRate source = new TaxRate();
		source.setId(9L);
		source.setCode("GST");
		source.setTaxPriority(2);
		source.setTaxRate(new BigDecimal("5"));
		source.setCountry(country);
		source.setZone(zone);
		source.setDescriptions(new ArrayList<>(Collections.singletonList(desc)));

		ReadableTaxRate readable = readableRateMapper.convert(source, store, language);
		assertThat(readable.getCode()).isEqualTo("GST");
		assertThat(readable.getCountry()).isEqualTo("CA");
		assertThat(readable.getZone()).isEqualTo("QC");
		assertThat(readable.getDescription().getName()).isEqualTo("GST");
	}
}
