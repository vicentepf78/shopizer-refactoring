---
provider: manual
pr:
round: 1
round_created_at: 2026-07-26T22:06:00Z
status: resolved
file: sm-core/src/main/java/com/salesmanager/core/business/services/checkout/CheckoutStagedOrderProcessor.java
line: 146
severity: medium
author: claude-code
provider_ref:
---

# Issue 010: resolveAggregateId random UUID defeats outbox idempotency

## Review Comment

The `UK_OUTBOX_AGG_TYPE(AGGREGATE_ID, EVENT_TYPE)` unique constraint exists to make outbox appends idempotent on retries (SAG-01). `resolveAggregateId` falls back to `UUID.randomUUID().toString()` when the order has no `id` and no `shoppingCartCode` (lines 139–147).

For API checkout flow (`CheckoutApplicationServiceImpl.placeApiOrder`), the pre-built `Order` typically has no `id` yet and no guarantee of `shoppingCartCode` (populated only in storefront path). Every invocation — including a client retry after timeout — generates a fresh UUID, so retried outbox rows duplicate under different aggregate IDs instead of deduplicating.

Suggested fix: require a caller-supplied idempotency key (e.g. request/correlation ID from `CheckoutCommand`) or fail fast when neither `id` nor `shoppingCartCode` is available. Document the aggregate-id contract in `CheckoutOutboxSnapshotDesign`.

## Triage

- Decision: `valid`
- Root cause: `resolveAggregateId` used `UUID.randomUUID()` as a silent fallback, breaking SAG-01 idempotency for API checkout where the order has no persisted id and often no cart code.
- Fix applied:
  1. Added `CheckoutCommand.idempotencyKey` (typically `X-Correlation-Id` from HTTP via MDC).
  2. `CheckoutStagedOrderProcessor.processOrder` accepts the key; `resolveAggregateId` prefers order id → shoppingCartCode → idempotencyKey, then throws `checkout.outbox.aggregate-id-required` instead of generating a random UUID.
  3. `CheckoutApplicationServiceImpl` forwards `command.getIdempotencyKey()` to the staged processor.
  4. `OrderFacadeImpl.checkoutIdempotencyKey()` reads `CorrelationIdFilter.MDC_KEY` for storefront and API paths.
  5. Documented aggregate-id resolution order in `CheckoutOutboxSnapshotDesign`.
  6. Tests: `usesIdempotencyKeyWhenOrderHasNoIdOrCartCode`, `throwsWhenOutboxEnabledAndNoStableAggregateId` in `CheckoutStagedOrderProcessorTest`; idempotency key builder coverage in `CheckoutCommandTest`.
- Files beyond batch scope: `CheckoutCommand`, `CheckoutApplicationServiceImpl`, `OrderFacadeImpl`, and tests were required to wire the caller-supplied key end-to-end.
- Verification: `./mvnw -pl sm-core -am test -Dtest=CheckoutStagedOrderProcessorTest,CheckoutCommandTest,CheckoutApplicationServicePlaceOrderTest -DfailIfNoTests=false` — 18 tests, 0 failures; `./mvnw -pl sm-core -am verify -DfailIfNoTests=false` — BUILD SUCCESS.
