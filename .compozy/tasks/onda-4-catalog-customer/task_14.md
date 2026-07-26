---
status: pending
title: ProductFacadeV2 + guards de wiring (writes admin locais)
type: backend
complexity: medium
---

# ProductFacadeV2 + guards de wiring (writes admin locais)

## Visão geral
Consolida TLC T29–T30. Delegação de leitura ProductFacadeV2 via CatalogServiceClient; testes ArchUnit/wiring provando que writes admin nunca roteiam HTTP.

<requirements>
1. MUST delegar caminhos de leitura ProductFacadeV2Impl para CatalogServiceClient quando strangler on — T29.
2. MUST adicionar Wave4WiringTest ou regra ArchUnit: sem HTTP em mutações privadas de produto — T30.
3. MUST documentar matriz de adaptadores em comentário de código referenciando AD-006.
4. MUST preservar paridade comportamental V1/V2 para GET dentro de GAP-CAT-01 documentado.
</requirements>

## ADRs relacionados
- [ADR-006](adrs/adr-006.md)
- OQ-05

## Entregáveis
- Delegação read V2
- Teste guard de wiring **(OBRIGATÓRIO)**

## Testes
- `./mvnw test -pl sm-shop -Dtest=*ProductFacadeV2*Test,*Wave4Wiring*Test`

## Critérios de sucesso
- V2 GET usa HTTP quando strangler on
- POST privado de produto nunca usa CatalogFacadeHttpAdapter
