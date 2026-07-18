# task_06 memory

## Objective

Deliver Boot executable `reference-service` (:8081) with contract-DTO facades and 5 public REST paths mirrored from monolito `ReferencesApi`. No JWT on P1 routes.

## Decisions

- Language resolution: `lang` → `LanguageService.getByCode`, else `defaultLanguage()`. `store` accepted for path parity; store-default language needs MerchantStore (outside thin cores). Strangler BFF resolves language before HTTP call.
- Facades/populators in `reference-service` only; contracts DTOs; no `sm-core`/`sm-shop` dependency.
- Plain populators (no `AbstractDataPopulator`). Zone null/empty → `200 []` (OQ-02).
- Ehcache via `CacheConfig` + `ehcache.xml` (`serviceCache` for `CacheUtils`).
- Controller API tests use standalone MockMvc (avoids pulling JPA services into web slice).
- JaCoCo 0.8.11 in this module (Java 21); parent empty `spring-boot-starter-web` BOM override removed.

## Touched surfaces

- `reference-service/` (new Boot app, facades, populators, controller, tests)
- Root `pom.xml` (module + remove empty Boot starter BOM overrides)

## Learnings

- Empty version-less Boot starters in parent `dependencyManagement` wipe BOM versions and break new modules that declare those starters.
- `@WebMvcTest` still loads `@SpringBootApplication` component scans → core services need EMF; prefer standalone MockMvc for controller contract tests.

## Follow-ups

- Store-code → default language if direct clients omit `lang`.
- Narrow `@EntityScan` later if full `com.salesmanager.core.model` scan becomes costly (Wave 1 shares schema).

## Status

Completed: `./mvnw verify -pl reference-service -am` green; line coverage ~88%; jar `reference-service/target/reference-service.jar`.
