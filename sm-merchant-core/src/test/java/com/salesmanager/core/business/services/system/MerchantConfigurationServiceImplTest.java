package com.salesmanager.core.business.services.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.repositories.system.MerchantConfigurationRepository;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.system.MerchantConfig;
import com.salesmanager.core.model.system.MerchantConfiguration;
import com.salesmanager.core.model.system.MerchantConfigurationType;

@ExtendWith(MockitoExtension.class)
class MerchantConfigurationServiceImplTest {

	@Mock
	private MerchantConfigurationRepository merchantConfigurationRepository;

	private MerchantConfigurationServiceImpl merchantConfigurationService;

	@BeforeEach
	void setUp() {
		merchantConfigurationService = new MerchantConfigurationServiceImpl(merchantConfigurationRepository);
	}

	@Test
	void getMerchantConfiguration_delegatesToRepository() throws Exception {
		MerchantStore store = store(1);
		MerchantConfiguration config = config(5L, "KEY");
		when(merchantConfigurationRepository.findByMerchantStoreAndKey(1, "KEY")).thenReturn(config);

		assertSame(config, merchantConfigurationService.getMerchantConfiguration("KEY", store));
	}

	@Test
	void listByStore_delegatesToRepository() throws Exception {
		MerchantStore store = store(2);
		List<MerchantConfiguration> expected = Collections.singletonList(config(1L, "A"));
		when(merchantConfigurationRepository.findByMerchantStore(2)).thenReturn(expected);

		assertSame(expected, merchantConfigurationService.listByStore(store));
	}

	@Test
	void listByType_delegatesToRepository() throws Exception {
		MerchantStore store = store(3);
		List<MerchantConfiguration> expected = Collections.singletonList(config(2L, "B"));
		when(merchantConfigurationRepository.findByMerchantStoreAndType(3, MerchantConfigurationType.CONFIG))
				.thenReturn(expected);

		assertSame(expected, merchantConfigurationService.listByType(MerchantConfigurationType.CONFIG, store));
	}

	@Test
	void saveOrUpdate_newEntity_creates() throws Exception {
		MerchantConfiguration config = config(null, "NEW");
		when(merchantConfigurationRepository.saveAndFlush(config)).thenReturn(config);

		merchantConfigurationService.saveOrUpdate(config);

		verify(merchantConfigurationRepository).saveAndFlush(config);
	}

	@Test
	void saveOrUpdate_existingEntity_updates() throws Exception {
		MerchantConfiguration config = config(9L, "OLD");
		when(merchantConfigurationRepository.saveAndFlush(config)).thenReturn(config);

		merchantConfigurationService.saveOrUpdate(config);

		verify(merchantConfigurationRepository).saveAndFlush(config);
	}

	@Test
	void delete_whenFound_removesEntity() throws Exception {
		MerchantConfiguration input = config(7L, "DEL");
		MerchantConfiguration loaded = config(7L, "DEL");
		when(merchantConfigurationRepository.getOne(7L)).thenReturn(loaded);

		merchantConfigurationService.delete(input);

		verify(merchantConfigurationRepository).delete(loaded);
	}

	@Test
	void delete_whenMissing_skipsDelete() throws Exception {
		MerchantConfiguration input = config(8L, "MISSING");
		when(merchantConfigurationRepository.getOne(8L)).thenReturn(null);

		merchantConfigurationService.delete(input);

		verify(merchantConfigurationRepository, never()).delete(any(MerchantConfiguration.class));
	}

	@Test
	void getMerchantConfig_parsesJsonValue() throws Exception {
		MerchantStore store = store(4);
		MerchantConfiguration configuration = config(1L, MerchantConfigurationType.CONFIG.name());
		configuration.setValue(new MerchantConfig().toJSONString());
		when(merchantConfigurationRepository.findByMerchantStoreAndKey(4, MerchantConfigurationType.CONFIG.name()))
				.thenReturn(configuration);

		MerchantConfig config = merchantConfigurationService.getMerchantConfig(store);

		assertNotNull(config);
	}

	@Test
	void getMerchantConfig_invalidJson_throwsServiceException() throws Exception {
		MerchantStore store = store(5);
		MerchantConfiguration configuration = config(2L, MerchantConfigurationType.CONFIG.name());
		configuration.setValue("{not-json");
		when(merchantConfigurationRepository.findByMerchantStoreAndKey(5, MerchantConfigurationType.CONFIG.name()))
				.thenReturn(configuration);

		assertThrows(ServiceException.class, () -> merchantConfigurationService.getMerchantConfig(store));
	}

	@Test
	void saveMerchantConfig_createsWhenMissing() throws Exception {
		MerchantStore store = store(6);
		MerchantConfig config = new MerchantConfig();
		when(merchantConfigurationRepository.findByMerchantStoreAndKey(6, MerchantConfigurationType.CONFIG.name()))
				.thenReturn(null);
		when(merchantConfigurationRepository.saveAndFlush(any(MerchantConfiguration.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		merchantConfigurationService.saveMerchantConfig(config, store);

		verify(merchantConfigurationRepository).saveAndFlush(any(MerchantConfiguration.class));
	}

	@Test
	void saveMerchantConfig_updatesExisting() throws Exception {
		MerchantStore store = store(7);
		MerchantConfig config = new MerchantConfig();
		MerchantConfiguration existing = config(3L, MerchantConfigurationType.CONFIG.name());
		existing.setMerchantStore(store);
		when(merchantConfigurationRepository.findByMerchantStoreAndKey(7, MerchantConfigurationType.CONFIG.name()))
				.thenReturn(existing);
		when(merchantConfigurationRepository.saveAndFlush(existing)).thenReturn(existing);

		merchantConfigurationService.saveMerchantConfig(config, store);

		verify(merchantConfigurationRepository).saveAndFlush(existing);
		assertEquals(MerchantConfigurationType.CONFIG.name(), existing.getKey());
	}

	private static MerchantStore store(int id) {
		MerchantStore store = new MerchantStore();
		store.setId(id);
		store.setCode("S" + id);
		return store;
	}

	private static MerchantConfiguration config(Long id, String key) {
		MerchantConfiguration configuration = new MerchantConfiguration();
		configuration.setId(id);
		configuration.setKey(key);
		return configuration;
	}
}
