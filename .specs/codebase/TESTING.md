# Testing Infrastructure — Shopizer (Wave 1 baseline)

**Analyzed:** 2026-07-04
**Scope:** Gates para Onda 1; estende brownfield mínimo.

## Test Frameworks

**Unit/Integration:** JUnit 4 (dominant) + JUnit 5 (partial) via `spring-boot-starter-test` 2.5.12
**E2E:** Nenhum dedicado; integração via `@SpringBootTest` + `TestRestTemplate`
**Coverage:** SpotBugs no build; sem JaCoCo enforcement

## Test Organization

**Location:** `sm-core/src/test/java`, `sm-shop/src/test/java`, `{service}/src/test/java` (novos módulos)
**Naming:** `*Test.java`, `*IntegrationTest.java`
**DB:** H2 in-memory `SALESMANAGER-TEST`; `hibernate.hbm2ddl.auto=create`

## Gate Check Commands

| Gate Level | When to Use | Command |
| ---------- | ----------- | ------- |
| **Quick** | Após task com unit test puro | `./mvnw test -pl {module} -Dtest={TestClass}` |
| **Quick (utils)** | Utility/mapper sem Spring | `./mvnw test -pl sm-core -Dtest=DataUtilsTest` |
| **Full (module)** | Após task com `@SpringBootTest` | `./mvnw test -pl {module}` |
| **Full (tax)** | Regressão tax admin | `./mvnw test -pl sm-shop -Dtest=TaxRateIntegrationTest` |
| **Build** | Fase completa / pre-merge | `./mvnw clean install` |

## Test Coverage Matrix

| Code Layer | Required Test Type | Location Pattern | Run Command |
| ---------- | ------------------ | ---------------- | ----------- |
| Pure utility/mapper (no Spring) | unit | `{module}/src/test/java/**/mapper/*Test.java` | `./mvnw test -pl {module} -Dtest=*Test` |
| sm-core / sm-*-core service | integration | `{module}/src/test/java/**/*Test.java` | `./mvnw test -pl {module}` |
| REST controller (service) | integration | `{service}/src/test/java/**/*IntegrationTest.java` | `./mvnw test -pl {service}` |
| Strangler adapter (sm-shop) | integration | `sm-shop/src/test/java/**/strangler/*Test.java` | `./mvnw test -pl sm-shop -Dtest=*Strangler*Test` |
| Pact contract | integration | `*/src/test/java/**/contract/*Test.java` | `./mvnw test -pl {module} -Dtest=*Pact*` |
| Maven module pom only | none | — | `./mvnw validate -pl {module}` |
| docker-compose | none (manual) | `docker compose config` | smoke only |

## Parallelism Assessment

| Test Type | Parallel-Safe? | Isolation Model | Evidence |
| --------- | -------------- | --------------- | -------- |
| Unit (no Spring) | Yes | No shared state | `DataUtilsTest` |
| Spring Boot integration | **No** | Shared H2 `SALESMANAGER-TEST` | `sm-shop/src/test/resources/database.properties` |
| Full module test suite | **No** | Single JVM fork default Surefire | No `parallel` in pom.xml |
| Pact tests | **No** | Provider state + shared DB | — |

**Rule for `[P]` tasks:** Apenas tasks com `Tests: none` ou `Tests: unit` (pure) podem ser `[P]`.
