---
provider: manual
pr:
round: 1
round_created_at: 2026-07-26T22:06:00Z
status: resolved
file: sm-core/src/main/resources/db/migration/V3_001__checkout_outbox.sql
line: 1
severity: medium
author: claude-code
provider_ref:
---

# Issue 012: Checkout outbox SQL migration is never executed

## Review Comment

TechSpec specifies delivering `CHECKOUT_OUTBOX` as a Flyway/Liquibase migration. The file `V3_001__checkout_outbox.sql` uses Flyway-style naming, but the reactor has no Flyway or Liquibase dependency. All environments use `hibernate.hbm2ddl.auto=update` (or `create` for tests), so the table is created from the `CheckoutOutboxEvent` JPA entity — the SQL file is dead code.

This is misleading: a future engineer will assume schema changes belong in `db/migration/` when in fact only the JPA entity matters. The unique constraint in the SQL (`UK_OUTBOX_AGG_TYPE`) may differ from what Hibernate generates.

Suggested fix: either wire an actual migration runner and disable `hbm2ddl` for this table, or remove/relabel the file and document that Hibernate entity annotations are the schema source of truth during the transition. Align the unique constraint between entity and intended DDL.

## Triage

- Decision: `valid`
- Notes: Confirmed no Flyway or Liquibase dependency anywhere in the reactor; all profiles use `hibernate.hbm2ddl.auto=update` (or `create` in tests). The Flyway-named SQL under `sm-core/src/main/resources/db/migration/` was never executed. The entity already declares `@UniqueConstraint(name = "UK_OUTBOX_AGG_TYPE", columnNames = { "AGGREGATE_ID", "EVENT_TYPE" })`, matching the removed DDL; `CheckoutOutboxIntegrationTest.appendEnforcesUniqueConstraintOnAggregateAndEventType` exercises idempotency against that constraint. Batch scope listed `CheckoutOutboxProperties.java` but the issue frontmatter and review comment target the dead migration file.
- Fix: Removed `V3_001__checkout_outbox.sql` (only file in `db/migration/`). Added class-level Javadoc on `CheckoutOutboxEvent` stating that JPA annotations are the schema source of truth during the transition. Did not wire Flyway/Liquibase — out of scope for Onda 3 and contradicts the existing Shopizer hbm2ddl pattern.
- Verification: `./mvnw -pl sm-core,sm-core-model -am test -Dtest=CheckoutOutboxIntegrationTest,CheckoutOutboxRepositoryTest -DfailIfNoTests=false` and `./mvnw -pl sm-core -am verify -DfailIfNoTests=false`.
