---
status: pending
title: shoppingcart-service Boot, REST, internal clear (SC-ready parcial)
type: backend
complexity: high
---

# shoppingcart-service Boot, REST, internal clear (SC-ready parcial)

## Visão geral
TLC T10–T12, T52, T54. App Spring Boot :8086; REST cart público espelhando `ShoppingCartApi`; clear interno pós-checkout; JWT em `/private/**`.

<requirements>
1. MUST fazer scaffold `shoppingcart-service` na porta 8086 — T10.
2. MUST implementar REST CRUD cart com mappers DTO — T11, CART-01, CART-07.
3. MUST implementar `DELETE /internal/v1/carts/{id}/after-checkout` — T12, CHK-06.
4. MUST replicar padrão JWT security dos serviços wave1 — T54.
5. MUST NOT expor entidades JPA em JSON.
</requirements>

## Subtarefas
- [ ] 4.1 Boot app + JPA + actuator (T10)
- [ ] 4.2 Controllers cart públicos + mappers (T11, T52)
- [ ] 4.3 API internal clear (T12)
- [ ] 4.4 Config JWT (T54)

## Entregáveis
- JAR implantável `shoppingcart-service`
- `ShoppingCartApiIntegrationTest`, `InternalCartControllerTest` **(REQUIRED)**

## Testes
- `./mvnw test -pl shoppingcart-service -Dtest=ShoppingCartApiIntegrationTest,InternalCartControllerTest`

## Critérios de sucesso
- Todos os testes cart-service passam
- Endpoint health UP
