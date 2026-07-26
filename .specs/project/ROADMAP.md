# Roadmap — Decomposição Shopizer

**Current Milestone:** Onda 3 — Contratos DTO (Specify/Tasks prontos)
**Status:** Ondas 1–2 Execute ✅; Ondas 3–6 documentação Compozy + TLC ✅ (Execute bloqueado por gates)

**Fonte:** [docs/decomposition/MIGRATION-MASTER-PLAN.md](../../docs/decomposition/MIGRATION-MASTER-PLAN.md)

---

## Fase 1 — Análise e Preparação (semanas 1–10)

**Goal:** Consolidar duplicações e mapear acoplamento antes de extrações.
**Target:** Inventário formal de componentes concluído

### Features

**Análise de acoplamento** - COMPLETE

- Modelo 3D (strength × distance × volatility)
- 22 pares cross-domain, 2 ciclos identificados
- Scores de dificuldade por domínio

**Quick wins de consolidação** - PLANNED

- FieldMatchValidator, RatingAggregateUpdater, AbstractConfigurationFacadeImpl
- Piloto Mapper/Populator (Product)

**Inventário de componentes** - PLANNED

---

## Fase 2 — Organização por Domínios (semanas 11–18)

**Goal:** 10 bounded contexts formalizados com contratos DTO iniciais.

### Features

**Bounded contexts** - PLANNED

- Catalog, Order, Customer, Merchant, Payment, Shipping, Content, Reference, Identity, System

---

## Fase 3 — Extração Incremental (semanas 19–68)

**Goal:** Serviços deployáveis por onda, ordem baseada em acoplamento.

### Onda 1 — Reference + Tax (semanas 19–24) - COMPLETE

- `reference-service` (dificuldade 3/10)
- `tax-service` admin CRUD (dificuldade 4/10)
- Strangler Fig + testes de contrato
- **TLC:** Specify ✅ → Design ✅ → Tasks ✅ → Execute ✅ (gate reactor 2026-07-26)

### Onda 2 — Content, Search, Merchant (semanas 25–32) - COMPLETE

- `content-service` (dificuldade 2–6/10) — split-brain JPA+blob
- `search-service` (dificuldade 5/10) — `ProductIndexPayload` interim
- `merchant-service` (dificuldade 5/10) — sem ProductType APIs
- **TLC:** Specify ✅ → Design ✅ → Tasks ✅ → Execute ✅ (gate reactor 2026-07-26, PR #4)

### Onda 3 — Contratos DTO (semanas 33–38) - READY

- ProductSnapshot, OrderSnapshot, CustomerSnapshot, LanguageCode, MerchantStoreId
- Checkout Application Service + outbox foundation (sem novos microserviços)
- **Compozy:** PRD ✅ → TechSpec ✅ → Tasks ✅ (10 tasks) — Execute bloqueado até aprovação

### Onda 4 — Catalog + Customer (semanas 39–48) - READY

- `catalog-service` read-first; `customer-service` após CustomerSnapshot
- **Compozy:** PRD ✅ → TechSpec ✅ → Tasks ✅ (15 tasks) — gate: Onda 3 Execute

### Onda 5 — Integration Service (semanas 49–56) - READY

- `integration-service` — payment/shipping orchestration stateless
- **Compozy:** PRD ✅ → TechSpec ✅ → Tasks ✅ (12 tasks) — gate: Onda 3 + Onda 4 partial

### Onda 6 — ShoppingCart + Order (semanas 57–68) - READY

- `shoppingcart-service` + `order-service` — última extração (coupling 9/10)
- **Compozy:** PRD ✅ → TechSpec ✅ → Tasks ✅ (16 tasks) — gate: Ondas 3–5

---

## Fase 4 — Otimização (semanas 43–48)

**Goal:** Deprecar API V1, observabilidade, fitness functions (ArchUnit).

### Features

**API V1 deprecation** - PLANNED
**ArchUnit fitness functions** - PLANNED

---

## Future Considerations

- Saga/outbox em `processOrder` (desbloqueia Order + Payments)
- Redesign `PaymentModule` / `ShippingQuoteModule` com DTOs
- Extração de `integration-service` após Onda 3
- Split de `InitializationDatabaseImpl` em bootstrap por domínio
