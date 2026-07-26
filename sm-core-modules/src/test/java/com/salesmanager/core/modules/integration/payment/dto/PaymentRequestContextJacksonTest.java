package com.salesmanager.core.modules.integration.payment.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.contracts.tenant.LanguageCode;
import com.salesmanager.contracts.tenant.MerchantStoreId;
import com.salesmanager.core.modules.integration.common.dto.IntegrationModuleDto;
import com.salesmanager.core.modules.integration.common.dto.IntegrationStoreContext;

class PaymentRequestContextJacksonTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	void roundTripJsonPreservesPaymentRequestFields() throws Exception {
		IntegrationStoreContext store = new IntegrationStoreContext();
		store.setStoreId(MerchantStoreId.of("DEFAULT"));
		store.setCurrencyCode("USD");
		store.setDefaultLanguage(LanguageCode.of("en"));

		PaymentLineItemDto lineItem = new PaymentLineItemDto();
		lineItem.setCartItemId(10L);
		lineItem.setSku("SKU-1");
		lineItem.setQuantity(2);
		lineItem.setItemPrice(new BigDecimal("19.99"));
		lineItem.setProductId(100L);
		lineItem.setVariant(1L);

		PaymentRequestContext context = new PaymentRequestContext();
		context.setStore(store);
		context.setCustomerId(42L);
		context.setCustomerEmail("buyer@example.com");
		context.setLineItems(Collections.singletonList(lineItem));
		context.setAmount(new BigDecimal("39.98"));
		context.setPaymentModuleCode("moneyorder");
		context.setPaymentType("MONEYORDER");
		context.setTransactionType("AUTHORIZECAPTURE");
		context.setCurrencyCode("USD");
		context.getPaymentMetaData().put("token", "abc123");

		IntegrationModuleDto module = new IntegrationModuleDto();
		module.setCode("moneyorder");
		module.setModule("moneyorder");
		module.setType("payment");
		module.setRegions("*");
		context.setModule(module);

		String json = mapper.writeValueAsString(context);
		JsonNode tree = mapper.readTree(json);

		assertEquals("DEFAULT", tree.get("store").get("storeId").asText());
		assertEquals("USD", tree.get("store").get("currencyCode").asText());
		assertEquals("en", tree.get("store").get("defaultLanguage").asText());
		assertEquals(42L, tree.get("customerId").asLong());
		assertEquals("buyer@example.com", tree.get("customerEmail").asText());
		assertEquals("39.98", tree.get("amount").asText());
		assertEquals("SKU-1", tree.get("lineItems").get(0).get("sku").asText());
		assertEquals("abc123", tree.get("paymentMetaData").get("token").asText());
		assertEquals("moneyorder", tree.get("module").get("code").asText());

		PaymentRequestContext roundTrip = mapper.readValue(json, PaymentRequestContext.class);
		assertNotNull(roundTrip.getStore());
		assertEquals("DEFAULT", roundTrip.getStore().getStoreId().getCode());
		assertEquals("USD", roundTrip.getStore().getCurrencyCode());
		assertEquals("en", roundTrip.getStore().getDefaultLanguage().getCode());
		assertEquals(Long.valueOf(42L), roundTrip.getCustomerId());
		assertEquals("buyer@example.com", roundTrip.getCustomerEmail());
		assertEquals(new BigDecimal("39.98"), roundTrip.getAmount());
		assertEquals(1, roundTrip.getLineItems().size());
		assertEquals("SKU-1", roundTrip.getLineItems().get(0).getSku());
		assertEquals("abc123", roundTrip.getPaymentMetaData().get("token"));
		assertEquals("moneyorder", roundTrip.getModule().getCode());
	}

	@Test
	void ignoresUnknownFieldsDuringDeserialization() throws Exception {
		String json = "{\"customerId\":1,\"customerEmail\":\"buyer@example.com\",\"futureField\":\"ignored\"}";

		PaymentRequestContext context = mapper.readValue(json, PaymentRequestContext.class);

		assertEquals(Long.valueOf(1L), context.getCustomerId());
		assertEquals("buyer@example.com", context.getCustomerEmail());
	}

}
