---
status: pending
title: Extrair sm-shoppingcart-core + validação catalog
type: backend
complexity: high
---

# Extrair sm-shoppingcart-core + validação catalog

## Visão geral
TLC T7–T9, T60. Novo módulo `sm-shoppingcart-core` com repositórios cart e `ShoppingCartServiceImpl` usando `CartTotalsClient` (sem `OrderService`). Validação de linha catalog via HTTP.

<requirements>
1. MUST fazer scaffold `sm-shoppingcart-core` com repositórios cart — T7.
2. MUST mover `ShoppingCartServiceImpl`; zero imports `OrderService` — T8, CART-03.
3. MUST adicionar `CatalogLineValidator` chamando catalog-service — T9, CART-02.
4. MUST adicionar regra ArchUnit: sem OrderService em sm-shoppingcart-core — T60.
</requirements>

## Subtarefas
- [ ] 3.1 Módulo Maven + repositórios (T7)
- [ ] 3.2 ShoppingCartService com CartTotalsClient (T8)
- [ ] 3.3 CatalogLineValidator HTTP (T9)
- [ ] 3.4 Teste ArchUnit (T60)

## Detalhes de implementação
Padrão: `sm-content-core` da Onda 2. Entidades permanecem em `sm-core-model`.

### Arquivos relevantes
- `sm-core/.../services/shoppingcart/ShoppingCartServiceImpl.java`
- `sm-core/.../repositories/shoppingcart/`

## Entregáveis
- Módulo `sm-shoppingcart-core`
- `./mvnw test -pl sm-shoppingcart-core` verde **(REQUIRED)**

## Critérios de sucesso
- ArchUnit passa
- Sem OrderService no módulo
