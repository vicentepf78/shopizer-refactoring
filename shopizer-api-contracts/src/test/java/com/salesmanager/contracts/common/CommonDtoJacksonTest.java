package com.salesmanager.contracts.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class CommonDtoJacksonTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	void entitySerializesAndDeserializesIdWithoutJpaFields() throws Exception {
		Entity entity = new Entity(42L);

		String json = mapper.writeValueAsString(entity);
		JsonNode tree = mapper.readTree(json);

		assertEquals(42L, tree.get("id").asLong());
		assertFalse(tree.has("auditSection"));
		assertFalse(tree.has("hibernateLazyInitializer"));
		assertEquals(1, tree.size());

		Entity roundTrip = mapper.readValue(json, Entity.class);
		assertEquals(Long.valueOf(42L), roundTrip.getId());
	}

	@Test
	void entityExistsSerializesBooleanFlag() throws Exception {
		String trueJson = mapper.writeValueAsString(new EntityExists(true));
		String falseJson = mapper.writeValueAsString(new EntityExists(false));

		assertTrue(mapper.readTree(trueJson).get("exists").asBoolean());
		assertFalse(mapper.readTree(falseJson).get("exists").asBoolean());

		assertTrue(mapper.readValue(trueJson, EntityExists.class).isExists());
		assertFalse(mapper.readValue(falseJson, EntityExists.class).isExists());
	}

	@Test
	void readableEntityListPreservesItemsAndPaginationMetadata() throws Exception {
		Entity first = new Entity(1L);
		Entity second = new Entity(2L);

		ReadableEntityList<Entity> list = new ReadableEntityList<>();
		list.setItems(Arrays.asList(first, second));
		list.setTotalPages(3);
		list.setNumber(2);
		list.setRecordsTotal(5L);
		list.setRecordsFiltered(5);

		String json = mapper.writeValueAsString(list);
		JsonNode tree = mapper.readTree(json);

		assertEquals(3, tree.get("totalPages").asInt());
		assertEquals(2, tree.get("number").asInt());
		assertEquals(5L, tree.get("recordsTotal").asLong());
		assertEquals(5, tree.get("recordsFiltered").asInt());
		assertEquals(2, tree.get("items").size());
		assertEquals(1L, tree.get("items").get(0).get("id").asLong());
		assertEquals(2L, tree.get("items").get(1).get("id").asLong());

		@SuppressWarnings("unchecked")
		ReadableEntityList<Entity> roundTrip = mapper.readValue(json, ReadableEntityList.class);
		List<?> items = roundTrip.getItems();
		assertEquals(2, items.size());
		assertEquals(3, roundTrip.getTotalPages());
		assertEquals(2, roundTrip.getNumber());
		assertEquals(5L, roundTrip.getRecordsTotal());
		assertEquals(5, roundTrip.getRecordsFiltered());
	}

	@Test
	void shopEntitySerializesLanguageAndId() throws Exception {
		ShopEntity entity = new ShopEntity() {
		};
		entity.setId(7L);
		entity.setLanguage("en");

		String json = mapper.writeValueAsString(entity);
		JsonNode tree = mapper.readTree(json);

		assertEquals(7L, tree.get("id").asLong());
		assertEquals("en", tree.get("language").asText());
		assertFalse(tree.has("auditSection"));
	}

}
