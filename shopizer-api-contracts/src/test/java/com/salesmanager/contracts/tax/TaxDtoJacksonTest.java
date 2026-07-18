package com.salesmanager.contracts.tax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class TaxDtoJacksonTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	void persistableAndReadableTaxRateRoundTripWithI18nDescriptions() throws Exception {
		TaxRateDescription description = new TaxRateDescription();
		description.setId(3L);
		description.setLanguage("en");
		description.setName("GST");
		description.setDescription("Goods and Services Tax");
		description.setTitle("GST Title");
		description.setFriendlyUrl("gst");
		description.setKeyWords("gst,tax");
		description.setHighlights("federal");
		description.setMetaDescription("gst meta");

		PersistableTaxRate persistable = new PersistableTaxRate();
		persistable.setId(100L);
		persistable.setCode("GST");
		persistable.setPriority(1);
		persistable.setRate(new BigDecimal("5.00"));
		persistable.setStore("DEFAULT");
		persistable.setCountry("CA");
		persistable.setZone("QC");
		persistable.setTaxClass("DEFAULT");
		persistable.setDescriptions(Arrays.asList(description));

		String persistableJson = mapper.writeValueAsString(persistable);
		JsonNode persistableTree = mapper.readTree(persistableJson);

		assertEquals("GST", persistableTree.get("code").asText());
		assertEquals("CA", persistableTree.get("country").asText());
		assertEquals("QC", persistableTree.get("zone").asText());
		assertEquals("DEFAULT", persistableTree.get("taxClass").asText());
		assertEquals(1, persistableTree.get("descriptions").size());
		assertEquals("GST", persistableTree.get("descriptions").get(0).get("name").asText());
		assertEquals("en", persistableTree.get("descriptions").get(0).get("language").asText());
		assertFalse(persistableTree.has("auditSection"));

		PersistableTaxRate persistableRoundTrip = mapper.readValue(persistableJson, PersistableTaxRate.class);
		assertEquals(new BigDecimal("5.00"), persistableRoundTrip.getRate());
		assertEquals("GST", persistableRoundTrip.getDescriptions().get(0).getName());
		assertEquals("Goods and Services Tax", persistableRoundTrip.getDescriptions().get(0).getDescription());
		assertEquals("gst", persistableRoundTrip.getDescriptions().get(0).getFriendlyUrl());
		assertEquals("gst,tax", persistableRoundTrip.getDescriptions().get(0).getKeyWords());
		assertEquals("federal", persistableRoundTrip.getDescriptions().get(0).getHighlights());
		assertEquals("gst meta", persistableRoundTrip.getDescriptions().get(0).getMetaDescription());
		assertEquals("GST Title", persistableRoundTrip.getDescriptions().get(0).getTitle());

		ReadableTaxRateDescription readableDescription = new ReadableTaxRateDescription();
		readableDescription.setId(3L);
		readableDescription.setLanguage("fr");
		readableDescription.setName("TPS");
		readableDescription.setDescription("Taxe sur les produits et services");

		ReadableTaxClass taxClass = new ReadableTaxClass();
		taxClass.setId(7L);
		taxClass.setCode("DEFAULT");
		taxClass.setName("Default");
		taxClass.setStore("DEFAULT");

		ReadableTaxRate readable = new ReadableTaxRate();
		readable.setId(100L);
		readable.setCode("GST");
		readable.setPriority(1);
		readable.setRate("5.00");
		readable.setStore("DEFAULT");
		readable.setCountry("CA");
		readable.setZone("QC");
		readable.setDescription(readableDescription);
		readable.setTaxClass(taxClass);

		String readableJson = mapper.writeValueAsString(readable);
		JsonNode readableTree = mapper.readTree(readableJson);

		assertEquals("5.00", readableTree.get("rate").asText());
		assertEquals("TPS", readableTree.get("description").get("name").asText());
		assertEquals("DEFAULT", readableTree.get("taxClass").get("code").asText());
		assertFalse(readableTree.has("hibernateLazyInitializer"));

		ReadableTaxRate readableRoundTrip = mapper.readValue(readableJson, ReadableTaxRate.class);
		assertEquals("5.00", readableRoundTrip.getRate());
		assertEquals("TPS", readableRoundTrip.getDescription().getName());
		assertEquals("fr", readableRoundTrip.getDescription().getLanguage());
		assertEquals("DEFAULT", readableRoundTrip.getTaxClass().getCode());
		assertEquals("Default", readableRoundTrip.getTaxClass().getName());
		assertEquals("DEFAULT", readableRoundTrip.getTaxClass().getStore());
	}

	@Test
	void taxClassAndFullRateRoundTrip() throws Exception {
		PersistableTaxClass persistableClass = new PersistableTaxClass();
		persistableClass.setId(1L);
		persistableClass.setCode("TAXABLE");
		persistableClass.setName("Taxable");
		persistableClass.setStore("DEFAULT");

		String classJson = mapper.writeValueAsString(persistableClass);
		PersistableTaxClass classRoundTrip = mapper.readValue(classJson, PersistableTaxClass.class);
		assertEquals("TAXABLE", classRoundTrip.getCode());
		assertEquals("Taxable", classRoundTrip.getName());

		ReadableTaxRateDescription d1 = new ReadableTaxRateDescription();
		d1.setName("EN");
		d1.setLanguage("en");
		ReadableTaxRateDescription d2 = new ReadableTaxRateDescription();
		d2.setName("FR");
		d2.setLanguage("fr");

		ReadableTaxRateFull full = new ReadableTaxRateFull();
		full.setId(9L);
		full.setCode("FULL");
		full.setPriority(2);
		full.setDescriptions(Arrays.asList(d1, d2));

		String fullJson = mapper.writeValueAsString(full);
		JsonNode fullTree = mapper.readTree(fullJson);
		assertEquals(2, fullTree.get("descriptions").size());

		ReadableTaxRateFull fullRoundTrip = mapper.readValue(fullJson, ReadableTaxRateFull.class);
		assertEquals("FULL", fullRoundTrip.getCode());
		assertEquals(2, fullRoundTrip.getPriority());
		assertEquals(2, fullRoundTrip.getDescriptions().size());
		assertEquals("EN", fullRoundTrip.getDescriptions().get(0).getName());
		assertEquals("FR", fullRoundTrip.getDescriptions().get(1).getName());
	}

}
