package com.salesmanager.core.business.services.checkout.outbox;

import java.util.List;

import com.salesmanager.core.model.checkout.outbox.CheckoutOutboxEvent;

public interface CheckoutOutboxRepository {

	void append(String aggregateId, CheckoutOutboxEventType eventType, String payloadJson);

	List<CheckoutOutboxEvent> findPending(int limit);

	void markProcessed(CheckoutOutboxEvent event);

}
