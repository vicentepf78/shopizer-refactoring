---
provider: manual
pr:
round: 2
round_created_at: 2026-07-26T22:52:00Z
status: resolved
file: sm-core/src/main/java/com/salesmanager/core/business/services/checkout/outbox/CheckoutOutboxDispatcher.java
line: 32
severity: medium
author: claude-code
provider_ref:
---

# Issue 009: CheckoutOutboxDispatcher has zero test coverage

## Review Comment

SAG-05 requires an in-process dispatcher that marks pending events processed. `CheckoutOutboxDispatcher` implements batch fetch (`BATCH_SIZE=100`), per-event error isolation (warn-and-continue), and the `DISPATCH_LOCK` guard, but has no unit or integration tests. reviews-001 issue_004 explicitly deferred dispatcher coverage when replacing the misnamed Mockito integration test with `@DataJpaTest` repository tests.

**Suggested fix:** add a focused unit test with a mocked `CheckoutOutboxRepository` verifying (1) `findPending(100)` is called, (2) each pending event is passed to `markProcessed`, (3) a failure on one event does not prevent processing of subsequent events.

## Triage

- Decision: `valid`
- Notes: `CheckoutOutboxDispatcher` implements SAG-05 batch dispatch with per-event error isolation but had no direct test coverage after reviews-001 deferred it to a dedicated unit test. Added `CheckoutOutboxDispatcherTest` with a mocked `CheckoutOutboxRepository` asserting `findPending(100)`, per-event `markProcessed`, and warn-and-continue when one event fails.
