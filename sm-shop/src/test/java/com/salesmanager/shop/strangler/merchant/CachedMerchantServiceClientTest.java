package com.salesmanager.shop.strangler.merchant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.contracts.client.MerchantServiceClient;
import com.salesmanager.contracts.merchant.MerchantStoreSnapshot;

@ExtendWith(MockitoExtension.class)
class CachedMerchantServiceClientTest {

	@Mock
	private MerchantServiceClient delegate;

	@Test
	void getStoreSnapshot_cachesWithinTtl() {
		MerchantStoreSnapshot snapshot = new MerchantStoreSnapshot();
		snapshot.setCode("DEFAULT");
		when(delegate.getStoreSnapshot("DEFAULT")).thenReturn(snapshot);

		CachedMerchantServiceClient cached = new CachedMerchantServiceClient(delegate, 60L);

		assertThat(cached.getStoreSnapshot("DEFAULT")).isSameAs(snapshot);
		assertThat(cached.getStoreSnapshot("DEFAULT")).isSameAs(snapshot);

		verify(delegate, times(1)).getStoreSnapshot("DEFAULT");
	}

	@Test
	void getStoreSnapshot_doesNotCacheNull() {
		when(delegate.getStoreSnapshot("MISSING")).thenReturn(null);

		CachedMerchantServiceClient cached = new CachedMerchantServiceClient(delegate, 60L);

		assertThat(cached.getStoreSnapshot("MISSING")).isNull();
		assertThat(cached.getStoreSnapshot("MISSING")).isNull();

		verify(delegate, times(2)).getStoreSnapshot("MISSING");
	}
}
