---
status: completed
title: DTOs Reference/Tax e interfaces de client nos contracts
type: backend
complexity: medium
---

# DTOs Reference/Tax e interfaces de client nos contracts

## Overview
Consolida TLC T3–T5. Completa o passo 2 da **Ordem de build**: migra DTOs de reference (incluindo o novo `ReadableCurrency`) e tax, e define `ReferenceServiceClient` / `TaxServiceClient` em `contracts/client` usando apenas tipos do JAR contracts (strings `storeCode`/`langCode`, sem entidades JPA).

<critical>
- ALWAYS READ the PRD and TechSpec before starting
- REFERENCE TECHSPEC for implementation details — do not duplicate here
- FOCUS ON "WHAT" — describe what needs to be accomplished, not how
- MINIMIZE CODE — show code only to illustrate current structure or problem areas
- TESTS REQUIRED — every task MUST include tests in deliverables
</critical>

<requirements>
1. MUST migrar DTOs e enums de reference para `com.salesmanager.contracts.reference` (ver TechSpec **Modelos de dados** e **Endpoints da API**).
2. MUST criar `ReadableCurrency` com campos id, code, name, symbol, supported (REF-05).
3. MUST migrar DTOs tax (+ `NamedEntity` slim se necessário) para `com.salesmanager.contracts.tax` / package catalog mínimo (TAX-02).
4. MUST criar interfaces `ReferenceServiceClient` e `TaxServiceClient` conforme TechSpec **Interfaces principais** — apenas tipos contracts.
5. MUST NOT importar `com.salesmanager.core.model` nem `MerchantStore`/`Language` JPA nas interfaces de client.
6. SHOULD manter forma JSON compatível com payloads atuais de storefront/admin (STR-04).
7. MUST compilar `./mvnw compile -pl shopizer-api-contracts`.
</requirements>

## Subtasks
- [x] 2.1 Migrar DTOs/enums reference e criar `ReadableCurrency`
- [x] 2.2 Migrar DTOs tax e `NamedEntity` slim
- [x] 2.3 Definir `ReferenceServiceClient` e `TaxServiceClient`
- [x] 2.4 Garantir ausência de imports JPA/core.model
- [x] 2.5 Testes de serialização dos DTOs-chave (currency, tax rate, language)

## Implementation Details
Ver TechSpec: **Interfaces principais** (`ReferenceServiceClient`), **Modelos de dados**, **Endpoints da API**. Fontes: `sm-shop-model/.../model/references/*` e `.../model/tax/*`.

### Relevant Files
- `sm-shop-model/src/main/java/com/salesmanager/shop/model/references/ReadableCountry.java` — fonte country DTO
- `sm-shop-model/src/main/java/com/salesmanager/shop/model/references/ReadableLanguage.java` — fonte language (só id/code/sortOrder no wire)
- `sm-shop-model/src/main/java/com/salesmanager/shop/model/references/SizeReferences.java` — measures
- `sm-shop-model/src/main/java/com/salesmanager/shop/model/tax/PersistableTaxRate.java` — fonte tax rate write
- `sm-shop-model/src/main/java/com/salesmanager/shop/model/tax/ReadableTaxClass.java` — fonte tax class read
- `shopizer-api-contracts/src/main/java/com/salesmanager/contracts/common/` — wrappers da task_01

### Dependent Files
- `shopizer-api-contracts/.../contracts/reference/*` — DTOs reference (a criar)
- `shopizer-api-contracts/.../contracts/tax/*` — DTOs tax (a criar)
- `shopizer-api-contracts/.../contracts/client/ReferenceServiceClient.java` — interface (a criar)
- `shopizer-api-contracts/.../contracts/client/TaxServiceClient.java` — interface (a criar)

### Related ADRs
- [ADR-005: Contract DTOs / no JPA in REST responses; Pact](adrs/adr-005.md) — pureza DTO + base Pact
- [ADR-006: GeoZone excluded from Wave 1](adrs/adr-006.md) — não criar API/DTO GeoZone

## Deliverables
- Packages reference + tax + client no contracts
- `ReadableCurrency` novo e serializável
- Unit tests com 80%+ coverage dos DTOs novos/críticos **(REQUIRED)**
- Compile gate do módulo contracts **(REQUIRED)**

## Tests
- Unit tests:
  - [x] `ReadableCurrency` serializa id, code, name, symbol, supported
  - [x] `ReadableLanguage` JSON contém apenas id, code, sortOrder (sem campos JPA)
  - [x] `PersistableTaxRate` / `ReadableTaxRate` round-trip com descriptions i18n
  - [x] Assinaturas de `ReferenceServiceClient` aceitam apenas String codes (sem MerchantStore)
- Integration tests:
  - [x] `./mvnw compile -pl shopizer-api-contracts` passa
- Test coverage target: >=80%
- All tests must pass

## Success Criteria
- All tests passing
- Test coverage >=80%
- Nenhum import `com.salesmanager.core.model` no módulo contracts
- Interfaces de client usam só tipos contracts
