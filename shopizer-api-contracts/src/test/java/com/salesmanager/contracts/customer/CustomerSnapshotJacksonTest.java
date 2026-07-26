package com.salesmanager.contracts.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class CustomerSnapshotJacksonTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	void roundTripPreservesCheckoutFieldsWithoutLazyCollections() throws Exception {
		AddressSnapshot billing = new AddressSnapshot();
		billing.setFirstName("Ada");
		billing.setLastName("Lovelace");
		billing.setCity("London");
		billing.setCountryCode("GB");

		AddressSnapshot delivery = new AddressSnapshot();
		delivery.setFirstName("Ada");
		delivery.setLastName("Lovelace");
		delivery.setCity("London");
		delivery.setCountryCode("GB");

		CustomerSnapshot snapshot = new CustomerSnapshot();
		snapshot.setSchemaVersion(1);
		snapshot.setId(55L);
		snapshot.setEmailAddress("ada@example.com");
		snapshot.setNick("ada");
		snapshot.setCompany("Analytical Engines");
		snapshot.setAnonymous(false);
		snapshot.setLanguage("en");
		snapshot.setBilling(billing);
		snapshot.setDelivery(delivery);

		String json = mapper.writeValueAsString(snapshot);
		CustomerSnapshot restored = mapper.readValue(json, CustomerSnapshot.class);
		JsonNode tree = mapper.readTree(json);

		assertEquals("ada@example.com", restored.getEmailAddress());
		assertEquals("GB", restored.getBilling().getCountryCode());
		assertEquals("ada@example.com", tree.get("emailAddress").asText());
		assertEquals("GB", tree.get("billing").get("countryCode").asText());
		assertEquals("en", tree.get("language").asText());
		assertFalse(tree.has("languageCode"));
		assertFalse(tree.has("attributes"));
		assertFalse(tree.has("reviews"));
		assertFalse(tree.has("groups"));
		assertFalse(tree.has("password"));
		assertFalse(tree.has("merchantStore"));
		assertFalse(tree.has("hibernateLazyInitializer"));
	}

	@Test
	void anonymousSnapshotHasNoLazyGraphFields() throws Exception {
		CustomerSnapshot snapshot = new CustomerSnapshot();
		snapshot.setAnonymous(true);
		snapshot.setEmailAddress("guest@example.com");

		JsonNode tree = mapper.readTree(mapper.writeValueAsString(snapshot));

		assertTrue(tree.get("anonymous").asBoolean());
		assertEquals("guest@example.com", tree.get("emailAddress").asText());
		assertFalse(tree.has("attributes"));
	}

	@Test
	void ignoresUnknownFieldsDuringDeserialization() throws Exception {
		String json = "{\"schemaVersion\":2,\"emailAddress\":\"ada@example.com\",\"futureField\":\"ignored\","
				+ "\"billing\":{\"countryCode\":\"GB\",\"futureAddressField\":true},"
				+ "\"delivery\":{\"countryCode\":\"GB\",\"zoneCode\":\"LDN\"}}";

		CustomerSnapshot snapshot = mapper.readValue(json, CustomerSnapshot.class);

		assertEquals(2, snapshot.getSchemaVersion());
		assertEquals("ada@example.com", snapshot.getEmailAddress());
		assertEquals("GB", snapshot.getBilling().getCountryCode());
		assertEquals("LDN", snapshot.getDelivery().getZoneCode());
	}

}
