package com.salesmanager.merchant.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.contracts.client.ContentServiceClient;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.merchant.MerchantStoreService;
import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.business.services.system.MerchantConfigurationService;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.merchant.populator.MerchantStoreSnapshotPopulator;
import com.salesmanager.merchant.populator.PersistableMerchantStorePopulator;
import com.salesmanager.merchant.populator.ReadableMerchantStorePopulator;
import com.salesmanager.merchant.util.MerchantLogoPath;

@ExtendWith(MockitoExtension.class)
class LogoOrchestrationTest {

	@Mock
	private MerchantStoreService merchantStoreService;
	@Mock
	private MerchantConfigurationService merchantConfigurationService;
	@Mock
	private LanguageService languageService;
	@Mock
	private ContentServiceClient contentServiceClient;
	@Mock
	private PersistableMerchantStorePopulator persistableMerchantStorePopulator;
	@Mock
	private ReadableMerchantStorePopulator readableMerchantStorePopulator;
	@Mock
	private MerchantStoreSnapshotPopulator merchantStoreSnapshotPopulator;
	@Mock
	private MerchantLogoPath merchantLogoPath;

	private StoreFacadeImpl storeFacade;

	@BeforeEach
	void setUp() {
		storeFacade = new StoreFacadeImpl(
				merchantStoreService,
				merchantConfigurationService,
				languageService,
				contentServiceClient,
				persistableMerchantStorePopulator,
				readableMerchantStorePopulator,
				merchantStoreSnapshotPopulator,
				merchantLogoPath);
	}

	@Test
	void uploadLogo_dbFailure_compensatesBlobDelete() throws Exception {
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		when(merchantStoreService.getByCode("DEFAULT")).thenReturn(store);
		doThrow(new ServiceException("db fail")).when(merchantStoreService).save(any(MerchantStore.class));

		try {
			storeFacade.addStoreLogo("DEFAULT", "logo.png", new byte[] { 1, 2 }, "image/png");
		} catch (RuntimeException ignored) {
			// expected
		}

		verify(contentServiceClient).uploadLogo(eq("DEFAULT"), eq("logo.png"), any(), eq("image/png"));
		verify(contentServiceClient).deleteLogo(eq("DEFAULT"), eq("logo.png"));
	}

	@Test
	void deleteLogo_contentFailure_keepsDbClear() throws Exception {
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		store.setStoreLogo("logo.png");
		when(merchantStoreService.getByCode("DEFAULT")).thenReturn(store);
		doAnswer(invocation -> invocation.getArgument(0)).when(merchantStoreService).update(any(MerchantStore.class));
		doThrow(new RuntimeException("content down"))
				.when(contentServiceClient).deleteLogo(eq("DEFAULT"), eq("logo.png"));

		storeFacade.deleteLogo("DEFAULT");

		verify(merchantStoreService).update(any(MerchantStore.class));
		assertThat(store.getStoreLogo()).isNull();
	}
}
