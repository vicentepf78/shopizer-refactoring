package com.salesmanager.core.business.services.search.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.contracts.catalog.ProductSnapshot;
import com.salesmanager.contracts.catalog.ProductSnapshotAttribute;
import com.salesmanager.contracts.catalog.ProductSnapshotInventory;
import com.salesmanager.contracts.catalog.ProductSnapshotVariant;
import com.salesmanager.contracts.search.ProductIndexPayload;

class ProductSnapshotIndexMapperTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	void toPayloadSetsSchemaVersionTwo() {
		ProductIndexPayload payload = ProductSnapshotIndexMapper.toPayload(sampleSnapshot());

		assertEquals(ProductSnapshotIndexMapper.SNAPSHOT_BACKED_SCHEMA_VERSION, payload.getSchemaVersion());
		assertEquals(2, payload.getSchemaVersion());
	}

	@Test
	void toPayloadMapsSnapshotFieldsToLegacyPayloadShape() {
		ProductIndexPayload payload = ProductSnapshotIndexMapper.toPayload(sampleSnapshot());

		assertEquals(Long.valueOf(42L), payload.getId());
		assertEquals("default", payload.getStore());
		assertEquals("en", payload.getLanguage());
		assertEquals("Sample product", payload.getName());
		assertEquals("English description", payload.getDescription());
		assertEquals("sample-product", payload.getLink());
		assertEquals("image.jpg", payload.getImage());
		assertEquals("Acme", payload.getBrand());
		assertEquals("Books", payload.getCategory());
		assertEquals("4.5", payload.getReviews());
		assertFalse(payload.getAddToCart());
		assertEquals("Red", payload.getAttributes().get("Color"));
		assertEquals(1, payload.getVariants().size());
		assertEquals("M", payload.getVariants().get(0).get("size"));
		assertEquals("V-SKU", payload.getVariants().get(0).get("VSKU"));
		assertEquals(1, payload.getInventory().size());
		assertEquals("SKU-1", payload.getInventory().get(0).get("SKU"));
		assertEquals("5", payload.getInventory().get(0).get("QTY"));
		assertEquals("19.99", payload.getInventory().get(0).get("PRICE"));
		assertEquals("17.99", payload.getInventory().get(0).get("DISCOUNT"));
	}

	@Test
	void productIndexPayloadV1StillDeserializes() throws Exception {
		String json = "{\"schemaVersion\":1,\"id\":42,\"store\":\"default\",\"language\":\"en\",\"name\":\"Sample\"}";

		ProductIndexPayload payload = mapper.readValue(json, ProductIndexPayload.class);

		assertEquals(1, payload.getSchemaVersion());
		assertEquals(Long.valueOf(42L), payload.getId());
		assertEquals("default", payload.getStore());
		assertEquals("en", payload.getLanguage());
		assertEquals("Sample", payload.getName());
		assertTrue(payload.getName().length() > 0);
	}

	private static ProductSnapshot sampleSnapshot() {
		ProductSnapshot snapshot = new ProductSnapshot();
		snapshot.setProductId(42L);
		snapshot.setStoreCode("default");
		snapshot.setSku("SKU-1");
		snapshot.setLanguage("en");
		snapshot.setName("Sample product");
		snapshot.setDescription("English description");
		snapshot.setFriendlyUrl("sample-product");
		snapshot.setImageUrl("image.jpg");
		snapshot.setReviewAverage("4.5");
		snapshot.setBrandName("Acme");
		snapshot.setCategoryName("Books");
		snapshot.setAddToCart(Boolean.FALSE);

		ProductSnapshotAttribute attribute = new ProductSnapshotAttribute();
		attribute.setName("Color");
		attribute.setValue("Red");
		snapshot.getAttributes().add(attribute);

		ProductSnapshotVariant variant = new ProductSnapshotVariant();
		variant.getOptions().put("size", "M");
		variant.setSku("V-SKU");
		snapshot.getVariants().add(variant);

		ProductSnapshotInventory inventory = new ProductSnapshotInventory();
		inventory.setSku("SKU-1");
		inventory.setQuantity(5L);
		inventory.setPrice("19.99");
		inventory.setDiscountPrice("17.99");
		snapshot.getInventory().add(inventory);

		return snapshot;
	}

}
