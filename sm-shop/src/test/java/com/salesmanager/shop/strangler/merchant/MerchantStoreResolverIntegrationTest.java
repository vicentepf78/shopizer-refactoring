package com.salesmanager.shop.strangler.merchant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import com.salesmanager.contracts.client.MerchantServiceClient;
import com.salesmanager.contracts.merchant.MerchantStoreSnapshot;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.shop.application.config.MerchantStoreArgumentResolver;
import com.salesmanager.shop.store.controller.store.facade.StoreFacade;
import com.salesmanager.shop.store.controller.user.facade.UserFacade;

@ExtendWith(MockitoExtension.class)
class MerchantStoreResolverIntegrationTest {

	@Mock
	private StoreFacade storeFacade;
	@Mock
	private UserFacade userFacade;
	@Mock
	private MerchantServiceClient merchantServiceClient;
	@Mock
	private MerchantStoreEntityHydrator merchantStoreEntityHydrator;

	@InjectMocks
	private MerchantStoreArgumentResolver resolver;

	@BeforeEach
	void authorizeAlways() {
		when(userFacade.authorizeStore(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
				.thenReturn(true);
	}

	@Test
	void resolveArgument_usesHttpSnapshotWhenClientPresent() throws Exception {
		MerchantStoreSnapshot snapshot = new MerchantStoreSnapshot();
		snapshot.setCode("DEFAULT");
		MerchantStore hydrated = new MerchantStore();
		hydrated.setCode("DEFAULT");
		when(merchantServiceClient.getStoreSnapshot("DEFAULT")).thenReturn(snapshot);
		when(merchantStoreEntityHydrator.hydrate(snapshot)).thenReturn(hydrated);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("store", "DEFAULT");
		ServletWebRequest webRequest = new ServletWebRequest(request);

		Object resolved = resolver.resolveArgument(null, null, webRequest, null);

		assertThat(resolved).isInstanceOf(MerchantStore.class);
		assertThat(((MerchantStore) resolved).getCode()).isEqualTo("DEFAULT");
	}
}
