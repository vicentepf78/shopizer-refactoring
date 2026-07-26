---
status: pending
title: catalog-service Boot, clients, REST read público (CAT-ready)
type: backend
complexity: high
---

# catalog-service Boot, clients, REST read público (CAT-ready)

## Visão geral
Consolida TLC T9–T13. Entrega `catalog-service` (:8086): app Boot, clients HTTP reference/merchant, REST GET público espelhando ProductApi/CategoryApi/manufacturer/inventory/price, API interna ProductSnapshot, JWT em leituras privadas. Marco **CAT-ready**.

<requirements>
1. MUST criar app Boot com JPA + sm-catalog-core — T9.
2. MUST integrar ReferenceServiceClient + MerchantServiceClient — T10.
3. MUST portar endpoints GET públicos product/category/manufacturer/inventory/price — T11.
4. MUST expor `GET /internal/v1/products/{id}/snapshot` com 422 em schemaVersion — T12.
5. MUST replicar segurança JWT para rotas que exigem auth hoje — T13.
6. MUST retornar 503 quando dependências down; sem JPA no JSON.
</requirements>

## Subtarefas
- [ ] 3.1 Scaffold Boot (T9)
- [ ] 3.2 Clients HTTP (T10)
- [ ] 3.3 Controllers read públicos (T11)
- [ ] 3.4 API interna snapshot (T12)
- [ ] 3.5 Security (T13)

## ADRs relacionados
- [ADR-002](adrs/adr-002.md)
- [ADR-003](adrs/adr-003.md)
- [ADR-006](adrs/adr-006.md) — sem writes admin

## Entregáveis
- catalog-service implantável
- Testes de integração product list + category tree + snapshot **(OBRIGATÓRIO)**

## Testes
- `./mvnw test -pl catalog-service`
- Paridade: GET product list vs baseline monólito fixture

## Critérios de sucesso
- Marco CAT-ready alcançado
- Porta 8086 health UP com MySQL
