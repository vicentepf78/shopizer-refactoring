package com.salesmanager.shop.strangler.merchant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.salesmanager.contracts.merchant.MerchantStoreSnapshot;
import com.salesmanager.core.model.merchant.MerchantStore;

class MerchantStoreEntityHydratorTest {

	private final MerchantStoreEntityHydrator hydrator = new MerchantStoreEntityHydrator();

	@Test
	void hydrate_nullSnapshot_returnsNull() {
		assertThat(hydrator.hydrate(null)).isNull();
	}

	@Test
	void hydrate_mapsFieldsAndRelations() {
		MerchantStoreSnapshot snapshot = new MerchantStoreSnapshot();
		snapshot.setId(42);
		snapshot.setCode("CHILD");
		snapshot.setName("Child Store");
		snapshot.setRetailer(true);
		snapshot.setEmail("child@example.com");
		snapshot.setPhone("555");
		snapshot.setTemplate("default");
		snapshot.setUseCache(true);
		snapshot.setCurrencyFormatNational(false);
		snapshot.setDefaultLanguage("en");
		snapshot.setParentCode("PARENT");

		MerchantStore store = hydrator.hydrate(snapshot);

		assertThat(store.getId()).isEqualTo(42);
		assertThat(store.getCode()).isEqualTo("CHILD");
		assertThat(store.getStorename()).isEqualTo("Child Store");
		assertThat(store.isRetailer()).isTrue();
		assertThat(store.getStoreEmailAddress()).isEqualTo("child@example.com");
		assertThat(store.getStorephone()).isEqualTo("555");
		assertThat(store.getStoreTemplate()).isEqualTo("default");
		assertThat(store.isUseCache()).isTrue();
		assertThat(store.isCurrencyFormatNational()).isFalse();
		assertThat(store.getDefaultLanguage().getCode()).isEqualTo("en");
		assertThat(store.getParent().getCode()).isEqualTo("PARENT");
	}

	@Test
	void hydrate_skipsOptionalFieldsWhenBlank() {
		MerchantStoreSnapshot snapshot = new MerchantStoreSnapshot();
		snapshot.setCode("SOLO");

		MerchantStore store = hydrator.hydrate(snapshot);

		assertThat(store.getCode()).isEqualTo("SOLO");
		assertThat(store.getDefaultLanguage()).isNull();
		assertThat(store.getParent()).isNull();
	}
}
