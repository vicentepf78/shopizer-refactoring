---
status: pending
title: Providers Pact + Wave4ConsumerPactTest
type: test
complexity: medium
---

# Providers Pact + Wave4ConsumerPactTest

## Visão geral
Consolida TLC T33–T34. Testes provider pact em catalog-service e customer-service; consumer `Wave4ConsumerPactTest` no sm-shop para endpoints P1 e snapshots.

<requirements>
1. MUST adicionar CatalogProviderPactTest para product/category GET — T33.
2. MUST adicionar CustomerProviderPactTest para profile GET — T33.
3. MUST adicionar Wave4ConsumerPactTest no sm-shop — T34.
4. MUST fixar ProductSnapshot schemaVersion 2 e CustomerSnapshot v1 em fixtures.
5. MUST executar no gate `./mvnw clean install`.
</requirements>

## Entregáveis
- 3 classes de teste pact
- Artefatos pact gerados em paths compatíveis com CI **(OBRIGATÓRIO)**

## Testes
- `./mvnw test -pl catalog-service,customer-service -Dtest=*ProviderPact*Test`
- `./mvnw test -pl sm-shop -Dtest=Wave4ConsumerPactTest`

## Critérios de sucesso
- Pacts consumer + provider verdes
- Mudança breaking em DTO falha CI
