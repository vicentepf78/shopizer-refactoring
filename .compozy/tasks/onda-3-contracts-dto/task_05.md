---
status: pending
title: DTOs de integração payment e shipping
type: backend
complexity: high
---

# DTOs de integração payment e shipping

## Visão geral
Consolida TLC T21–T24. Adiciona DTOs de contexto de integração em `sm-core-modules` sem tipos JPA, preparando PaymentModuleV2 e ShippingQuoteModuleV2.

<critical>
- SEMPRE LER o PRD e a TechSpec antes de começar
- REFERENCIAR A TECHSPEC para detalhes de implementação — não duplicar aqui
- FOCAR NO "O QUÊ" — descrever o que precisa ser feito, não como
- MINIMIZAR CÓDIGO — mostrar código apenas para ilustrar estrutura atual ou áreas problemáticas
- TESTES OBRIGATÓRIOS — toda task DEVE incluir testes nos entregáveis
</critical>

<requirements>
1. MUST adicionar `IntegrationStoreContext` com store code, currency, language default — TLC T21.
2. MUST adicionar `PaymentRequestContext`, `PaymentCaptureContext`, `PaymentLineItemDto`, `TransactionResult` — TLC T22.
3. MUST adicionar `ShippingQuoteRequestContext`, `ShippingAddressDto`, `PackageDetailsDto` — TLC T23.
4. MUST usar `MerchantStoreId` de api-contracts (sm-core-modules depende de contracts) — TLC T24.
5. MUST NOT referenciar entidades `Order`, `Customer`, `ShoppingCartItem` nos novos DTOs.
</requirements>

## Subtarefas
- [ ] 5.1 DTOs de contexto integração comum (T21)
- [ ] 5.2 DTOs de contexto payment (T22)
- [ ] 5.3 DTOs de contexto shipping (T23)
- [ ] 5.4 Dependência Maven contracts → sm-core-modules (T24)

## Detalhes de implementação
Ver TechSpec: **Modelos de dados**, ADR-004. Mapear campos das assinaturas atuais de `PaymentModule`.

### Arquivos relevantes
- `sm-core-modules/.../payment/model/PaymentModule.java`
- `sm-core-modules/.../shipping/model/ShippingQuoteModule.java`
- `sm-core/.../payments/PaymentServiceImpl.java`

### Arquivos dependentes
- `sm-core-modules/.../integration/common/dto/` — criar
- `sm-core-modules/.../integration/payment/dto/` — criar
- `sm-core-modules/.../integration/shipping/dto/` — criar

### ADRs relacionados
- [ADR-004: Interfaces V2 paralelas](../adrs/adr-004.md)

## Entregáveis
- Packages DTO integração em sm-core-modules
- Testes unitários de serialização
- `./mvnw compile -pl sm-core-modules` verde **(OBRIGATÓRIO)**

## Testes
- Testes unitários:
  - [ ] PaymentRequestContext round-trip JSON
  - [ ] ShippingQuoteRequestContext contém DTOs delivery/origin
- Meta de cobertura: >=80%
- Todos os testes devem passar

## Critérios de sucesso
- Todos os testes passando
- DTOs cobrem todas as necessidades de parâmetro de método V2
- Sem imports JPA nos novos DTOs
