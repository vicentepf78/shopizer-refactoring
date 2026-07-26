# State

**Last Updated:** 2026-07-26T10:13:00-03:00
**Current Work:** onda-1-reference-tax — COMPLETE (gate verde). Próximo: Execute onda-2-content-search-merchant task_01

---

## Recent Decisions (Last 60 days)

### AD-001: TLC spec-driven para Onda 1 (2026-07-04)

**Decision:** Usar `tlc-spec-driven` em vez de plano avulso para Onda 1.
**Reason:** Escopo Large (multi-módulo, Strangler, contratos); define padrão para Ondas 2–6; exige traceability e STATE persistente.
**Trade-off:** Mais documentação upfront vs velocidade inicial.
**Impact:** `.specs/` como fonte de verdade; Execute bloqueado até `tasks.md` aprovado.

### AD-002: Tax admin vs Tax calculation split (2026-07-04)

**Decision:** Onda 1 extrai apenas CRUD admin (`TaxClassService`, `TaxRateService`, APIs privadas). `TaxService` permanece no monólito.
**Reason:** `TaxServiceImpl` acopla order, shipping, customer, catalog e system; score 6/10 se incluído.
**Trade-off:** Dois bounded contexts lógicos no mesmo domínio de negócio.
**Impact:** Order continua chamando `TaxService` in-process; cálculo remoto fica para Onda 6.

### AD-003: Schema compartilhado na Onda 1 (2026-07-04)

**Decision:** Manter FKs JPA (`Product`→`TaxClass`, `MerchantStore`→`Language`, etc.) no mesmo schema DB inicialmente.
**Reason:** Remover FKs exige Onda 3 (`LanguageCode`, `MerchantStoreId`); extração com DB separado quebraria monólito.
**Trade-off:** Não é extração "pura" de dados; é extração de runtime/API primeiro.
**Impact:** Serviços extraídos acessam tabelas de referência/tax no DB compartilhado; split de schema em onda futura.

### AD-004: InitializationDatabaseImpl fora da Onda 1 (2026-07-04)

**Decision:** Bootstrap multi-domínio permanece no monólito; não mover para reference-service.
**Reason:** `InitializationDatabaseImpl` orquestra merchant, catalog, tax, user, system — hub de startup.
**Trade-off:** reference-service não é 100% autônomo em deploy greenfield.
**Impact:** Novos ambientes continuam usando monólito para seed; serviços extraídos assumem dados já existentes.

### AD-005: RestTemplate para HTTP clients (2026-07-04)

**Decision:** Strangler adapters e tax→reference usam `RestTemplate` com `@Bean` configurado.
**Reason:** Sem precedente Feign/WebClient no codebase; `TestRestTemplate` nos testes; evita Spring Cloud na Onda 1.
**Trade-off:** Menos declarativo que OpenFeign.
**Impact:** Padrão único para todos os clients HTTP da Onda 1.

### AD-006: JWT replication em tax-service (2026-07-04)

**Decision:** tax-service replica cadeia JWT completa (filter + user lookup + store authorization).
**Reason:** Spec exige auth idêntica; shared DB viabiliza `UserService`/`MerchantStore` lookup.
**Trade-off:** Duplicação de security config; login permanece no monólito.
**Impact:** Admin pode chamar tax-service diretamente ou via Strangler com mesmo JWT.

### AD-008: Onda 2 em três serviços paralelos (2026-07-04)

**Decision:** Uma feature TLC `onda-2-content-search-merchant` cobre content, search e merchant (mesmo padrão Onda 1 com reference+tax).
**Reason:** Mesma janela temporal (semanas 25–32); dependências cruzadas (merchant→content logo); Strangler unificado.
**Trade-off:** Spec maior vs três features separadas.
**Impact:** 28 requirement IDs (CNT/SRCH/MCH/STR); Design pode paralelizar por serviço.

### AD-009: ProductIndexPayload interim para Search (2026-07-04) — CONFIRMADO

**Decision:** Monólito produz `ProductIndexPayload` HTTP; `search-service` consome sem JPA (OQ-01 A).
**Reason:** `ProductSnapshot` completo é Onda 3; adiar search inteiro perde validação do padrão.
**Trade-off:** `ProductIndexPayloadBuilder` permanece no monólito (catalog coupling) até Onda 3/4.
**Impact:** SRCH-04, SRCH-06, SRCH-07; `SearchIndexProducerHttp` + internal API.

### AD-011: Módulos thin sm-content-core / sm-merchant-core (2026-07-04)

**Decision:** Extrair subset de sm-core em JARs intermediários antes dos executáveis.
**Reason:** Evita dependência circular; search-service fica sem sm-core.
**Impact:** T1/T3 tasks; shopizer-core-cms.xml split.

### AD-012: search-service sem JPA (2026-07-04)

**Decision:** search-service não acessa MySQL; só OpenSearch + REST.
**Reason:** Domínio read-model; indexação via payload HTTP.
**Impact:** Único serviço Onda 2 stateless em DB.

### AD-013: Internal APIs network-isolated (2026-07-04)

**Decision:** `/internal/v1/**` com network policy; search usa `X-Internal-Token`.
**Reason:** Sem JWT em rotas internas; confiança de rede + token.
**Impact:** Docker/K8s network policies em deploy.

### AD-014: Logo upload blob-first (2026-07-04)

**Decision:** merchant-service POST blob → content; ON OK UPDATE storeLogo; compensate on DB fail.
**Reason:** Fix ordering bug atual (DB antes de blob).
**Impact:** MCH-04; ContentServiceClient em merchant-service.

### AD-010: Merchant sem ProductType na Onda 2 (2026-07-04)

**Decision:** `ProductTypeApi` permanece no monólito; `ProductTypeService` injection morta ignorada.
**Reason:** Subagente confirmou zero uso em `MerchantStoreServiceImpl`; FK catalog não bloqueia store CRUD.
**Trade-off:** `PRODUCT_TYPE.MERCHANT_ID` permanece cross-schema.
**Impact:** MCH-06 confirmed out of scope.

### AD-007: GeoZone excluído da Onda 1 (2026-07-04)

**Decision:** Sem API nem service para GeoZone/GeoZoneDescription.
**Reason:** OQ-01 — sem service layer; entidade orphan.
**Trade-off:** `Country.geoZone` inacessível via reference-service.
**Impact:** Resolve blocker B-004.

---

## Active Blockers

### B-001: Facade interfaces passam entidades Language/MerchantStore

**Discovered:** 2026-07-04
**Impact:** 20+ interfaces em `sm-shop-model`; `AbstractDataPopulator` hard-wired; impede contratos HTTP limpos.
**Workaround:** Onda 1 limita refatoração às fronteiras Reference/Tax APIs; callers internos mantêm entidades temporariamente.
**Resolution:** Onda 3 — `LanguageCode` / `MerchantStoreId` (story B do backlog mestre).

### B-002: ReferencesApi expõe entidades Language e Currency

**Discovered:** 2026-07-04
**Impact:** Viola critério de sucesso da Onda 1; `ReadableLanguage` existe mas não está wired.
**Workaround:** Nenhum em produção extraída.
**Resolution:** REF-04, REF-05 — design em `design.md`; implementar em Execute.

### B-003: PersistableTaxRateMapper depende de reference services

**Discovered:** 2026-07-04
**Impact:** tax-service precisa resolver country/zone/language codes na criação de tax rates.
**Workaround:** Deploy co-localizado; chamada HTTP reference-service desde dia 1 da extração tax.
**Resolution:** TAX-06 — `ReferenceServiceClient` em design.md ✅

### B-004: GeoZone sem service layer — RESOLVED

**Resolution:** AD-007 — excluído do escopo Onda 1.

---

## Lessons Learned

### L-001: Acoplamento real difere de hipóteses iniciais (2026-07-04)

**Context:** Análise coupling-analysis substituiu hipóteses da Fase 3 original.
**Problem:** Integration Hub e Customer-before-Catalog estavam mal priorizados.
**Solution:** Reordenar ondas por scores 3D; Order por último (9/10).
**Prevents:** Extrações prematuras de catalog/order/integration.

### L-002: Contracts lib ≠ mappers (2026-07-04)

**Context:** OQ-04 recomendava `shopizer-api-contracts` mas mappers dependem de JPA.
**Problem:** Colocar mappers no JAR de contratos puxaria `sm-core-model`.
**Solution:** Contracts = DTOs only; mappers/populators dentro de cada serviço.
**Prevents:** Acoplamento MODEL no artefato publicável.

---

## Quick Tasks Completed

| #   | Description                              | Date       | Commit | Status  |
| --- | ---------------------------------------- | ---------- | ------ | ------- |
| —   | Plano mestre de decomposição documentado | 2026-07-04 | —      | ✅ Done |
| —   | Inicialização `.specs/` + Specify Onda 1 | 2026-07-04 | —      | ✅ Done |
| —   | Design Onda 1 + OQ-01..06 resolvidas   | 2026-07-04 | —      | ✅ Done |
| —   | Tasks Onda 1 (30 tarefas T1–T30)        | 2026-07-04 | —      | ✅ Done |
| —   | Specify Onda 2 (content/search/merchant) | 2026-07-04 | —      | ✅ Done |
| —   | Design Onda 2 + OQ-01..06 confirmadas   | 2026-07-04 | —      | ✅ Done |
| —   | Tasks Onda 2 (54 tarefas T1–T54)        | 2026-07-04 | —      | ✅ Done |

---

## Deferred Ideas

- [ ] Expor hierarquia piggyback/parent em TaxRate via API — Captured during: Specify Onda 1
- [ ] API REST para `TaxConfiguration` (hoje só via `MerchantConfiguration`) — Captured during: Specify Onda 1
- [ ] `ReadableTaxRateFull` multi-description — existe mas não usado — Captured during: Specify Onda 1
- [ ] Corrigir query `TaxRateRepository` com parâmetro taxClass não usado — Captured during: Specify Onda 1
- [ ] OpenFeign / service discovery — Captured during: Design Onda 1 (Onda 2+)

---

## Todos

- [x] Onda 1 gate `./mvnw clean install` verde (2026-07-26)
- [ ] Iniciar Execute Onda 2 — task_01
