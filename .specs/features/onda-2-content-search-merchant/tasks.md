# Onda 2 — Content, Search, Merchant Tasks

**Design:** `.specs/features/onda-2-content-search-merchant/design.md`
**Spec:** `.specs/features/onda-2-content-search-merchant/spec.md`
**Status:** Approved — pronto para Execute
**Testing:** `.specs/codebase/TESTING.md`
**Prerequisite:** Onda 1 Execute completo (`onda-1-reference-tax` T1–T32)

---

## Execution Plan

### Phase 1: Contracts + Wave2 Config (Sequential → Parallel)

```
Onda1-T32 ──→ T1 ──┬──→ T2 [P]
                   └──→ T3 [P]
T1,T2,T3 ──→ T4
```

### Phase 2: Core Extraction (3 parallel tracks)

```
T4 ──┬──→ T5 ──→ T6 ──→ T7 ──→ T8 ──→ T9 ──→ T10 ──┬──→ T11 ──→ T12 ──→ T13 ──→ T14 ──→ T15 ──→ T16 ──→ T17
     │                                                  │
     ├──→ T18 ──→ T19 ──→ T20 ──→ T21 ──→ T22 ──→ T23 [P] ──→ T24 ──→ T25 ──→ T26 ──→ T27 ──→ T28 ──→ T29
     │         T23 [P] (após T2, ∥ T18+)
     │
     └──→ T30 ──→ T31 ──→ T32 ──→ T33 ──→ T34 ──→ T35 ──→ T36 ──→ T37 ──→ T38 ──→ T39 ──→ T40
```

**Track A (Content):** T5–T17  
**Track B (Search):** T18–T29  
**Track C (Merchant):** T30–T40 (T35 bloqueia em T15 — internal logo API)

### Phase 3: Strangler (Sequential, cross-track)

```
T17,T29,T40 ──→ T41 ──→ T42 ──→ T43 ──→ T44 ──→ T45 ──→ T46
```

### Phase 4: Integration & Gate (Sequential tail)

```
T46 ──→ T47 ──┬──→ T48 [P providers]
              └──→ T49
T48,T49 ──→ T50 ──→ T51 ──→ T52 ──→ T53 ──→ T54
```

---

## Parallel Execution Map

```
Phase 1:
  Onda1-T32 → T1 → (T2 ∥ T3) → T4

Phase 2 (3 tracks após T4):
  Content:  T5 → T6 → T7 → T8 → T9 → T10 → T11 → T12 → T13 → T14 → T15 → T16 → T17
  Search:   T18 → T19 → T20 → T21 → T22 → T23 [P∥T18] → T24 → T25 → T26 → T27 → T28 → T29
  Merchant: T30 → T31 → T32 → T33 → T34 → (T35 após T15) → T36 → T37 → T38 → T39 → T40

Phase 3:
  T41 → T42 → T43 → T44 → T45 → T46

Phase 4:
  T47 → (T48 ∥ 3 subagents) → T49 → T50 → T51 → T52 → T53 → T54
```

**Subagent rule:** `[P]` → um subagent por task na mesma fase. Tracks Content/Search/Merchant em Phase 2 → **3 subagents paralelos** por task ordinal (ex.: T5+T18+T30). Tasks sem `[P]` → subagent sequencial dentro do track.

**Milestone `C-ready`:** T15 completo (internal static + logo API). **Milestone `S-ready`:** T22 completo (internal index API).

---

## Task Breakdown

### T1: Content DTOs + `ContentServiceClient` em contracts

**What:** Migrar DTOs content para `com.salesmanager.contracts.content`; criar `ContentServiceClient` (storeCode/langCode, sem JPA).
**Where:** `shopizer-api-contracts/.../content/`, `.../client/ContentServiceClient.java`
**Depends on:** Onda 1 T6
**Reuses:** `sm-shop-model/.../model/content/*`
**Requirement:** CNT-06, STR-04, OQ-04

**Done when:**
- [ ] DTOs compilam sem `com.salesmanager.core.model`
- [ ] `./mvnw compile -pl shopizer-api-contracts` passa

**Tests:** none
**Gate:** `./mvnw compile -pl shopizer-api-contracts`

**Commit:** `feat(contracts): add content DTOs and ContentServiceClient`

---

### T2: Search DTOs + `SearchIndexClient` em contracts [P]

**What:** Criar `ProductIndexPayload`, `ProductIndexBulkPayload`, `ValueList`; interface `SearchIndexClient`.
**Where:** `shopizer-api-contracts/.../search/`, `.../client/SearchIndexClient.java`
**Depends on:** Onda 1 T6
**Reuses:** design § ProductIndexPayload (`schemaVersion=1`)
**Requirement:** SRCH-04, SRCH-07, STR-04

**Done when:**
- [ ] `ProductIndexPayload` serializável; `schemaVersion` default 1
- [ ] `./mvnw compile -pl shopizer-api-contracts` passa

**Tests:** none
**Gate:** `./mvnw compile -pl shopizer-api-contracts`

**Commit:** `feat(contracts): add search index DTOs and SearchIndexClient`

---

### T3: Merchant DTOs + `MerchantServiceClient` em contracts [P]

**What:** Migrar DTOs merchant; criar `MerchantStoreSnapshot`; `MerchantServiceClient`.
**Where:** `shopizer-api-contracts/.../merchant/`, `.../client/MerchantServiceClient.java`
**Depends on:** Onda 1 T6
**Reuses:** `sm-shop-model/.../model/store/*`, `Configs.java`
**Requirement:** MCH-02, MCH-03, MCH-07, STR-04

**Done when:**
- [ ] `MerchantStoreSnapshot` criado
- [ ] `./mvnw compile -pl shopizer-api-contracts` passa

**Tests:** none
**Gate:** `./mvnw compile -pl shopizer-api-contracts`

**Commit:** `feat(contracts): add merchant DTOs and MerchantServiceClient`

---

### T4: Wave2 Strangler properties + `RestTemplate` (monólito)

**What:** Profile `strangler-wave2`; properties `wave2.*.base-url`, `wave2.strangler.enabled`, `wave2.search-service.internal-token`; `@Bean RestTemplate` + correlation interceptor stub; `SearchIndexClientRestTemplateImpl` no sm-shop.
**Where:** `sm-shop/.../strangler/config/Wave2ClientConfig.java`, `application-strangler-wave2.properties`
**Depends on:** T1, T2, T3
**Reuses:** Onda 1 `Wave1ClientConfig`; STR-01
**Requirement:** STR-01, SRCH-06

**Done when:**
- [ ] Properties coexistem com `wave1.*`
- [ ] `./mvnw test -pl sm-shop -Dtest=Wave2ClientConfigTest`

**Tests:** unit
**Gate:** `./mvnw test -pl sm-shop -Dtest=Wave2ClientConfigTest`

**Commit:** `feat(shop): add wave2 RestTemplate and strangler properties`

---

### T5: Scaffold `sm-content-core` + repositories

**What:** Novo módulo; mover `ContentRepository*`, `PageContentRepository`.
**Where:** `sm-content-core/`, root `pom.xml`
**Depends on:** T1
**Reuses:** Onda 1 `sm-reference-core` pattern
**Requirement:** CNT-05, STR-03

**Done when:**
- [ ] `@DataJpaTest` smoke passa
- [ ] `./mvnw test -pl sm-content-core`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-content-core`

**Commit:** `feat(content-core): scaffold and extract content repositories`

---

### T6: Mover `ContentService` + CMS content modules

**What:** Mover `ContentServiceImpl`; `modules/cms/content/` (exceto `product/`); cache managers.
**Where:** `sm-content-core/.../services/content/`, `.../modules/cms/`
**Depends on:** T5
**Reuses:** design § sm-content-core
**Requirement:** CNT-04, CNT-05, OQ-02

**Done when:**
- [ ] Zero `productFileManager` no módulo
- [ ] `./mvnw test -pl sm-content-core`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-content-core`

**Commit:** `feat(content-core): extract ContentService and CMS modules`

---

### T7: Split `shopizer-content-cms.xml`

**What:** Subset XML só `contentFileManager` + backends (infinispan/local/aws/gcp).
**Where:** `sm-content-core/src/main/resources/spring/shopizer-content-cms.xml`
**Depends on:** T6
**Reuses:** `sm-core/.../shopizer-core-cms.xml`
**Requirement:** CNT-04

**Done when:**
- [ ] Beans resolvem via `@ImportResource`
- [ ] `./mvnw test -pl sm-content-core`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-content-core`

**Commit:** `feat(content-core): add shopizer-content-cms.xml`

---

### T8: Scaffold `content-service` Boot app

**What:** Executable jar port 8083; `ContentServiceApplication` + `@ImportResource` CMS XML.
**Where:** `content-service/`
**Depends on:** T7
**Reuses:** `reference-service` packaging
**Requirement:** CNT-01

**Done when:**
- [ ] `./mvnw package -pl content-service`
- [ ] `GET /actuator/health` UP

**Tests:** integration
**Gate:** `./mvnw test -pl content-service -Dtest=ContentServiceApplicationTest`

**Commit:** `feat(content-service): scaffold Spring Boot application`

---

### T9: JWT security em `content-service`

**What:** Copiar cadeia JWT de tax-service; proteger `/private/**`; público aberto.
**Where:** `content-service/.../security/`
**Depends on:** T8
**Reuses:** AD-006
**Requirement:** CNT-01, STR-04

**Done when:**
- [ ] Private sem JWT → 401
- [ ] `./mvnw test -pl content-service -Dtest=ContentSecurityIntegrationTest`

**Tests:** integration
**Gate:** `./mvnw test -pl content-service -Dtest=ContentSecurityIntegrationTest`

**Commit:** `feat(content-service): add JWT security`

---

### T10: `ReferenceServiceClient` em content-service

**What:** `ReferenceServiceClientRestTemplateImpl` → `wave1.reference-service.base-url`.
**Where:** `content-service/.../client/`
**Depends on:** T9
**Reuses:** Onda 1 tax client pattern
**Requirement:** CNT-07, STR-06

**Done when:**
- [ ] Resolve language por code via HTTP
- [ ] `./mvnw test -pl content-service -Dtest=ReferenceServiceClientTest`

**Tests:** unit
**Gate:** `./mvnw test -pl content-service -Dtest=ReferenceServiceClientTest`

**Commit:** `feat(content-service): add ReferenceServiceClient`

---

### T11: `ContentPagesController`

**What:** Endpoints pages espelhando `ContentApi` (list, get, CRUD private).
**Where:** `content-service/.../api/v1/content/ContentPagesController.java`
**Depends on:** T10
**Reuses:** `ContentApi` paths
**Requirement:** CNT-02, CNT-06

**Done when:**
- [ ] CRUD página 2 idiomas retorna DTOs
- [ ] `./mvnw test -pl content-service -Dtest=ContentPagesIntegrationTest`

**Tests:** integration
**Gate:** `./mvnw test -pl content-service -Dtest=ContentPagesIntegrationTest`

**Commit:** `feat(content-service): content pages REST endpoints`

---

### T12: `ContentBoxesController`

**What:** Endpoints boxes (list, get by code, CRUD).
**Where:** `content-service/.../api/v1/content/ContentBoxesController.java`
**Depends on:** T11
**Requirement:** CNT-02

**Done when:**
- [ ] Box CRUD localizado por `lang`
- [ ] `./mvnw test -pl content-service -Dtest=ContentBoxesIntegrationTest`

**Tests:** integration
**Gate:** `./mvnw test -pl content-service -Dtest=ContentBoxesIntegrationTest`

**Commit:** `feat(content-service): content boxes REST endpoints`

---

### T13: `ContentFilesController`

**What:** Upload `POST /private/file(s)`; `GET /content/images`.
**Where:** `content-service/.../api/v1/content/ContentFilesController.java`
**Depends on:** T12
**Requirement:** CNT-03, CNT-04

**Done when:**
- [ ] Upload IMAGE persiste blob
- [ ] `./mvnw test -pl content-service -Dtest=ContentFilesIntegrationTest`

**Tests:** integration
**Gate:** `./mvnw test -pl content-service -Dtest=ContentFilesIntegrationTest`

**Commit:** `feat(content-service): content file upload endpoints`

---

### T14: `ContentAdminController`

**What:** Admin CMS list/folder/images add/rename/remove; stubs OQ-04 preservados.
**Where:** `content-service/.../api/v1/content/ContentAdminController.java`
**Depends on:** T13
**Requirement:** CNT-03, OQ-04

**Done when:**
- [ ] Stubs retornam `null`/no-op como monólito
- [ ] `./mvnw test -pl content-service -Dtest=ContentAdminIntegrationTest`

**Tests:** integration
**Gate:** `./mvnw test -pl content-service -Dtest=ContentAdminIntegrationTest`

**Commit:** `feat(content-service): content admin CMS endpoints`

---

### T15: Internal APIs — static files + logo (`C-ready`)

**What:** `StaticFilesInternalController` (`/internal/v1/static/files/**`); `InternalLogoController` (`POST/DELETE /internal/v1/content/logo`).
**Where:** `content-service/.../api/internal/`
**Depends on:** T14
**Reuses:** OQ-03 B, AD-014, AD-013
**Requirement:** CNT-08, MCH-04, STR-06

**Done when:**
- [ ] Static GET retorna bytes + content-type
- [ ] Logo upload/delete interno funcional
- [ ] `./mvnw test -pl content-service -Dtest=InternalContentApiIntegrationTest`

**Tests:** integration
**Gate:** `./mvnw test -pl content-service -Dtest=InternalContentApiIntegrationTest`

**Commit:** `feat(content-service): internal static and logo APIs`

---

### T16: Portar `ContentFacadeImpl` + mappers

**What:** Mover facade/populators/mappers para content-service; wire controllers → facade.
**Where:** `content-service/.../facade/content/`
**Depends on:** T15
**Reuses:** `sm-shop/.../ContentFacadeImpl.java`
**Requirement:** CNT-02, CNT-03, CNT-06

**Done when:**
- [ ] Facade retorna contracts DTOs
- [ ] `./mvnw test -pl content-service -Dtest=*ContentFacade*Test`

**Tests:** unit
**Gate:** `./mvnw test -pl content-service -Dtest=*ContentFacade*Test`

**Commit:** `feat(content-service): port ContentFacadeImpl`

---

### T17: `sm-core` delegate content + trim CMS XML

**What:** Remover classes movidas; dep `sm-content-core`; `shopizer-core-cms.xml` só product beans.
**Where:** `sm-core/pom.xml`, `shopizer-core-cms.xml`
**Depends on:** T7, T16
**Requirement:** CNT-04, CNT-05, STR-03

**Done when:**
- [ ] `./mvnw test -pl sm-core` verde

**Tests:** integration
**Gate:** `./mvnw test -pl sm-core`

**Commit:** `refactor(sm-core): delegate content to sm-content-core`

---

### T18: Scaffold `search-service` + OpenSearch deps

**What:** Módulo executable port 8084; deps commons + opensearch starter; **sem JPA**.
**Where:** `search-service/`
**Depends on:** T2, T4
**Reuses:** AD-012
**Requirement:** SRCH-01

**Done when:**
- [ ] `./mvnw package -pl search-service`
- [ ] Contexto sobe sem DataSource

**Tests:** integration
**Gate:** `./mvnw test -pl search-service -Dtest=SearchServiceApplicationTest`

**Commit:** `feat(search-service): scaffold application`

---

### T19: Migrar OpenSearch config + `SearchModuleBootstrap`

**What:** Mover `search/*.json`, `ApplicationSearchConfiguration`, bootstrap `@PostConstruct`.
**Where:** `search-service/src/main/resources/search/`, `.../configuration/`
**Depends on:** T18
**Requirement:** SRCH-03

**Done when:**
- [ ] Bootstrap executa no startup
- [ ] `./mvnw test -pl search-service -Dtest=SearchModuleBootstrapTest`

**Tests:** unit
**Gate:** `./mvnw test -pl search-service -Dtest=SearchModuleBootstrapTest`

**Commit:** `feat(search-service): migrate OpenSearch bootstrap`

---

### T20: `SearchQueryServiceImpl`

**What:** Extrair query/autocomplete de `SearchServiceImpl`; retorno `SearchItem` (commons).
**Where:** `search-service/.../services/SearchQueryServiceImpl.java`
**Depends on:** T19
**Requirement:** SRCH-02, OQ-06

**Done when:**
- [ ] Zero imports `sm-core-model`
- [ ] `./mvnw test -pl search-service -Dtest=SearchQueryServiceImplTest`

**Tests:** unit
**Gate:** `./mvnw test -pl search-service -Dtest=SearchQueryServiceImplTest`

**Commit:** `feat(search-service): query and autocomplete service`

---

### T21: `SearchIndexServiceImpl` + internal index API (`S-ready` partial)

**What:** Index/delete from `ProductIndexPayload`; `InternalIndexController`; `X-Internal-Token` filter.
**Where:** `search-service/.../services/`, `.../api/internal/`
**Depends on:** T2, T19
**Requirement:** SRCH-04, AD-013, OQ-01

**Done when:**
- [ ] schemaVersion ≠ 1 → 422
- [ ] Token inválido → 401
- [ ] `./mvnw test -pl search-service -Dtest=InternalIndexControllerIntegrationTest`

**Tests:** integration
**Gate:** `./mvnw test -pl search-service -Dtest=InternalIndexControllerIntegrationTest`

**Commit:** `feat(search-service): internal index API`

---

### T22: Public `SearchController` (`S-ready`)

**What:** `POST /search`, `/autocomplete`; admin reindex → 501.
**Where:** `search-service/.../api/v1/SearchController.java`
**Depends on:** T20, T21
**Requirement:** SRCH-02, SRCH-08, OQ-04

**Done when:**
- [ ] 3 paths registrados
- [ ] `./mvnw test -pl search-service -Dtest=SearchApiIntegrationTest`

**Tests:** integration
**Gate:** `./mvnw test -pl search-service -Dtest=SearchApiIntegrationTest`

**Commit:** `feat(search-service): public search REST endpoints`

---

### T23: `ProductIndexPayloadBuilder` em sm-core [P]

**What:** Extrair build logic de `SearchServiceImpl` → `List<ProductIndexPayload>` por idioma.
**Where:** `sm-core/.../search/index/ProductIndexPayloadBuilder.java`
**Depends on:** T2
**Reuses:** `SearchServiceImpl` index methods
**Requirement:** SRCH-06, SRCH-07

**Done when:**
- [ ] Builder unit test com fixture Product
- [ ] `./mvnw test -pl sm-core -Dtest=ProductIndexPayloadBuilderTest`

**Tests:** unit
**Gate:** `./mvnw test -pl sm-core -Dtest=ProductIndexPayloadBuilderTest`

**Commit:** `feat(sm-core): extract ProductIndexPayloadBuilder`

**Note:** Pode executar em paralelo com T18–T22 após T2.

---

### T24: `SearchIndexProducer` InProcess + Http

**What:** Interface + `SearchIndexProducerInProcess` + `SearchIndexProducerHttp` (DELETE + bulk POST).
**Where:** `sm-core/.../search/index/`, `sm-shop/.../strangler/search/`
**Depends on:** T21, T23, T4
**Requirement:** SRCH-06, STR-01

**Done when:**
- [ ] Um producer ativo por profile
- [ ] `./mvnw test -pl sm-shop -Dtest=SearchIndexProducerHttpTest`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=SearchIndexProducerHttpTest`

**Commit:** `feat(search): SearchIndexProducer in-process and HTTP`

---

### T25: Refatorar `IndexProductEventListener`

**What:** Injetar `SearchIndexProducer` em vez de `SearchService`.
**Where:** `sm-core/.../IndexProductEventListener.java`
**Depends on:** T24
**Requirement:** SRCH-06

**Done when:**
- [ ] `./mvnw test -pl sm-core -Dtest=IndexProductEventListenerTest`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-core -Dtest=IndexProductEventListenerTest`

**Commit:** `refactor(sm-core): listener uses SearchIndexProducer`

---

### T26: `SearchBulkIndexOrchestrator`

**What:** `indexAllData` via `ProductService.listByStore` + producer; delay configurável.
**Where:** `sm-shop/.../strangler/search/SearchBulkIndexOrchestrator.java`
**Depends on:** T24
**Requirement:** SRCH-08, GAP-SRCH-06

**Done when:**
- [ ] Admin reindex dispara orchestrator local
- [ ] `./mvnw test -pl sm-shop -Dtest=SearchBulkIndexOrchestratorTest`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=SearchBulkIndexOrchestratorTest`

**Commit:** `feat(shop): SearchBulkIndexOrchestrator`

---

### T27: `SearchFacadeHttpAdapter`

**What:** HTTP delegate query/autocomplete; `indexAllData` local via orchestrator.
**Where:** `sm-shop/.../strangler/search/SearchFacadeHttpAdapter.java`
**Depends on:** T22, T4, T26
**Requirement:** SRCH-05, STR-01

**Done when:**
- [ ] `./mvnw test -pl sm-shop -Dtest=SearchFacadeHttpAdapterTest`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=SearchFacadeHttpAdapterTest`

**Commit:** `feat(shop): search facade HTTP adapter`

---

### T28: Desabilitar OpenSearch no monólito (strangler profile)

**What:** `@ConditionalOnProperty` SearchFacade; remover starter de sm-core em strangler; desativar bootstrap monólito.
**Where:** `sm-core/pom.xml`, `sm-shop/.../SearchFacadeImpl.java`
**Depends on:** T24, T27
**Requirement:** STR-01, AD-012

**Done when:**
- [ ] Profile strangler não conecta OpenSearch
- [ ] Profile monolith regressão verde

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dspring.profiles.active=monolith`

**Commit:** `feat(shop): disable monolith OpenSearch when wave2 enabled`

---

### T29: GAP-SRCH documentation [P]

**What:** `search-service/README.md` seção Known gaps GAP-SRCH-01..10.
**Where:** `search-service/README.md`
**Depends on:** T19
**Requirement:** OQ-05

**Done when:**
- [ ] 10 gaps documentados

**Tests:** none
**Gate:** manual review

**Commit:** `docs(search-service): document indexing gaps`

---

### T30: Scaffold `sm-merchant-core` + repositories [P]

**What:** Mover merchant repos + MerchantConfiguration/MerchantLog repos.
**Where:** `sm-merchant-core/`
**Depends on:** T3
**Requirement:** MCH-01, STR-03

**Done when:**
- [ ] `./mvnw test -pl sm-merchant-core`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-merchant-core`

**Commit:** `feat(merchant-core): extract repositories`

---

### T31: Move merchant services + drop `ProductTypeService`

**What:** Mover `MerchantStoreService*`, `MerchantConfiguration*`, `MerchantLog*`; remover injeção morta.
**Where:** `sm-merchant-core/.../services/`
**Depends on:** T30
**Requirement:** MCH-01, MCH-06

**Done when:**
- [ ] Zero `ProductTypeService` em merchant
- [ ] `./mvnw test -pl sm-merchant-core`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-merchant-core`

**Commit:** `feat(merchant-core): extract merchant services`

---

### T32: Wire `sm-core` → `sm-merchant-core`

**What:** Remover classes movidas; dep `sm-merchant-core`.
**Where:** `sm-core/pom.xml`
**Depends on:** T31
**Requirement:** STR-03

**Done when:**
- [ ] `./mvnw test -pl sm-core`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-core`

**Commit:** `refactor(sm-core): delegate merchant to sm-merchant-core`

---

### T33: Scaffold `merchant-service` + JWT

**What:** Boot app port 8085; security chain replicada.
**Where:** `merchant-service/`
**Depends on:** T32
**Requirement:** MCH-01, MCH-02

**Done when:**
- [ ] `./mvnw test -pl merchant-service -Dtest=MerchantSecurityIntegrationTest`

**Tests:** integration
**Gate:** `./mvnw test -pl merchant-service -Dtest=MerchantSecurityIntegrationTest`

**Commit:** `feat(merchant-service): scaffold with JWT`

---

### T34: HTTP clients reference + content

**What:** `ReferenceServiceClient` + `ContentServiceClient` RestTemplate em merchant-service.
**Where:** `merchant-service/.../client/`
**Depends on:** T33, **T15**
**Requirement:** MCH-04, MCH-05, STR-06

**Done when:**
- [ ] Clients compilam; mock tests passam
- [ ] `./mvnw test -pl merchant-service -Dtest=*ServiceClientTest`

**Tests:** unit
**Gate:** `./mvnw test -pl merchant-service -Dtest=*ServiceClientTest`

**Commit:** `feat(merchant-service): reference and content HTTP clients`

---

### T35: Port `StoreFacadeImpl` + populators

**What:** Mover facade + populators; refs via HTTP client.
**Where:** `merchant-service/.../facade/`, `.../populator/`
**Depends on:** T34
**Requirement:** MCH-02, MCH-05

**Done when:**
- [ ] `./mvnw test -pl merchant-service -Dtest=*StoreFacade*Test`

**Tests:** unit
**Gate:** `./mvnw test -pl merchant-service -Dtest=*StoreFacade*Test`

**Commit:** `feat(merchant-service): port StoreFacadeImpl`

---

### T36: `MerchantConfigurationFacade` + `GET /config`

**What:** Port config facade; `PublicConfigsController`.
**Where:** `merchant-service/.../api/v1/system/`
**Depends on:** T35
**Requirement:** MCH-03

**Done when:**
- [ ] `./mvnw test -pl merchant-service -Dtest=PublicConfigsIntegrationTest`

**Tests:** integration
**Gate:** `./mvnw test -pl merchant-service -Dtest=PublicConfigsIntegrationTest`

**Commit:** `feat(merchant-service): public config endpoint`

---

### T37: `MerchantStoreController` — store REST

**What:** Espelhar `MerchantStoreApi` (~18 endpoints, sem logo).
**Where:** `merchant-service/.../api/v1/store/`
**Depends on:** T35
**Requirement:** MCH-02, MCH-06

**Done when:**
- [ ] Zero rotas ProductType
- [ ] `./mvnw test -pl merchant-service -Dtest=MerchantStoreApiIntegrationTest`

**Tests:** integration
**Gate:** `./mvnw test -pl merchant-service -Dtest=MerchantStoreApiIntegrationTest`

**Commit:** `feat(merchant-service): merchant store REST endpoints`

---

### T38: `InternalStoreController` snapshot

**What:** `GET /internal/v1/store/{code}` → `MerchantStoreSnapshot`.
**Where:** `merchant-service/.../api/internal/`
**Depends on:** T35
**Requirement:** MCH-07, AD-013

**Done when:**
- [ ] `./mvnw test -pl merchant-service -Dtest=InternalStoreControllerIntegrationTest`

**Tests:** integration
**Gate:** `./mvnw test -pl merchant-service -Dtest=InternalStoreControllerIntegrationTest`

**Commit:** `feat(merchant-service): internal store snapshot API`

---

### T39: Logo orchestration AD-014

**What:** Upload blob-first + compensate; delete DB-first + tolerate orphan blob.
**Where:** `merchant-service/.../facade/StoreFacadeImpl.java`
**Depends on:** T34, T37, T15
**Requirement:** MCH-04, AD-014

**Done when:**
- [ ] Compensação testada em upload fail DB
- [ ] `./mvnw test -pl merchant-service -Dtest=LogoOrchestrationTest`

**Tests:** integration
**Gate:** `./mvnw test -pl merchant-service -Dtest=LogoOrchestrationTest`

**Commit:** `feat(merchant-service): logo orchestration AD-014`

---

### T40: Merchant track module gate

**What:** `./mvnw test -pl merchant-service` completo.
**Where:** `merchant-service/`
**Depends on:** T36, T37, T38, T39
**Requirement:** MCH-01

**Done when:**
- [ ] Módulo merchant-service verde

**Tests:** integration
**Gate:** `./mvnw test -pl merchant-service`

**Commit:** `chore(merchant-service): module integration gate`

---

### T41: `ContentFacadeHttpAdapter`

**What:** Strangler adapter content; `@ConditionalOnProperty`; JWT + correlation forward.
**Where:** `sm-shop/.../strangler/content/ContentFacadeHttpAdapter.java`
**Depends on:** T16, T17, T4
**Requirement:** CNT-09, STR-01

**Done when:**
- [ ] `./mvnw test -pl sm-shop -Dtest=ContentFacadeHttpAdapterTest`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=ContentFacadeHttpAdapterTest`

**Commit:** `feat(shop): content facade HTTP adapter`

---

### T42: `StaticContentProxy`

**What:** Wire `ImagesController`/`FilesController` → content internal static API.
**Where:** `sm-shop/.../strangler/content/StaticContentProxy.java`
**Depends on:** T15, T41
**Requirement:** CNT-08, OQ-03

**Done when:**
- [ ] `/static/files/**` serve via HTTP quando strangler on
- [ ] `./mvnw test -pl sm-shop -Dtest=StaticContentProxyTest`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=StaticContentProxyTest`

**Commit:** `feat(shop): static content proxy`

---

### T43: `ContentBlobClient` (catalog P2)

**What:** HTTP client para `ProductOptionFacadeImpl` (PROPERTY) e `ProductVariantGroupFacadeImpl` (VARIANT).
**Where:** `sm-shop/.../strangler/content/ContentBlobClient.java`
**Depends on:** T15, T41
**Requirement:** CNT-08, STR-06

**Done when:**
- [ ] `./mvnw test -pl sm-shop -Dtest=ContentBlobClientTest`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=ContentBlobClientTest`

**Commit:** `feat(shop): content blob client for catalog facades`

---

### T44: Merchant strangler adapters

**What:** `StoreFacadeHttpAdapter`, `MerchantConfigurationFacadeHttpAdapter`.
**Where:** `sm-shop/.../strangler/merchant/`
**Depends on:** T40, T4
**Requirement:** MCH-08, STR-01

**Done when:**
- [ ] `./mvnw test -pl sm-shop -Dtest=*MerchantFacadeHttpAdapterTest`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=*MerchantFacadeHttpAdapterTest`

**Commit:** `feat(shop): merchant facade HTTP adapters`

---

### T45: `MerchantServiceClient` + resolver + hydrator

**What:** Client RestTemplate; `MerchantStoreEntityHydrator`; atualizar `MerchantStoreArgumentResolver`; cache TTL opcional.
**Where:** `sm-shop/.../strangler/merchant/`, `MerchantStoreArgumentResolver.java`
**Depends on:** T38, T44
**Requirement:** MCH-07, MCH-08

**Done when:**
- [ ] Resolver usa HTTP snapshot
- [ ] `./mvnw test -pl sm-shop -Dtest=MerchantStoreResolverIntegrationTest`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=MerchantStoreResolverIntegrationTest`

**Commit:** `feat(shop): merchant resolver HTTP client`

---

### T46: Strangler conditional wiring gate

**What:** Verificar um bean por interface (Content/Search/Merchant facades + producers); profiles monolith vs strangler-wave2.
**Where:** `sm-shop/`, `sm-core/`
**Depends on:** T28, T41, T43, T45
**Requirement:** STR-01, STR-04

**Done when:**
- [ ] `./mvnw test -pl sm-shop -Dspring.profiles.active=strangler-wave2`
- [ ] Monolith profile verde

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop`

**Commit:** `feat(shop): wave2 strangler conditional wiring gate`

---

### T47: Correlation ID filter (all Wave2 apps)

**What:** `CorrelationIdFilter` em content/search/merchant/sm-shop; RestTemplate interceptor.
**Where:** `*/.../web/CorrelationIdFilter.java`
**Depends on:** T8, T18, T33, T4
**Requirement:** STR-05

**Done when:**
- [ ] Header propagado downstream
- [ ] `./mvnw test -pl merchant-service -Dtest=CorrelationIdFilterTest`

**Tests:** unit
**Gate:** `./mvnw test -pl merchant-service,content-service,search-service,sm-shop -Dtest=CorrelationId*Test`

**Commit:** `feat(wave2): correlation id propagation`

---

### T48: Actuator health indicators [P]

**What:** Custom health: content (db/cms/reference); search (openSearch); merchant (db/reference/content).
**Where:** `{content,search,merchant}-service/.../health/`
**Depends on:** T17, T22, T40
**Requirement:** STR-05

**Done when:**
- [ ] `/actuator/health` mostra componentes nos 3 serviços
- [ ] `./mvnw test -pl content-service,search-service,merchant-service -Dtest=*HealthIndicatorTest`

**Tests:** unit
**Gate:** `./mvnw test -pl content-service,search-service,merchant-service -Dtest=*HealthIndicatorTest`

**Commit:** `feat(wave2): custom health indicators`

**[P]** — 3 subagents (um por serviço)

---

### T49: Pact provider tests [P]

**What:** Provider verification content + search + merchant endpoints P1.
**Where:** `*/src/test/java/**/contract/*ProviderPactTest.java`
**Depends on:** T17, T22, T40
**Requirement:** STR-02

**Done when:**
- [ ] `./mvnw test -pl content-service,search-service,merchant-service -Dtest=*ProviderPactTest`

**Tests:** integration
**Gate:** `./mvnw test -pl content-service,search-service,merchant-service -Dtest=*ProviderPactTest`

**Commit:** `test(wave2): Pact provider verification`

**[P]** — 3 subagents

---

### T50: Pact consumer (sm-shop)

**What:** `Wave2ConsumerPactTest` verifica contratos dos 3 serviços.
**Where:** `sm-shop/src/test/java/.../contract/Wave2ConsumerPactTest.java`
**Depends on:** T49
**Requirement:** STR-02

**Done when:**
- [ ] `./mvnw test -pl sm-shop -Dtest=Wave2ConsumerPactTest`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=Wave2ConsumerPactTest`

**Commit:** `test(shop): Wave2 Pact consumer tests`

---

### T51: Docker Compose Wave 2

**What:** `docker-compose-wave2.yml` — mysql, opensearch, 4 services + sm-shop; CMS volume só content.
**Where:** `/docker-compose-wave2.yml`
**Depends on:** T46
**Requirement:** STR-03, STR-06

**Done when:**
- [ ] `docker compose -f docker-compose-wave2.yml config` válido

**Tests:** none
**Gate:** `docker compose -f docker-compose-wave2.yml config`

**Commit:** `chore(deploy): add wave2 docker compose`

---

### T52: Wave2 integration test suite

**What:** Consolidar testes search/content/merchant strangler; `@Ignore` ES opcional documentado.
**Where:** `*/src/test/java/`
**Depends on:** T46, T50
**Requirement:** SRCH-01..06, CNT-01..09, MCH-01..08

**Done when:**
- [ ] `./mvnw test -pl content-service,search-service,merchant-service,sm-shop -Dtest=*Wave2*Test,*IntegrationTest`

**Tests:** integration
**Gate:** `./mvnw test -pl content-service,search-service,merchant-service,sm-shop`

**Commit:** `test(wave2): integration test suite`

---

### T53: Full build gate

**What:** `./mvnw clean install` reactor completo Onda 1 + Onda 2.
**Where:** root
**Depends on:** T51, T52
**Requirement:** all Wave 2

**Done when:**
- [ ] `./mvnw clean install` passa
- [ ] Pact consumer verde

**Tests:** integration
**Gate:** `./mvnw clean install`

**Commit:** `chore(wave2): complete build gate`

---

### T54: Traceability + STATE update

**What:** Atualizar `spec.md` requirements → Verified; `STATE.md` Onda 2 Tasks complete; `design.md` Status Approved.
**Where:** `.specs/features/onda-2-content-search-merchant/`, `.specs/project/STATE.md`
**Depends on:** T53
**Requirement:** STR-02, STR-05

**Done when:**
- [ ] 28 requirements mapped; 0 unmapped
- [ ] STATE.md reflete pronto para Execute

**Tests:** none
**Gate:** manual checklist

**Commit:** `docs(wave2): update traceability and project state`

---

## Task Granularity Check

| Task | Scope | Status |
| ---- | ----- | ------ |
| T1–T3 | 1 contract package each | ✅ |
| T11–T14 | 1 controller each | ✅ |
| T15 | 2 internal controllers | ⚠️ OK — coeso (C-ready) |
| T23 | 3 producer beans | ⚠️ OK — 1 concern |
| T37 | ~18 endpoints | ⚠️ OK — 1 controller |
| T48/T49 | 3 services | ⚠️ OK — `[P]` split |
| T53 | build gate | ✅ |

---

## Diagram–Definition Cross-Check

| Task | Depends On | Diagram | Status |
| ---- | ---------- | ------- | ------ |
| T1 | Onda1-T32 | Phase1 | ✅ |
| T2,T3 | Onda1-T32 | ∥ após T1 | ✅ |
| T4 | T1,T2,T3 | T1–T3→T4 | ✅ |
| T5–T17 | chain content | Track A | ✅ |
| T18–T29 | chain search | Track B | ✅ |
| T23 | T2 | ∥ T18+ | ✅ |
| T30–T40 | chain merchant | Track C | ✅ |
| T34,T39 | T15 | C-ready | ✅ |
| T41–T46 | T17,T29,T40 | Phase3 | ✅ |
| T47–T54 | Phase4 tail | Phase4 | ✅ |

---

## Test Co-location Validation

| Task | Code Layer | Matrix | Task Says | Status |
| ---- | ---------- | ------ | --------- | ------ |
| T1–T3 | DTOs | none | none | ✅ |
| T5–T7,T17,T30–T32 | JPA/service | integration | integration | ✅ |
| T8,T9,T33 | Boot/security | integration | integration | ✅ |
| T10,T23,T34 | client/builder | unit | unit | ✅ |
| T11–T15,T21,T37–T39 | REST | integration | integration | ✅ |
| T16,T35 | facade | unit | unit | ✅ |
| T20 | service | unit | unit | ✅ |
| T23–T28,T41–T46 | strangler | integration | integration | ✅ |
| T47,T48 | filter/health | unit | unit | ✅ |
| T49,T50 | Pact | integration | integration | ✅ |
| T51 | docker | none | none | ✅ |
| T52,T53 | full suite | integration | integration | ✅ |
| T54 | docs | none | none | ✅ |

---

## Requirement Traceability → Tasks

| Requirement | Task(s) |
| ----------- | ------- |
| CNT-01 | T8, T52 |
| CNT-02 | T1, T11, T12, T16 |
| CNT-03 | T13, T14, T16 |
| CNT-04 | T6, T7, T13, T17 |
| CNT-05 | T5, T6, T17 |
| CNT-06 | T1, T11, T16 |
| CNT-07 | T10 |
| CNT-08 | T15, T42, T43 |
| CNT-09 | T41 |
| SRCH-01 | T18, T28, T52 |
| SRCH-02 | T19, T20, T22 |
| SRCH-03 | T19 |
| SRCH-04 | T2, T21, T49 |
| SRCH-05 | T27, T52 |
| SRCH-06 | T4, T24, T25, T23 |
| SRCH-07 | T2, T23 |
| SRCH-08 | T22, T26 |
| MCH-01 | T33, T37, T40 |
| MCH-02 | T3, T35, T37 |
| MCH-03 | T3, T36 |
| MCH-04 | T15, T34, T39 |
| MCH-05 | T34, T35 |
| MCH-06 | T31, T37 |
| MCH-07 | T3, T38, T45 |
| MCH-08 | T44, T45 |
| STR-01 | T4, T24, T27, T28, T41, T44, T46 |
| STR-02 | T49, T50, T54 |
| STR-03 | T5, T17, T30, T32, T51 |
| STR-04 | T1–T3, T9, T22, T37, T46 |
| STR-05 | T47, T48, T54 |
| STR-06 | T10, T15, T34, T43, T51 |

**Coverage:** 28 requirements → 54 tasks; 0 unmapped ✅

---

## Execute — Subagent Delegation Plan

| Phase | Tasks | Subagents | Parallel? |
| ----- | ----- | --------- | --------- |
| 1 | T1→T4 | 1 (+ T2∥T3) | Partial |
| 2 | T5–T17 / T18–T29 / T30–T40 | **3 tracks** | Yes (3 subagents/task wave) |
| 2 | T23 | 1 | ∥ search track após T2 |
| 3 | T41→T46 | 1 each | No |
| 4 | T48, T49 | 3 each | Yes `[P]` |
| 4 | T47,T50–T54 | sequential | No |

**Prerequisite gate:** Não iniciar T34/T39 até **T15** (`C-ready`). Não iniciar T24 até **T21** + **T23**.

**Cada subagent retorna:** Status, files changed, gate result, `SPEC_DEVIATION` se houver.

---

## Próximo passo

**Execute** — iniciar **T1** após Onda 1 T32 completo. Código bloqueado até aprovação explícita do usuário.

**Ferramentas sugeridas:**
- T1–T4: Maven + filesystem
- T5–T40: Maven + spring-docs MCP
- T41–T46: Maven
- T49–T50: Pact JVM
- T51: Docker
- T53: `./mvnw clean install`
