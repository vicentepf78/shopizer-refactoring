---
provider: manual
pr:
round: 1
round_created_at: 2026-07-26T22:06:00Z
status: resolved
file: sm-core/src/main/java/com/salesmanager/core/business/services/checkout/CheckoutStagedOrderProcessor.java
line: 69
severity: medium
author: claude-code
provider_ref:
---

# Issue 011: Staged processor wraps payment gateway in single @Transactional

## Review Comment

Pre-refactor `OrderServiceImpl.process(...)` has no `@Transactional` — each downstream service call commits independently. `CheckoutStagedOrderProcessor.processOrder` wraps the entire sequence (outbox insert, `paymentService.processPayment` invoking external payment gateways, customer/order persistence, transaction records, inventory decrement) in a single `@Transactional` method (line 69).

Consequences when `checkout.outbox.enabled=true`:
1. DB connection held open for the duration of external HTTP calls to payment gateways — connection-pool exhaustion risk under load.
2. Rollback granularity changes: if `decrementInventory` throws (`ServiceException.EXCEPTION_INVENTORY_MISMATCH`), the whole transaction rolls back including already-created customer/order records, whereas the legacy path committed those independently. This is a behavioral difference on a documented error path (CHK-05).

Suggested fix: document the intentional change or split transactions so payment gateway calls and per-step commits mirror legacy behavior. Add an explicit test for inventory-mismatch with outbox enabled vs disabled.

## Triage

- Decision: `valid`
- Root cause: `CheckoutStagedOrderProcessor.processOrder` wraps payment gateway, persistence, inventory, and outbox appends in one `@Transactional` boundary. Legacy `OrderServiceImpl.process` has no class-level transaction — each `customerService.create`, `orderService.create`, etc. commits independently. SAG-03 mandates same-transaction outbox writes, which structurally requires the single boundary when the flag is on.
- Fix approach: Document intentional trade-offs in STATE.md (AD-W3-006) rather than split transactions in Onda 3 — splitting would break SAG-03 or require compensating sagas not in scope. CHK-05 parity preserved on the default path (`checkout.outbox.enabled=false` → legacy `OrderServiceImpl`). Inventory-mismatch rollback difference is an accepted deviation when outbox is enabled.
- Verification: `./mvnw -pl sm-core -am test -Dtest=CheckoutStagedOrderProcessorTest,CheckoutApplicationServicePlaceOrderTest -DfailIfNoTests=false` — BUILD SUCCESS.

## Resolution

- Added AD-W3-006 to `.specs/project/STATE.md` documenting single-transaction boundary, connection-pool consideration, and inventory-mismatch rollback deviation vs legacy.
