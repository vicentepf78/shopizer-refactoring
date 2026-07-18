# task_09 memory

## Objective

Add Pact consumer (sm-shop) + provider (reference-service, tax-service) contract gates for Wave 1 P1 endpoints; local pact files; drift fails CI.

## Decisions

- Pact JVM 4.6.17 (JUnit5); Spring Boot 2.5 → `junit5spring` for MockMvcTestTarget.
- Shared pact dir at repo `pacts/` (consumer writes; providers `@PactFolder("../pacts")`). Committed JSON so providers can verify without ordering hacks.
- Provider tests use standalone MockMvc + mocked facades (no Boot/DB/JWT) — assert only consumer-needed fields.
- Drift proof is a controlled test that asserts verification fails when `code` is omitted from languages — not a always-red provider suite.

## Touched surfaces

- parent `pom.xml` (pact.version + dependencyManagement)
- `sm-shop`, `reference-service`, `tax-service` poms + contract tests
- `pacts/*.json`

## Learnings

- Filtered reactor Pact run: Tests run 6 (reference+drift) / 12 (tax) / 2 (consumer); BUILD SUCCESS.
- Pact JVM phones home to Google Analytics during tests (noise in logs; ignore).

## Handoff

- Gate commands documented in `task_09.md` Verification.
- Status: completed; commit task_09 surfaces only (exclude task_10 docker/health/correlation).
