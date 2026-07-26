package com.salesmanager.shop.strangler.merchant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.salesmanager.contracts.merchant.MerchantStoreSnapshot;
import com.salesmanager.core.model.merchant.MerchantStore;

class MerchantStoreEntityHydratorTest {

	private final MerchantStoreEntityHydrator hydrator = new MerchantStoreEntityHydrator();

	@Test
	void hydrate_mapsSnapshotFieldsUsedByResolver() {
		MerchantStoreSnapshot snapshot = new MerchantStoreSnapshot();
		snapshot.setId(42);
		snapshot.setCode("DEFAULT");
		snapshot.setName("Default Store");
		snapshot.setRetailer(true);
		snapshot.setDefaultLanguage("en");
		snapshot.setParentCode("PARENT");

		MerchantStore store = hydrator.hydrate(snapshot);

		assertThat(store.getId()).isEqualTo(42);
		assertThat(store.getCode()).isEqualTo("DEFAULT");
		assertThat(store.getStorename()).isEqualTo("Default Store");
		assertThat(store.isRetailer()).isTrue();
		assertThat(store.getDefaultLanguage().getCode()).isEqualTo("en");
		assertThat(store.getParent().getCode()).isEqualTo("PARENT");
	}
}
