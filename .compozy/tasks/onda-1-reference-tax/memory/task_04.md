# task_04 memory

## Objective Snapshot

Extract Country/Zone/Language/Currency repos+CRUD into `sm-reference-core`, REF-08 `toLocale(Language, String)`, rewire `sm-core`; init/loader stay — complete.

## Important Decisions

- T8 slice: move `services/common/generic/*`, `CacheUtils`, `Constants` into `sm-reference-core` (same packages) so the module depends only on `sm-core-model` among Shopizer modules — avoids circular dep with `sm-core`.
- JaCoCo excludes `CacheUtils` (ehcache-native key scan), `ZoneTransient` (unused), `Constants` (javac-inlined statics); measured bundle after excludes: 94.6% lines.
- MerchantStore `toLocale` delegates to the String overload (REF-08).

## Learnings

- `./mvnw test -pl sm-core` without `-am`/`install` tries to resolve `sm-reference-core` from `repo.spring.io` (401). Use `-am` or install the thin core first.

## Files / Surfaces

- New module `sm-reference-core/` (pom, repos, CRUD services, generic slice, tests)
- Root `pom.xml` module + dependencyManagement; `sm-core/pom.xml` dep
- Init/loader remain under `sm-core/.../services/reference/{init,loader}/`

## Errors / Corrections

- Missing `spring-boot-starter-cache` version in parent BOM override → use `spring-context-support` instead.
- JaCoCo check excludes must be plugin-level (not only check execution) to affect the ratio.

## Ready for Next Run

Done. Hand off: task_05 can mirror this thin-core pattern; generics/CacheUtils already on classpath via `sm-core` → `sm-reference-core` (tax-core may need its own slice or a shared home later).
