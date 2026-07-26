---
status: pending
title: Runbook CHK-ready + docs cutover checkout
type: docs
complexity: medium
---

# Runbook CHK-ready + docs cutover checkout

## Visão geral
TLC T32, T62. Runbook cutover checkout; checklist canary staging; script drill rollback. Marco **CHK-ready**.

<requirements>
1. MUST documentar `docs/runbooks/wave6-checkout-cutover.md` com combinações válidas de flags — T32, STR-07, ADR-008.
2. MUST configurar profile staging docker-compose-wave6 para saga — T32.
3. MUST adicionar `scripts/wave6-rollback-drill.sh` — T62.
4. MUST registrar CHK-ready em STATE.md.
</requirements>

## ADRs relacionados
- [ADR-008: Feature flags e rollback](adrs/adr-008.md)

## Entregáveis
- Runbooks + script drill rollback
- Checklist E2E staging completado **(REQUIRED manual gate)**

## Critérios de sucesso
- Marco CHK-ready em STATE.md
- Drill rollback completa < 5 min
