---
status: pending
title: Pact consumer/provider + JaCoCo Wave6 gates
type: test
complexity: medium
---

# Pact consumer/provider + JaCoCo Wave6 gates

## Overview
TLC T39–T42. Wave6ConsumerPactTest; ShoppingCartProviderPactTest; OrderProviderPactTest; JaCoCo verify thresholds for wave6 modules.

<requirements>
1. MUST add Wave6ConsumerPactTest in sm-shop — T39, STR-01.
2. MUST add ShoppingCartProviderPactTest — T40, STR-02.
3. MUST add OrderProviderPactTest (read + totals + commit) — T41, STR-03, STR-04.
4. MUST configure JaCoCo gates on shoppingcart-service, order-service — T42.
</requirements>

## Deliverables
- Pact consumer + 2 providers
- JaCoCo verify config

## Tests
```bash
./mvnw -pl sm-shop,shoppingcart-service,order-service -am test \
  -Dtest=Wave6ConsumerPactTest,ShoppingCartProviderPactTest,OrderProviderPactTest \
  -DfailIfNoTests=false
```

## Success Criteria
- All pact tests green
- verify passes coverage gates
