package com.salesmanager.core.business.services.payments;

import java.math.BigDecimal;
import java.util.List;

import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.payments.Payment;
import com.salesmanager.core.model.payments.Transaction;
import com.salesmanager.core.model.shoppingcart.ShoppingCartItem;
import com.salesmanager.core.model.system.IntegrationModule;

final class LegacyPaymentEntityBundle {

	private final MerchantStore store;
	private final Customer customer;
	private final List<ShoppingCartItem> items;
	private final Payment payment;
	private final Order order;
	private final Transaction capturableTransaction;
	private final Transaction refundableTransaction;
	private final BigDecimal refundAmount;
	private final boolean partialRefund;
	private final IntegrationModule integrationModule;

	private LegacyPaymentEntityBundle(MerchantStore store, Customer customer, List<ShoppingCartItem> items,
			Payment payment, Order order, Transaction capturableTransaction, Transaction refundableTransaction,
			BigDecimal refundAmount, boolean partialRefund, IntegrationModule integrationModule) {
		this.store = store;
		this.customer = customer;
		this.items = items;
		this.payment = payment;
		this.order = order;
		this.capturableTransaction = capturableTransaction;
		this.refundableTransaction = refundableTransaction;
		this.refundAmount = refundAmount;
		this.partialRefund = partialRefund;
		this.integrationModule = integrationModule;
	}

	static LegacyPaymentEntityBundle forPayment(MerchantStore store, Customer customer, List<ShoppingCartItem> items,
			Payment payment, IntegrationModule integrationModule) {
		return new LegacyPaymentEntityBundle(store, customer, items, payment, null, null, null, null, false,
				integrationModule);
	}

	static LegacyPaymentEntityBundle forCapture(MerchantStore store, Customer customer, Order order,
			Transaction capturableTransaction, IntegrationModule integrationModule) {
		return new LegacyPaymentEntityBundle(store, customer, null, null, order, capturableTransaction, null, null,
				false, integrationModule);
	}

	static LegacyPaymentEntityBundle forRefund(MerchantStore store, Customer customer, Order order,
			Transaction refundableTransaction, BigDecimal amount, boolean partial,
			IntegrationModule integrationModule) {
		return new LegacyPaymentEntityBundle(store, customer, null, null, order, null, refundableTransaction, amount,
				partial, integrationModule);
	}

	MerchantStore getStore() {
		return store;
	}

	Customer getCustomer() {
		return customer;
	}

	List<ShoppingCartItem> getItems() {
		return items;
	}

	Payment getPayment() {
		return payment;
	}

	Order getOrder() {
		return order;
	}

	Transaction getCapturableTransaction() {
		return capturableTransaction;
	}

	Transaction getRefundableTransaction() {
		return refundableTransaction;
	}

	BigDecimal getRefundAmount() {
		return refundAmount;
	}

	boolean isPartialRefund() {
		return partialRefund;
	}

	IntegrationModule getIntegrationModule() {
		return integrationModule;
	}

}
