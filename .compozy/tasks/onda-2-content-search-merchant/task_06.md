---
status: pending
title: ProductIndexPayload, producers e listener de indexação
type: backend
complexity: high
---

# ProductIndexPayload, producers e listener de indexação

## Overview
Consolida TLC T23–T26. Extrai `ProductIndexPayloadBuilder` no sm-core, introduz `SearchIndexProducer` in-process e HTTP, refatora `IndexProductEventListener` e adiciona `SearchBulkIndexOrchestrator` no BFF. Depende de S-ready (`task_05`) e dos contracts/config Wave2 (`task_01`).

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST extrair build logic de `SearchServiceImpl` para `ProductIndexPayloadBuilder` produzindo `List<ProductIndexPayload>` por idioma — TLC T23.
2. MUST criar interface `SearchIndexProducer` + impls InProcess e Http (DELETE + bulk POST com token) — TLC T24.
3. MUST refatorar `IndexProductEventListener` para injetar `SearchIndexProducer` em vez de `SearchService` — TLC T25.
4. MUST implementar `SearchBulkIndexOrchestrator` (`indexAllData` via `ProductService.listByStore` + producer; delay configurável) — TLC T26.
5. MUST ativar apenas um producer por profile (`wave2.strangler.enabled`).
6. MUST NOT exigir ProductSnapshot da Onda 3; builder permanece no monólito (ADR-002).
7. MUST logar falhas HTTP de índice sem outbox (GAP documentado em task_07).
</requirements>

## Subtasks
- [ ] 6.1 ProductIndexPayloadBuilder + testes com fixture Product (T23)
- [ ] 6.2 SearchIndexProducer InProcess + Http (T24)
- [ ] 6.3 Refatorar IndexProductEventListener (T25)
- [ ] 6.4 SearchBulkIndexOrchestrator + delay config (T26)
- [ ] 6.5 Wiring condicional por profile

## Implementation Details
Ver TechSpec: **Pipeline de índice**, **Interfaces** SearchIndexProducer/SearchIndexClient, **Ordem de construção** passos 19–20. Sequência: Catalog → PublishProductAspect → listener → builder → producer → search-service.

### Relevant Files
- `sm-core/src/main/java/com/salesmanager/core/business/services/search/SearchServiceImpl.java` — fonte do builder
- `sm-core/src/main/java/com/salesmanager/core/business/configuration/events/products/listeners/IndexProductEventListener.java` — listener
- `shopizer-api-contracts/.../client/SearchIndexClient.java` — client HTTP
- `sm-shop/.../strangler/config/Wave2ClientConfig.java` — RestTemplate/token (task_01)

### Dependent Files
- `sm-core/.../search/index/ProductIndexPayloadBuilder.java` — a criar
- `sm-core/.../search/index/SearchIndexProducer.java` — interface
- `sm-core/.../search/index/SearchIndexProducerInProcess.java` — impl
- `sm-shop/.../strangler/search/SearchIndexProducerHttp.java` — impl HTTP
- `sm-shop/.../strangler/search/SearchBulkIndexOrchestrator.java` — orchestrator
- `IndexProductEventListener.java` — refatorado

### Related ADRs
- [ADR-002: ProductIndexPayload](adrs/adr-002.md) — builder no monólito
- [ADR-005: token interno](adrs/adr-005.md) — producer HTTP

## Deliverables
- Builder + producers + listener + orchestrator
- Unit tests builder 80%+ **(REQUIRED)**
- Integration tests producer HTTP e listener **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] Builder com fixture Product gera payloads por idioma com schemaVersion=1
  - [ ] Producer InProcess chama SearchService legado
  - [ ] Producer Http emite DELETE + POST bulk com header token
- Integration tests:
  - [ ] Listener usa SearchIndexProducer (mock) em SaveProductEvent
  - [ ] Orchestrator indexAllData lista por store e aplica delay
  - [ ] `./mvnw test -pl sm-core -Dtest=ProductIndexPayloadBuilderTest,IndexProductEventListenerTest`
  - [ ] `./mvnw test -pl sm-shop -Dtest=SearchIndexProducerHttpTest,SearchBulkIndexOrchestratorTest`
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- Test coverage >=80%
- Um producer ativo por profile
- Eventos de catálogo não dependem mais de SearchService in-process no path strangler
