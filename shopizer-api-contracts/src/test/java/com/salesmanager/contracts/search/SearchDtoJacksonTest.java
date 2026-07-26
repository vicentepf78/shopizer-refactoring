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

}
