---
provider: manual
pr:
round: 2
round_created_at: 2026-07-26T22:52:00Z
status: resolved
file: sm-core/src/main/java/com/salesmanager/core/business/services/shipping/LegacyShippingQuoteModuleBridge.java
line: 31
severity: medium
author: claude-code
provider_ref:
---

# Issue 011: No integration test covers shipping V2 bridge path (INT-06)

## Review Comment

INT-06 requires at least one plugin path tested through the V2 bridge. `MoneyOrderPaymentModuleV2IntegrationTest` covers payment authorize/authorizeAndCapture, but there is zero test for `LegacyShippingQuoteModuleBridge` or `ShippingQuoteModuleV2` routing in `ShippingServiceImpl`. A focused test would have caught the critical module-stripping regression in issue_001.

**Suggested fix:** add a `sm-core` test similar to `MoneyOrderPaymentModuleV2IntegrationTest` that exercises `getShippingQuotes` via the bridge, asserting `moduleConfigs` are passed through.

## Triage

- Decision: `valid`
- Root cause: INT-06 required a real legacy shipping plugin exercised through `LegacyShippingQuoteModuleBridge`, mirroring `MoneyOrderPaymentModuleV2IntegrationTest`. Only payment had that integration coverage; shipping had no equivalent when the review was written.
- Fix: Added `LegacyShippingQuoteModuleBridgeIntegrationTest` using production `PriceByDistanceShippingQuoteRules` wired through the bridge and asserting quote options are returned on the V2 path. Module-config pass-through remains covered by `LegacyShippingQuoteModuleBridgeTest` (added with issue_001).
- Verification: `./mvnw -pl sm-core -am test -Dtest=LegacyShippingQuoteModuleBridgeIntegrationTest,LegacyShippingQuoteModuleBridgeTest -DfailIfNoTests=false` and `./mvnw -pl sm-core -am verify -DfailIfNoTests=false` — BUILD SUCCESS (2 bridge tests, 90 sm-core tests total).
