# Roadmap — Decomposição Shopizer

**Current Milestone:** Onda 2 — Content, Search, Merchant (Execute)
**Status:** Ready — aguardando aprovação código

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

### Onda 1 — Reference + Tax (semanas 19–24) - IN PROGRESS

- `reference-service` (dificuldade 3/10)
- `tax-service` admin CRUD (dificuldade 4/10)
- Strangler Fig + testes de contrato
- **TLC:** Specify ✅ → Design ✅ → Tasks ✅ → Execute (aguardando aprovação código)

### Onda 2 — Content, Search, Merchant (semanas 25–32) - IN PROGRESS

- `content-service` (dificuldade 2–6/10) — split-brain JPA+blob
- `search-service` (dificuldade 5/10) — `ProductIndexPayload` interim
- `merchant-service` (dificuldade 5/10) — sem ProductType APIs
- **TLC:** Specify ✅ → Design ✅ → Tasks ✅ → Execute (bloqueado)

### Onda 3 — Contratos DTO (semanas 33–38) - PLANNED

- ProductSnapshot, LanguageCode, MerchantStoreId
- Checkout Application Service
- Sem deploy de novos microserviços

### Onda 4 — Catalog + Customer (semanas 39–48) - PLANNED

### Onda 5 — Integration Service (semanas 49–56) - PLANNED

### Onda 6 — ShoppingCart + Order (semanas 57–68) - PLANNED

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
