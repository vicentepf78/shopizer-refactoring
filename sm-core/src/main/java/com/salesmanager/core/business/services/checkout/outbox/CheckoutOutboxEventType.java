package com.salesmanager.core.business.services.checkout.outbox;

public enum CheckoutOutboxEventType {
	PAYMENT_REQUESTED,
	PAYMENT_CONFIRMED,
	ORDER_PERSISTED,
	INVENTORY_DECREMENTED
}
