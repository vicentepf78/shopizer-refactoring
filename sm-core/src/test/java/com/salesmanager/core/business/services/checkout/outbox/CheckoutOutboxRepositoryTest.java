package com.salesmanager.core.business.services.checkout.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;

import com.salesmanager.core.business.repositories.checkout.CheckoutOutboxJpaRepository;
import com.salesmanager.core.model.checkout.outbox.CheckoutOutboxEvent;

@ExtendWith(MockitoExtension.class)
class CheckoutOutboxRepositoryTest {

	@Mock
	private CheckoutOutboxJpaRepository jpaRepository;

	private CheckoutOutboxRepositoryImpl repository;

	@BeforeEach
	void setUp() {
		repository = new CheckoutOutboxRepositoryImpl(jpaRepository);
	}

	@Test
	void appendIsIdempotentForAggregateAndEventType() {
		when(jpaRepository.existsByAggregateIdAndEventType("cart-123", "PAYMENT_REQUESTED")).thenReturn(true);

		repository.append("cart-123", CheckoutOutboxEventType.PAYMENT_REQUESTED, "{}");

		verify(jpaRepository, never()).saveAndFlush(any());
	}

	@Test
	void appendPersistsNewEventWhenNotExists() {
		when(jpaRepository.existsByAggregateIdAndEventType("cart-123", "PAYMENT_REQUESTED")).thenReturn(false);

		repository.append("cart-123", CheckoutOutboxEventType.PAYMENT_REQUESTED, "{\"storeCode\":\"DEFAULT\"}");

		ArgumentCaptor<CheckoutOutboxEvent> captor = ArgumentCaptor.forClass(CheckoutOutboxEvent.class);
		verify(jpaRepository).saveAndFlush(captor.capture());
		CheckoutOutboxEvent saved = captor.getValue();
		assertThat(saved.getAggregateId()).isEqualTo("cart-123");
		assertThat(saved.getEventType()).isEqualTo("PAYMENT_REQUESTED");
		assertThat(saved.getStatus()).isEqualTo(CheckoutOutboxStatus.PENDING);
		assertThat(saved.getPayload()).contains("DEFAULT");
	}

	@Test
	void appendIgnoresDuplicateKeyRace() {
		when(jpaRepository.existsByAggregateIdAndEventType("cart-123", "PAYMENT_REQUESTED")).thenReturn(false);
		when(jpaRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

		repository.append("cart-123", CheckoutOutboxEventType.PAYMENT_REQUESTED, "{}");
	}

	@Test
	void findPendingDelegatesToJpaRepository() {
		CheckoutOutboxEvent pending = new CheckoutOutboxEvent();
		pending.setAggregateId("agg-1");
		when(jpaRepository.findPendingEvents(any(Pageable.class))).thenReturn(Collections.singletonList(pending));

		assertThat(repository.findPending(10)).hasSize(1);
		verify(jpaRepository).findPendingEvents(any(Pageable.class));
	}

	@Test
	void markProcessedUpdatesStatusAndTimestamp() {
		CheckoutOutboxEvent event = new CheckoutOutboxEvent();
		event.setStatus(CheckoutOutboxStatus.PENDING);

		repository.markProcessed(event);

		assertThat(event.getStatus()).isEqualTo(CheckoutOutboxStatus.PROCESSED);
		assertThat(event.getProcessedAt()).isNotNull();
		verify(jpaRepository).save(event);
	}

}
