---
status: pending
title: Pact consumer/provider + gates JaCoCo Wave6
type: test
complexity: medium
---

# Pact consumer/provider + gates JaCoCo Wave6

## Visão geral
TLC T39–T42. Wave6ConsumerPactTest; ShoppingCartProviderPactTest; OrderProviderPactTest; thresholds verify JaCoCo para módulos wave6.

<requirements>
1. MUST adicionar Wave6ConsumerPactTest em sm-shop — T39, STR-01.
2. MUST adicionar ShoppingCartProviderPactTest — T40, STR-02.
3. MUST adicionar OrderProviderPactTest (read + totals + commit) — T41, STR-03, STR-04.
4. MUST configurar gates JaCoCo em shoppingcart-service, order-service — T42.
</requirements>

## Entregáveis
- Pact consumer + 2 providers
- Config verify JaCoCo

## Testes
```bash
./mvnw -pl sm-shop,shoppingcart-service,order-service -am test \
  -Dtest=Wave6ConsumerPactTest,ShoppingCartProviderPactTest,OrderProviderPactTest \
  -DfailIfNoTests=false
```

## Critérios de sucesso
- Todos os testes pact verdes
- verify passa gates de cobertura
