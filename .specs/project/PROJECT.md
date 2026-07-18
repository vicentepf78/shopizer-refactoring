# Shopizer — Decomposição Monólito → Serviços de Domínio

**Vision:** Migrar incrementalmente o monólito Shopizer 3.2.5 para serviços de domínio deployáveis, consolidando duplicações internas antes de cada extração e validando o padrão Strangler Fig com domínios de menor risco.

**For:** Equipe de arquitetura e desenvolvimento Shopizer (1–2 devs em tempo parcial de arquitetura).

**Solves:** Monólito Maven com ~1.167 arquivos Java, ~94% MODEL coupling nas facades, ciclos order↔payments e hub de checkout com 12 services — impossibilitando evolução independente por domínio sem decomposição planejada.

## Goals

- Extrair **reference-service** e **tax-service** (Onda 1) como serviços REST deployáveis com DTOs, sem expor entidades JPA na API pública
- Extrair **content-service**, **search-service** e **merchant-service** (Onda 2) replicando padrão Strangler + Pact
- Estabelecer padrão replicável (Strangler HTTP client, testes de contrato, traceability TLC) para Ondas 3–6
- Introduzir `ProductIndexPayload` como contrato mínimo de indexação (ponte para `ProductSnapshot` na Onda 3)

## Tech Stack

**Core:**

- Framework: Spring Boot 2.5.12
- Language: Java 11
- Database: JPA/Hibernate (schema compartilhado inicialmente na Onda 1)
- Build: Maven multi-módulo

**Key dependencies:**

- `sm-core-model` — entidades JPA
- `sm-core` — serviços de domínio
- `sm-shop` — REST APIs, facades, mappers
- `sm-shop-model` — DTOs e interfaces de facade
- `sm-core-modules` — contratos de integração (plugins)

## Scope

**Onda 1 (v1 desta iniciativa) inclui:**

- `reference-service`: Country, Zone, Language, Currency — CRUD + leitura localizada
- `tax-service` (admin CRUD): TaxClass, TaxRate — APIs privadas existentes espelhadas
- Strangler no monólito: HTTP clients substituindo injeção direta de services nas fronteiras definidas
- DTOs: `ReadableLanguage`, novo `ReadableCurrency`; eliminar retorno de entidades em `ReferencesApi`
- Testes de contrato (Pact ou equivalente) entre monólito e serviços extraídos

**Explicitamente fora de escopo (Onda 1):**

- `TaxService` / cálculo de impostos em checkout (permanece no monólito)
- `InitializationDatabaseImpl` — bootstrap multi-domínio (split em onda futura)
- Substituição dos ~60 callers de `LanguageService` no monólito (Onda 3 — contratos `LanguageCode`)
- Remoção de FKs JPA cross-domínio (`MerchantStore`→`Language`, `Product`→`TaxClass`)
- Ondas 3–6 (Contratos DTO completos, Catalog, Integration, Order, etc.)
- `productFileManager` / imagens de produto catalog (Onda 4)
- Quick wins de consolidação Mapper/Populator (Fase 1 — paralelo, não bloqueante)

## Constraints

- Timeline: Onda 1 estimada em 4–5 semanas (semanas 19–24 do plano mestre)
- Technical: Spring Boot 2.5.12 / Java 11; schema DB compartilhado na primeira extração; sem breaking changes em paths REST existentes
- Resources: Specify → Design → Tasks → Execute via `tlc-spec-driven`; código somente após `tasks.md` aprovado
