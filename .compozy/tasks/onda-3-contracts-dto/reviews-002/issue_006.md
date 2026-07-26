---
provider: manual
pr:
round: 2
round_created_at: 2026-07-26T22:52:00Z
status: resolved
file: shopizer-api-contracts/src/main/java/com/salesmanager/contracts/search/ProductSnapshotIndexMapper.java
line: 13
severity: medium
author: claude-code
provider_ref:
---

# Issue 006: ProductSnapshotIndexMapper lives in contracts JAR (CTR-03)

## Review Comment

CTR-03 (P1) and design principle L-002 require the contracts module to publish DTOs only; mappers/builders belong in `sm-core` or `sm-shop`. `ProductSnapshotIndexMapper` contains non-trivial transformation logic (attribute flattening, variant VSKU injection, inventory key mapping) and is called from both `sm-core` (`ProductIndexPayloadBuilder`) and `sm-shop` (`ProductIndexPayloadMapper`).

Keeping it in `shopizer-api-contracts` couples the shared JAR to index-shape knowledge and contradicts ADR-002 implementation notes.

**Suggested fix:** move `ProductSnapshotIndexMapper` to `sm-core` alongside `ProductIndexPayloadBuilder`; keep `sm-shop`'s `ProductIndexPayloadMapper` as a thin delegate. Move the test to `sm-core`.

## Triage

- Decision: `valid`
- Notes: CTR-03 and L-002 require `shopizer-api-contracts` to publish DTOs only. `ProductSnapshotIndexMapper` contains transformation logic (attribute flattening, variant VSKU injection, inventory key mapping) and belongs in `sm-core` alongside `ProductIndexPayloadBuilder`.
- Root cause: mapper was placed in the contracts JAR during initial extraction, coupling shared DTOs to index-shape knowledge.
- Fix: moved `ProductSnapshotIndexMapper` to `com.salesmanager.core.business.services.search.index` in `sm-core`; moved its unit test to `sm-core`; updated `ProductIndexPayloadBuilder` (same package), `ProductIndexPayloadMapper`, and their tests to reference the new location; deleted the contracts copy.
