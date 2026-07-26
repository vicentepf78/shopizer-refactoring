---
provider: manual
pr:
round: 1
round_created_at: 2026-07-26T22:06:00Z
status: resolved
file: shopizer-api-contracts/src/main/java/com/salesmanager/contracts/catalog/ProductSnapshot.java
line: 7
severity: medium
author: claude-code
provider_ref:
---

# Issue 008: Snapshot DTOs lack @JsonIgnoreProperties for schema evolution

## Review Comment

`ProductSnapshot`, `OrderSnapshot`, and `CustomerSnapshot` are versioned cross-boundary payloads (`schemaVersion`) intended to evolve over time (CTR-02, SNP-02, SAG-02). None carry `@JsonIgnoreProperties(ignoreUnknown = true)`, and no shared `ObjectMapper` configuration disables `FAIL_ON_UNKNOWN_PROPERTIES`.

When a producer adds a field for a newer `schemaVersion`, older consumers deserializing with the default Jackson `ObjectMapper` will throw `UnrecognizedPropertyException` instead of gracefully ignoring the new field — undermining the schema-evolution story that `schemaVersion` was introduced to support.

Suggested fix: add `@JsonIgnoreProperties(ignoreUnknown = true)` to all three snapshot DTOs and their nested types. Add a Jackson test that deserializes JSON with an extra unknown field and asserts success.

## Triage

- Decision: `valid`
- Root cause: Snapshot DTOs in `shopizer-api-contracts` had no `@JsonIgnoreProperties(ignoreUnknown = true)` on the root or nested types. Default Jackson `ObjectMapper` fails on unknown properties, breaking forward-compatible deserialization when `schemaVersion` increments.
- Fix: Added `@JsonIgnoreProperties(ignoreUnknown = true)` to `ProductSnapshot`, `OrderSnapshot`, `CustomerSnapshot`, and nested types (`ProductSnapshotAttribute`, `ProductSnapshotVariant`, `ProductSnapshotInventory`, `OrderLineSnapshot`, `OrderTotalSnapshot`, `AddressSnapshot`). Extended existing Jackson tests with `ignoresUnknownFieldsDuringDeserialization` cases covering root and nested unknown fields.
- Scope note: Batch metadata listed `CheckoutOutboxRepositoryTest.java`, but the issue targets snapshot contract DTOs; changes were limited to `shopizer-api-contracts` production DTOs and their Jackson tests.
- Verification: `./mvnw -pl shopizer-api-contracts -am verify -DfailIfNoTests=false` — BUILD SUCCESS, 80 tests, 0 failures.
