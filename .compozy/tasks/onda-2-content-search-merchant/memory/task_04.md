# Task Memory: task_04.md

## Objective Snapshot

Rewire sm-core to delegate content to sm-content-core; trim shopizer-core-cms.xml to product-only beans.

## Important Decisions

- Import `shopizer-content-cms.xml` before `shopizer-core-cms.xml` in `shopizer-core-context.xml`; shared `httpdAssetsManager`/`awsAssetsManager` come from thin-core XML.
- Rewire tests use static XML/source assertions + classpath checks; avoid `@SpringBootTest(ConfigurationTest)` in new tests (poisons shared context cache for legacy integration tests).

## Learnings

- `PropertySourcesPlaceholderConfigurer` in isolated `@ContextConfiguration` tests can break subsequent `ConfigurationTest` context loads in the same Surefire JVM.

## Files / Surfaces

- `sm-core/src/main/resources/spring/shopizer-core-cms.xml` — product-only CMS
- `sm-core/src/main/resources/spring/shopizer-core-context.xml` — imports content-cms
- `sm-core/pom.xml` — sm-content-core dep (already present from prior work)
- Tests: `NoContentDuplicateInSmCoreSourceTest`, `CoreCmsRewireTest`, `ContentServiceThinCoreWiringTest`

## Errors / Corrections

- Initial Spring-heavy CMS test caused integration test pollution; replaced with static XML checks.
- Package typo (`com.salesmanager/core.business`) caught via compile error.

## Ready for Next Run

Complete. Monolith resolves ContentService via component scan from sm-content-core JAR; product CMS stays in sm-core XML. task_11 strangler adapters can assume in-process content wiring.
