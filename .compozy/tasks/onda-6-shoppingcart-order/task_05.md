---
status: pending
title: Adaptador Strangler ShoppingCart + shadow mode (SC-ready)
type: backend
complexity: high
---

# Adaptador Strangler ShoppingCart + shadow mode (SC-ready)

## Visão geral
TLC T13–T14, T50. `ShoppingCartFacadeHttpAdapter` com `wave6.shoppingcart.strangler.enabled`; comparação shadow de leitura; runbook de cutover. Marco **SC-ready**.

<requirements>
1. MUST implementar adaptador HTTP delegando a shoppingcart-service — T13, CART-04.
2. MUST retornar 503 em falha remota com correlation id — STR-06.
3. MUST implementar shadow mode comparando in-process vs remoto (somente log) — T14.
4. MUST documentar `docs/runbooks/wave6-cart-cutover.md` — T14, STR-07.
5. MUST implementar `ShoppingCartServiceClientRestTemplateImpl` — T50.
</requirements>

## Subtarefas
- [ ] 5.1 Adaptador facade HTTP + flag (T13)
- [ ] 5.2 Impl client RestTemplate (T50)
- [ ] 5.3 Shadow mode (T14)
- [ ] 5.4 Runbook + SC-ready STATE.md (T14)

## ADRs relacionados
- [ADR-007: Faseamento](adrs/adr-007.md)
- [ADR-008: Rollback](adrs/adr-008.md)

## Entregáveis
- Adaptador Strangler + runbook
- `ShoppingCartFacadeHttpAdapterTest`, `ShoppingCartShadowModeTest` **(REQUIRED)**

## Critérios de sucesso
- Marco SC-ready em STATE.md
- Flag alterna in-process vs remoto
