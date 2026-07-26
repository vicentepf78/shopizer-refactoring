# PRD: Onda 4 — Catalog + Customer

**Slug da feature:** `onda-4-catalog-customer`
**Fonte da verdade:** TLC em `.specs/features/onda-4-catalog-customer/` (Opção A — autoritativa; escopo congelado)
**Status:** Pronto para TechSpec
**Data:** 2026-07-26

---

## Visão geral

Após as Ondas 1–2 validarem a extração Strangler para reference, tax, content, search e merchant, e a Onda 3 entregar contratos cross-service (`ProductSnapshot`, `CustomerSnapshot`, `LanguageCode`, `MerchantStoreId`), a Onda 4 extrai capacidades de **leitura de catálogo** e **perfil de cliente** para serviços independentemente implantáveis.

O problema é acoplamento: o catálogo tem o **maior acoplamento aferente** em `sm-core` (10 referências inbound). Extrair CRUD completo de catálogo arrastaria order, cart, shipping e search para um monólito distribuído. Customer é relativamente isolado no nível de serviço, mas está **transacionalmente ligado à criação de pedidos** e ao **merge de carrinho** no login.

Este PRD define **o quê e o porquê** da Onda 4: dois serviços atrás do BFF existente, caminhos REST congelados, fronteira de catálogo read-first e desacoplamento de merge de carrinho via `CustomerSnapshot`. O como técnico fica na TechSpec e nos ADRs.

**Usuários primários:** visitantes da vitrine, admins de loja (catálogo read-only via serviço; writes ainda no monólito), clientes registrados, engenheiros de plataforma operando o rollout Strangler.

---

## Objetivos

- Entregar serviços implantáveis de **leitura de catálogo** e **perfil de cliente** enquanto `sm-shop` permanece o BFF voltado ao cliente.
- Preservar jornadas existentes de vitrine e admin para navegação de produtos/categorias, perfil de cliente, endereços e opt-in.
- Permitir que a indexação de busca use **`ProductSnapshot`** canônico (Onda 3) em vez do `ProductIndexPayload` intermediário.
- Desacoplar **merge de carrinho** do `CustomerService` in-process usando HTTP com **`CustomerSnapshot`**.
- Manter **mutações admin de catálogo** (CRUD privado de produto) no monólito na Onda 4.
- Provar estabilidade de contrato (Pact) para superfícies P1 antes de declarar a Onda 4 concluída.
- Estender `shopizer-api-contracts` e thin cores (`sm-catalog-core`, `sm-customer-core`).
- **Bloqueado até a conclusão do Execute da Onda 3** — nenhum código da Onda 4 antes do gate de contratos.

### Resultados de negócio

| Resultado | Indicador |
| ------- | ----------- |
| Decomposição de catálogo mais segura | Caminho de leitura sai do monólito sem mover o grafo de write dos 22 serviços de catálogo |
| Isolamento do domínio customer | CRUD de perfil pertence ao customer-service; auth permanece centralizada |
| Upgrade do contrato de busca | Indexação `ProductSnapshot` v2 end-to-end |
| Prontidão para merge de carrinho | Merge no login funciona com snapshot remoto de customer |
| Continuidade | Sem breaking change nos caminhos REST congelados |

---

## Histórias de usuário

### Visitante da vitrine — navegar produtos e categorias (P1 / CAT)

Como **visitante da vitrine**, quero navegar produtos e categorias pelas mesmas APIs públicas, para que a descoberta de produtos não exija serviços de catálogo in-process.

**Aceite (negócio):**

1. Listagens paginadas de produtos, detalhe, lookup por SKU, produtos relacionados e grupos retornam DTOs legíveis.
2. Árvore de categorias e detalhe de categoria são localizados por idioma.
3. Leituras públicas de inventário e preço funcionam sem dependências de order/cart.
4. Contexto de loja e idioma usam value types da Onda 3 via serviços HTTP reference/merchant.
5. Indisponibilidade do catálogo expõe falha clara — sem catálogo vazio silencioso.

**IDs de requisito:** CAT-01…CAT-07, CAT-12

### Plataforma — ProductSnapshot para busca e integrações (P1 / CAT)

Como **engenheiro de plataforma**, quero um contrato versionado de snapshot de produto, para que indexação de busca e serviços futuros compartilhem um modelo de leitura de produto.

**IDs:** CAT-08, CAT-09, STR-07

### Cliente registrado — perfil e endereços (P1 / CUS)

Como **cliente registrado**, quero visualizar e atualizar meu perfil, endereços de entrega/cobrança e opt-in de marketing pelas APIs existentes.

**IDs:** CUS-01…CUS-07

### Cliente recorrente — merge de carrinho no login (P1 / CUS)

Como **cliente recorrente**, quero que meu carrinho de sessão seja mesclado com o carrinho salvo após o login, sem transação distribuída entre cart e customer services.

**IDs:** CUS-08, CUS-09, STR-08

### Plataforma — Strangler BFF (P1 / STR)

Como **engenheiro de plataforma**, quero delegação HTTP para facades de leitura de catálogo e perfil de cliente, com writes admin de produto permanecendo in-process.

**IDs:** STR-01, STR-04, STR-06, AD-020

### Admin — imagens de produto (P2 / CAT)

Como **admin de loja**, quero imagens de produto/variante armazenadas via content-service (adiamento da Onda 2 concluído).

**IDs:** CAT-10

### Desenvolvedor — confiança de contrato (P2 / STR)

Como **desenvolvedor**, quero testes Pact para endpoints de leitura de catálogo e perfil de cliente.

**IDs:** STR-02, CAT-11, CUS-10

### Operador — observabilidade (P3 / STR)

Como **operador**, quero health checks e correlation IDs em catalog-service e customer-service.

**IDs:** STR-05

---

## Funcionalidades principais

### F1 — Catalog service (MVP, fronteira read-only)

Possui APIs de **leitura** de vitrine para produtos, categorias, fabricantes, inventário e preços; API interna `ProductSnapshot`. **Não** possui mutações admin de produto.

### F2 — Customer service (MVP)

Possui perfil, endereço, opt-in (e leitura de reviews); API interna `CustomerSnapshot`. **Não** possui login/emissão de JWT.

### F3 — Desacoplamento de merge de carrinho (MVP)

Monólito orquestra merge usando `CustomerSnapshot` do customer-service; `ShoppingCartService` refatorado para evitar dependência de entidade customer in-process.

### F4 — Migração de snapshot de busca (MVP)

`ProductSnapshotBuilder` substitui `ProductIndexPayloadBuilder`; search-service aceita v2.

### F5 — Strangler BFF (MVP)

Adaptadores HTTP com `wave4.strangler.enabled`; writes admin permanecem locais.

### F6 — Imagens de produto via content (Fase 2)

Product file managers chamam content-service HTTP.

### F7 — Contratos e observabilidade (Fases 2–3)

Pact; health; propagação de correlação.

---

## Experiência do usuário

| Persona | Objetivo |
| ------- | ---- |
| Visitante | Navegar/buscar catálogo inalterado |
| Cliente | Gerenciar perfil/endereços |
| Admin | Editar produtos no monólito (UX de write inalterada) |
| Engenheiro de plataforma | Alternar strangler; observar health; confiar nos gates pact |

**Restrição de UX:** Sem novas telas — apenas paridade comportamental. p95 de leitura pública ≤ 2× baseline do monólito.

---

## Restrições técnicas de alto nível

- Integrar serviços da Onda 1 **reference** e Onda 2 **merchant** / **content** / **search**.
- Preservar **caminhos REST congelados** (STR-04).
- Sem entidades JPA nas respostas JSON migradas.
- **DB operacional compartilhado** (AD-003/AD-022).
- **Onda 3 completa** antes do Execute.
- JWT em rotas privadas de customer equivalente ao de hoje.
- Catálogo **read-only** na fronteira do serviço (AD-020).

---

## Não-objetivos

| Excluído | Motivo |
| -------- | --- |
| Execute antes da Onda 3 | Pré-requisito ProductSnapshot/CustomerSnapshot |
| Writes admin de catálogo no catalog-service | Extração faseada do plano mestre; acoplamento 10/10 |
| shoppingcart-service | Onda 6 |
| Extração de order/checkout | Onda 6 |
| Login/registro de customer no customer-service | Autoridade de auth permanece sm-shop (OQ-06) |
| Split DB-per-service | AD-022 |
| Merge completo de facades de produto (4 facades) | Trabalho paralelo Fase 1 |
| Redesign de integração payment/shipping | Onda 5 |

---

## Rollout faseado

### MVP (Fase 1) — histórias P1

- catalog-service leitura pública + snapshot interno
- customer-service profile/address/optin + snapshot interno
- Desacoplamento de merge de carrinho
- Indexação ProductSnapshot
- Adaptadores Strangler (somente read/profile)

**Saída:** endpoints P1 saudáveis; sem JPA no JSON; pact verde para P1; teste de merge passa.

### Fase 2

- Imagens de produto via content-service
- Suite Pact completa; docker-compose-wave4.yml

### Fase 3

- Indicadores de health; STATE/rastreabilidade; docs GAP

---

## Métricas de sucesso

| Métrica | Meta |
| ------ | ------ |
| Disponibilidade endpoints P1 | Ambos os serviços + caminhos BFF respondem |
| Paridade de contrato | Pact verde catalog + customer |
| Vazamento de entidades | Zero tipos JPA no JSON migrado |
| Search v2 | Índice aceita ProductSnapshot |
| Merge de carrinho | Teste de integração com snapshot remoto |
| Latência | p95 leitura pública ≤ 2× baseline |
| Disciplina de pré-requisito | Sem Execute antes do gate da Onda 3 |

---

## Riscos e mitigações

| Risco | Mitigação |
| ---- | ---------- |
| Atraso da Onda 3 | Docs prontos; Execute bloqueado |
| Confusão split read/write de catálogo | AD-020; matriz de adaptadores explícita |
| Regressão de merge de carrinho | Testes de integração dedicados; fail-closed em snapshot ausente |
| Drift ProductSnapshot vs índice | schemaVersion + pact |
| Scope creep (writes admin) | Não-objetivos congelados |

---

## Registros de decisão arquitetural

- [ADR-001: Um workflow Compozy para Catalog + Customer](adrs/adr-001.md)
- [ADR-002: Extração read-only de catálogo primeiro](adrs/adr-002.md)
- [ADR-003: ProductSnapshot como contrato canônico de produto](adrs/adr-003.md)
- [ADR-004: Módulos thin sm-catalog-core / sm-customer-core](adrs/adr-004.md)
- [ADR-005: Merge de carrinho via CustomerSnapshot orquestrado no monólito](adrs/adr-005.md)
- [ADR-006: Writes admin de catálogo permanecem no monólito](adrs/adr-006.md)
- [ADR-007: Imagens de produto via content-service](adrs/adr-007.md)

---

## Questões em aberto

Todas OQ-01…OQ-06 resolvidas em `.specs/features/onda-4-catalog-customer/context.md`. Nenhuma ambiguidade de produto bloqueante permanece.

Residual (não bloqueante):

- TTL exato de cache para adaptador de leitura de catálogo — tuning durante Execute.
- Se POST de review migra para customer-service na Onda 4 ou Onda 5 — padrão read-only na Onda 4.
