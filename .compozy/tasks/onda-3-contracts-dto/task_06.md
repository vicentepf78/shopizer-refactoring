---
status: pending
title: PaymentModuleV2, ShippingQuoteModuleV2 e bridges
type: backend
complexity: high
---

# PaymentModuleV2, ShippingQuoteModuleV2 e bridges

## Visão geral
Consolida TLC T25–T29. Introduz interfaces V2 de plugin, mappers entidade→DTO em serviços Payment/Shipping e bridge legacy para plugins existentes.

<critical>
- SEMPRE LER o PRD e a TechSpec antes de começar
- REFERENCIAR A TECHSPEC para detalhes de implementação — não duplicar aqui
- FOCAR NO "O QUÊ" — descrever o que precisa ser feito, não como
- MINIMIZAR CÓDIGO — mostrar código apenas para ilustrar estrutura atual ou áreas problemáticas
- TESTES OBRIGATÓRIOS — toda task DEVE incluir testes nos entregáveis
</critical>

<requirements>
1. MUST definir interfaces `PaymentModuleV2` e `ShippingQuoteModuleV2` — TLC T25.
2. MUST implementar `LegacyPaymentModuleBridge` encapsulando plugins V1 como V2 — TLC T26.
3. MUST atualizar `PaymentServiceImpl` para preferir V2 quando disponível — TLC T27.
4. MUST atualizar `ShippingServiceImpl` similarmente — TLC T28.
5. MUST adicionar teste de integração com plugin mais simples (ex. MoneyOrder) via caminho V2 — TLC T29.
6. MUST NOT quebrar registro de plugin V1 existente.
</requirements>

## Subtarefas
- [ ] 6.1 Definições de interface V2 (T25)
- [ ] 6.2 Bridges legacy V1→V2 (T26)
- [ ] 6.3 Roteamento PaymentServiceImpl (T27)
- [ ] 6.4 Roteamento ShippingServiceImpl (T28)
- [ ] 6.5 Teste de integração caminho plugin (T29)

## Detalhes de implementação
Ver TechSpec: **Interfaces principais**, ADR-004. Padrão registry: `Map<String, PaymentModule>` inalterado; adicionar map V2 opcional ou wrapper adapter.

### Arquivos relevantes
- `sm-core/.../payments/PaymentServiceImpl.java`
- `sm-core/.../shipping/ShippingServiceImpl.java`
- `sm-core/.../modules/integration/payment/impl/MoneyOrderPayment.java`

### Arquivos dependentes
- `sm-core-modules/.../PaymentModuleV2.java` — criar
- `sm-core/.../payments/LegacyPaymentModuleBridge.java` — criar
- `sm-core/.../shipping/LegacyShippingQuoteModuleBridge.java` — criar

### ADRs relacionados
- [ADR-004: Interfaces V2 paralelas](../adrs/adr-004.md)

## Entregáveis
- Interfaces V2 + bridges legacy
- Roteamento camada de serviço
- Teste de integração provando caminho authorize V2 **(OBRIGATÓRIO)**

## Testes
- Testes unitários:
  - [ ] Bridge mapeia cart items entidade para PaymentLineItemDto
- Testes de integração:
  - [ ] MoneyOrder authorize via bridge V2
- Meta de cobertura: >=80%
- Todos os testes devem passar

## Critérios de sucesso
- Todos os testes passando
- Milestone INT-ready
- Plugins V1 ainda passam testes existentes
