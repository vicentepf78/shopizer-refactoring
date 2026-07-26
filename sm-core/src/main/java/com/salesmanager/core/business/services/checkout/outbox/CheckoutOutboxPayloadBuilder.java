package com.salesmanager.core.business.services.checkout.outbox;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.contracts.customer.CustomerSnapshot;
import com.salesmanager.contracts.order.OrderSnapshot;
import com.salesmanager.core.business.services.checkout.CustomerSnapshotBuilder;
import com.salesmanager.core.business.services.checkout.OrderSnapshotBuilder;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.payments.Payment;
import com.salesmanager.core.model.payments.PaymentType;
import com.salesmanager.core.model.payments.Transaction;

public final class CheckoutOutboxPayloadBuilder {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private CheckoutOutboxPayloadBuilder() {
	}

	public static String paymentRequested(Customer customer, Payment payment, MerchantStore store, String shoppingCartCode)
			throws JsonProcessingException {
		Map<String, Object> payload = new LinkedHashMap<String, Object>();
		payload.put("customer", customerSnapshot(customer));
		payload.put("paymentModuleCode", payment.getModuleName());
		if (payment.getPaymentType() != null) {
			payload.put("paymentType", payment.getPaymentType().name());
		}
		payload.put("storeCode", store.getCode());
		payload.put("shoppingCartCode", shoppingCartCode);
		return MAPPER.writeValueAsString(payload);
	}

	public static String paymentConfirmed(Customer customer, Payment payment, Transaction processTransaction)
			throws JsonProcessingException {
		Map<String, Object> payload = new LinkedHashMap<String, Object>();
		payload.put("customer", customerSnapshot(customer));
		payload.put("paymentModuleCode", payment.getModuleName());
		if (payment.getPaymentType() != null) {
			payload.put("paymentType", payment.getPaymentType().name());
		}
		if (processTransaction != null && processTransaction.getId() != null) {
			payload.put("transactionId", processTransaction.getId());
		}
		return MAPPER.writeValueAsString(payload);
	}

	public static String orderPersisted(Order order) throws JsonProcessingException {
		Map<String, Object> payload = new LinkedHashMap<String, Object>();
		payload.put("order", OrderSnapshotBuilder.from(order));
		return MAPPER.writeValueAsString(payload);
	}

	public static String inventoryDecremented(Order order) throws JsonProcessingException {
		OrderSnapshot snapshot = OrderSnapshotBuilder.from(order);
		Map<String, Object> payload = new LinkedHashMap<String, Object>();
		payload.put("order", snapshot);
		return MAPPER.writeValueAsString(payload);
	}

	private static CustomerSnapshot customerSnapshot(Customer customer) {
		return CustomerSnapshotBuilder.from(customer);
	}

}
