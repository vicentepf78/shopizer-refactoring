---
provider: manual
pr:
round: 2
round_created_at: 2026-07-26T22:52:00Z
status: resolved
file: sm-core/src/test/java/com/salesmanager/core/business/services/checkout/CheckoutApplicationServicePlaceOrderTest.java
line: 223
severity: medium
author: claude-code
provider_ref:
---

# Issue 010: API checkout + outbox routing path untested

## Review Comment

`apiFlowDelegatesToOrderServiceWithPreBuiltOrder` exercises the API (pre-built order) path only with default `outboxProperties.isEnabled()=false`. When `checkout.outbox.enabled=true`, `placeApiOrder` routes to `CheckoutStagedOrderProcessor` instead — a distinct code path. There is no regression test asserting that API checkout with outbox enabled calls `stagedOrderProcessor.processOrder` (with `idempotencyKey` forwarded) and never calls legacy `orderService.processOrder` overloads.

This is a CHK-04 gap for the API flow under the outbox flag.

**Suggested fix:** add `whenOutboxEnabledApiFlowUsesStagedProcessor` mirroring the existing storefront outbox test.

## Triage

- Decision: `valid`
- Root cause: `apiFlowDelegatesToOrderServiceWithPreBuiltOrder` only runs with default `outboxProperties.isEnabled()=false` from `@BeforeEach`. When outbox is enabled, `placeApiOrder` routes through `processCheckoutOrder` → `stagedOrderProcessor.processOrder`, but no test asserted that path or idempotency-key forwarding.
- Fix: Added `whenOutboxEnabledApiFlowUsesStagedProcessor` mirroring the storefront outbox test and the existing API legacy test — asserts staged processor is invoked with the pre-built order and idempotency key, and legacy `orderService.processOrder` overloads are never called.
