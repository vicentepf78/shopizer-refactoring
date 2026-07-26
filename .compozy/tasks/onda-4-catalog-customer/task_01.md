---
status: pending
title: Contratos catalog/customer snapshots + config Strangler Wave4
type: backend
complexity: medium
---

# Contratos catalog/customer snapshots + config Strangler Wave4

## Visão geral
Consolida TLC T1–T4. Entrega DTOs catalog/customer, `ProductSnapshot` v2, `CustomerSnapshot` v1, `CatalogServiceClient`, `CustomerServiceClient`, mais profile/properties Strangler Wave4 no sm-shop. **Pré-requisito externo:** Execute da Onda 3 completo (snapshots + value types nos contratos).

<critical>
- SEMPRE LER o PRD e a TechSpec antes de começar
- REFERENCIAR A TECHSPEC para detalhes de implementação — não duplicar aqui
- FOCAR NO "O QUÊ" — descrever o que precisa ser feito, não como
- MINIMIZAR CÓDIGO — mostrar código apenas para ilustrar estrutura atual ou áreas problemáticas
- TESTES OBRIGATÓRIOS — toda task DEVE incluir testes nos entregáveis
</critical>

<requirements>
1. MUST verificar `LanguageCode`, `MerchantStoreId` da Onda 3 nos contratos — TLC T1.
2. MUST adicionar/migrar DTOs catalog + `ProductSnapshot` (schemaVersion default 2) + `CatalogServiceClient` — TLC T2.
3. MUST adicionar/migrar DTOs customer + `CustomerSnapshot` (schemaVersion default 1) + `CustomerServiceClient` com `getSnapshot` — TLC T3.
4. MUST adicionar profile `strangler-wave4`, properties `wave4.*.base-url`, `wave4.strangler.enabled`, RestTemplate + interceptor de correlação, stubs de client — TLC T4.
5. MUST NOT importar `com.salesmanager.core.model` nos contratos.
6. MUST coexistir com properties `wave1.*` e `wave2.*`.
7. MUST compilar `shopizer-api-contracts` e passar `Wave4ClientConfigTest` no sm-shop.
</requirements>

## Subtarefas
- [ ] 1.1 Verificar value types da Onda 3 (T1)
- [ ] 1.2 DTOs catalog + ProductSnapshot + CatalogServiceClient (T2)
- [ ] 1.3 DTOs customer + CustomerSnapshot + CustomerServiceClient (T3)
- [ ] 1.4 Wave4ClientConfig + properties + RestTemplate (T4)
- [ ] 1.5 Stubs RestTemplate client + testes de config

## Detalhes de implementação
Ver TechSpec: **Interfaces principais**, **Configuração Strangler**, ordem de construção passos 2–5. Reutilizar padrão `Wave2ClientConfig`. Fontes DTO: `sm-shop-model/.../catalog/`, `.../customer/`.

### Arquivos relevantes
- `shopizer-api-contracts/` — pacotes destino
- `sm-shop-model/.../model/catalog/`, `.../customer/` — fontes DTO
- `sm-shop/.../api/v1/product/ProductApi.java` — paths congelados
- `sm-shop/.../api/v1/customer/CustomerApi.java`

### Arquivos dependentes
- `shopizer-api-contracts/.../catalog/`, `.../customer/`, `.../client/`
- `sm-shop/.../strangler/config/Wave4ClientConfig.java`
- `sm-shop/src/main/resources/application-strangler-wave4.properties`

### ADRs relacionados
- [ADR-001](adrs/adr-001.md) — gate Onda 3
- [ADR-003](adrs/adr-003.md) — ProductSnapshot v2
- [ADR-005](adrs/adr-005.md) — CustomerSnapshot

## Entregáveis
- Pacotes contracts + 2 interfaces de client
- Profile/properties Strangler Wave4
- Testes unitários 80%+ em DTOs serializáveis **(OBRIGATÓRIO)**
- `Wave4ClientConfigTest` **(OBRIGATÓRIO)**

## Testes
- Unit: ProductSnapshot default schemaVersion 2; CustomerSnapshot default 1; sem imports core.model
- Integração: `./mvnw compile -pl shopizer-api-contracts`; `./mvnw test -pl sm-shop -Dtest=Wave4ClientConfigTest`
- Meta de cobertura: >=80%

## Critérios de sucesso
- Todos os testes passando
- Contratos compilam isolados
- wave4.* coexiste com wave1/2
- Gate da Onda 3 verificado antes de iniciar
