---
status: pending
title: Extração CheckoutApplicationService
type: backend
complexity: high
---

# Extração CheckoutApplicationService

## Visão geral
Consolida TLC T39–T43. Extrai orquestração place-order de `OrderFacadeImpl` para `CheckoutApplicationService` sem alterar comportamento REST público (requisitos CHK).

<critical>
- SEMPRE LER o PRD e a TechSpec antes de começar
- REFERENCIAR A TECHSPEC para detalhes de implementação — não duplicar aqui
- FOCAR NO "O QUÊ" — descrever o que precisa ser feito, não como
- MINIMIZAR CÓDIGO — mostrar código apenas para ilustrar estrutura atual ou áreas problemáticas
- TESTES OBRIGATÓRIOS — toda task DEVE incluir testes nos entregáveis
</critical>

<requirements>
1. MUST definir `CheckoutApplicationService` e `CheckoutCommand` em `sm-core/.../checkout` — TLC T39.
2. MUST mover lógica de orquestração dos métodos place-order de `OrderFacadeImpl` ao serviço — TLC T40–T41.
3. MUST manter `OrderFacadeImpl` como delegação fina (apenas validação + mapeamento DTO) — TLC T42.
4. MUST usar `CustomerSnapshot` / tipos tenant no command onde aplicável — TLC T43.
5. MUST preservar resultados idênticos para happy path e erros de validação conhecidos (CHK-01..CHK-06).
6. MUST NOT alterar caminhos ou schemas request/response de `OrderApi`.
</requirements>

## Subtarefas
- [ ] 9.1 Interface CheckoutApplicationService + command (T39)
- [ ] 9.2 Extrair fluxo process de OrderFacadeImpl (T40–T41)
- [ ] 9.3 Delegação fina facade (T42)
- [ ] 9.4 Testes integração paridade (T43)

## Detalhes de implementação
Ver TechSpec: **Fluxo checkout**. `OrderFacadeImpl` injeta 12+ serviços hoje — CAS deve ser dono da orquestração, facade mantém preocupações HTTP.

### Arquivos relevantes
- `sm-shop/.../order/facade/OrderFacadeImpl.java` (~1600 linhas)
- `sm-core/.../order/OrderServiceImpl.java`
- `sm-shop/.../api/v1/order/OrderApi.java`

### Arquivos dependentes
- `sm-core/.../checkout/CheckoutApplicationService.java` — criar
- `sm-core/.../checkout/CheckoutApplicationServiceImpl.java` — criar
- `sm-core/.../checkout/CheckoutCommand.java` — criar

### ADRs relacionados
- [ADR-001: Apenas monólito](../adrs/adr-001.md)
- [ADR-005: Hooks outbox preparados em task_10](../adrs/adr-005.md)

## Entregáveis
- CheckoutApplicationService com fluxo extraído
- OrderFacadeImpl reduzido
- Testes integração paridade checkout **(OBRIGATÓRIO)**

## Testes
- Testes unitários:
  - [ ] Validação builder CheckoutCommand
- Testes de integração:
  - [ ] Happy path place order bate com comportamento pré-extração
  - [ ] Caminhos falha payment inalterados
- Meta de cobertura: >=80%
- Todos os testes devem passar

## Critérios de sucesso
- Todos os testes passando
- Milestone CHK-ready
- Contagem de linhas OrderFacadeImpl materialmente reduzida
