---
provider: manual
pr:
round: 1
round_created_at: 2026-07-26T22:06:00Z
status: resolved
file: shopizer-api-contracts/src/main/java/com/salesmanager/contracts/customer/CustomerSnapshot.java
line: 20
severity: medium
author: claude-code
provider_ref:
---

# Issue 009: Inconsistent language-code field naming across snapshots

## Review Comment

`ProductSnapshot` uses field name `language` (line 15) while `CustomerSnapshot` uses `languageCode` (line 20) for the same semantic concept — an ISO language code string. These three snapshot DTOs were introduced together as a coherent contract family (CTR-02/CTR-04).

Inconsistent naming increases consumer bugs (generic snapshot tooling, Pact generators, outbox payload consumers) and makes the JSON surface harder to reason about as a family. `OrderSnapshot` should be checked for the same convention when extended.

Suggested fix: standardize on one name (`language` or `languageCode`) across `ProductSnapshot`, `OrderSnapshot`, and `CustomerSnapshot`, updating builders and Jackson tests accordingly. If renaming is too disruptive for this wave, document the convention in ADR-002 and add a follow-up task for Onda 4.

## Triage

- Decision: `valid`
- Notes: Confirmed inconsistency — `ProductSnapshot` serializes ISO code as `language` while `CustomerSnapshot` used `languageCode`. `OrderSnapshot` has no language field yet (checkout order projection omits locale). Standardized on `language` to match `ProductSnapshot`, `ProductIndexPayload`, and existing tenant entity JSON shape. Renamed field and accessors in `CustomerSnapshot`, updated `CustomerSnapshotBuilder`, Jackson/builder tests, and added `CheckoutStagedOrderProcessorTest.outboxCustomerSnapshotUsesLanguageFieldName` to lock the outbox JSON contract. Files outside batch scope (`CustomerSnapshot.java`, builder, contract tests) were required for the production fix; batch-scoped test validates end-to-end outbox serialization.

## Resolution

- Renamed `CustomerSnapshot.languageCode` → `language` with matching getters/setters.
- Updated `CustomerSnapshotBuilder` and all snapshot Jackson/builder tests.
- Added outbox regression test asserting `customer.language` (not `languageCode`) in `PAYMENT_REQUESTED` payload.
