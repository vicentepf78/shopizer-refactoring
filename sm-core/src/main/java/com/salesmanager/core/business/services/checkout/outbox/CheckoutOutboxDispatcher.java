package com.salesmanager.core.business.services.checkout.outbox;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.salesmanager.core.model.checkout.outbox.CheckoutOutboxEvent;

@Component
@ConditionalOnProperty(name = "checkout.outbox.enabled", havingValue = "true")
public class CheckoutOutboxDispatcher {

	private static final Logger LOGGER = LoggerFactory.getLogger(CheckoutOutboxDispatcher.class);
	private static final int BATCH_SIZE = 100;
	// ponytail: global lock OK for Onda 3 monolith; partition by store if throughput demands
	private static final Object DISPATCH_LOCK = new Object();

	private final CheckoutOutboxRepository outboxRepository;

	@Inject
	public CheckoutOutboxDispatcher(CheckoutOutboxRepository outboxRepository) {
		this.outboxRepository = outboxRepository;
	}

	@Scheduled(fixedDelayString = "#{@checkoutOutboxProperties.dispatcherIntervalMs}")
	public void dispatchPendingEvents() {
		synchronized (DISPATCH_LOCK) {
			List<CheckoutOutboxEvent> pending = outboxRepository.findPending(BATCH_SIZE);
			for (CheckoutOutboxEvent event : pending) {
				try {
					outboxRepository.markProcessed(event);
				} catch (Exception e) {
					LOGGER.warn("Failed to mark outbox event {} as processed", event.getId(), e);
				}
			}
		}
	}

}
