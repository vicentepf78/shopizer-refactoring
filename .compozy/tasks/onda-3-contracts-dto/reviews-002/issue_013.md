---
provider: manual
pr:
round: 2
round_created_at: 2026-07-26T22:52:00Z
status: resolved
file: sm-core/src/main/java/com/salesmanager/core/business/services/checkout/outbox/CheckoutOutboxProperties.java
line: 11
severity: low
author: claude-code
provider_ref:
---

# Issue 013: dispatcherIntervalMs property is never consumed

## Review Comment

`CheckoutOutboxProperties` declares `dispatcherIntervalMs` under prefix `checkout.outbox` (relaxed binding: `checkout.outbox.dispatcher-interval-ms`), but nothing reads this field. `CheckoutOutboxDispatcher` hardcodes the schedule via `@Scheduled(fixedDelayString = "${checkout.outbox.dispatcher.interval-ms:5000}")` — a different property key (`dispatcher.interval-ms` vs `dispatcher-interval-ms`). Operators configuring interval through the `@ConfigurationProperties` bean will see no effect.

**Suggested fix:** wire the dispatcher interval from `CheckoutOutboxProperties.getDispatcherIntervalMs()` or remove the unused field.

## Triage

- Decision: `valid`
- Root cause: `dispatcherIntervalMs` was a flat `@ConfigurationProperties` field (`checkout.outbox.dispatcher-interval-ms`) while runtime config and `@Scheduled` used the nested key `checkout.outbox.dispatcher.interval-ms`, so the bean never reflected operator configuration.
- Fix: Nested `Dispatcher.intervalMs` under `checkout.outbox` to bind the documented property key; `CheckoutOutboxDispatcher` now reads `#{@checkoutOutboxProperties.dispatcherIntervalMs}` so scheduling follows the same bean. Also touched `CheckoutOutboxDispatcher.java` (minimum wiring change outside batch file list).
- Verification: `./mvnw -pl sm-core -am test -Dtest=CheckoutOutboxPropertiesTest,CheckoutOutboxDispatcherTest -DfailIfNoTests=false` and `./mvnw -pl sm-core -am verify -DfailIfNoTests=false`.
