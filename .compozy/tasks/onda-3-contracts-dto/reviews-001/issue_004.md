---
provider: manual
pr:
round: 1
round_created_at: 2026-07-26T22:06:00Z
status: resolved
file: sm-core/src/test/java/com/salesmanager/core/business/services/checkout/outbox/CheckoutOutboxIntegrationTest.java
line: 48
severity: high
author: claude-code
provider_ref:
---

# Issue 004: Outbox feature lacks real integration test coverage

## Review Comment

SAG-01/SAG-03 require verifying outbox rows are written in the same transaction as business mutations. `CheckoutOutboxIntegrationTest` is a Mockito unit test — every collaborator is `@Mock` and tests only verify routing between `CheckoutStagedOrderProcessor` and legacy `OrderService`. No test exercises `CheckoutOutboxRepositoryImpl`, the JPA entity, or the `UK_OUTBOX_AGG_TYPE` constraint against a real database.

Additional gaps in the same area:
- `CheckoutOutboxDispatcher` (SAG-05) has zero test coverage — no verification of pending-event processing, batch limits, or per-event error isolation.
- `CheckoutStagedOrderProcessorTest` uses `any()` for outbox payloads, so `CheckoutOutboxPayloadBuilder` JSON field names are never asserted.

Suggested fix: rename the existing class to reflect its unit scope; add a `@DataJpaTest` or `@SpringBootTest` test that appends through `CheckoutOutboxRepositoryImpl` against H2 and asserts persisted rows/columns. Add focused unit tests for `CheckoutOutboxDispatcher` and capture outbox payloads with `ArgumentCaptor` to verify JSON content.

## Triage

- Decision: `valid`
- Root cause: `CheckoutOutboxIntegrationTest` used `@ExtendWith(MockitoExtension.class)` with all collaborators mocked, so no JPA entity, repository impl, or `UK_OUTBOX_AGG_TYPE` constraint was exercised against H2 despite the class name implying integration coverage.
- Fix: Replaced the Mockito routing tests with a `@DataJpaTest` that wires `CheckoutOutboxRepositoryImpl` + `CheckoutOutboxJpaRepository` against H2, asserting persisted columns, idempotent append, unique-constraint handling, and `findPending`/`markProcessed`. Moved the former routing unit tests to `CheckoutApplicationServicePlaceOrderTest` (minimal out-of-scope change to preserve flag on/off coverage). Dispatcher and ArgumentCaptor payload assertions remain out of scope for this batch file.
- Verification: `./mvnw -pl sm-core -am test -Dtest=CheckoutOutboxIntegrationTest,CheckoutApplicationServicePlaceOrderTest,CheckoutOutboxRepositoryTest -DfailIfNoTests=false` and `./mvnw -pl sm-core -am verify -DfailIfNoTests=false` — BUILD SUCCESS (74 tests, 0 failures).
