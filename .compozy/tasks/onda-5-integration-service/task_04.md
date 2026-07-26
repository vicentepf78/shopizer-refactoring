---
status: pending
title: Plugins frete + orquestrador + client catálogo
type: backend
complexity: high
---

# Plugins frete + orquestrador + client catálogo

## Visão geral
Consolida TLC T12–T16. Move plugins de frete e empacotamento para `sm-integration-core`, adiciona adaptador V2, implementa `CatalogServiceClient` para `ShippingProductSnapshot` e extrai `ShippingOrchestratorImpl` de `ShippingServiceImpl`.

<critical>
- SEMPRE LER o PRD e a TechSpec antes de iniciar
- API de leitura parcial de catálogo da Onda 4 MUST existir antes da implementação do client catálogo
- TESTES OBRIGATÓRIOS — toda task DEVE incluir testes nos entregáveis
</critical>

<requirements>
1. MUST relocar shipping `impl/*`, `DefaultPackagingImpl`, preprocessors — T12.
2. MUST implementar `ShippingQuoteModuleV2Adapter` — T13.
3. MUST implementar client HTTP para snapshots de frete do catálogo — T14.
4. MUST extrair montagem de cotação, `requiresShipping`, metadata para orquestrador — T15.
5. MUST usar `ReferenceServiceClient` para lista de países — SHP-03.
6. MUST implementar fallback GAP-INT-01 com WARN quando catálogo indisponível.
7. MUST atingir JaCoCo S-ready ≥70% no orquestrador de frete — T16.
</requirements>

## Subtarefas
- [ ] 4.1 Mover plugins de frete + empacotamento (T12)
- [ ] 4.2 Adaptador V2 (T13)
- [ ] 4.3 `CatalogServiceClient` + teste WireMock (T14)
- [ ] 4.4 `ShippingOrchestratorImpl` (T15)
- [ ] 4.5 Cobertura S-ready (T16)

## ADRs relacionados
- [ADR-007](adrs/adr-007.md) — HTTP catálogo

## Entregáveis
- Orquestrador de frete + plugins em sm-integration-core
- Client catálogo com fallback
- Testes: cotação com 2 módulos, carrinho digital vazio **(REQUIRED)**

## Critérios de sucesso
- Marco S-ready atingido
- Teste de cotação usa fixture HTTP de catálogo, não PricingService in-process
