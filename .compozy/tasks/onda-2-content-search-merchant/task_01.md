---
status: pending
title: Contracts Content/Search/Merchant e properties Strangler Wave2
type: backend
complexity: medium
---

# Contracts Content/Search/Merchant e properties Strangler Wave2

## Overview
Consolida TLC T1–T4. Entrega os DTOs e clients de content, search e merchant em `shopizer-api-contracts`, mais o profile/properties Strangler Wave2 e RestTemplate no monólito. **Pré-requisito externo:** Execute da Onda 1 completo (reference-service, tax-service, pacote de contratos base, padrões Strangler/JWT/Pact) — esta task NÃO inicia sem esse gate.

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST migrar DTOs content para `com.salesmanager.contracts.content` e criar `ContentServiceClient` (storeCode/langCode; sem JPA) — TLC T1.
2. MUST criar `ProductIndexPayload` (`schemaVersion` default 1), `ProductIndexBulkPayload` (batch máx. 50), `ValueList` e `SearchIndexClient` — TLC T2.
3. MUST migrar DTOs merchant, criar `MerchantStoreSnapshot` e `MerchantServiceClient` — TLC T3.
4. MUST adicionar profile `strangler-wave2`, properties `wave2.*.base-url`, `wave2.strangler.enabled`, `wave2.search-service.internal-token`, bean `RestTemplate` com interceptor de correlação e stub `SearchIndexClientRestTemplateImpl` — TLC T4.
5. MUST garantir que properties `wave2.*` coexistam com `wave1.*` (TechSpec **Configuração Strangler**).
6. MUST NOT introduzir imports de `com.salesmanager.core.model` nos contracts.
7. MUST compilar `shopizer-api-contracts` e passar testes de config Wave2 em sm-shop.
</requirements>

## Subtasks
- [ ] 1.1 DTOs content + `ContentServiceClient` nos contracts (T1)
- [ ] 1.2 DTOs search índice + `SearchIndexClient` (T2)
- [ ] 1.3 DTOs merchant + `MerchantStoreSnapshot` + `MerchantServiceClient` (T3)
- [ ] 1.4 Config Wave2ClientConfig, properties e RestTemplate no sm-shop (T4)
- [ ] 1.5 Stub `SearchIndexClientRestTemplateImpl` e testes de config

## Implementation Details
Ver TechSpec: **Interfaces principais**, **Modelos de dados**, **Configuração Strangler (`sm-shop`)**, **Ordem de construção** passos 2–5. Reutilizar padrão Wave1ClientConfig da Onda 1. Fontes DTO atuais em `sm-shop-model`.

### Relevant Files
- `sm-shop-model/src/main/java/com/salesmanager/shop/model/content/` — fonte DTOs content
- `sm-shop-model/src/main/java/com/salesmanager/shop/model/store/` — fonte DTOs merchant / Configs
- `sm-shop/src/main/java/com/salesmanager/shop/store/api/v1/content/ContentApi.java` — paths REST congelados content
- `sm-shop/src/main/java/com/salesmanager/shop/store/api/v1/search/SearchApi.java` — paths search
- `sm-shop/src/main/java/com/salesmanager/shop/store/api/v1/store/MerchantStoreApi.java` — paths merchant
- `pom.xml` — reactor (módulo contracts da Onda 1)

### Dependent Files
- `shopizer-api-contracts/src/main/java/com/salesmanager/contracts/content/` — DTOs content (a criar)
- `shopizer-api-contracts/src/main/java/com/salesmanager/contracts/search/` — DTOs índice (a criar)
- `shopizer-api-contracts/src/main/java/com/salesmanager/contracts/merchant/` — DTOs merchant (a criar)
- `shopizer-api-contracts/src/main/java/com/salesmanager/contracts/client/` — clients (a criar)
- `sm-shop/.../strangler/config/Wave2ClientConfig.java` — config Wave2 (a criar)
- `sm-shop/src/main/resources/application-strangler-wave2.properties` — properties (a criar)

### Related ADRs
- [ADR-001: Um workflow Compozy para Content + Search + Merchant](adrs/adr-001.md) — bloqueio Onda 1
- [ADR-002: Contrato intermediário ProductIndexPayload](adrs/adr-002.md) — schemaVersion
- [ADR-005: APIs internas e X-Internal-Token](adrs/adr-005.md) — token de índice

## Deliverables
- Packages contracts content/search/merchant + 3 interfaces de client
- Profile e properties Strangler Wave2 no sm-shop
- Unit tests com 80%+ coverage dos DTOs serializáveis **(REQUIRED)**
- Integration/unit tests de `Wave2ClientConfig` **(REQUIRED)**

## Tests
- Unit tests:
  - [ ] `ProductIndexPayload` serializa com `schemaVersion` default 1
  - [ ] DTOs content/merchant compilam sem `com.salesmanager.core.model`
  - [ ] `Wave2ClientConfig` registra RestTemplate e lê `wave2.*.base-url`
- Integration tests:
  - [ ] `./mvnw compile -pl shopizer-api-contracts` passa
  - [ ] `./mvnw test -pl sm-shop -Dtest=Wave2ClientConfigTest` passa
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- Test coverage >=80%
- Contracts Wave2 compilam isolados
- Properties `wave2.*` coexistindo com `wave1.*`
- Gate externo Onda 1 verificado antes do início
