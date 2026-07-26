package com.salesmanager.contracts.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class SearchDtoJacksonTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	void productIndexPayloadSerializesSchemaVersionDefaultOne() throws Exception {
		ProductIndexPayload payload = new ProductIndexPayload();
		payload.setId(42L);
		payload.setStore("default");
		payload.setLanguage("en");
		payload.setName("Sample");

		assertEquals(1, payload.getSchemaVersion());

		String json = mapper.writeValueAsString(payload);
		JsonNode tree = mapper.readTree(json);

		assertEquals(1, tree.get("schemaVersion").asInt());
		assertEquals(42L, tree.get("id").asLong());
		assertEquals("default", tree.get("store").asText());
		assertEquals("en", tree.get("language").asText());
		assertEquals("Sample", tree.get("name").asText());
		assertFalse(tree.get("addToCart").asBoolean());

		ProductIndexPayload roundTrip = mapper.readValue(json, ProductIndexPayload.class);
		assertEquals(1, roundTrip.getSchemaVersion());
		assertEquals(Long.valueOf(42L), roundTrip.getId());
		assertEquals("default", roundTrip.getStore());
	}

	@Test
	void valueListRoundTripsStringValues() throws Exception {
		ValueList list = new ValueList();
		list.getValues().add("shirt");
		list.getValues().add("shoes");

		String json = mapper.writeValueAsString(list);
		JsonNode tree = mapper.readTree(json);

		assertEquals(2, tree.get("values").size());
		assertEquals("shirt", tree.get("values").get(0).asText());

		ValueList roundTrip = mapper.readValue(json, ValueList.class);
		assertEquals(2, roundTrip.getValues().size());
		assertEquals("shoes", roundTrip.getValues().get(1));
	}

	@Test
	void productIndexBulkPayloadRespectsMaxBatchConstant() {
		assertEquals(50, ProductIndexBulkPayload.MAX_BATCH_SIZE);
		assertTrue(ProductIndexBulkPayload.MAX_BATCH_SIZE > 0);
	}

	@Test
	void searchItemIgnoresUnknownFieldsDuringDeserialization() throws Exception {
		String json = "[{\"id\":1,\"name\":\"Phone\",\"futureField\":\"ignored\","
				+ "\"attributes\":{\"color\":\"red\",\"futureAttr\":\"x\"},"
				+ "\"variants\":[{\"sku\":\"V-1\",\"futureVariant\":1}],"
				+ "\"inventory\":[{\"qty\":\"5\",\"futureQty\":\"0\"}]}]";

		SearchItem[] items = mapper.readValue(json, SearchItem[].class);

		assertEquals(1, items.length);
		assertEquals(Long.valueOf(1L), items[0].getId());
		assertEquals("Phone", items[0].getName());
		assertEquals("red", items[0].getAttributes().get("color"));
		assertEquals("V-1", items[0].getVariants().get(0).get("sku"));
		assertEquals("5", items[0].getInventory().get(0).get("qty"));
	}

	@Test
	void searchItemRoundTripsLegacyPactJsonShape() throws Exception {
		String legacyJson = "[{\"id\":1,\"name\":\"Phone\",\"description\":\"Smart phone\"}]";

		SearchItem[] items = mapper.readValue(legacyJson, SearchItem[].class);

		assertEquals(1, items.length);
		assertEquals(Long.valueOf(1L), items[0].getId());
		assertEquals("Phone", items[0].getName());
		assertEquals("Smart phone", items[0].getDescription());
		assertTrue(items[0].isAddToCart());

		String reserialized = mapper.writeValueAsString(items[0]);
		JsonNode tree = mapper.readTree(reserialized);
		assertEquals(1L, tree.get("id").asLong());
		assertEquals("Phone", tree.get("name").asText());
		assertEquals("Smart phone", tree.get("description").asText());
	}

}
