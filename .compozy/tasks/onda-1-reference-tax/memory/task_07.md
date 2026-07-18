# task_07 memory

## Objective

Deliver `tax-service` (:8082): Boot + JWT private chain, HTTP `ReferenceServiceClient`, TaxFacade/mappers, REST `/api/v1/private/tax/*`. No calculateTax — complete.

## Decisions

- Depend on `sm-tax-core` + contracts + security; not `sm-core`/`sm-shop`. Local `AdminUserRepository` / `MerchantStoreRepository` for JWT user + store lookup.
- Store mismatch → 403 (`StoreForbiddenException`); techspec over monolito’s Unauthorized→401 mapping.
- `PersistableTaxRateMapper` uses HTTP client; builds detached Country/Zone/Language by id from Readable*.
- `@Transactional` on `TaxFacadeImpl` required (`open-in-view=false` + `getOne` proxies).
- Named ehcache `com.shopizer.tax.cache` + `shared=true` so multiple Spring test contexts reuse CacheManager.

## Touched surfaces

- `tax-service/` (new Boot app)
- Root `pom.xml` (module registration)

## Learnings

- JaCoCo CSV column order is LINE_MISSED then LINE_COVERED — swap breaks ratio math.
- Align IT `@Import`/`@MockBean` across SpringBootTests to share one context and avoid ehcache clash.

## Status

Completed: `./mvnw verify -pl tax-service -am` green; line coverage ~82%; gate ITs green; jar `tax-service/target/tax-service.jar`.
