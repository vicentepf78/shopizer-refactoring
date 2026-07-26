package com.salesmanager.core.business.services.checkout.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.salesmanager.core.business.repositories.checkout.CheckoutOutboxJpaRepository;
import com.salesmanager.core.model.checkout.outbox.CheckoutOutboxEvent;

@DataJpaTest
@Import(CheckoutOutboxRepositoryImpl.class)
@ContextConfiguration(classes = CheckoutOutboxIntegrationTest.Config.class)
@TestPropertySource(properties = {
		"spring.jpa.properties.hibernate.default_schema=",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
class CheckoutOutboxIntegrationTest {

	@EnableJpaRepositories(basePackageClasses = CheckoutOutboxJpaRepository.class)
	@EntityScan(basePackageClasses = CheckoutOutboxEvent.class)
	static class Config {
	}

	@Autowired
	private CheckoutOutboxRepository repository;

	@Autowired
	private CheckoutOutboxJpaRepository jpaRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void appendPersistsRowWithExpectedColumns() {
		String payload = "{\"storeCode\":\"DEFAULT\"}";

		repository.append("cart-123", CheckoutOutboxEventType.PAYMENT_REQUESTED, payload);
		entityManager.flush();
		entityManager.clear();

		List<CheckoutOutboxEvent> rows = jpaRepository.findAll();
		assertThat(rows).hasSize(1);
		CheckoutOutboxEvent row = rows.get(0);
		assertThat(row.getId()).isNotNull();
		assertThat(row.getAggregateId()).isEqualTo("cart-123");
		assertThat(row.getEventType()).isEqualTo("PAYMENT_REQUESTED");
		assertThat(row.getPayload()).isEqualTo(payload);
		assertThat(row.getStatus()).isEqualTo(CheckoutOutboxStatus.PENDING);
		assertThat(row.getCreatedAt()).isNotNull();
		assertThat(row.getProcessedAt()).isNull();
	}

	@Test
	void appendIsIdempotentForSameAggregateAndEventType() {
		repository.append("cart-123", CheckoutOutboxEventType.PAYMENT_REQUESTED, "{\"first\":true}");
		repository.append("cart-123", CheckoutOutboxEventType.PAYMENT_REQUESTED, "{\"second\":true}");
		entityManager.flush();
		entityManager.clear();

		List<CheckoutOutboxEvent> rows = jpaRepository.findAll();
		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).getPayload()).contains("first");
	}

	@Test
	void appendEnforcesUniqueConstraintOnAggregateAndEventType() {
		CheckoutOutboxEvent existing = new CheckoutOutboxEvent();
		existing.setAggregateId("cart-456");
		existing.setEventType(CheckoutOutboxEventType.ORDER_PERSISTED.name());
		existing.setPayload("{\"seed\":true}");
		existing.setStatus(CheckoutOutboxStatus.PENDING);
		existing.setCreatedAt(new Date());
		jpaRepository.saveAndFlush(existing);
		entityManager.clear();

		repository.append("cart-456", CheckoutOutboxEventType.ORDER_PERSISTED, "{\"retry\":true}");
		entityManager.flush();
		entityManager.clear();

		assertThat(jpaRepository.findAll()).hasSize(1);
		assertThat(jpaRepository.findAll().get(0).getPayload()).contains("seed");
	}

	@Test
	void appendAllowsSameAggregateWithDifferentEventTypes() {
		repository.append("cart-789", CheckoutOutboxEventType.PAYMENT_REQUESTED, "{}");
		repository.append("cart-789", CheckoutOutboxEventType.PAYMENT_CONFIRMED, "{}");
		entityManager.flush();
		entityManager.clear();

		assertThat(jpaRepository.findAll()).hasSize(2);
	}

	@Test
	void findPendingAndMarkProcessedAgainstDatabase() {
		repository.append("agg-pending", CheckoutOutboxEventType.INVENTORY_DECREMENTED, "{}");
		entityManager.flush();
		entityManager.clear();

		List<CheckoutOutboxEvent> pending = repository.findPending(10);
		assertThat(pending).hasSize(1);
		assertThat(pending.get(0).getStatus()).isEqualTo(CheckoutOutboxStatus.PENDING);

		repository.markProcessed(pending.get(0));
		entityManager.flush();
		entityManager.clear();

		CheckoutOutboxEvent processed = jpaRepository.findAll().get(0);
		assertThat(processed.getStatus()).isEqualTo(CheckoutOutboxStatus.PROCESSED);
		assertThat(processed.getProcessedAt()).isNotNull();
		assertThat(repository.findPending(10)).isEmpty();
	}

}
