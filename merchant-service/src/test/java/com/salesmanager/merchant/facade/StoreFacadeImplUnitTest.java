package com.salesmanager.merchant.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.salesmanager.contracts.client.ContentServiceClient;
import com.salesmanager.contracts.merchant.MerchantConfigEntity;
import com.salesmanager.contracts.merchant.MerchantStoreSnapshot;
import com.salesmanager.contracts.merchant.PersistableBrand;
import com.salesmanager.contracts.merchant.PersistableMerchantStore;
import com.salesmanager.contracts.merchant.ReadableBrand;
import com.salesmanager.contracts.merchant.ReadableMerchantStore;
import com.salesmanager.contracts.merchant.ReadableMerchantStoreList;
import com.salesmanager.core.business.exception.ConversionException;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.merchant.MerchantStoreService;
import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.business.services.system.MerchantConfigurationService;
import com.salesmanager.core.model.common.GenericEntityList;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.merchant.MerchantStoreCriteria;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.system.MerchantConfiguration;
import com.salesmanager.core.model.system.MerchantConfigurationType;
import com.salesmanager.merchant.populator.MerchantStoreSnapshotPopulator;
import com.salesmanager.merchant.populator.PersistableMerchantStorePopulator;
import com.salesmanager.merchant.populator.ReadableMerchantStorePopulator;
import com.salesmanager.merchant.support.ConversionRuntimeException;
import com.salesmanager.merchant.support.ResourceNotFoundException;
import com.salesmanager.merchant.support.ServiceRuntimeException;
import com.salesmanager.merchant.util.MerchantLogoPath;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StoreFacadeImplUnitTest {

	@Mock private MerchantStoreService merchantStoreService;
	@Mock private MerchantConfigurationService merchantConfigurationService;
	@Mock private LanguageService languageService;
	@Mock private ContentServiceClient contentServiceClient;
	@Mock private PersistableMerchantStorePopulator persistableMerchantStorePopulator;
	@Mock private ReadableMerchantStorePopulator readableMerchantStorePopulator;
	@Mock private MerchantStoreSnapshotPopulator merchantStoreSnapshotPopulator;
	@Mock private MerchantLogoPath merchantLogoPath;

	private StoreFacadeImpl facade;
	private MerchantStore store;
	private Language language;
	private ReadableMerchantStore readable;

	@BeforeEach
	void setUp() throws Exception {
		facade = new StoreFacadeImpl(
				merchantStoreService,
				merchantConfigurationService,
				languageService,
				contentServiceClient,
				persistableMerchantStorePopulator,
				readableMerchantStorePopulator,
				merchantStoreSnapshotPopulator,
				merchantLogoPath);
		store = new MerchantStore();
		store.setId(1);
		store.setCode("DEFAULT");
		store.setStorename("Default Store");
		store.setRetailer(true);
		store.setWeightunitcode("KG");
		store.setSeizeunitcode("IN");
		language = new Language("en");

		readable = new ReadableMerchantStore();
		readable.setCode("DEFAULT");
		readable.setName("Default Store");
		doAnswer(invocation -> {
			ReadableMerchantStore target = invocation.getArgument(1);
			target.setCode("DEFAULT");
			target.setName("Default Store");
			return target;
		}).when(readableMerchantStorePopulator).populate(any(), any(), any(), any());
		when(languageService.defaultLanguage()).thenReturn(language);
		when(languageService.getByCode("en")).thenReturn(language);
	}

	@Test
	void existByCode_delegatesToService() throws Exception {
		when(merchantStoreService.getByCode("DEFAULT")).thenReturn(store);
		assertThat(facade.existByCode("DEFAULT")).isTrue();
	}

	@Test
	void existByCode_serviceFailure_wrapsRuntime() throws Exception {
		when(merchantStoreService.getByCode("MISSING")).thenThrow(new ServiceException("fail"));
		assertThatThrownBy(() -> facade.existByCode("MISSING"))
				.isInstanceOf(ServiceRuntimeException.class);
	}

	@Test
	void findAll_retailers_usesListAllRetailers() throws Exception {
		MerchantStoreCriteria criteria = new MerchantStoreCriteria();
		criteria.setRetailers(true);
		when(merchantStoreService.listAllRetailers(eq(Optional.empty()), eq(0), eq(10)))
				.thenReturn(pageOf(store));
		ReadableMerchantStoreList list = facade.findAll(criteria, language, 0, 10);
		assertThat(list.getData()).hasSize(1);
	}

	@Test
	void findAll_allStores_usesListAll() throws Exception {
		MerchantStoreCriteria criteria = new MerchantStoreCriteria();
		when(merchantStoreService.listAll(eq(Optional.empty()), eq(0), eq(10)))
				.thenReturn(pageOf(store));
		ReadableMerchantStoreList list = facade.findAll(criteria, language, 0, 10);
		assertThat(list.getData()).hasSize(1);
	}

	@Test
	void findAll_byGroup_usesListByGroup() throws Exception {
		MerchantStoreCriteria criteria = new MerchantStoreCriteria();
		criteria.setStoreCode("DEFAULT");
		when(merchantStoreService.listByGroup(eq(Optional.empty()), eq("DEFAULT"), eq(0), eq(10)))
				.thenReturn(pageOf(store));
		ReadableMerchantStoreList list = facade.findAll(criteria, language, 0, 10);
		assertThat(list.getData()).hasSize(1);
	}

	@Test
	void getChildStores_returnsReadableList() throws Exception {
		when(merchantStoreService.getByCode("DEFAULT")).thenReturn(store);
		when(merchantStoreService.listChildren(eq("DEFAULT"), eq(0), eq(10)))
				.thenReturn(pageOf(store));
		ReadableMerchantStoreList list = facade.getChildStores(language, "DEFAULT", 0, 10);
		assertThat(list.getData()).hasSize(1);
	}

	@Test
	void getChildStores_missingStore_throwsNotFound() throws Exception {
		when(merchantStoreService.getByCode("MISSING")).thenReturn(null);
		assertThatThrownBy(() -> facade.getChildStores(language, "MISSING", 0, 10))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void getChildStores_notRetailer_throwsNotFound() throws Exception {
		store.setRetailer(false);
		when(merchantStoreService.getByCode("DEFAULT")).thenReturn(store);
		assertThatThrownBy(() -> facade.getChildStores(language, "DEFAULT", 0, 10))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void getMerchantStoreNames_withStoreCode() throws Exception {
		when(merchantStoreService.findAllStoreNames("DEFAULT"))
				.thenReturn(Collections.singletonList(store));
		List<ReadableMerchantStore> names = facade.getMerchantStoreNames(criteriaWithCode("DEFAULT"));
		assertThat(names).hasSize(1);
		assertThat(names.get(0).getCode()).isEqualTo("DEFAULT");
	}

	@Test
	void getMerchantStoreNames_withoutStoreCode() throws Exception {
		when(merchantStoreService.findAllStoreNames()).thenReturn(Collections.singletonList(store));
		List<ReadableMerchantStore> names = facade.getMerchantStoreNames(new MerchantStoreCriteria());
		assertThat(names).hasSize(1);
	}

	@Test
	void getBrand_withoutLogo_returnsSocialOnly() throws Exception {
		when(merchantStoreService.getByCode("DEFAULT")).thenReturn(store);
		when(merchantConfigurationService.listByType(eq(MerchantConfigurationType.SOCIAL), eq(store)))
				.thenReturn(Collections.emptyList());
		ReadableBrand brand = facade.getBrand("DEFAULT");
		assertThat(brand.getLogo()).isNull();
	}

	@Test
	void getBrand_withLogo_buildsReadableImage() throws Exception {
		store.setStoreLogo("logo.png");
		when(merchantStoreService.getByCode("DEFAULT")).thenReturn(store);
		when(merchantLogoPath.buildStoreLogoFilePath(store)).thenReturn("/files/logo.png");
		when(merchantConfigurationService.listByType(eq(MerchantConfigurationType.SOCIAL), eq(store)))
				.thenReturn(Collections.emptyList());
		ReadableBrand brand = facade.getBrand("DEFAULT");
		assertThat(brand.getLogo()).isNotNull();
		assertThat(brand.getLogo().getPath()).isEqualTo("/files/logo.png");
	}

	@Test
	void createBrand_savesSocialConfigs() throws Exception {
		when(merchantStoreService.getByCode("DEFAULT")).thenReturn(store);
		MerchantConfigEntity entity = new MerchantConfigEntity();
		entity.setKey("instagram");
		entity.setValue("https://instagram.test");
		entity.setActive(true);
		PersistableBrand brand = new PersistableBrand();
		brand.setSocialNetworks(Collections.singletonList(entity));
		facade.createBrand("DEFAULT", brand);
		verify(merchantConfigurationService).saveOrUpdate(any(MerchantConfiguration.class));
	}

	@Test
	void createBrand_emptyValue_deletesExistingConfig() throws Exception {
		when(merchantStoreService.getByCode("DEFAULT")).thenReturn(store);
		MerchantConfiguration existing = new MerchantConfiguration();
		existing.setKey("instagram");
		when(merchantConfigurationService.getMerchantConfiguration(eq("instagram"), eq(store)))
				.thenReturn(existing);
		MerchantConfigEntity entity = new MerchantConfigEntity();
		entity.setKey("instagram");
		entity.setValue("");
		PersistableBrand brand = new PersistableBrand();
		brand.setSocialNetworks(Collections.singletonList(entity));
		facade.createBrand("DEFAULT", brand);
		verify(merchantConfigurationService).delete(existing);
	}

	@Test
	void create_duplicateStore_throws() throws Exception {
		when(merchantStoreService.getByCode("child01")).thenReturn(store);
		PersistableMerchantStore incoming = new PersistableMerchantStore();
		incoming.setCode("child01");
		assertThatThrownBy(() -> facade.create(incoming))
				.isInstanceOf(ServiceRuntimeException.class)
				.hasMessageContaining("already exists");
	}

	@Test
	void create_persistsNewStore() throws Exception {
		when(merchantStoreService.getByCode("child01")).thenReturn(null);
		when(persistableMerchantStorePopulator.populate(any(), any(), any(), eq(language)))
				.thenReturn(store);
		PersistableMerchantStore incoming = new PersistableMerchantStore();
		incoming.setCode("child01");
		facade.create(incoming);
		verify(merchantStoreService).saveOrUpdate(store);
	}

	@Test
	void update_mergesPersistableStore() throws Exception {
		when(merchantStoreService.getByCode("DEFAULT")).thenReturn(store);
		when(persistableMerchantStorePopulator.populate(any(), eq(store), eq(store), eq(language)))
				.thenReturn(store);
		PersistableMerchantStore incoming = new PersistableMerchantStore();
		incoming.setCode("DEFAULT");
		facade.update(incoming);
		verify(merchantStoreService).update(store);
	}

	@Test
	void getByCode_withLangString_resolvesLanguage() throws Exception {
		when(merchantStoreService.getByCode("DEFAULT")).thenReturn(store);
		ReadableMerchantStore result = facade.getByCode("DEFAULT", "en");
		assertThat(result.getCode()).isEqualTo("DEFAULT");
	}

	@Test
	void getByCriteria_mapsStores() throws Exception {
		GenericEntityList<MerchantStore> entityList = new GenericEntityList<>();
		entityList.setList(Collections.singletonList(store));
		entityList.setTotalCount(1);
		entityList.setTotalPages(1);
		when(merchantStoreService.getByCriteria(any(MerchantStoreCriteria.class))).thenReturn(entityList);
		MerchantStoreCriteria criteria = new MerchantStoreCriteria();
		criteria.setStoreCode("DEFAULT");
		ReadableMerchantStoreList list = facade.getByCriteria(criteria, language);
		assertThat(list.getData()).hasSize(1);
	}

	@Test
	void supportedLanguages_usesStoreLanguagesWhenPresent() {
		store.setLanguages(Arrays.asList(language));
		assertThat(facade.supportedLanguages(store)).containsExactly(language);
	}

	@Test
	void supportedLanguages_refreshesFromServiceWhenEmpty() throws Exception {
		MerchantStore bare = new MerchantStore();
		bare.setCode("DEFAULT");
		store.setLanguages(Arrays.asList(language));
		when(merchantStoreService.getByCode("DEFAULT")).thenReturn(store);
		assertThat(facade.supportedLanguages(bare)).containsExactly(language);
	}

	@Test
	void getSnapshot_delegatesToPopulator() throws Exception {
		when(merchantStoreService.getByCode("DEFAULT")).thenReturn(store);
		MerchantStoreSnapshot snapshot = new MerchantStoreSnapshot();
		snapshot.setCode("DEFAULT");
		when(merchantStoreSnapshotPopulator.toSnapshot(any(ReadableMerchantStore.class))).thenReturn(snapshot);
		assertThat(facade.getSnapshot("DEFAULT", language).getCode()).isEqualTo("DEFAULT");
	}

	@Test
	void convertPopulatorFailure_wrapsConversionRuntime() throws Exception {
		when(merchantStoreService.getByCode("DEFAULT")).thenReturn(store);
		doThrow(new ConversionException("bad data"))
				.when(readableMerchantStorePopulator).populate(any(), any(), any(), any());
		assertThatThrownBy(() -> facade.getByCode("DEFAULT", language))
				.isInstanceOf(ConversionRuntimeException.class);
	}

	@Test
	void get_missingStore_throwsResourceNotFound() throws Exception {
		when(merchantStoreService.getByCode("MISSING")).thenReturn(null);
		assertThatThrownBy(() -> facade.getByCode("MISSING", language))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void findAll_serviceFailure_wrapsRuntime() throws Exception {
		when(merchantStoreService.listAll(any(), eq(0), eq(10)))
				.thenThrow(new ServiceException("fail"));
		assertThatThrownBy(() -> facade.findAll(new MerchantStoreCriteria(), language, 0, 10))
				.isInstanceOf(ServiceRuntimeException.class);
	}

	@Test
	void createBrand_serviceFailure_wrapsRuntime() throws Exception {
		when(merchantStoreService.getByCode("DEFAULT")).thenReturn(store);
		MerchantConfigEntity entity = new MerchantConfigEntity();
		entity.setKey("instagram");
		entity.setValue("https://instagram.test");
		PersistableBrand brand = new PersistableBrand();
		brand.setSocialNetworks(Collections.singletonList(entity));
		doThrow(new ServiceException("fail")).when(merchantConfigurationService)
				.saveOrUpdate(any(MerchantConfiguration.class));
		assertThatThrownBy(() -> facade.createBrand("DEFAULT", brand))
				.isInstanceOf(ServiceRuntimeException.class);
	}

	private static MerchantStoreCriteria criteriaWithCode(String code) {
		MerchantStoreCriteria criteria = new MerchantStoreCriteria();
		criteria.setStoreCode(code);
		return criteria;
	}

	private static Page<MerchantStore> pageOf(MerchantStore store) {
		return new PageImpl<>(Collections.singletonList(store));
	}
}
