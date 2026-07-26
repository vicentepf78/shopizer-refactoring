# Task Memory: task_10.md

## Objective Snapshot

Merchant-service C-ready: Store/Config facades, ~18 REST endpoints (no ProductType), GET /config, internal snapshot, AD-014 logo orchestration, JaCoCo gate ≥80%.

## Important Decisions

- Populators are plain `@Component` classes (no `AbstractDataPopulator` — not on merchant-service classpath).
- Reference Country/Language via HTTP + `ReferenceEntityMapper`; Currency via local `CurrencyRepository`.
- Address province stored as text only on create (no transient Zone entity).
- `MerchantConfigurationFacadeImpl` returns empty `MerchantConfig` when DB row missing (null-safe).
- Logo AD-014: upload blob-first + compensate; delete DB-first + WARN on orphan blob.
- Integration update test uses DEFAULT store (admin `authorizedStore` only allows own/child stores).

## Learnings

- Mockito strict stubbing: use `@MockitoSettings(LENIENT)` or `doAnswer` on populate target arg (populate is void-side-effect, not return value).
- H2 shared Spring context: mutating DEFAULT in one integration test breaks snapshot assertions — load name from DB dynamically.
- `languageByCode` ehcache region required for `LanguageService.getByCode`.
- Admin private POST needs `ADMIN_RETAILER` group (`TestDataFactory.ensureGroup`).

## Files / Surfaces

- `merchant-service/src/main/java/com/salesmanager/merchant/facade/`
- `merchant-service/src/main/java/com/salesmanager/merchant/populator/`
- `merchant-service/src/main/java/com/salesmanager/merchant/api/v1/store/MerchantStoreController.java`
- `merchant-service/src/main/java/com/salesmanager/merchant/api/v1/system/PublicConfigsController.java`
- `merchant-service/src/main/java/com/salesmanager/merchant/api/internal/InternalStoreController.java`
- `merchant-service/src/test/java/com/salesmanager/merchant/**`
- `merchant-service/pom.xml` (JaCoCo includes facade/populator/client)

## Errors / Corrections

- Compilation: `listAllRetailers(Optional, int, int)` not `(Object, String, int, int)`.
- 401 on PUT new store: expected — use DEFAULT for update path in test.
- JaCoCo 61% → expanded `StoreFacadeImplUnitTest` + `PersistableMerchantStorePopulatorTest` → 83.2%.

## Pronto para próxima execução

- task_11: Strangler BFF adapters consuming merchant-service REST + internal snapshot.
