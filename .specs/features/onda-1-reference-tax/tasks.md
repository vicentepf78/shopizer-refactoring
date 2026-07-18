# Onda 1 — Reference + Tax Tasks

**Design:** `.specs/features/onda-1-reference-tax/design.md`
**Spec:** `.specs/features/onda-1-reference-tax/spec.md`
**Status:** Approved — pronto para Execute
**Testing:** `.specs/codebase/TESTING.md`

---

## Execution Plan

### Phase 1: Contracts Foundation (Sequential → Parallel)

```
T1 ──→ T2 ──┬──→ T3 [P]
            └──→ T4 [P]
T3,T4 ──→ T5 ──→ T6
```

### Phase 2: Core Extraction (Parallel after T6)

```
T6 ──┬──→ T7 ──→ T8 ──→ T9
     └──→ T10 ──→ T11 ──→ T12
```

### Phase 3: Microservices (Parallel tracks)

```
T9 ──→ T13 ──→ T14 ──→ T15 ──→ T16
T12 ──→ T17 ──→ T18 ──→ T19 ──→ T20 ──→ T21
```

### Phase 4: Strangler (Sequential)

```
T16,T21 ──→ T22 ──→ T23 ──→ T24 ──→ T25 ──→ T26
```

### Phase 5: Contracts, Deploy & Gate (Sequential tail)

```
T26 ──→ T27 ──→ T28 ──→ T29 ──→ T30 ──→ T31 ──→ T32
```

---

## Parallel Execution Map

```
Phase 1:
  T1 → T2 → (T3 ∥ T4) → T5 → T6

Phase 2:
  T6 → (T7→T8→T9 ∥ T10→T11→T12)

Phase 3:
  Track A: T9 → T13 → T14 → T15 → T16
  Track B: T12 → T17 → T18 → T19 → T20 → T21

Phase 4:
  T22 → T23 → T24 → T25 → T26

Phase 5:
  T27 → T28 → T29 → T30 → T31 → T32
```

**Subagent rule:** Tasks `[P]` na mesma fase → um subagent por task, em paralelo. Tasks sem `[P]` → subagent sequencial. Cada subagent recebe: definição da task, `CONVENTIONS`/`design.md` seção relevante, `TESTING.md` gate command.

---

## Task Breakdown

### T1: Scaffold `shopizer-api-contracts` Maven module

**What:** Criar módulo `shopizer-api-contracts` no root reactor com `pom.xml` (parent shopizer 3.2.5, deps: jackson-annotations, validation-api).
**Where:** `/shopizer-api-contracts/pom.xml`, `/pom.xml` (add module)
**Depends on:** None
**Reuses:** `sm-core-modules/pom.xml` como template de jar fino
**Requirement:** OQ-04, REF-05

**Done when:**
- [ ] Módulo listado no root `<modules>` antes dos services
- [ ] `./mvnw validate -pl shopizer-api-contracts` passa
- [ ] Zero dependência de `sm-core-model`

**Tests:** none
**Gate:** `./mvnw validate -pl shopizer-api-contracts`

**Commit:** `feat(contracts): scaffold shopizer-api-contracts module`

---

### T2: Common DTOs em contracts

**What:** Migrar `Entity`, `ShopEntity`, `ReadableList`, `ReadableEntityList`, `EntityExists` para `com.salesmanager.contracts.common`.
**Where:** `shopizer-api-contracts/src/main/java/com/salesmanager/contracts/common/`
**Depends on:** T1
**Reuses:** `sm-shop-model/.../model/entity/*.java`
**Requirement:** OQ-04

**Done when:**
- [ ] 5 classes compilam no módulo contracts
- [ ] `./mvnw compile -pl shopizer-api-contracts` passa

**Tests:** none
**Gate:** `./mvnw compile -pl shopizer-api-contracts`

**Commit:** `feat(contracts): add common DTO wrappers`

---

### T3: Reference DTOs + `ReadableCurrency` [P]

**What:** Migrar DTOs reference + enums; **criar** `ReadableCurrency` (id, code, name, symbol, supported).
**Where:** `shopizer-api-contracts/.../contracts/reference/`
**Depends on:** T2
**Reuses:** `sm-shop-model/.../model/references/*`; spec REF-04, REF-05
**Requirement:** REF-04, REF-05, REF-06

**Done when:**
- [ ] `ReadableCurrency` criado e serializável Jackson
- [ ] 13 tipos reference (+ enums) compilam
- [ ] Nenhum import `com.salesmanager.core.model`

**Tests:** none
**Gate:** `./mvnw compile -pl shopizer-api-contracts`

**Commit:** `feat(contracts): add reference DTOs and ReadableCurrency`

---

### T4: Tax DTOs em contracts [P]

**What:** Migrar 9 classes tax + `NamedEntity` slim para `com.salesmanager.contracts.tax` e `contracts/catalog`.
**Where:** `shopizer-api-contracts/.../contracts/tax/`, `.../catalog/NamedEntity.java`
**Depends on:** T2
**Reuses:** `sm-shop-model/.../model/tax/*`
**Requirement:** OQ-04, TAX-02

**Done when:**
- [ ] Tax DTOs compilam sem JPA imports
- [ ] `NamedEntity` disponível para tax descriptions

**Tests:** none
**Gate:** `./mvnw compile -pl shopizer-api-contracts`

**Commit:** `feat(contracts): add tax DTOs`

---

### T5: Client interfaces em contracts

**What:** Criar `ReferenceServiceClient` e `TaxServiceClient` interfaces (métodos espelhando facades) em `contracts/client`.
**Where:** `shopizer-api-contracts/.../contracts/client/`
**Depends on:** T3, T4
**Reuses:** design.md § Components 2–3
**Requirement:** TAX-06, REF-09, TAX-10

**Done when:**
- [ ] Interfaces usam apenas tipos contracts (sem `MerchantStore` JPA — usar `storeCode`/`langCode` strings nos clients)
- [ ] Compilação OK

**Tests:** none
**Gate:** `./mvnw compile -pl shopizer-api-contracts`

**Commit:** `feat(contracts): add Reference and Tax client interfaces`

---

### T6: `sm-shop-model` depende de contracts

**What:** Adicionar dep `shopizer-api-contracts` em `sm-shop-model`; deprecar DTOs duplicados via re-export ou `@Deprecated` aliases.
**Where:** `sm-shop-model/pom.xml`, packages `com.salesmanager.shop.model.*`
**Depends on:** T5
**Reuses:** Padrão transitivo Maven
**Requirement:** STR-04

**Done when:**
- [ ] `sm-shop-model` compila com dep contracts
- [ ] `./mvnw compile -pl sm-shop-model` passa
- [ ] Monólito existente compila (`./mvnw compile -pl sm-shop -am`)

**Tests:** none
**Gate:** `./mvnw compile -pl sm-shop -am`

**Commit:** `refactor(shop-model): depend on shopizer-api-contracts`

---

### T7: Scaffold `sm-reference-core` + mover repositories

**What:** Novo módulo; mover 4 `*Repository.java` de reference; configurar dep `sm-core-model`.
**Where:** `sm-reference-core/`, paths em design inventory
**Depends on:** T6
**Reuses:** `sm-core/.../repositories/reference/**`
**Requirement:** REF-07

**Done when:**
- [ ] 4 repositories compilam em `sm-reference-core`
- [ ] `sm-core` ainda compila (repos duplicados temporariamente OU removidos com re-export — preferir mover e atualizar sm-core dep)

**Tests:** integration
**Gate:** `./mvnw test -pl sm-reference-core` (criar `CountryRepositoryTest` mínimo `@DataJpaTest`)

**Commit:** `feat(reference-core): extract reference repositories`

---

### T8: Mover reference CRUD services para `sm-reference-core`

**What:** Mover 8 arquivos (Country/Zone/Language/Currency Service+Impl); **não** mover init/loader.
**Where:** `sm-reference-core/.../services/reference/{country,zone,language,currency}/`
**Depends on:** T7
**Reuses:** Lista subagent inventory; excluir `init/`, `loader/`
**Requirement:** REF-01, REF-07

**Done when:**
- [ ] Services compilam com dep em `SalesManagerEntityServiceImpl` (extrair slice ou dep `sm-core` thin)
- [ ] `LanguageServiceImpl.toLocale(Language, String countryCode)` overload adicionado (REF-08)
- [ ] Gate: `./mvnw test -pl sm-reference-core`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-reference-core`

**Commit:** `feat(reference-core): extract reference CRUD services`

---

### T9: Atualizar `sm-core` para depender de `sm-reference-core`

**What:** Remover classes movidas de `sm-core`; adicionar dep `sm-reference-core`; manter init/loader em `sm-core`.
**Where:** `sm-core/pom.xml`, delete moved files
**Depends on:** T8
**Reuses:** —
**Requirement:** REF-01

**Done when:**
- [ ] `./mvnw compile -pl sm-core` passa
- [ ] `./mvnw test -pl sm-core` passa (testes existentes verdes)
- [ ] Init/loader ainda em `sm-core`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-core`

**Commit:** `refactor(sm-core): delegate reference CRUD to sm-reference-core`

---

### T10: Scaffold `sm-tax-core` + mover tax repositories [P]

**What:** Novo módulo; mover `TaxClassRepository`, `TaxRateRepository`.
**Where:** `sm-tax-core/`
**Depends on:** T6
**Reuses:** `sm-core/.../repositories/tax/**`
**Requirement:** TAX-01

**Done when:**
- [ ] Repos compilam
- [ ] `@DataJpaTest` smoke passa

**Tests:** integration
**Gate:** `./mvnw test -pl sm-tax-core`

**Commit:** `feat(tax-core): extract tax repositories`

---

### T11: Mover TaxClass/TaxRate services para `sm-tax-core`

**What:** Mover 4 arquivos service (excluir `TaxService*`).
**Where:** `sm-tax-core/.../services/tax/`
**Depends on:** T10
**Reuses:** inventory list
**Requirement:** TAX-01, TAX-03, TAX-04

**Done when:**
- [ ] `TaxService`/`TaxServiceImpl` permanecem em `sm-core`
- [ ] `./mvnw test -pl sm-tax-core` passa

**Tests:** integration
**Gate:** `./mvnw test -pl sm-tax-core`

**Commit:** `feat(tax-core): extract tax CRUD services`

---

### T12: TaxClass DELETE 409 guard + `existsTaxRate` fix

**What:** Adicionar `ProductRepository.countByTaxClassId`; lançar `TaxClassInUseException` → 409; corrigir `existsTaxRate` sem throw (TAX-09).
**Where:** `sm-tax-core/.../TaxClassServiceImpl.java`; facade será movida em T20 — aplicar fix na impl que vai para tax-service
**Depends on:** T11
**Reuses:** design OQ-03, TAX-09
**Requirement:** OQ-03, TAX-09

**Done when:**
- [ ] Unit test: delete com products → exception
- [ ] Unit test: existsTaxRate false sem throw
- [ ] `./mvnw test -pl sm-tax-core -Dtest=TaxClassServiceImplTest`

**Tests:** unit
**Gate:** `./mvnw test -pl sm-tax-core -Dtest=TaxClassServiceImplTest`

**Commit:** `fix(tax-core): 409 on tax class in use and existsTaxRate semantics`

---

### T13: Scaffold `reference-service` Boot application

**What:** Módulo executable jar, `ReferenceServiceApplication`, `application.properties` (port 8081), datasource H2/MySQL, actuator.
**Where:** `reference-service/`
**Depends on:** T9
**Reuses:** `sm-shop` packaging pattern
**Requirement:** REF-01, STR-05

**Done when:**
- [ ] `./mvnw package -pl reference-service` produz jar
- [ ] `GET /actuator/health` → UP (contexto mínimo)

**Tests:** integration
**Gate:** `./mvnw test -pl reference-service -Dtest=ReferenceServiceApplicationTest`

**Commit:** `feat(reference-service): scaffold Spring Boot application`

---

### T14: Portar facades + populators para `reference-service`

**What:** Mover 8 facades + 2 populators; wire `LanguageFacade`/`CurrencyFacade` → DTOs; dep `sm-reference-core`.
**Where:** `reference-service/src/main/java/...`
**Depends on:** T13
**Reuses:** inventory reference-service files; `AbstractDataPopulator`
**Requirement:** REF-03, REF-04, REF-05, REF-06

**Done when:**
- [ ] Facades retornam contracts DTOs (não entidades)
- [ ] Populators funcionam com services injetados

**Tests:** unit
**Gate:** `./mvnw test -pl reference-service -Dtest=*FacadeTest`

**Commit:** `feat(reference-service): port facades and populators`

---

### T15: REST controllers reference-service (5 endpoints)

**What:** `ReferencesController` espelhando paths: `/country`, `/zones`, `/languages`, `/currency`, `/measures`.
**Where:** `reference-service/.../api/v1/ReferencesController.java`
**Depends on:** T14
**Reuses:** `ReferencesApi` behavior; OQ-02 frozen (`/zones` → 200+[])
**Requirement:** REF-02, REF-10, OQ-02

**Done when:**
- [ ] 5 endpoints respondem JSON DTO
- [ ] `/zones?code=XX` retorna `[]` com 200
- [ ] `./mvnw test -pl reference-service -Dtest=ReferenceApiIntegrationTest`

**Tests:** integration
**Gate:** `./mvnw test -pl reference-service -Dtest=ReferenceApiIntegrationTest`

**Commit:** `feat(reference-service): expose reference REST endpoints`

---

### T16: Atualizar `sm-core` para depender de `sm-tax-core`

**What:** Remover tax CRUD movido; manter `TaxService*`; dep `sm-tax-core`.
**Where:** `sm-core/pom.xml`
**Depends on:** T12
**Reuses:** T9 pattern
**Requirement:** TAX-07

**Done when:**
- [ ] `TaxServiceImpl` permanece em sm-core
- [ ] `./mvnw test -pl sm-core` verde
- [ ] `./mvnw test -pl sm-shop -Dtest=TaxRateIntegrationTest` verde (monólito in-process)

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=TaxRateIntegrationTest`

**Commit:** `refactor(sm-core): delegate tax CRUD to sm-tax-core`

---

### T17: Scaffold `tax-service` Boot + JWT security chain

**What:** Boot app port 8082; copiar/adaptar JWT filter, `TaxSecurityConfig`, argument resolvers; shared DB.
**Where:** `tax-service/`
**Depends on:** T16
**Reuses:** security inventory list; AD-006
**Requirement:** TAX-01, TAX-05, AD-006

**Done when:**
- [ ] `/api/v1/private/**` exige JWT
- [ ] Request sem token → 401
- [ ] `./mvnw test -pl tax-service -Dtest=TaxSecurityIntegrationTest`

**Tests:** integration
**Gate:** `./mvnw test -pl tax-service -Dtest=TaxSecurityIntegrationTest`

**Commit:** `feat(tax-service): scaffold app with JWT security`

---

### T18: `ReferenceServiceClient` RestTemplate em tax-service

**What:** Implementar client HTTP para resolver country/zone/language codes; config `wave1.reference-service.base-url`.
**Where:** `tax-service/.../client/ReferenceServiceClientImpl.java`
**Depends on:** T15, T17
**Reuses:** AD-005 RestTemplate
**Requirement:** TAX-06

**Done when:**
- [ ] Client resolve country/zone por code
- [ ] Test com MockRestServiceServer ou WireMock
- [ ] `./mvnw test -pl tax-service -Dtest=ReferenceServiceClientTest`

**Tests:** unit
**Gate:** `./mvnw test -pl tax-service -Dtest=ReferenceServiceClientTest`

**Commit:** `feat(tax-service): add ReferenceServiceClient HTTP adapter`

---

### T19: Portar `TaxFacadeImpl` + 4 mappers para tax-service

**What:** Mover facade + mappers; `PersistableTaxRateMapper` usa `ReferenceServiceClient` (não CountryService).
**Where:** `tax-service/.../facade/`, `.../mapper/tax/`
**Depends on:** T18
**Reuses:** OQ-05
**Requirement:** TAX-03, TAX-04, OQ-05

**Done when:**
- [ ] Mapper não importa reference services in-process
- [ ] CRUD facade funciona em testes

**Tests:** unit
**Gate:** `./mvnw test -pl tax-service -Dtest=*TaxFacade*Test`

**Commit:** `feat(tax-service): port TaxFacadeImpl and mappers`

---

### T20: REST controllers tax-service (private tax APIs)

**What:** Espelhar `TaxClassApi` + `TaxRatesApi` paths sob `/api/v1/private/tax/*`.
**Where:** `tax-service/.../api/v1/tax/`
**Depends on:** T19
**Reuses:** `TaxClassApi`, `TaxRatesApi` signatures
**Requirement:** TAX-02

**Done when:**
- [ ] Paridade de paths com monólito
- [ ] `./mvnw test -pl tax-service -Dtest=TaxApiIntegrationTest` (CRUD class+rate)

**Tests:** integration
**Gate:** `./mvnw test -pl tax-service -Dtest=TaxApiIntegrationTest`

**Commit:** `feat(tax-service): expose private tax REST endpoints`

---

### T21: `RestTemplate` config + wave1 properties no monólito

**What:** `@Bean RestTemplate` com timeout; properties `wave1.*`; profile `strangler` vs `monolith`.
**Where:** `sm-shop/.../strangler/config/Wave1ClientConfig.java`, `application-strangler.properties`
**Depends on:** T6
**Reuses:** design.md configuration
**Requirement:** STR-01, OQ-06

**Done when:**
- [ ] Properties bind corretamente
- [ ] `./mvnw compile -pl sm-shop` passa

**Tests:** unit
**Gate:** `./mvnw test -pl sm-shop -Dtest=Wave1ClientConfigTest`

**Commit:** `feat(shop): add wave1 RestTemplate and strangler properties`

---

### T22: Reference facade HTTP adapters (4)

**What:** `CountryFacadeHttpAdapter`, `ZoneFacadeHttpAdapter`, `LanguageFacadeHttpAdapter`, `CurrencyFacadeHttpAdapter` com `@ConditionalOnProperty`.
**Where:** `sm-shop/.../strangler/reference/`
**Depends on:** T15, T21
**Reuses:** `ReferenceServiceClient`; propagar `X-Correlation-Id`
**Requirement:** REF-09, STR-01

**Done when:**
- [ ] Adapters delegam HTTP quando `wave1.strangler.enabled=true`
- [ ] `./mvnw test -pl sm-shop -Dtest=ReferenceFacadeHttpAdapterTest`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=ReferenceFacadeHttpAdapterTest`

**Commit:** `feat(shop): add reference facade HTTP strangler adapters`

---

### T23: `TaxFacadeHttpAdapter`

**What:** Adapter HTTP para `TaxFacade`; repassa JWT + store/lang query params.
**Where:** `sm-shop/.../strangler/tax/TaxFacadeHttpAdapter.java`
**Depends on:** T20, T21
**Reuses:** `TaxServiceClient`
**Requirement:** TAX-10, STR-01

**Done when:**
- [ ] In-process `TaxFacadeImpl` desativado quando strangler on
- [ ] `./mvnw test -pl sm-shop -Dtest=TaxFacadeHttpAdapterTest`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=TaxFacadeHttpAdapterTest`

**Commit:** `feat(shop): add tax facade HTTP strangler adapter`

---

### T24: Conditional beans — in-process vs HTTP

**What:** `@ConditionalOnProperty` em facades legadas vs adapters; profile `monolith` default, `strangler` prod.
**Where:** Facades existentes + adapters T22/T23
**Depends on:** T22, T23
**Reuses:** design § Strangler
**Requirement:** STR-01

**Done when:**
- [ ] Apenas um bean por facade interface no contexto
- [ ] `./mvnw test -pl sm-shop` passa em ambos profiles

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dspring.profiles.active=monolith` e `=strangler`

**Commit:** `feat(shop): conditional strangler facade wiring`

---

### T25: Pact provider — reference-service

**What:** Pact JVM provider tests para 5 endpoints reference.
**Where:** `reference-service/src/test/java/.../contract/`
**Depends on:** T15
**Reuses:** STR-02
**Requirement:** STR-02

**Done when:**
- [ ] Pact files gerados em `target/pacts`
- [ ] `./mvnw test -pl reference-service -Dtest=ReferenceProviderPactTest`

**Tests:** integration
**Gate:** `./mvnw test -pl reference-service -Dtest=ReferenceProviderPactTest`

**Commit:** `test(reference-service): add Pact provider verification`

---

### T26: Pact provider — tax-service

**What:** Pact provider tests para tax class + rate endpoints.
**Where:** `tax-service/src/test/java/.../contract/`
**Depends on:** T20
**Reuses:** TAX-08
**Requirement:** TAX-08, STR-02

**Done when:**
- [ ] Pact files gerados
- [ ] `./mvnw test -pl tax-service -Dtest=TaxProviderPactTest`

**Tests:** integration
**Gate:** `./mvnw test -pl tax-service -Dtest=TaxProviderPactTest`

**Commit:** `test(tax-service): add Pact provider verification`

---

### T27: Pact consumer — monólito Strangler

**What:** Consumer tests verificando contratos reference + tax contra pacts publicados.
**Where:** `sm-shop/src/test/java/.../contract/`
**Depends on:** T25, T26
**Reuses:** STR-02
**Requirement:** STR-02

**Done when:**
- [ ] Consumer tests passam com pacts locais
- [ ] `./mvnw test -pl sm-shop -Dtest=Wave1ConsumerPactTest`

**Tests:** integration
**Gate:** `./mvnw test -pl sm-shop -Dtest=Wave1ConsumerPactTest`

**Commit:** `test(shop): add Wave 1 Pact consumer tests`

---

### T28: Docker Compose Wave 1 + correlation ID

**What:** `docker-compose-wave1.yml` (mysql, reference, tax, shop); actuator health; `X-Correlation-Id` filter nos 3 apps.
**Where:** `/docker-compose-wave1.yml`, filters em cada service
**Depends on:** T24
**Reuses:** design deployment topology
**Requirement:** STR-05

**Done when:**
- [ ] `docker compose -f docker-compose-wave1.yml config` válido
- [ ] Smoke manual documentado em task verify
- [ ] Health indicators: DB + tax→reference

**Tests:** none
**Gate:** `docker compose -f docker-compose-wave1.yml config`

**Commit:** `chore(deploy): add wave1 docker compose and correlation id`

---

### T29: Actuator health custom indicators

**What:** `ReferenceHealthIndicator` (DB); `TaxHealthIndicator` (DB + reference HTTP).
**Where:** `reference-service/`, `tax-service/`
**Depends on:** T13, T17
**Reuses:** STR-05
**Requirement:** STR-05

**Done when:**
- [ ] `/actuator/health` mostra componentes custom
- [ ] `./mvnw test -pl reference-service,tax-service -Dtest=*HealthIndicatorTest`

**Tests:** unit
**Gate:** `./mvnw test -pl reference-service -Dtest=HealthIndicatorTest`

**Commit:** `feat(services): custom actuator health indicators`

---

### T30: Build gate — full reactor

**What:** Verificar build completo monólito + novos módulos; atualizar traceability spec.
**Where:** root reactor
**Depends on:** T24, T27, T28, T29
**Reuses:** —
**Requirement:** All REF/TAX/STR

**Done when:**
- [ ] `./mvnw clean install` passa
- [ ] `TaxRateIntegrationTest` verde (profile monolith)
- [ ] `Wave1ConsumerPactTest` verde
- [ ] spec.md requirements → status Verified

**Tests:** integration
**Gate:** `./mvnw clean install`

**Commit:** `chore(wave1): complete onda 1 build gate`

---

## Task Granularity Check

| Task | Scope | Status |
| ---- | ----- | ------ |
| T1: contracts pom | 1 module | ✅ Granular |
| T2: common DTOs | 1 package | ✅ Granular |
| T3: reference DTOs | 1 package | ✅ Granular |
| T4: tax DTOs | 1 package | ✅ Granular |
| T5: client interfaces | 2 interfaces | ✅ Granular |
| T6: shop-model dep | 1 pom change | ✅ Granular |
| T7: reference repos | 4 files | ✅ Granular |
| T8: reference services | 8 files | ✅ Granular |
| T9: sm-core reference wire | 1 module integration | ✅ Granular |
| T10: tax repos | 2 files | ✅ Granular |
| T11: tax services | 4 files | ✅ Granular |
| T12: tax fixes | 2 behaviors | ✅ Granular |
| T13: reference-service boot | 1 app scaffold | ✅ Granular |
| T14: reference facades | 10 files | ⚠️ OK — coeso (facade layer) |
| T15: reference REST | 5 endpoints | ✅ Granular |
| T16: sm-core tax wire | 1 module | ✅ Granular |
| T17: tax-service security | 1 security chain | ✅ Granular |
| T18: ReferenceServiceClient | 1 client | ✅ Granular |
| T19: tax facade+mappers | 1 facade layer | ⚠️ OK — coeso |
| T20: tax REST | 2 API classes | ✅ Granular |
| T21: RestTemplate config | 1 config | ✅ Granular |
| T22: reference adapters | 4 adapters | ✅ Granular |
| T23: tax adapter | 1 adapter | ✅ Granular |
| T24: conditional wiring | config only | ✅ Granular |
| T25–T27: Pact | 1 concern each | ✅ Granular |
| T28: docker compose | 1 file | ✅ Granular |
| T29: health indicators | 2 indicators | ✅ Granular |
| T30: build gate | verification | ✅ Granular |

---

## Diagram-Definition Cross-Check

| Task | Depends On (body) | Diagram Shows | Status |
| ---- | ----------------- | ------------- | ------ |
| T1 | None | Phase1 start | ✅ |
| T2 | T1 | T1→T2 | ✅ |
| T3 | T2 | T2→T3 | ✅ |
| T4 | T2 | T2→T4 | ✅ |
| T5 | T3,T4 | T3,T4→T5 | ✅ |
| T6 | T5 | T5→T6 | ✅ |
| T7 | T6 | T6→T7 | ✅ |
| T8 | T7 | T7→T8 | ✅ |
| T9 | T8 | T8→T9 | ✅ |
| T10 | T6 | T6→T10 | ✅ |
| T11 | T10 | T10→T11 | ✅ |
| T12 | T11 | T11→T12 | ✅ |
| T13 | T9 | T9→T13 | ✅ |
| T14 | T13 | T13→T14 | ✅ |
| T15 | T14 | T14→T15 | ✅ |
| T16 | T12 | T12→T16 | ✅ |
| T17 | T16 | T16→T17 | ✅ |
| T18 | T15,T17 | T15+T17→T18 | ✅ |
| T19 | T18 | T18→T19 | ✅ |
| T20 | T19 | T19→T20 | ✅ |
| T21 | T6 | T6→T21 (Phase4) | ✅ |
| T22 | T15,T21 | T15,T21→T22 | ✅ |
| T23 | T20,T21 | T20,T21→T23 | ✅ |
| T24 | T22,T23 | T22,T23→T24 | ✅ |
| T25 | T15 | T15→T25 | ✅ |
| T26 | T20 | T20→T26 | ✅ |
| T27 | T25,T26 | T25,T26→T27 | ✅ |
| T28 | T24 | T24→T28 | ✅ |
| T29 | T13,T17 | T13,T17→T29 | ✅ |
| T30 | T24,T27,T28,T29 | final | ✅ |

---

## Test Co-location Validation

| Task | Code Layer | Matrix Requires | Task Says | Status |
| ---- | ---------- | --------------- | --------- | ------ |
| T1 | Maven pom | none | none | ✅ |
| T2–T6 | DTOs/pom | none | none | ✅ |
| T7 | JPA repository | integration | integration | ✅ |
| T8 | domain service | integration | integration | ✅ |
| T9 | module wiring | integration | integration | ✅ |
| T10 | JPA repository | integration | integration | ✅ |
| T11 | domain service | integration | integration | ✅ |
| T12 | service logic | unit | unit | ✅ |
| T13 | Boot app | integration | integration | ✅ |
| T14 | facade | unit | unit | ✅ |
| T15 | REST controller | integration | integration | ✅ |
| T16 | module wiring | integration | integration | ✅ |
| T17 | security | integration | integration | ✅ |
| T18 | HTTP client | unit | unit | ✅ |
| T19 | facade/mapper | unit | unit | ✅ |
| T20 | REST controller | integration | integration | ✅ |
| T21 | config bean | unit | unit | ✅ |
| T22 | strangler adapter | integration | integration | ✅ |
| T23 | strangler adapter | integration | integration | ✅ |
| T24 | conditional config | integration | integration | ✅ |
| T25–T27 | Pact | integration | integration | ✅ |
| T28 | docker compose | none | none | ✅ |
| T29 | health indicator | unit | unit | ✅ |
| T30 | full build | integration | integration | ✅ |

---

## Requirement Traceability → Tasks

| Requirement | Task(s) |
| ----------- | ------- |
| REF-01 | T13, T15 |
| REF-02 | T15 |
| REF-03 | T14 |
| REF-04 | T3, T14, T15 |
| REF-05 | T3, T14, T15 |
| REF-06 | T3, T14, T15 |
| REF-07 | T7, T8 |
| REF-08 | T8 |
| REF-09 | T22 |
| REF-10 | T15 |
| TAX-01 | T17, T20 |
| TAX-02 | T20 |
| TAX-03 | T11, T19, T20 |
| TAX-04 | T11, T19, T20 |
| TAX-05 | T17 |
| TAX-06 | T18, T19 |
| TAX-07 | T16 (TaxService stays) |
| TAX-08 | T26, T27 |
| TAX-09 | T12 |
| TAX-10 | T23 |
| STR-01 | T21, T24 |
| STR-02 | T25, T26, T27 |
| STR-03 | T7–T20 (shared DB config) |
| STR-04 | T6, T15, T20 |
| STR-05 | T28, T29 |
| OQ-01 | T15 (no GeoZone) |
| OQ-02 | T15 |
| OQ-03 | T12 |
| OQ-04 | T1–T6 |
| OQ-05 | T19 |
| OQ-06 | T21 |

**Coverage:** 22 requirements + 6 OQ → 30 tasks; 0 unmapped ✅

---

## Execute — Subagent Delegation Plan

| Phase | Tasks | Subagents | Parallel? |
| ----- | ----- | --------- | ----------- |
| 1 | T1 | 1 | No |
| 1 | T2 | 1 | No |
| 1 | T3, T4 | 2 | Yes `[P]` |
| 1 | T5, T6 | 1 each | No |
| 2 | T7→T9 vs T10→T12 | 2 tracks | Tracks parallel |
| 3 | T13→T15 vs T16→T20 | 2 tracks | Tracks parallel |
| 4 | T21→T24 | 1 each | No (integration) |
| 5 | T25, T26 | 2 | Yes `[P]` |
| 5 | T27→T30 | sequential | No |

**Cada subagent retorna:** Status, files changed, gate result, SPEC_DEVIATION se houver.

---

## Próximo passo

**Execute** — iniciar T1. Não implementar até confirmação do usuário para começar execução (ou iniciar T1 se aprovação implícita).

**Ferramentas sugeridas por fase:**
- T1–T6: filesystem, Maven
- T7–T12: filesystem + Spring Data JPA docs (spring-docs MCP)
- T13–T20: filesystem + spring-docs MCP
- T21–T24: filesystem
- T25–T27: Pact JVM docs + Maven
- T28–T30: Docker + Maven
