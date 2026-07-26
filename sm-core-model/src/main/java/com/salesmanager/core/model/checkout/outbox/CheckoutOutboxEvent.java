package com.salesmanager.core.model.checkout.outbox;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

/**
 * Transactional outbox for staged checkout (SAG-01 / ADR-005).
 * <p>
 * Schema source of truth: this entity's JPA annotations. Shopizer has no Flyway/Liquibase
 * runner; {@code hibernate.hbm2ddl.auto} creates/updates {@code CHECKOUT_OUTBOX} from here.
 */
@Entity
@Table(name = "CHECKOUT_OUTBOX", uniqueConstraints = {
		@UniqueConstraint(name = "UK_OUTBOX_AGG_TYPE", columnNames = { "AGGREGATE_ID", "EVENT_TYPE" }) })
public class CheckoutOutboxEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "AGGREGATE_ID", nullable = false, length = 64)
	private String aggregateId;

	@Column(name = "EVENT_TYPE", nullable = false, length = 64)
	private String eventType;

	@Column(name = "PAYLOAD", nullable = false, columnDefinition = "TEXT")
	private String payload;

	@Column(name = "STATUS", nullable = false, length = 16)
	private String status;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_AT", nullable = false)
	private Date createdAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "PROCESSED_AT")
	private Date processedAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAggregateId() {
		return aggregateId;
	}

	public void setAggregateId(String aggregateId) {
		this.aggregateId = aggregateId;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getProcessedAt() {
		return processedAt;
	}

	public void setProcessedAt(Date processedAt) {
		this.processedAt = processedAt;
	}

}
