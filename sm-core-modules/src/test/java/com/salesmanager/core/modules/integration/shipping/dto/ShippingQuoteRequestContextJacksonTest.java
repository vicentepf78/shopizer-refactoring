package com.salesmanager.core.modules.integration.shipping.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.contracts.tenant.LanguageCode;
import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.modules.integration.common.dto.IntegrationStoreContext;

class ShippingQuoteRequestContextJacksonTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	void serializesDeliveryAndOriginAddressDtos() throws Exception {
		ShippingAddressDto delivery = address("Jane", "Doe", "90210", "US");
		ShippingAddressDto origin = address("Store", "Warehouse", "10001", "US");

		PackageDetailsDto pkg = new PackageDetailsDto();
		pkg.setCode("BOX-1");
		pkg.setShippingWeight(2.5);
		pkg.setType("BOX");

		IntegrationStoreContext store = new IntegrationStoreContext();
		store.setStoreId(MerchantStoreId.of("DEFAULT"));
		store.setCurrencyCode("USD");
		store.setDefaultLanguage(LanguageCode.of("en"));

		ShippingQuoteRequestContext context = new ShippingQuoteRequestContext();
		context.setStore(store);
		context.setDelivery(delivery);
		context.setOrigin(origin);
		context.getPackages().add(pkg);
		context.setOrderTotal(new BigDecimal("150.00"));
		context.setLocale("en_US");

		String json = mapper.writeValueAsString(context);
		JsonNode tree = mapper.readTree(json);

		assertNotNull(tree.get("delivery"));
		assertEquals("Jane", tree.get("delivery").get("firstName").asText());
		assertEquals("90210", tree.get("delivery").get("postalCode").asText());
		assertEquals("US", tree.get("delivery").get("countryCode").asText());

		assertNotNull(tree.get("origin"));
		assertEquals("Store", tree.get("origin").get("firstName").asText());
		assertEquals("10001", tree.get("origin").get("postalCode").asText());
		assertEquals("US", tree.get("origin").get("countryCode").asText());

		assertEquals("BOX-1", tree.get("packages").get(0).get("code").asText());
		assertEquals(0, new BigDecimal("150.00").compareTo(tree.get("orderTotal").decimalValue()));

		ShippingQuoteRequestContext roundTrip = mapper.readValue(json, ShippingQuoteRequestContext.class);
		assertEquals("Jane", roundTrip.getDelivery().getFirstName());
		assertEquals("90210", roundTrip.getDelivery().getPostalCode());
		assertEquals("Store", roundTrip.getOrigin().getFirstName());
		assertEquals("10001", roundTrip.getOrigin().getPostalCode());
		assertEquals(new BigDecimal("150.00"), roundTrip.getOrderTotal());
	}

	private ShippingAddressDto address(String firstName, String lastName, String postalCode, String countryCode) {
		ShippingAddressDto dto = new ShippingAddressDto();
		dto.setFirstName(firstName);
		dto.setLastName(lastName);
		dto.setPostalCode(postalCode);
		dto.setCountryCode(countryCode);
		return dto;
	}

}
