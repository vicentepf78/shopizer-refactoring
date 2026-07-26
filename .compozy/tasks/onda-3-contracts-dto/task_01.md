---
status: pending
title: Tipos tenant e base de contracts
type: backend
complexity: medium
---

# Tipos tenant e base de contracts

## Visão geral
Consolida TLC T1–T6. Introduz `MerchantStoreId`, `LanguageCode`, convenções compartilhadas de contracts, stub `TenantEntityBridge` e baseline ArchUnit para pureza de contracts. **Pré-requisito externo:** gate Onda 2 verde.

<critical>
- SEMPRE LER o PRD e a TechSpec antes de começar
- REFERENCIAR A TECHSPEC para detalhes de implementação — não duplicar aqui
- FOCAR NO "O QUÊ" — descrever o que precisa ser feito, não como
- MINIMIZAR CÓDIGO — mostrar código apenas para ilustrar estrutura atual ou áreas problemáticas
- TESTES OBRIGATÓRIOS — toda task DEVE incluir testes nos entregáveis
</critical>

<requirements>
1. MUST adicionar `MerchantStoreId` e `LanguageCode` em `shopizer-api-contracts` com validação e suporte Jackson — TLC T1–T2.
2. MUST adicionar placeholders de package `com.salesmanager.contracts.order` e `com.salesmanager.contracts.customer` com convenções `ShopEntity` — TLC T3.
3. MUST criar interface `TenantEntityBridge` em `sm-shop` para resolver entidades store/lang a partir de códigos — TLC T4.
4. MUST adicionar teste ArchUnit `ContractsMustNotDependOnCoreModel` em `shopizer-api-contracts` ou `sm-shop` — TLC T5.
5. MUST estender `AbstractDataPopulator` com overload tenant-primitive (retrocompatível) — TLC T6.
6. MUST NOT introduzir imports `com.salesmanager.core.model` em api-contracts.
7. MUST compilar `./mvnw compile -pl shopizer-api-contracts,sm-shop-model,sm-shop -am`.
</requirements>

## Subtarefas
- [ ] 1.1 Value types `MerchantStoreId`, `LanguageCode` + testes unitários (T1–T2)
- [ ] 1.2 Estrutura de packages e testes de serialização compartilhados (T3)
- [ ] 1.3 `TenantEntityBridge` + impl default usando serviços existentes (T4)
- [ ] 1.4 Regra ArchUnit pureza contracts (T5)
- [ ] 1.5 Overload `AbstractDataPopulator` (T6)

## Detalhes de implementação
Ver TechSpec: **Interfaces principais**, **Princípios**. Reutilizar padrões de código `ReadableLanguage` dos contracts de referência.

### Arquivos relevantes
- `shopizer-api-contracts/src/main/java/com/salesmanager/contracts/` — packages destino
- `sm-core/src/main/java/com/salesmanager/core/business/utils/AbstractDataPopulator.java`
- `sm-shop-model/src/main/java/com/salesmanager/shop/store/controller/` — consumidores facade

### Arquivos dependentes
- `shopizer-api-contracts/.../tenant/MerchantStoreId.java` — criar
- `shopizer-api-contracts/.../tenant/LanguageCode.java` — criar
- `sm-shop/.../tenant/TenantEntityBridge.java` — criar
- `sm-shop/.../tenant/TenantEntityBridgeImpl.java` — criar

### ADRs relacionados
- [ADR-003: Migração facade faseada](../adrs/adr-003.md)
- [ADR-001: Onda apenas no monólito](../adrs/adr-001.md)

## Entregáveis
- Value types tenant em api-contracts
- Interface bridge + implementação
- Teste ArchUnit para módulo contracts
- Overload populator
- Testes unitários com cobertura 80%+ nos value types **(OBRIGATÓRIO)**

## Testes
- Testes unitários:
  - [ ] `MerchantStoreId` rejeita código vazio
  - [ ] `LanguageCode` serializa/deserializa em JSON
  - [ ] Bridge retorna store para código válido
- Testes de integração:
  - [ ] `./mvnw test -pl shopizer-api-contracts -Dtest=*Tenant*`
- Meta de cobertura: >=80%
- Todos os testes devem passar

## Critérios de sucesso
- Todos os testes passando
- Cobertura de testes >=80%
- Zero imports core.model em api-contracts
- Gate Onda 2 verificado antes do início
