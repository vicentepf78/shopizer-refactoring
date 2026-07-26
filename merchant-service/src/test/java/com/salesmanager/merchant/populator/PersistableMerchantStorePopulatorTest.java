package com.salesmanager.merchant.populator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.salesmanager.contracts.client.ReferenceServiceClient;
import com.salesmanager.contracts.merchant.PersistableMerchantStore;
import com.salesmanager.contracts.reference.PersistableAddress;
import com.salesmanager.contracts.reference.ReadableCountry;
import com.salesmanager.contracts.reference.ReadableLanguage;
import com.salesmanager.core.business.exception.ConversionException;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.merchant.support.TestDataFactory;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDataFactory.class)
class PersistableMerchantStorePopulatorTest {

	@Autowired
	private PersistableMerchantStorePopulator populator;

	@Autowired
	private TestDataFactory testDataFactory;

	@MockBean
	private ReferenceServiceClient referenceServiceClient;

	@MockBean
	private com.salesmanager.contracts.client.ContentServiceClient contentServiceClient;

	private Language language;

	@BeforeEach
	void setUp() {
		TestDataFactory.Seed seed = testDataFactory.ensureDefaultAdmin();
		language = seed.language;

		ReadableLanguage readableEn = new ReadableLanguage();
		readableEn.setId(language.getId());
		readableEn.setCode("en");
		when(referenceServiceClient.getLanguageByCode(eq("en"))).thenReturn(readableEn);

		ReadableCountry country = new ReadableCountry();
		country.setCode("CA");
		country.setName("Canada");
		when(referenceServiceClient.getCountryByCode(eq("CA"), eq("en"))).thenReturn(country);
	}

	@Test
	void populate_mapsFieldsAndReferenceData() throws Exception {
		PersistableAddress address = new PersistableAddress();
		address.setAddress("1 Main");
		address.setCity("Montreal");
		address.setCountry("CA");
		address.setPostalCode("H2X1Y4");
		address.setStateProvince("QC");

		PersistableMerchantStore source = new PersistableMerchantStore();
		source.setCode("child02");
		source.setCurrency("CAD");
		source.setDefaultLanguage("en");
		source.setEmail("child2@test.local");
		source.setName("Child Two");
		source.setPhone("555-2222");
		source.setSupportedLanguages(Arrays.asList("en"));
		source.setAddress(address);
		source.setInBusinessSince("2020-01-15");

		MerchantStore target = new MerchantStore();
		MerchantStore result = populator.populate(source, target, null, language);

		assertThat(result.getCode()).isEqualTo("child02");
		assertThat(result.getStorename()).isEqualTo("Child Two");
		assertThat(result.getCurrency().getCode()).isEqualTo("CAD");
		assertThat(result.getInBusinessSince()).isNotNull();
	}

	@Test
	void populate_invalidDate_throwsConversionException() {
		PersistableMerchantStore source = new PersistableMerchantStore();
		source.setCode("bad-date");
		source.setCurrency("CAD");
		source.setDefaultLanguage("en");
		source.setName("Bad Date");
		source.setInBusinessSince("not-a-date");

		assertThatThrownBy(() -> populator.populate(source, new MerchantStore(), null, language))
				.isInstanceOf(ConversionException.class);
	}
}
