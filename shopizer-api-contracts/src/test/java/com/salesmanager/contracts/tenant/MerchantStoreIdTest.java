package com.salesmanager.contracts.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class MerchantStoreIdTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	void rejectsBlankCode() {
		assertThrows(IllegalArgumentException.class, () -> new MerchantStoreId(""));
		assertThrows(IllegalArgumentException.class, () -> new MerchantStoreId("   "));
		assertThrows(IllegalArgumentException.class, () -> new MerchantStoreId(null));
	}

	@Test
	void trimsCodeAndExposesValue() {
		MerchantStoreId storeId = new MerchantStoreId("  DEFAULT  ");
		assertEquals("DEFAULT", storeId.getCode());
		assertEquals("DEFAULT", storeId.toString());
	}

	@Test
	void equalsAndHashCodeUseNormalizedCode() {
		MerchantStoreId left = MerchantStoreId.of("DEFAULT");
		MerchantStoreId right = new MerchantStoreId("DEFAULT");
		MerchantStoreId other = MerchantStoreId.of("OTHER");

		assertEquals(left, right);
		assertEquals(left.hashCode(), right.hashCode());
		assertNotEquals(left, other);
	}

	@Test
	void serializesAsPlainString() throws Exception {
		String json = mapper.writeValueAsString(MerchantStoreId.of("DEFAULT"));
		JsonNode tree = mapper.readTree(json);

		assertEquals("DEFAULT", tree.asText());

		MerchantStoreId roundTrip = mapper.readValue(json, MerchantStoreId.class);
		assertEquals("DEFAULT", roundTrip.getCode());
	}

}
