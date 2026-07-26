package com.salesmanager.contracts.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class LanguageCodeTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	void rejectsBlankCode() {
		assertThrows(IllegalArgumentException.class, () -> new LanguageCode(""));
		assertThrows(IllegalArgumentException.class, () -> new LanguageCode(null));
	}

	@Test
	void serializesAndDeserializesJson() throws Exception {
		LanguageCode languageCode = LanguageCode.of("en");

		String json = mapper.writeValueAsString(languageCode);
		JsonNode tree = mapper.readTree(json);

		assertEquals("en", tree.asText());

		LanguageCode roundTrip = mapper.readValue(json, LanguageCode.class);
		assertEquals("en", roundTrip.getCode());
	}

}
