# Workflow Memory

Keep only durable, cross-task context here. Do not duplicate facts that are obvious from the repository, PRD documents, or git history.

## Current State

Onda 3 Execute complete (task_01–task_10). Gate `./mvnw clean install` verde 2026-07-26.

## Shared Decisions

- Outbox routing lives in `CheckoutApplicationServiceImpl` (not `OrderServiceImpl`) to avoid circular dependency with `CheckoutStagedOrderProcessor`.
- JPA entity `CheckoutOutboxEvent` in `sm-core-model`; repository/dispatcher in `sm-core`.

## Shared Learnings

- `OrderServiceImpl` → `CheckoutStagedOrderProcessor` → `OrderService` is a cycle; keep staged path entry at CAS only.

## Open Risks

- `checkout.outbox.enabled` default false until Onda 6 rollout; production needs explicit enable decision.

## Handoffs

- Onda 4: remaining ~60 facades, catalog/customer extraction per MIGRATION-MASTER-PLAN.
