package com.salesmanager.merchant.populator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.salesmanager.contracts.client.ReferenceServiceClient;
import com.salesmanager.contracts.merchant.ReadableMerchantStore;
import com.salesmanager.contracts.reference.ReadableCountry;
import com.salesmanager.contracts.reference.ReadableLanguage;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.merchant.support.TestDataFactory;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDataFactory.class)
class ReadableMerchantStorePopulatorTest {

	@Autowired
	private ReadableMerchantStorePopulator populator;

	@Autowired
	private TestDataFactory testDataFactory;
	@Autowired
	private com.salesmanager.merchant.security.MerchantStoreRepository merchantStoreRepository;

	@MockBean
	private ReferenceServiceClient referenceServiceClient;

	@MockBean
	private com.salesmanager.contracts.client.ContentServiceClient contentServiceClient;

	private MerchantStore store;
	private Language language;

	@BeforeEach
	void setUp() {
		TestDataFactory.Seed seed = testDataFactory.ensureDefaultAdmin();
		store = merchantStoreRepository.findByCode(seed.store.getCode());
		language = seed.language;

		ReadableLanguage readableEn = new ReadableLanguage();
		readableEn.setId(language.getId());
		readableEn.setCode("en");
		when(referenceServiceClient.getLanguageByCode(eq("en"))).thenReturn(readableEn);

		ReadableCountry country = new ReadableCountry();
		country.setCode("CA");
		when(referenceServiceClient.getCountryByCode(eq("CA"), eq("en"))).thenReturn(country);
	}

	@Test
	void populate_returnsContractDto_notJpaEntity() throws Exception {
		ReadableMerchantStore readable = populator.populate(store, new ReadableMerchantStore(), store, language);
		assertThat(readable.getClass().getName()).doesNotContain("core.model.merchant.MerchantStore");
		assertThat(readable.getCode()).isEqualTo(store.getCode());
	}
}
