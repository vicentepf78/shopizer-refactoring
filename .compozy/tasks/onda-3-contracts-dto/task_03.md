---
status: pending
title: DTOs OrderSnapshot e CustomerSnapshot
type: backend
complexity: medium
---

# DTOs OrderSnapshot e CustomerSnapshot

## Visão geral
Consolida TLC T13–T16. Adiciona DTOs snapshot de order e customer relevantes ao checkout mais builders para uso por CheckoutApplicationService e payloads outbox.

<critical>
- SEMPRE LER o PRD e a TechSpec antes de começar
- REFERENCIAR A TECHSPEC para detalhes de implementação — não duplicar aqui
- FOCAR NO "O QUÊ" — descrever o que precisa ser feito, não como
- MINIMIZAR CÓDIGO — mostrar código apenas para ilustrar estrutura atual ou áreas problemáticas
- TESTES OBRIGATÓRIOS — toda task DEVE incluir testes nos entregáveis
</critical>

<requirements>
1. MUST definir `OrderSnapshot`, `OrderLineSnapshot`, `OrderTotalSnapshot` em contracts — TLC T13.
2. MUST definir `CustomerSnapshot`, `AddressSnapshot` (billing/delivery) em contracts — TLC T14.
3. MUST implementar `OrderSnapshotBuilder` e `CustomerSnapshotBuilder` em sm-core — TLC T15.
4. MUST usar snapshots no design de payload JSON outbox (sem grafos de entidade) — TLC T16.
5. MUST compilar sem JPA no módulo contracts.
</requirements>

## Subtarefas
- [ ] 3.1 DTOs snapshot order + testes (T13)
- [ ] 3.2 DTOs snapshot customer + testes (T14)
- [ ] 3.3 Builders de entidades `Order` / `Customer` (T15)
- [ ] 3.4 Documentar campos snapshot para estágios outbox (T16)

## Detalhes de implementação
Ver TechSpec: **Modelos de dados**. Referência `OrderFacadeImpl` e `OrderServiceImpl` para campos necessários no checkout.

### Arquivos relevantes
- `sm-core-model/.../order/Order.java`
- `sm-core-model/.../customer/Customer.java`
- `sm-shop-model/.../order/` — DTOs readable existentes como guia de campos

### Arquivos dependentes
- `shopizer-api-contracts/.../order/OrderSnapshot.java` — criar
- `shopizer-api-contracts/.../customer/CustomerSnapshot.java` — criar
- `sm-core/.../checkout/OrderSnapshotBuilder.java` — criar

### ADRs relacionados
- [ADR-005: Payload outbox usa snapshots](../adrs/adr-005.md)

## Entregáveis
- DTOs snapshot order e customer
- Builders entidade→snapshot
- Testes round-trip Jackson **(OBRIGATÓRIO)**

## Testes
- Testes unitários:
  - [ ] Snapshot order inclui status, totais, SKU/qty de linha
  - [ ] Snapshot customer exclui coleções lazy
  - [ ] Builders lidam com customer anônimo
- Meta de cobertura: >=80%
- Todos os testes devem passar

## Critérios de sucesso
- Todos os testes passando
- Snapshots utilizáveis no design CheckoutCommand
