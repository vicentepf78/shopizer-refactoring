package com.salesmanager.core.business.services.search.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.contracts.catalog.ProductSnapshot;
import com.salesmanager.contracts.search.ProductIndexPayload;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.merchant.MerchantStore;

@ExtendWith(MockitoExtension.class)
class ProductIndexPayloadBuilderTest {

	@Mock
	private com.salesmanager.core.business.services.catalog.ProductSnapshotBuilder snapshotBuilder;

	@InjectMocks
	private ProductIndexPayloadBuilder builder;

	@Test
	void buildAll_mapsSnapshotToPayloadWithSchemaVersionTwo() throws ServiceException {
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		Product product = new Product();
		product.setId(42L);

		ProductSnapshot snapshot = new ProductSnapshot();
		snapshot.setProductId(42L);
		snapshot.setStoreCode("default");
		snapshot.setSku("SKU-1");
		snapshot.setLanguage("en");
		snapshot.setName("Sample product");
		when(snapshotBuilder.buildAll(store, product)).thenReturn(Collections.singletonList(snapshot));

		List<ProductIndexPayload> payloads = builder.buildAll(store, product);

		assertThat(payloads).hasSize(1);
		assertThat(payloads.get(0).getSchemaVersion())
				.isEqualTo(ProductSnapshotIndexMapper.SNAPSHOT_BACKED_SCHEMA_VERSION);
		assertThat(payloads.get(0).getId()).isEqualTo(42L);
		assertThat(payloads.get(0).getStore()).isEqualTo("default");
		assertThat(payloads.get(0).getLanguage()).isEqualTo("en");
		assertThat(payloads.get(0).getName()).isEqualTo("Sample product");
	}

}
