package com.salesmanager.core.business.services.checkout;

/**
 * Documents which snapshot fields appear in checkout outbox JSON per stage (ADR-005).
 * <p>
 * Payloads are flat Jackson-serialized DTOs — never full JPA entity graphs.
 *
 * <pre>
 * PAYMENT_REQUESTED:
 *   customer: CustomerSnapshot (email, billing, delivery, anonymous flag)
 *   paymentModuleCode, paymentType, storeCode, shoppingCartCode
 *
 * PAYMENT_CONFIRMED:
 *   customer: CustomerSnapshot
 *   paymentModuleCode, paymentType, transactionId (added by outbox writer in task_10)
 *
 * ORDER_PERSISTED:
 *   order: OrderSnapshot (id, status, lines, totals, customerId, customerEmailAddress, total)
 *
 * INVENTORY_DECREMENTED:
 *   order: OrderSnapshot (id, status, lines with sku + quantity only required for audit)
 *
 * Outbox aggregate ID (SAG-01 idempotency via UK_OUTBOX_AGG_TYPE):
 *   1. order.id when already assigned
 *   2. order.shoppingCartCode when present (storefront / API with cart)
 *   3. CheckoutCommand.idempotencyKey (typically X-Correlation-Id from HTTP)
 * When outbox is enabled and none of the above is available, checkout fails fast
 * rather than generating a random UUID (retries would duplicate rows otherwise).
 * </pre>
 */
public final class CheckoutOutboxSnapshotDesign {

	private CheckoutOutboxSnapshotDesign() {
	}

}
