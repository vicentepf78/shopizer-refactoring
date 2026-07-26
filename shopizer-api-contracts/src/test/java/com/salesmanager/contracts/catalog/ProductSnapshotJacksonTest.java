package com.salesmanager.contracts.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class ProductSnapshotJacksonTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	void productSnapshotSerializesSchemaVersionDefaultOne() throws Exception {
		ProductSnapshot snapshot = new ProductSnapshot();
		snapshot.setProductId(42L);
		snapshot.setStoreCode("default");
		snapshot.setSku("SKU-1");
		snapshot.setLanguage("en");
		snapshot.setName("Sample");

		assertEquals(1, snapshot.getSchemaVersion());

		String json = mapper.writeValueAsString(snapshot);
		JsonNode tree = mapper.readTree(json);

		assertEquals(1, tree.get("schemaVersion").asInt());
		assertEquals(42L, tree.get("productId").asLong());
		assertEquals("default", tree.get("storeCode").asText());
		assertEquals("SKU-1", tree.get("sku").asText());
		assertEquals("en", tree.get("language").asText());
		assertEquals("Sample", tree.get("name").asText());
		assertFalse(tree.get("addToCart").asBoolean());

		ProductSnapshot roundTrip = mapper.readValue(json, ProductSnapshot.class);
		assertEquals(1, roundTrip.getSchemaVersion());
		assertEquals(Long.valueOf(42L), roundTrip.getProductId());
		assertEquals("default", roundTrip.getStoreCode());
		assertEquals("SKU-1", roundTrip.getSku());
	}

	@Test
	void nestedTypesRoundTrip() throws Exception {
		ProductSnapshot snapshot = new ProductSnapshot();
		snapshot.setProductId(7L);
		snapshot.setStoreCode("default");
		snapshot.setLanguage("en");

		ProductSnapshotAttribute attribute = new ProductSnapshotAttribute();
		attribute.setName("Color");
		attribute.setValue("Red");
		snapshot.getAttributes().add(attribute);

		ProductSnapshotVariant variant = new ProductSnapshotVariant();
		variant.getOptions().put("size", "M");
		variant.setSku("V-SKU");
		snapshot.getVariants().add(variant);

		ProductSnapshotInventory inventory = new ProductSnapshotInventory();
		inventory.setSku("SKU-7");
		inventory.setQuantity(3L);
		inventory.setPrice("9.99");
		snapshot.getInventory().add(inventory);

		ProductSnapshot roundTrip = mapper.readValue(mapper.writeValueAsString(snapshot), ProductSnapshot.class);
		assertEquals(1, roundTrip.getAttributes().size());
		assertEquals("Color", roundTrip.getAttributes().get(0).getName());
		assertEquals(1, roundTrip.getVariants().size());
		assertEquals("M", roundTrip.getVariants().get(0).getOptions().get("size"));
		assertEquals("V-SKU", roundTrip.getVariants().get(0).getSku());
		assertEquals(3L, roundTrip.getInventory().get(0).getQuantity());
		assertTrue(roundTrip.getInventory().get(0).getPrice().contains("9.99"));
	}

}
