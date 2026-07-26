---
provider: manual
pr:
round: 1
round_created_at: 2026-07-26T22:06:00Z
status: resolved
file: sm-core/src/main/java/com/salesmanager/core/business/services/checkout/CheckoutApplicationServiceImpl.java
line: 206
severity: high
author: claude-code
provider_ref:
---

# Issue 002: PayPal transaction persistence diverges when outbox flag is on

## Review Comment

`processCheckoutOrder` routes differently depending on `checkout.outbox.enabled`:

- **Flag off** (lines 210–211): when `transaction != null` (PayPal express-checkout), calls the 6-arg `orderService.processOrder(...)` overload — the pre-existing transaction is **not** passed through.
- **Flag on** (lines 206–208): always routes to `CheckoutStagedOrderProcessor`, which unconditionally persists a non-null `transaction` via `saveOrUpdateTransaction` (lines 96–99 of `CheckoutStagedOrderProcessor.java`).

Enabling the outbox flag therefore changes real business behavior (extra `TRANSACTION` row for PayPal orders), not just outbox bookkeeping. This violates CHK-04/CHK-05 (happy-path parity) and SAG-03 (flag should only affect outbox).

Suggested fix: make `CheckoutStagedOrderProcessor` mirror the legacy branch exactly — skip `saveOrUpdateTransaction(transaction)` when the legacy path would have dropped it, or unify both paths on one correct behavior. Add a regression test for "outbox enabled + non-null PayPal transaction" in both flag states.

## Triage

- Decision: `valid`
- Root cause: With outbox enabled, `processCheckoutOrder` forwarded `command.getTransaction()` to `CheckoutStagedOrderProcessor`, which persists it via `saveOrUpdateTransaction`. With outbox disabled, PayPal storefront calls the 6-arg `orderService.processOrder(..., payment, store)` when `transaction != null`, so the command `Transaction` is never passed through — legacy behavior drops it.
- Fix: Pass `null` instead of `transaction` to `stagedOrderProcessor.processOrder` in `CheckoutApplicationServiceImpl`, matching legacy routing. Outbox flag now differs only by outbox writes (ADR-005), not PayPal linkage.
- Verification: `./mvnw -pl sm-core -am test -Dtest=CheckoutApplicationServicePlaceOrderTest,CheckoutOutboxIntegrationTest` and `./mvnw -pl sm-core -am verify` — BUILD SUCCESS.
