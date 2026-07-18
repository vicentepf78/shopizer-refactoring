# task_05 memory

## Objective Snapshot

Extract TaxClass/TaxRate CRUD into `sm-tax-core`, DELETE 409 guard + TAX-09 existsTaxRate, rewire `sm-core` keeping `TaxService*` — complete.

## Important Decisions

- Depend on `sm-reference-core` for `SalesManagerEntityServiceImpl` (avoids cycle with `sm-core`).
- Product count via `ProductTaxClassCountRepository` in `sm-tax-core` (not `ProductRepository` in sm-core).
- `TaxClassInUseException` (RuntimeException) in `sm-core-model` so facade `catch (ServiceException)` does not swallow it into 500.
- TAX-09: `TaxRateService.exists` + `TaxFacadeImpl` uses it instead of `taxRateByCode`.

## Learnings

- `./mvnw test -pl sm-shop -am -Dtest=TaxRateIntegrationTest` needs `-DfailIfNoTests=false`.

## Files / Surfaces

- New `sm-tax-core/` (pom, repos, CRUD services, ProductTaxClassCountRepository, tests, JaCoCo 80%)
- Root + `sm-core` pom rewire; `TaxService*` stays under `sm-core/.../services/tax/`
- `TaxClassInUseException` in sm-core-model
- `TaxFacadeImpl.existsTaxRate` TAX-09 fix

## Errors / Corrections

## Ready for Next Run

Done. Hand off: task_07 maps `TaxClassInUseException` → HTTP 409; task_06 parallel for reference-service.
