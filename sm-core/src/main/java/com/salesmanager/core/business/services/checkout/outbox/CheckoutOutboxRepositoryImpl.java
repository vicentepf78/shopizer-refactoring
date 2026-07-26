package com.salesmanager.core.business.services.checkout.outbox;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.salesmanager.core.business.repositories.checkout.CheckoutOutboxJpaRepository;
import com.salesmanager.core.model.checkout.outbox.CheckoutOutboxEvent;

@Repository
public class CheckoutOutboxRepositoryImpl implements CheckoutOutboxRepository {

	private final CheckoutOutboxJpaRepository jpaRepository;

	@Inject
	public CheckoutOutboxRepositoryImpl(CheckoutOutboxJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public void append(String aggregateId, CheckoutOutboxEventType eventType, String payloadJson) {
		if (jpaRepository.existsByAggregateIdAndEventType(aggregateId, eventType.name())) {
			return;
		}

		CheckoutOutboxEvent row = new CheckoutOutboxEvent();
		row.setAggregateId(aggregateId);
		row.setEventType(eventType.name());
		row.setPayload(payloadJson);
		row.setStatus(CheckoutOutboxStatus.PENDING);
		row.setCreatedAt(new Date());

		try {
			jpaRepository.saveAndFlush(row);
		} catch (DataIntegrityViolationException ex) {
			if (!isDuplicateKeyViolation(ex)) {
				throw ex;
			}
			// ponytail: concurrent append on same aggregate+type — idempotent no-op
		}
	}

	private static boolean isDuplicateKeyViolation(DataIntegrityViolationException ex) {
		if (ex instanceof DuplicateKeyException) {
			return true;
		}
		Throwable cause = ex.getMostSpecificCause();
		if (!(cause instanceof SQLException)) {
			return false;
		}
		SQLException sql = (SQLException) cause;
		if ("23505".equals(sql.getSQLState())) {
			return true;
		}
		return "23000".equals(sql.getSQLState()) && sql.getErrorCode() == 1062;
	}

	@Override
	public List<CheckoutOutboxEvent> findPending(int limit) {
		return jpaRepository.findPendingEvents(PageRequest.of(0, limit));
	}

	@Override
	public void markProcessed(CheckoutOutboxEvent event) {
		event.setStatus(CheckoutOutboxStatus.PROCESSED);
		event.setProcessedAt(new Date());
		jpaRepository.save(event);
	}

}
