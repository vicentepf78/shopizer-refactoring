---
status: pending
title: Desacoplamento merge de carrinho + orquestração CustomerFacade
type: backend
complexity: high
---

# Desacoplamento merge de carrinho + orquestração CustomerFacade

## Visão geral
Consolida TLC T24–T25. Refatora `ShoppingCartService.mergeShoppingCarts` para usar `CustomerSnapshot`; CustomerFacade busca snapshot via HTTP do customer-service no login antes do merge.

<requirements>
1. MUST refatorar mergeShoppingCarts para aceitar CustomerSnapshot ou ids primitivos — T24.
2. MUST atualizar fluxo de login do CustomerFacade para chamar CustomerServiceClient.getSnapshot — T25.
3. MUST fail closed quando customer-service indisponível durante merge (documentado).
4. MUST NOT adicionar chamadas HTTP dentro de ShoppingCartServiceImpl.
5. MUST passar teste de integração merge de carrinho com mock strangler.
</requirements>

## Subtarefas
- [ ] 7.1 Refatoração assinatura ShoppingCartService (T24)
- [ ] 7.2 Orquestração CustomerFacade (T25)
- [ ] 7.3 Teste de integração login+merge

## ADRs relacionados
- [ADR-005](adrs/adr-005.md)

## Entregáveis
- Refatoração merge + orquestração facade
- Teste de integração login merge **(OBRIGATÓRIO)**

## Testes
- `./mvnw test -pl sm-core -Dtest=*ShoppingCart*Merge*Test`
- `./mvnw test -pl sm-shop -Dtest=*CustomerFacade*Merge*Test`

## Critérios de sucesso
- Merge funciona com input snapshot
- Sem CustomerService in-process exigido no caminho de merge
