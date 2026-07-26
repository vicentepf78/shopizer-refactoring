---
status: pending
title: customer-service Boot, REST, snapshot (CUS-ready)
type: backend
complexity: high
---

# customer-service Boot, REST, snapshot (CUS-ready)

## Visão geral
Consolida TLC T18–T20. Entrega `customer-service` (:8087): Boot, REST profile/address/optin, client reference, CustomerSnapshot interno, JWT. Exclui AuthenticateCustomerApi (OQ-06). Marco **CUS-ready**.

<requirements>
1. MUST criar Boot + JPA + sm-customer-core — T18.
2. MUST portar seções profile/address/optin do CustomerApi — T19.
3. MUST expor `GET /internal/v1/customers/{id}/snapshot` — T20.
4. MUST replicar JWT para rotas privadas — T20.
5. MUST NOT expor endpoints login/register/password.
</requirements>

## Subtarefas
- [ ] 5.1 Scaffold Boot (T18)
- [ ] 5.2 REST profile + client reference (T19)
- [ ] 5.3 Snapshot interno + security (T20)

## ADRs relacionados
- [ADR-005](adrs/adr-005.md)
- OQ-06 auth permanece no monólito

## Entregáveis
- customer-service implantável
- Testes de integração profile update + snapshot **(OBRIGATÓRIO)**

## Testes
- `./mvnw test -pl customer-service`

## Critérios de sucesso
- Marco CUS-ready
- Porta 8087 health UP
