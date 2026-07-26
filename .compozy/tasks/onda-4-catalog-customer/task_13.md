---
status: pending
title: Pact providers + Wave4ConsumerPactTest
type: test
complexity: medium
---

# Pact providers + Wave4ConsumerPactTest

## Overview
Consolidates TLC T33–T34. Provider pact tests on catalog-service and customer-service; consumer `Wave4ConsumerPactTest` in sm-shop for P1 endpoints and snapshots.

<requirements>
1. MUST add CatalogProviderPactTest for product/category GET — T33.
2. MUST add CustomerProviderPactTest for profile GET — T33.
3. MUST add Wave4ConsumerPactTest in sm-shop — T34.
4. MUST pin ProductSnapshot schemaVersion 2 and CustomerSnapshot v1 in fixtures.
5. MUST run in `./mvnw clean install` gate.
</requirements>

## Deliverables
- 3 pact test classes
- Pact artifacts generated in CI-compatible paths **(REQUIRED)**

## Tests
- `./mvnw test -pl catalog-service,customer-service -Dtest=*ProviderPact*Test`
- `./mvnw test -pl sm-shop -Dtest=Wave4ConsumerPactTest`

## Success Criteria
- Consumer + provider pacts green
- Breaking DTO change fails CI
