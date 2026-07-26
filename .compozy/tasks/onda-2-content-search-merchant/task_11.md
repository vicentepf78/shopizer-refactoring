---
status: completed
title: Strangler Content/Merchant, StaticContentProxy e wiring
type: backend
complexity: high
---

# Strangler Content/Merchant, StaticContentProxy e wiring

## Visão geral
Consolida TLC T41–T46. Liga o BFF sm-shop aos serviços content e merchant via adapters HTTP, proxy estático, ContentBlobClient (catálogo P2), hydrator do `MerchantStoreArgumentResolver` e gate de wiring condicional. Junta as três trilhas após task_04, task_07 e task_10.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST implementar `ContentFacadeHttpAdapter` com JWT + correlation forward — TLC T41.
2. MUST implementar `StaticContentProxy` ligando Images/FilesController → API interna static — TLC T42 / ADR-009.
3. MUST implementar `ContentBlobClient` para option/variant facades (PROPERTY/VARIANT) — TLC T43.
4. MUST implementar `StoreFacadeHttpAdapter` e `MerchantConfigurationFacadeHttpAdapter` — TLC T44.
5. MUST wire `MerchantServiceClient` + `MerchantStoreEntityHydrator` no resolver (cache TTL opcional) — TLC T45; **NÃO** reescrever ~450 call sites.
6. MUST garantir um bean por interface (Content/Search/Merchant facades + producers) nos profiles monolith vs strangler-wave2 — TLC T46.
7. MUST retornar 503 remoto sem fallback silencioso quando strangler ligado (STR-01).
</requirements>

## Subtarefas
- [x] 11.1 ContentFacadeHttpAdapter (T41)
- [x] 11.2 StaticContentProxy em Images/FilesController (T42)
- [x] 11.3 ContentBlobClient catálogo P2 (T43)
- [x] 11.4 Merchant facade HTTP adapters (T44)
- [x] 11.5 MerchantServiceClient + hydrator + resolver (T45)
- [x] 11.6 Gate wiring condicional profiles (T46)

## Detalhes de implementação
Ver TechSpec: **Matriz de adapters**, **Análise de impacto** (ImagesController, resolver), **Ordem de construção** passo 27. Search adapter já coberto em task_07.

### Arquivos relevantes
- `sm-shop/src/main/java/com/salesmanager/shop/store/facade/content/ContentFacadeImpl.java` — in-process
- `sm-shop/src/main/java/com/salesmanager/shop/controller/ImagesController.java` — static
- `sm-shop/src/main/java/com/salesmanager/shop/application/config/MerchantStoreArgumentResolver.java` — resolver
- `sm-shop/src/main/java/com/salesmanager/shop/store/controller/store/facade/StoreFacadeImpl.java` — merchant in-process
- `sm-shop/.../strangler/search/SearchFacadeHttpAdapter.java` — já existente (task_07)

### Arquivos dependentes
- `sm-shop/.../strangler/content/ContentFacadeHttpAdapter.java`
- `sm-shop/.../strangler/content/StaticContentProxy.java`
- `sm-shop/.../strangler/content/ContentBlobClient.java`
- `sm-shop/.../strangler/merchant/StoreFacadeHttpAdapter.java`
- `sm-shop/.../strangler/merchant/MerchantConfigurationFacadeHttpAdapter.java`
- `sm-shop/.../strangler/merchant/MerchantStoreEntityHydrator.java`
- `MerchantStoreArgumentResolver.java` — atualizado

### ADRs relacionados
- [ADR-009: Thin proxy estático](adrs/adr-009.md) — StaticContentProxy
- [ADR-005: APIs internas](adrs/adr-005.md) — static/snapshot
- [ADR-001: workflow único](adrs/adr-001.md) — Strangler compartilhado

## Entregáveis
- Adapters Content/Merchant + proxy + blob client + hydrator
- Gate profiles monolith e strangler-wave2
- Integration tests adapters 80%+ **(REQUIRED)**
- Regressão ambos profiles **(REQUIRED)**

## Testes
- Unit tests:
  - [x] ContentFacadeHttpAdapter propaga JWT e X-Correlation-Id
  - [x] Hydrator mapeia MerchantStoreSnapshot → entidade usada pelo resolver
- Integration tests:
  - [x] `/static/files/**` via HTTP quando strangler on
  - [x] ContentBlobClient usado por option/variant facades
  - [x] Store/Config adapters HTTP; falha → 503
  - [x] Resolver usa snapshot HTTP com cache TTL
  - [x] Um bean por facade interface em cada profile
  - [x] `./mvnw test -pl sm-shop -Dspring.profiles.active=strangler-wave2`
  - [x] Profile monolith verde
- Test coverage target: >=80%
- All tests must pass

## Critérios de sucesso
- All tests passing
- Test coverage >=80%
- Strangler Wave2 content+search+merchant wired
- Sem rewrite do MerchantStoreArgumentResolver call-sites
