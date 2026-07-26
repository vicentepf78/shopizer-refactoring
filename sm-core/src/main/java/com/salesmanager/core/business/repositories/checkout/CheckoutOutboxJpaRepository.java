package com.salesmanager.core.business.repositories.checkout;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.salesmanager.core.model.checkout.outbox.CheckoutOutboxEvent;

public interface CheckoutOutboxJpaRepository extends JpaRepository<CheckoutOutboxEvent, Long> {

	boolean existsByAggregateIdAndEventType(String aggregateId, String eventType);

	@Query("select e from CheckoutOutboxEvent e where e.status = 'PENDING' order by e.createdAt asc")
	List<CheckoutOutboxEvent> findPendingEvents(org.springframework.data.domain.Pageable pageable);

}
