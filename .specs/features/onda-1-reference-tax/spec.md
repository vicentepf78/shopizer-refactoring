# Onda 1 — Reference + Tax Specification

**Feature ID:** `onda-1-reference-tax`
**Phase:** Tasks (aprovadas — pronto para Execute)
**Complexity:** Large
**Source:** [MIGRATION-MASTER-PLAN.md](../../docs/decomposition/MIGRATION-MASTER-PLAN.md) § Onda 1
**Exploração:** Subagentes Reference, Tax e Cross-domain consumers (2026-07-04)

---

## Problem Statement

O monólito Shopizer concentra dados de referência (país, zona, idioma, moeda) e administração fiscal (classes e alíquotas) no mesmo runtime que order, catalog e checkout. Com ~94% MODEL coupling nas facades e `LanguageService` injetado em 60+ pontos, qualquer tentativa de deploy independente falha sem fronteiras DTO e padrão Strangler.

A Onda 1 resolve isso extraindo os domínios de **menor risco** (reference 3/10, tax admin 4/10) para validar o padrão de migração antes de atacar order (9/10) e catalog (7/10 afferent). Sem esta onda piloto, as Ondas 2–6 não têm template comprovado de contrato HTTP, testes Pact e adapters no monólito.

---

## Goals

- [ ] `reference-service` e `tax-service` deployáveis como aplicações Spring Boot independentes
- [ ] Monólito consome serviços extraídos via HTTP client (Strangler) nas fronteiras Reference/Tax definidas
- [ ] Zero entidades JPA (`Language`, `Currency`, `TaxClass`, `TaxRate`) nas respostas REST públicas/privadas da Onda 1
- [ ] Testes de contrato (Pact ou equivalente) cobrindo todos os endpoints migrados
- [ ] Paths REST existentes preservados (`/api/v1/country`, `/api/v1/private/tax/*`, etc.) — sem breaking change para clientes

---

## Out of Scope

Explicitamente excluído da Onda 1. Documentado para prevenir scope creep.

| Feature | Reason |
| --------- | ------ |
| `TaxService.calculateTax` e motor de cálculo | Acoplamento order/shipping/customer/system (6/10); permanece in-process |
| `InitializationDatabaseImpl` / bootstrap multi-domínio | Orquestra merchant+catalog+tax+user; split em onda futura (AD-004) |
| Substituição dos ~60 callers de `LanguageService` no monólito | Requer `LanguageCode` sistêmico — Onda 3 |
| Remoção de FKs JPA (`Product`→`TaxClass`, `MerchantStore`→`Language`) | Requer schema evolution — pós Onda 3 |
| `GeoZone` / `GeoZoneDescription` | Sem service layer; excluído OQ-01 / AD-007 |
| Database split (schema/tabelas dedicados por serviço) | AD-003 — DB compartilhado na primeira extração |
| APIs de `TaxConfiguration` | Armazenado em `MerchantConfiguration` (system domain) |
| Hierarquia piggyback/parent em `TaxRate` | Campo existe na entidade; não exposto hoje |
| Quick wins Fase 1 (Mapper/Populator merge) | Paralelo, não bloqueante |

---

## User Stories

### P1: Reference Service — leitura pública de dados geográficos e idiomas ⭐ MVP

**User Story**: Como consumidor da API Shopizer (storefront ou admin), quero consultar países, zonas, idiomas e moedas via endpoints estáveis, para configurar loja e checkout sem acoplamento a entidades JPA internas.

**Why P1**: Reference é o domínio com menor score de extração (3/10), sem ciclos de serviço, e desbloqueia tax-service (resolução country/zone). É o candidato ideal para validar Strangler Fig.

**Acceptance Criteria**:

1. WHEN `GET /api/v1/country?lang={code}` é chamado THEN `reference-service` SHALL retornar `List<ReadableCountry>` com nomes localizados e zonas aninhadas
2. WHEN `GET /api/v1/zones?code={iso}&lang={code}` é chamado THEN `reference-service` SHALL retornar `List<ReadableZone>` para o país informado
3. WHEN `GET /api/v1/languages` é chamado THEN `reference-service` SHALL retornar `List<ReadableLanguage>` — SHALL NOT retornar entidade `Language`
4. WHEN `GET /api/v1/currency` é chamado THEN `reference-service` SHALL retornar `List<ReadableCurrency>` — SHALL NOT retornar entidade `Currency`
5. WHEN `GET /api/v1/measures` é chamado THEN `reference-service` SHALL retornar `SizeReferences` (enums de unidades — sem alteração de contrato)
6. WHEN o monólito recebe requisição nos paths acima THEN o Strangler adapter SHALL rotear para `reference-service` via HTTP mantendo path e query params idênticos
7. WHEN `reference-service` está indisponível THEN o monólito SHALL retornar HTTP 503 com corpo de erro estruturado — SHALL NOT fazer fallback silencioso para JPA in-process

**Independent Test**: Deploy `reference-service` isolado; chamar os 5 endpoints; validar JSON contra schemas DTO sem campos JPA (`auditSection`, `stores`, lazy proxies).

**Componentes fonte (brownfield):**

| Papel | Caminho |
|-------|---------|
| Entidades | `sm-core-model/.../model/reference/` |
| Services | `sm-core/.../services/reference/` |
| API | `sm-shop/.../api/v1/references/ReferencesApi.java` |
| Facades | `sm-shop/.../controller/{country,zone,language,currency}/facade/` |
| DTOs | `sm-shop-model/.../model/references/` |

---

### P1: Tax Service — administração de classes e alíquotas ⭐ MVP

**User Story**: Como administrador de loja autenticado, quero gerenciar tax classes e tax rates via APIs privadas existentes, para configurar tributação sem depender do runtime monolítico.

**Why P1**: Tax admin CRUD é isolado (mapper-based facade, APIs privadas JWT), sem ciclos de serviço. Compartilha onda com reference por dependência de country/zone na criação de rates.

**Acceptance Criteria**:

1. WHEN `POST /api/v1/private/tax/class` com JWT válido e `PersistableTaxClass` THEN `tax-service` SHALL criar tax class escopada à loja e retornar `Entity` com id
2. WHEN `GET /api/v1/private/tax/class` THEN `tax-service` SHALL retornar `ReadableEntityList<ReadableTaxClass>` da loja autenticada
3. WHEN `GET /api/v1/private/tax/class/{code}` THEN `tax-service` SHALL retornar `ReadableTaxClass` ou HTTP 404
4. WHEN `PUT /api/v1/private/tax/class/{id}` ou `DELETE /api/v1/private/tax/class/{id}` THEN `tax-service` SHALL mutar somente se `merchantStore` do recurso corresponde à loja do token
5. WHEN `POST /api/v1/private/tax/rate` com country/zone codes no DTO THEN `tax-service` SHALL resolver códigos via `reference-service` (HTTP) e persistir `TaxRate` com FKs válidas
6. WHEN `GET /api/v1/private/tax/rates` THEN `tax-service` SHALL retornar lista localizada (`ReadableTaxRate` com description no idioma da request)
7. WHEN `GET /api/v1/private/tax/rate/unique?code=` com código inexistente THEN `tax-service` SHALL retornar `EntityExists{exists: false}` — SHALL NOT lançar `ResourceNotFoundException` (fix TAX-09)
8. WHEN requisição sem JWT ou store inválido THEN `tax-service` SHALL retornar HTTP 401/403 — comportamento idêntico ao monólito

**Independent Test**: Deploy `tax-service` + `reference-service`; autenticar admin; CRUD completo de class + rate com country `US` e zone `NY`; verificar isolamento entre lojas.

**Componentes fonte (brownfield):**

| Papel | Caminho |
|-------|---------|
| Services | `sm-core/.../services/tax/TaxClassService`, `TaxRateService` |
| Facade | `sm-shop/.../facade/tax/TaxFacadeImpl.java` |
| APIs | `sm-shop/.../api/v1/tax/TaxClassApi.java`, `TaxRatesApi.java` |
| Mappers | `sm-shop/.../mapper/tax/` |
| DTOs | `sm-shop-model/.../model/tax/` |

**Explicitamente FORA desta story:** `TaxService.calculateTax`, `OrderServiceImpl` integration, `TaxConfiguration`.

---

### P1: Strangler Fig — monólito como BFF com adapters HTTP ⭐ MVP

**User Story**: Como equipe de plataforma, quero que o monólito delegue Reference/Tax para serviços remotos via adapters configuráveis, para validar extração sem reescrever 60+ callers de `LanguageService` de uma vez.

**Why P1**: Sem Strangler, a extração exige big-bang. Onda 1 limita adapters às fronteiras já REST-shaped (ReferencesApi, TaxClassApi, TaxRatesApi, facades dedicadas).

**Acceptance Criteria**:

1. WHEN `shopizer.reference.service.url` (ou equivalente) está configurado THEN `CountryFacadeImpl`, `ZoneFacadeImpl`, `LanguageFacadeImpl`, `CurrencyFacadeImpl` SHALL delegar via HTTP client ao `reference-service`
2. WHEN `shopizer.tax.service.url` está configurado THEN `TaxFacadeImpl` SHALL delegar via HTTP client ao `tax-service`
3. WHEN URLs de serviço NÃO estão configuradas (dev local) THEN adapters SHALL usar implementação in-process legada (feature flag / profile `monolith`)
4. WHEN adapter HTTP recebe timeout (> configurable, default 5s) THEN SHALL propagar erro com correlation id para tracing
5. WHEN deploy de produção da Onda 1 está ativo THEN profile padrão SHALL usar HTTP adapters — in-process legado apenas em dev

**Independent Test**: Subir monólito + 2 serviços; toggle property; comparar respostas byte-a-byte (ou schema-equivalent) entre modos in-process e HTTP.

**Fronteiras Strangler Onda 1 (escopo fechado):**

| Adapter substitui | Permanece in-process |
|-------------------|---------------------|
| `CountryFacadeImpl` → HTTP | `LanguageService` em 31 arquivos catalog/customer/order |
| `ZoneFacadeImpl` → HTTP | `CountryService` em `OrderShippingApi`, `ShippingFacadeImpl` |
| `LanguageFacadeImpl` → HTTP | `InitializationDatabaseImpl` |
| `CurrencyFacadeImpl` → HTTP | `TaxService` / `OrderServiceImpl` |
| `TaxFacadeImpl` → HTTP | `PersistableTaxRateMapper` no monólito (move com tax-service) |

---

### P2: Testes de contrato entre monólito e serviços extraídos

**User Story**: Como desenvolvedor, quero testes de contrato automatizados entre monólito (consumer) e reference/tax-service (providers), para detectar breaking changes antes de deploy.

**Why P2**: Onda 1 define padrão para 5 ondas seguintes; sem contratos, regressões silenciosas em DTOs.

**Acceptance Criteria**:

1. WHEN `reference-service` altera schema de `ReadableCountry` THEN pipeline CI SHALL falhar no consumer pact test
2. WHEN `tax-service` altera `PersistableTaxRate` THEN pipeline CI SHALL falhar no consumer pact test
3. WHEN build da Onda 1 executa gate Full THEN SHALL incluir publicação/verificação Pact para todos os endpoints P1
4. WHEN novo endpoint é adicionado à Onda 1 THEN SHALL ter pact correspondente antes de merge

**Independent Test**: Quebrar campo em provider localmente; confirmar falha no consumer test do monólito.

---

### P2: DTOs e anti-corruption na fronteira Reference

**User Story**: Como arquiteto, quero DTOs completos na API de referência, para eliminar vazamento de modelo JPA identificado no plano mestre.

**Why P2**: Critério de sucesso explícito do plano: "`ReferencesApi` não retorna entidade `Language` diretamente".

**Acceptance Criteria**:

1. WHEN `ReadableLanguage` é serializado THEN SHALL conter apenas `id`, `code`, `sortOrder` — SHALL NOT conter `stores`, `storesDefaultLanguage`, `auditSection`
2. WHEN `ReadableCurrency` é criado (novo DTO) THEN SHALL conter `id`, `code`, `name`, `symbol`, `supported` — mapear de `java.util.Currency` sem expor entidade
3. WHEN facade interfaces de reference são chamadas internamente no monólito THEN assinaturas MAY manter `Language`/`MerchantStore` temporariamente — refatoração sistêmica é Onda 3
4. WHEN `LanguageService.toLocale(Language, MerchantStore)` for exposto via reference-service THEN SHALL aceitar `countryCode: String` como alternativa (REF-08) — desacoplar de entidade `MerchantStore`

**Independent Test**: JSON schema validation nos responses; ausência de campos `@OneToMany` / `AuditSection`.

---

### P3: Observabilidade e health dos serviços extraídos

**User Story**: Como operador, quero health checks e métricas básicas nos serviços extraídos, para monitorar a primeira onda em produção.

**Why P3**: Não bloqueia MVP funcional; necessário antes de produção real.

**Acceptance Criteria**:

1. WHEN `GET /actuator/health` em cada serviço THEN SHALL reportar status DB e dependência HTTP (tax → reference)
2. WHEN requisição é processada THEN logs SHALL incluir `X-Correlation-Id` propagado do monólito
3. WHEN métricas estão habilitadas THEN SHALL expor contadores HTTP server + client latency histograms

**Independent Test**: Hit endpoints; verificar health UP; rastrear correlation id em logs monólito ↔ serviço.

---

## Edge Cases

- WHEN país solicitado não existe (`/zones?code=XX`) THEN system SHALL retornar **HTTP 200 + `[]`** — congelado OQ-02 (indistinguível de país sem zonas)
- WHEN `lang` query param ausente THEN system SHALL resolver idioma default da loja via `StoreFacade` / header — Strangler SHALL preservar `LanguageArgumentResolver` semantics no monólito
- WHEN tax rate referencia country/zone code inválido THEN `tax-service` SHALL retornar HTTP 400 com mensagem de validação — SHALL NOT persistir FK órfã
- WHEN tax class tem produtos associados (`Product.taxClass` FK) e DELETE é solicitado THEN system SHALL retornar HTTP 409 Conflict — OQ-03 (novo guard; hoje delete não verifica FK)
- WHEN `reference-service` cache está stale após update admin de country THEN system SHALL invalidar cache ou TTL ≤ 5min — preservar comportamento `@Cacheable` atual
- WHEN descriptions de country/zone estão ausentes para idioma solicitado THEN system SHALL retornar fallback (primeira description ou código) — documentar em Design; hoje `descriptions.get(0)` é frágil
- WHEN deploy greenfield sem dados seed THEN serviços extraídos SHALL assumir DB já populado pelo monólito (AD-004) — documentar dependência operacional
- WHEN `PersistableTaxRate` inclui múltiplas descriptions THEN SHALL persistir todas (cascade ALL na entidade) — single aggregate save

---

## Requirement Traceability

| Requirement ID | Story | Descrição resumida | Phase | Status |
| -------------- | ----- | ------------------ | ----- | ------ |
| REF-01 | P1: Reference Service | `reference-service` deployável Spring Boot | Design | In Design ✅ |
| REF-02 | P1: Reference Service | Endpoints espelham paths monólito | Design | In Design ✅ |
| REF-03 | P1: Reference Service | `ReadableCountry` / `ReadableZone` preservados | Design | In Design ✅ |
| REF-04 | P1/P2: DTOs | `GET /languages` → `ReadableLanguage` | Design | In Design ✅ |
| REF-05 | P1/P2: DTOs | Criar `ReadableCurrency`; eliminar entidade em `/currency` | Design | In Design ✅ |
| REF-06 | P1: Reference Service | Zero entidade JPA em responses públicas | Design | In Design ✅ |
| REF-07 | P1: Reference Service | Tabelas: COUNTRY, ZONE, LANGUAGE, CURRENCY, *_DESCRIPTION | Design | In Design ✅ |
| REF-08 | P2: DTOs | `toLocale` aceita `countryCode` sem `MerchantStore` | Design | In Design ✅ |
| REF-09 | P1: Strangler | HTTP adapters para 4 facades reference | Design | In Design ✅ |
| REF-10 | P1: Reference Service | Endpoint `/measures` incluído | Design | In Design ✅ |
| TAX-01 | P1: Tax Service | `tax-service` deployável (admin CRUD only) | Design | In Design ✅ |
| TAX-02 | P1: Tax Service | Endpoints espelham `/api/v1/private/tax/*` | Design | In Design ✅ |
| TAX-03 | P1: Tax Service | CRUD TaxClass completo com store scoping | Design | In Design ✅ |
| TAX-04 | P1: Tax Service | CRUD TaxRate com descriptions i18n | Design | In Design ✅ |
| TAX-05 | P1: Tax Service | JWT auth + store resolver preservados | Design | In Design ✅ |
| TAX-06 | P1: Tax Service | Resolução country/zone via reference-service HTTP | Design | In Design ✅ |
| TAX-07 | Out of Scope | `TaxService` permanece no monólito | — | Confirmed |
| TAX-08 | P2: Contract tests | Pact consumer/provider tax endpoints | Tasks | Mapped → T25–T27 |
| TAX-09 | P1: Tax Service | Fix `existsTaxRate` semântica | Tasks | Mapped → T12 |
| TAX-10 | P1: Strangler | HTTP adapter para `TaxFacadeImpl` | Design | In Design ✅ |
| STR-01 | P1: Strangler | Feature flag in-process vs HTTP | Design | In Design ✅ |
| STR-02 | P2: Contract tests | Pact para reference + tax endpoints | Tasks | Mapped → T25–T27 |
| STR-03 | AD-003 | DB compartilhado — sem split de schema | Design | In Design ✅ |
| STR-04 | P1 | Paths REST sem breaking change | Design | In Design ✅ |
| STR-05 | P3 | Actuator health + correlation id | Tasks | Mapped → T28–T29 |

**Coverage:** 22 total, 22 mapped to tasks (T1–T30) ✅

---

## Open Questions — Resolvidas ✅

Decisões em [context.md](./context.md) e [design.md](./design.md).

| ID | Decisão |
|----|---------|
| OQ-01 | GeoZone **excluído** (AD-007) |
| OQ-02 | `/zones?code=XX` → **200 + `[]`** (congelado) |
| OQ-03 | DELETE TaxClass com Products → **409 Conflict** (novo guard) |
| OQ-04 | `shopizer-api-contracts` = **DTOs only**; mappers/populators nos serviços |
| OQ-05 | `PersistableTaxRateMapper` → **tax-service** |
| OQ-06 | **Config URL** (`wave1.*.base-url`); discovery Onda 2+ |

---

## Success Criteria

Como sabemos que a Onda 1 foi bem-sucedida:

- [ ] `reference-service` e `tax-service` passam health check e respondem a todos os endpoints P1 em ambiente de integração
- [ ] Monólito em modo Strangler produz respostas equivalentes ao modo in-process (testes de contrato verdes)
- [ ] Nenhum endpoint migrado retorna entidade JPA no JSON de resposta
- [ ] `TaxService.calculateTax` continua funcionando in-process (regressão zero em order totals)
- [ ] Documentação Design + Tasks aprovada; padrão replicável documentado em `.specs/project/STATE.md` para Onda 2
- [ ] Tempo de resposta p95 dos endpoints reference ≤ 2× baseline monólito (meta operacional P3)

---

## Appendix A — Dependency context (exploração)

### Reference inbound coupling (top consumers)

| Service | ~sm-shop files | Domínios |
|---------|----------------|----------|
| `LanguageService` | 31 | catalog, customer, order, store, user, content |
| `CountryService` | 14 | order, shipping, customer, store, tax mapper |
| `ZoneService` | 13 | order, shipping, customer, store |
| `CurrencyService` | 4 | store, order populators |

### Tax inbound coupling

| Consumer | Service | Notas |
|----------|---------|-------|
| `OrderServiceImpl` | `TaxService` | **Fora Onda 1** |
| `InitializationDatabaseImpl` | `TaxClassService` | Bootstrap — permanece monólito |
| `TaxFacadeImpl` | `TaxClassService`, `TaxRateService` | **Dentro Onda 1** |
| `Product` (JPA) | `TaxClass` FK | Model coupling — DB compartilhado |

### sm-core direct injectors (precisarão HTTP client em ondas futuras, NÃO Onda 1)

- `ShippingServiceImpl` → Country, Language
- `USPSShippingQuote` → Country
- `ODSInvoiceModule` → Country, Zone
- `OrderServiceImpl` → TaxService (permanece in-process)

---

## Appendix B — Critérios do plano mestre (traceability)

| Critério plano mestre | Requirement ID |
|-----------------------|----------------|
| Serviços deployáveis com REST + DTOs | REF-01, TAX-01, REF-03 |
| Monólito consome via HTTP (Strangler) | STR-01, REF-09, TAX-10 |
| ReferencesApi não retorna Language | REF-04, REF-06 |
| Testes de contrato (Pact) | STR-02, TAX-08 |
| Bloqueador: facade interfaces Language/MerchantStore | B-001 (Onda 3); parcial REF-08 |

---

**Próxima fase:** Execute — iniciar T1 (`shopizer-api-contracts` scaffold).

**Tasks:** [tasks.md](./tasks.md) — 30 tarefas atômicas, 5 fases, plano de subagents.
