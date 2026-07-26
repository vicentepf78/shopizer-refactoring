package com.salesmanager.shop.strangler.search;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.salesmanager.contracts.catalog.ProductSnapshot;
import com.salesmanager.contracts.search.ProductIndexPayload;
import com.salesmanager.core.business.services.search.index.ProductSnapshotIndexMapper;

class ProductIndexPayloadMapperTest {

	private final ProductIndexPayloadMapper mapper = new ProductIndexPayloadMapper();

	@Test
	void toPayload_setsSchemaVersionTwoFromSnapshot() {
		ProductSnapshot snapshot = new ProductSnapshot();
		snapshot.setProductId(99L);
		snapshot.setStoreCode("default");
		snapshot.setSku("SKU-99");
		snapshot.setLanguage("en");
		snapshot.setName("Indexed product");

		ProductIndexPayload payload = mapper.toPayload(snapshot);

		assertThat(payload.getSchemaVersion()).isEqualTo(ProductSnapshotIndexMapper.SNAPSHOT_BACKED_SCHEMA_VERSION);
		assertThat(payload.getId()).isEqualTo(99L);
		assertThat(payload.getStore()).isEqualTo("default");
		assertThat(payload.getLanguage()).isEqualTo("en");
		assertThat(payload.getName()).isEqualTo("Indexed product");
	}

}
