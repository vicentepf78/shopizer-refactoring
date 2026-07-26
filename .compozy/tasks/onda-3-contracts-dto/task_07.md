---
status: pending
title: Migração facade P1 para identificadores tenant
type: refactor
complexity: high
---

# Migração facade P1 para identificadores tenant

## Visão geral
Consolida TLC T30–T34. Migra seis interfaces e implementações facade P1 para usar `MerchantStoreId` e `LanguageCode` conforme ADR-003.

<critical>
- SEMPRE LER o PRD e a TechSpec antes de começar
- REFERENCIAR A TECHSPEC para detalhes de implementação — não duplicar aqui
- FOCAR NO "O QUÊ" — descrever o que precisa ser feito, não como
- MINIMIZAR CÓDIGO — mostrar código apenas para ilustrar estrutura atual ou áreas problemáticas
- TESTES OBRIGATÓRIOS — toda task DEVE incluir testes nos entregáveis
</critical>

<requirements>
1. MUST atualizar interfaces: `OrderFacade`, `ShoppingCartFacade`, `SearchFacade`, `ShippingFacade`, `CategoryFacade`, `ProductCommonFacade` — TLC T30.
2. MUST atualizar todas as implementações e adapters HTTP (Wave 2 search/content inalterados) — TLC T31–T32.
3. MUST corrigir erros de compilação em controllers chamando facades (converter entidade → tenant na fronteira) — TLC T33.
4. MUST adicionar ArchUnit `facades_no_new_entity_params` para sm-shop-model — TLC T34.
5. MUST usar `TenantEntityBridge` apenas dentro de implementações.
</requirements>

## Subtarefas
- [ ] 7.1 Atualizações de assinatura de interface (T30)
- [ ] 7.2 Implementação + hidratação bridge (T31–T32)
- [ ] 7.3 Correções call-site controller (T33)
- [ ] 7.4 Regra ArchUnit facade (T34)

## Detalhes de implementação
Ver TechSpec: **Migração facade (Fase 1)**. `MerchantStoreArgumentResolver` ainda fornece entidades a controllers — converter na chamada facade.

### Arquivos relevantes
- `sm-shop-model/.../order/facade/v1/OrderFacade.java`
- `sm-shop-model/.../shoppingCart/facade/v1/ShoppingCartFacade.java`
- `sm-shop-model/.../search/facade/SearchFacade.java`
- `sm-shop/.../facade/*Impl.java`

### Arquivos dependentes
- Todas interfaces e implementações facade P1
- Controllers sob `sm-shop/.../api/v1/order/`, `search/`, `shipping/`

### ADRs relacionados
- [ADR-003: Migração facade faseada](../adrs/adr-003.md)

## Entregáveis
- Seis interfaces facade migradas + impls
- sm-shop compilando com call sites atualizados
- Teste ArchUnit **(OBRIGATÓRIO)**

## Testes
- Testes unitários:
  - [ ] Impl facade hidrata store via bridge
- Testes de integração:
  - [ ] Testes facade existentes compilam e passam
- Meta de cobertura: >=80%
- Todos os testes devem passar

## Critérios de sucesso
- Todos os testes passando
- B-001 parcialmente resolvido para facades P1
- Sem mudanças de caminho REST
