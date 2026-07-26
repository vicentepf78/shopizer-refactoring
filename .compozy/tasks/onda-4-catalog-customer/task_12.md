---
status: pending
title: JaCoCo verify gates Wave4 modules
type: test
complexity: low
---

# JaCoCo verify gates Wave4 modules

## Overview
Consolidates TLC T32. Adds JaCoCo verify thresholds to catalog-service, customer-service, sm-catalog-core, sm-customer-core matching Waves 1–2 pattern.

<requirements>
1. MUST configure JaCoCo in pom.xml for all 4 Wave 4 modules — T32.
2. MUST pass `./mvnw verify` on Wave 4 modules.
3. SHOULD align threshold with existing reference-service / merchant-service gates.
</requirements>

## Deliverables
- JaCoCo config in 4 poms
- verify gate green **(REQUIRED)**

## Tests
- `./mvnw verify -pl catalog-service,customer-service,sm-catalog-core,sm-customer-core`

## Success Criteria
- verify phase passes on Wave 4 modules
