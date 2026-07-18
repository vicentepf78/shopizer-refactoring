package com.salesmanager.contracts.reference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class ReferenceDtoJacksonTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	void readableCurrencySerializesIdCodeNameSymbolSupported() throws Exception {
		ReadableCurrency currency = new ReadableCurrency();
		currency.setId(1L);
		currency.setCode("USD");
		currency.setName("US Dollar");
		currency.setSymbol("$");
		currency.setSupported(true);

		String json = mapper.writeValueAsString(currency);
		JsonNode tree = mapper.readTree(json);

		assertEquals(1L, tree.get("id").asLong());
		assertEquals("USD", tree.get("code").asText());
		assertEquals("US Dollar", tree.get("name").asText());
		assertEquals("$", tree.get("symbol").asText());
		assertTrue(tree.get("supported").asBoolean());
		assertFalse(tree.has("auditSection"));
		assertFalse(tree.has("stores"));

		ReadableCurrency roundTrip = mapper.readValue(json, ReadableCurrency.class);
		assertEquals(Long.valueOf(1L), roundTrip.getId());
		assertEquals("USD", roundTrip.getCode());
		assertEquals("US Dollar", roundTrip.getName());
		assertEquals("$", roundTrip.getSymbol());
		assertTrue(roundTrip.isSupported());
	}

	@Test
	void readableLanguageJsonContainsOnlyIdCodeSortOrder() throws Exception {
		ReadableLanguage language = new ReadableLanguage();
		language.setId(2);
		language.setCode("en");
		language.setSortOrder(10);

		String json = mapper.writeValueAsString(language);
		JsonNode tree = mapper.readTree(json);

		assertEquals(2, tree.get("id").asInt());
		assertEquals("en", tree.get("code").asText());
		assertEquals(10, tree.get("sortOrder").asInt());
		assertEquals(3, tree.size());
		assertFalse(tree.has("auditSection"));
		assertFalse(tree.has("stores"));
		assertFalse(tree.has("hibernateLazyInitializer"));

		ReadableLanguage roundTrip = mapper.readValue(json, ReadableLanguage.class);
		assertEquals(2, roundTrip.getId());
		assertEquals("en", roundTrip.getCode());
		assertEquals(10, roundTrip.getSortOrder());
	}

	@Test
	void readableCountryAndZoneRoundTripWithNestedZones() throws Exception {
		ReadableZone zone = new ReadableZone();
		zone.setId(5L);
		zone.setCode("QC");
		zone.setCountryCode("CA");
		zone.setName("Quebec");

		ReadableCountry country = new ReadableCountry();
		country.setId(10L);
		country.setCode("CA");
		country.setSupported(true);
		country.setName("Canada");
		country.setZones(Arrays.asList(zone));

		String json = mapper.writeValueAsString(country);
		JsonNode tree = mapper.readTree(json);

		assertEquals("CA", tree.get("code").asText());
		assertEquals("Canada", tree.get("name").asText());
		assertTrue(tree.get("supported").asBoolean());
		assertEquals(1, tree.get("zones").size());
		assertEquals("QC", tree.get("zones").get(0).get("code").asText());
		assertEquals("Quebec", tree.get("zones").get(0).get("name").asText());

		ReadableCountry roundTrip = mapper.readValue(json, ReadableCountry.class);
		assertEquals("CA", roundTrip.getCode());
		assertEquals(1, roundTrip.getZones().size());
		assertEquals("QC", roundTrip.getZones().get(0).getCode());
		assertEquals("CA", roundTrip.getZones().get(0).getCountryCode());
		assertEquals("Quebec", roundTrip.getZones().get(0).getName());
	}

	@Test
	void sizeReferencesSerializesMeasureEnums() throws Exception {
		SizeReferences refs = new SizeReferences();
		refs.setWeights(Arrays.asList(WeightUnit.LB, WeightUnit.KG));
		refs.setMeasures(Arrays.asList(MeasureUnit.CM, MeasureUnit.IN));

		String json = mapper.writeValueAsString(refs);
		JsonNode tree = mapper.readTree(json);

		assertEquals("LB", tree.get("weights").get(0).asText());
		assertEquals("KG", tree.get("weights").get(1).asText());
		assertEquals("CM", tree.get("measures").get(0).asText());
		assertEquals("IN", tree.get("measures").get(1).asText());

		SizeReferences roundTrip = mapper.readValue(json, SizeReferences.class);
		assertEquals(WeightUnit.LB, roundTrip.getWeights().get(0));
		assertEquals(MeasureUnit.IN, roundTrip.getMeasures().get(1));

		assertEquals(DimensionUnitOfMeasure.cm, DimensionUnitOfMeasure.valueOf("cm"));
		assertEquals(WeightUnitOfMeasure.kg, WeightUnitOfMeasure.valueOf("kg"));
		assertEquals(5, DimensionUnitOfMeasure.values().length);
		assertEquals(5, WeightUnitOfMeasure.values().length);
	}

	@Test
	void measureAndWeightEnumValuesAreStable() {
		Iterator<MeasureUnit> measures = Arrays.asList(MeasureUnit.values()).iterator();
		assertEquals(MeasureUnit.CM, measures.next());
		assertEquals(MeasureUnit.IN, measures.next());
		assertFalse(measures.hasNext());

		Iterator<WeightUnit> weights = Arrays.asList(WeightUnit.values()).iterator();
		assertEquals(WeightUnit.LB, weights.next());
		assertEquals(WeightUnit.KG, weights.next());
		assertFalse(weights.hasNext());
	}

}
