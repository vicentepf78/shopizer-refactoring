package com.salesmanager.contracts.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.contracts.customer.CustomerEntity;
import com.salesmanager.contracts.order.OrderEntity;

class TenantContractJacksonTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	void orderAndCustomerEntitiesFollowShopEntityShape() throws Exception {
		OrderEntity order = new OrderEntity();
		order.setId(11L);
		order.setLanguage("en");

		CustomerEntity customer = new CustomerEntity();
		customer.setId(22L);
		customer.setLanguage("fr");

		JsonNode orderJson = mapper.readTree(mapper.writeValueAsString(order));
		JsonNode customerJson = mapper.readTree(mapper.writeValueAsString(customer));

		assertEquals(11L, orderJson.get("id").asLong());
		assertEquals("en", orderJson.get("language").asText());
		assertEquals(22L, customerJson.get("id").asLong());
		assertEquals("fr", customerJson.get("language").asText());
		assertFalse(orderJson.has("auditSection"));
		assertFalse(customerJson.has("hibernateLazyInitializer"));
	}

}
