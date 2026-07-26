---
status: pending
title: Contratos integration, client, config Strangler Wave5
type: backend
complexity: medium
---

# Contratos integration, client, config Strangler Wave5

## Visão geral
Consolida TLC T1–T5. Entrega DTOs de integration e `IntegrationServiceClient` em `shopizer-api-contracts`, registra módulo `sm-integration-core` e adiciona profile/properties Strangler Wave5 no monólito. **Gates externos:** Execute da Onda 3 + leitura parcial de catálogo da Onda 4 DEVEM estar completos antes de iniciar.

<critical>
- SEMPRE LER o PRD e a TechSpec antes de iniciar
- CONSULTAR a TechSpec para detalhes de implementação — não duplicar aqui
- FOCAR NO "O QUÊ" — descrever o que precisa ser feito, não como
- MINIMIZAR CÓDIGO — mostrar código só para ilustrar estrutura atual ou áreas problemáticas
- TESTES OBRIGATÓRIOS — toda task DEVE incluir testes nos entregáveis
</critical>

<requirements>
1. MUST verificar artefatos Onda 3 compilando: `PaymentModuleV2`, `ShippingQuoteModuleV2`, snapshots, checkout service — TLC T1.
2. MUST adicionar DTOs de integration em `com.salesmanager.contracts.integration` — TLC T2.
3. MUST criar `IntegrationServiceClient` em `com.salesmanager.contracts.client` — TLC T3.
4. MUST adicionar profile `strangler-wave5`, properties `wave5.*`, `Wave5ClientConfig`, RestTemplate com correlation — TLC T4.
5. MUST registrar módulo Maven `sm-integration-core` no reactor — TLC T5.
6. MUST NOT importar `com.salesmanager.core.model` nos contracts.
7. MUST compilar `shopizer-api-contracts` e passar `Wave5ClientConfigTest`.
</requirements>

## Subtarefas
- [ ] 1.1 Script/teste de gate para pré-requisitos Onda 3 + Onda 4 (T1)
- [ ] 1.2 DTOs request/response de integration (T2)
- [ ] 1.3 Interface `IntegrationServiceClient` (T3)
- [ ] 1.4 Properties Wave5 + beans de config (T4)
- [ ] 1.5 pom `sm-integration-core` + entrada no reactor (T5)

## Detalhes de implementação
Ver TechSpec: **Interfaces principais**, **Modelos de dados**, **Properties Strangler**. Reutilizar padrões `Wave1ClientConfig` / `Wave2ClientConfig`.

### Arquivos relevantes
- `shopizer-api-contracts/` — novo pacote integration
- `sm-shop-model/.../order/transaction/` — formas fonte de DTOs de pagamento
- `sm-shop-model/.../order/shipping/` — formas fonte de DTOs de frete
- `sm-core-modules/.../PaymentModule.java` — referência de contrato legado

### Arquivos dependentes
- `sm-integration-core/pom.xml` — a criar
- `sm-shop/.../strangler/config/Wave5ClientConfig.java` — a criar
- `sm-shop/src/main/resources/application-strangler-wave5.properties` — a criar

### ADRs relacionados
- [ADR-001](adrs/adr-001.md) — workflow único
- [ADR-004](adrs/adr-004.md) — contratos V2 da Onda 3

## Entregáveis
- DTOs integration + interface client nos contracts
- Esqueleto do módulo `sm-integration-core`
- Config Strangler Wave5 em sm-shop
- Testes unitários de serialização DTO **(REQUIRED)**
- `Wave5ClientConfigTest` **(REQUIRED)**

## Testes
- Unit tests:
  - [ ] DTOs serializam/deserializam sem tipos JPA
  - [ ] `Wave5ClientConfig` carrega `wave5.integration-service.base-url`
- Integration tests:
  - [ ] `./mvnw compile -pl shopizer-api-contracts,sm-integration-core -am`
- Test coverage target: >=80% nos novos DTOs
- Todos os testes devem passar

## Critérios de sucesso
- Todos os testes passando
- Gates externos verificados e documentados
- `wave5.*` coexiste com `wave1.*`–`wave4.*`
