---
provider: manual
pr:
round: 2
round_created_at: 2026-07-26T22:52:00Z
status: resolved
file: sm-core/src/main/java/com/salesmanager/core/business/services/checkout/CheckoutStagedOrderProcessor.java
line: 68
severity: high
author: claude-code
provider_ref:
---

# Issue 005: @Transactional missing rollbackFor ServiceException

## Review Comment

`processOrder` is annotated with plain `@Transactional`, but `ServiceException` extends `java.lang.Exception` (checked). Spring's default rollback policy only rolls back on unchecked exceptions and `Error`, so failures such as payment decline, inventory mismatch, or `checkout.outbox.aggregate-id-required` will commit all work already executed — including outbox rows appended before the failure point and any persisted customer/order/transaction rows.

The legacy XML `tx:advice` in `shopizer-core-config.xml` defines `rollback-for=ServiceException`, but its pointcut only matches `TransactionalAspectAwareService`, which `CheckoutStagedOrderProcessor` does not implement.

**Suggested fix:** add `rollbackFor = ServiceException.class` to the `@Transactional` annotation. Add an integration test that drives `processOrder` through a failing step and asserts zero outbox rows remain after the exception.

## Triage

- Decision: `valid`
- Notes: Confirmed — `ServiceException` extends checked `Exception`, so Spring's default `@Transactional` rollback policy does not apply. Legacy XML `rollback-for=ServiceException` only matches `TransactionalAspectAwareService`, which this processor does not implement. Outbox appends and any persisted rows would commit on payment decline, inventory mismatch, or aggregate-id failure.
- Root cause: Missing `rollbackFor = ServiceException.class` on `processOrder`.
- Fix: Add `rollbackFor = ServiceException.class` to `@Transactional`. Add `CheckoutStagedOrderProcessorRollbackIntegrationTest` with `@Transactional(propagation = NOT_SUPPORTED)` so `processOrder` runs in its own transaction; mock payment failure after first outbox append and assert zero outbox rows remain.
