package com.salesmanager.core.business.services.checkout.outbox;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.salesmanager.core.model.checkout.outbox.CheckoutOutboxEvent;

@ExtendWith(MockitoExtension.class)
class CheckoutOutboxDispatcherTest {

	@Mock
	private CheckoutOutboxRepository outboxRepository;

	private CheckoutOutboxDispatcher dispatcher;

	@BeforeEach
	void setUp() {
		dispatcher = new CheckoutOutboxDispatcher(outboxRepository);
	}

	@Test
	void dispatchPendingEvents_fetchesBatchAndMarksEachProcessed() {
		CheckoutOutboxEvent first = event(1L);
		CheckoutOutboxEvent second = event(2L);
		when(outboxRepository.findPending(100)).thenReturn(Arrays.asList(first, second));

		dispatcher.dispatchPendingEvents();

		verify(outboxRepository).findPending(100);
		verify(outboxRepository).markProcessed(first);
		verify(outboxRepository).markProcessed(second);
	}

	@Test
	void dispatchPendingEvents_continuesWhenOneEventFails() {
		CheckoutOutboxEvent failing = event(1L);
		CheckoutOutboxEvent succeeding = event(2L);
		when(outboxRepository.findPending(100)).thenReturn(Arrays.asList(failing, succeeding));
		doThrow(new RuntimeException("db error")).when(outboxRepository).markProcessed(failing);

		dispatcher.dispatchPendingEvents();

		verify(outboxRepository).findPending(100);
		InOrder inOrder = inOrder(outboxRepository);
		inOrder.verify(outboxRepository).markProcessed(failing);
		inOrder.verify(outboxRepository).markProcessed(succeeding);
	}

	private static CheckoutOutboxEvent event(Long id) {
		CheckoutOutboxEvent event = new CheckoutOutboxEvent();
		event.setId(id);
		return event;
	}

}
