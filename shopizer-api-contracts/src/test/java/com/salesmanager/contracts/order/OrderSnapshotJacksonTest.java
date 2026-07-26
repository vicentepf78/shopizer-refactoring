package com.salesmanager.contracts.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class OrderSnapshotJacksonTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	void roundTripPreservesStatusTotalsAndLineSkuQuantity() throws Exception {
		OrderLineSnapshot line = new OrderLineSnapshot();
		line.setSku("SKU-42");
		line.setProductName("Widget");
		line.setQuantity(3);
		line.setOneTimeCharge(new BigDecimal("29.99"));

		OrderTotalSnapshot total = new OrderTotalSnapshot();
		total.setCode("order.total.total");
		total.setTitle("Total");
		total.setValue(new BigDecimal("89.97"));
		total.setOrderTotalType("TOTAL");
		total.setSortOrder(99);

		OrderSnapshot snapshot = new OrderSnapshot();
		snapshot.setSchemaVersion(1);
		snapshot.setId(100L);
		snapshot.setStatus("ORDERED");
		snapshot.setCustomerId(7L);
		snapshot.setCustomerEmailAddress("buyer@example.com");
		snapshot.setStoreCode("DEFAULT");
		snapshot.setCurrencyCode("USD");
		snapshot.setTotal(new BigDecimal("89.97"));
		snapshot.setLines(List.of(line));
		snapshot.setTotals(List.of(total));

		String json = mapper.writeValueAsString(snapshot);
		OrderSnapshot restored = mapper.readValue(json, OrderSnapshot.class);
		JsonNode tree = mapper.readTree(json);

		assertEquals("ORDERED", restored.getStatus());
		assertEquals("SKU-42", restored.getLines().get(0).getSku());
		assertEquals(3, restored.getLines().get(0).getQuantity());
		assertEquals(0, new BigDecimal("89.97").compareTo(restored.getTotals().get(0).getValue()));
		assertEquals("ORDERED", tree.get("status").asText());
		assertEquals("SKU-42", tree.get("lines").get(0).get("sku").asText());
		assertEquals(3, tree.get("lines").get(0).get("quantity").asInt());
		assertEquals("89.97", tree.get("totals").get(0).get("value").asText());
		assertFalse(tree.has("orderProducts"));
		assertFalse(tree.has("hibernateLazyInitializer"));
	}

	@Test
	void emptySnapshotSerializesStableFieldNames() throws Exception {
		OrderSnapshot snapshot = new OrderSnapshot();

		JsonNode tree = mapper.readTree(mapper.writeValueAsString(snapshot));

		assertTrue(tree.has("schemaVersion"));
		assertTrue(tree.has("lines"));
		assertTrue(tree.has("totals"));
		assertEquals(1, tree.get("schemaVersion").asInt());
	}

}
