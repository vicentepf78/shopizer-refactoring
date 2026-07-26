package com.salesmanager.merchant.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.salesmanager.contracts.client.ContentServiceClient;
import com.salesmanager.contracts.client.ReferenceServiceClient;
import com.salesmanager.contracts.merchant.MerchantStoreSnapshot;
import com.salesmanager.contracts.merchant.PersistableMerchantStore;
import com.salesmanager.contracts.merchant.ReadableBrand;
import com.salesmanager.contracts.merchant.ReadableMerchantStore;
import com.salesmanager.contracts.reference.PersistableAddress;
import com.salesmanager.contracts.reference.ReadableCountry;
import com.salesmanager.contracts.reference.ReadableLanguage;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.merchant.support.MerchantConstants;
import com.salesmanager.merchant.support.ServiceRuntimeException;
import com.salesmanager.merchant.support.TestDataFactory;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDataFactory.class)
class StoreFacadeImplTest {

	@Autowired
	private StoreFacade storeFacade;
	@Autowired
	private TestDataFactory testDataFactory;
	@Autowired
	private com.salesmanager.merchant.security.MerchantStoreRepository merchantStoreRepository;

	@MockBean
	private ReferenceServiceClient referenceServiceClient;

	@MockBean
	private ContentServiceClient contentServiceClient;

	private MerchantStore store;
	private Language language;

	@BeforeEach
	void setUp() {
		TestDataFactory.Seed seed = testDataFactory.ensureDefaultAdmin();
		store = seed.store;
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
	void createStore_usesReferenceServiceClient() {
		PersistableAddress address = new PersistableAddress();
		address.setAddress("1 Main");
		address.setCity("Montreal");
		address.setCountry("CA");
		address.setPostalCode("H2X1Y4");
		address.setStateProvince("QC");

		PersistableMerchantStore created = new PersistableMerchantStore();
		created.setCode("child01");
		created.setCurrency("CAD");
		created.setDefaultLanguage("en");
		created.setEmail("child@test.local");
		created.setName("child01");
		created.setPhone("555-1111");
		created.setSupportedLanguages(Arrays.asList("en"));
		created.setAddress(address);

		storeFacade.create(created);

		verify(referenceServiceClient, atLeastOnce()).getLanguageByCode(eq("en"));
		verify(referenceServiceClient).getCountryByCode(eq("CA"), eq("en"));
		assertThat(storeFacade.existByCode("child01")).isTrue();
	}

	@Test
	void getByCode_returnsContractDto_notJpaEntity() {
		ReadableMerchantStore readable = storeFacade.getByCode(store.getCode(), language);
		assertThat(readable.getClass().getName()).doesNotContain("core.model.merchant.MerchantStore");
		assertThat(readable.getCode()).isEqualTo(store.getCode());
	}

	@Test
	void deleteDefaultStore_throwsBusinessError() {
		assertThatThrownBy(() -> storeFacade.delete(MerchantStore.DEFAULT_STORE))
				.isInstanceOf(ServiceRuntimeException.class)
				.hasMessageContaining("Cannot remove default store");
	}

	@Test
	void getSnapshot_returnsMerchantStoreSnapshot() {
		MerchantStoreSnapshot snapshot = storeFacade.getSnapshot(store.getCode(), language);
		assertThat(snapshot.getCode()).isEqualTo(store.getCode());
		assertThat(snapshot.getName()).isEqualTo(store.getStorename());
	}

	@Test
	void getBrand_returnsLogoAndSocialConfigs() {
		testDataFactory.seedSocialConfig(store, MerchantConstants.KEY_INSTAGRAM_URL, "https://instagram.test");
		doNothing().when(contentServiceClient).uploadLogo(eq(store.getCode()), eq("logo.png"), any(), eq("image/png"));
		storeFacade.addStoreLogo(store.getCode(), "logo.png", new byte[] { 1 }, "image/png");
		ReadableBrand brand = storeFacade.getBrand(store.getCode());
		assertThat(brand.getLogo()).isNotNull();
		assertThat(brand.getSocialNetworks()).isNotEmpty();
	}

	@Test
	void supportedLanguages_returnsConfiguredLanguages() {
		MerchantStore loaded = merchantStoreRepository.findByCode(store.getCode());
		assertThat(storeFacade.supportedLanguages(loaded)).isNotEmpty();
	}

	@Test
	void deleteLogo_clearsDbAndCallsContentClient() {
		doNothing().when(contentServiceClient).uploadLogo(eq(store.getCode()), eq("logo.png"), any(), eq("image/png"));
		storeFacade.addStoreLogo(store.getCode(), "logo.png", new byte[] { 1 }, "image/png");
		doNothing().when(contentServiceClient).deleteLogo(eq(store.getCode()), eq("logo.png"));
		storeFacade.deleteLogo(store.getCode());
		verify(contentServiceClient).deleteLogo(eq(store.getCode()), eq("logo.png"));
	}
}
