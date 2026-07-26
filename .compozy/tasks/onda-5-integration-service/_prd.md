# PRD: Onda 5 — Integration Service

**Slug da feature:** `onda-5-integration-service`
**Fonte da verdade:** TLC em `.specs/features/onda-5-integration-service/` (Opção A — autoritativa; escopo congelado)
**Status:** Pronto para TechSpec
**Data:** 2026-07-26

---

## Visão geral

Após as Ondas 1–2 validarem o padrão Strangler e a Onda 3 entregar contratos DTO mais fundação de saga de checkout, a Onda 5 extrai a **orquestração de pagamento e cotação de frete** para `integration-service`, para que operadores de loja e clientes possam configurar gateways, obter cotações de frete e processar pagamentos sem que essas capacidades permaneçam presas no runtime `sm-core` do monólito.

O problema hoje é arquitetural: os contratos `PaymentModule` e `ShippingQuoteModule` aceitam entidades JPA (`Order`, `Customer`, `ShoppingCartItem`), e `PaymentServiceImpl` grava status do pedido após chamadas ao gateway — criando o **ciclo order ↔ payments** que torna Order o domínio mais difícil de extrair (9/10). A orquestração de frete é menos acoplada, mas ainda puxa preço de catálogo e dados de referência in-process. A Onda 5 deve ser **stateless em relação a Order** — integration retorna DTOs de transação e cotação; o application service de checkout (Onda 3) é dono das mutações de pedido.

Este PRD define **o quê e o porquê de negócio** de um serviço implantável atrás do BFF shop existente, com caminhos REST congelados e sem quebras de API voltadas ao usuário. O como técnico fica para a TechSpec e os ADRs.

**Usuários primários:** administradores de loja (config de pagamento/frete), clientes da vitrine (cotações e pagamento no checkout), engenheiros de plataforma (rollout Strangler) e arquitetos desbloqueando o split order/payments da Onda 6.

**Pré-requisitos rígidos:** Execute da Onda 3 completo (módulo de contratos DTO, application service de checkout, fundação saga/outbox); Onda 4 parcial (caminhos de leitura de catálogo para snapshots de produto de frete).

---

## Objetivos

- Entregar **integration-service** como capability implantável independentemente para orquestração de módulos de pagamento e cotação de frete, enquanto o monólito permanece o BFF voltado ao cliente.
- Preservar jornadas admin e vitrine existentes (setup de módulo de pagamento, configuração de frete, cotações de frete do carrinho, pagamento no checkout) com respostas equivalentes ao monólito de hoje.
- **Quebrar o ciclo order ↔ payments** garantindo que integration-service nunca persista nem mute entidades `Order`.
- Hospedar o registry de plugins existente (`Stripe`, `PayPal`, `UPS`, `USPS`, regras de peso, etc.) in-process dentro de integration-service — plugins permanecem bibliotecas, não microsserviços separados.
- Provar estabilidade de contrato (Pact consumer/provider) para superfícies P1 migradas antes de declarar a Onda 5 concluída.
- Estender `shopizer-api-contracts` com DTOs de integration e clients HTTP seguindo padrões das Ondas 1–2.
- Permanecer **bloqueada em Onda 3 + Onda 4 parcial** — o Execute da Onda 5 não inicia até esses gates passarem.

### Resultados de negócio

| Resultado | Indicador |
| ------- | ----------- |
| Onda 6 desbloqueada | Caminho de processamento de pagamento não exige mais `OrderService` in-process a partir de integration |
| Continuidade para merchants | Sem breaking change nos caminhos REST de pagamento/frete usados pela UI admin e checkout |
| Isolamento de gateway | Novos deploys de pagamento/frete não exigem release do monólito |
| Ranking de acoplamento honesto | Onda agendada em 9º lugar nos dados — não apressada como 4ª prioridade |

---

## Histórias de usuário

### Administrador de loja — configuração de módulo de pagamento (P1 / PAY)

Como **administrador de loja**, quero configurar módulos de pagamento pelas mesmas APIs admin que uso hoje, para que credenciais de gateway e habilitação de módulos não sejam de propriedade do runtime do monólito.

**Aceite (negócio):**

1. Listar, criar, atualizar e excluir configurações de módulos de pagamento por loja.
2. Segredos armazenados criptografados — mesma postura de segurança de hoje.
3. Listagem pública de métodos de pagamento da loja reflete módulos configurados.
4. Tenant identificado por store code como hoje.

**IDs de requisito:** PAY-01…PAY-06

### Cliente da vitrine — cotações de frete (P1 / SHP)

Como **cliente**, quero opções de frete para meu carrinho pelas APIs existentes, para escolher entrega sem o monólito executar plugins de transportadora in-process.

**Aceite:**

1. Respostas de cotação batem com o schema atual (`ReadableShippingQuote`, lista de opções).
2. Lista de países de entrega localizada via capability de reference (Onda 1).
3. Carrinhos apenas digitais retornam frete não necessário.
4. Pesos de produto vêm da API de leitura de catálogo (Onda 4 parcial) — não do grafo JPA do monólito.

**IDs de requisito:** SHP-01…SHP-07

### Checkout — processamento de pagamento stateless (P1 / PAY)

Como **fluxo de checkout**, quero autorização/captura/reembolso executados via integration-service retornando resultado de transação, para que atualizações de status do pedido permaneçam na saga de checkout e o ciclo payments seja quebrado.

**Aceite:**

1. Process, capture, refund e init (express checkout) retornam DTOs `TransactionResult`.
2. Integration-service pode persistir registros `Transaction`, mas **não deve** atualizar `Order`.
3. Falhas de gateway aparecem claramente; saga de checkout trata compensação.
4. Pedido referenciado apenas por id de snapshot — sem entidade `Order` cruzando a fronteira.

**IDs de requisito:** PAY-07…PAY-12

### Time de plataforma — Strangler BFF (P1 / STR)

Como **engenheiro de plataforma**, quero delegação HTTP para facades de pagamento/frete atrás de `wave5.strangler.enabled`, para validar extração sem reescrever controllers de checkout.

**Aceite:**

1. Strangler ligado → facades delegam a integration-service; desligado → legado in-process.
2. Falha remota → 503 com correlation id — sem fallback silencioso.
3. `OrderPaymentApi` roteia via application service de checkout + client de integration.
4. `OrderShippingApi` monta requests DTO a partir de snapshots de carrinho + catálogo.

**IDs de requisito:** STR-01…STR-06

### Desenvolvedor — confiança de contrato (P2 / STR)

Como **desenvolvedor**, quero testes Pact para superfícies P1 de config de pagamento e cotação de frete, para que mudanças quebradoras de DTO falhem no CI antes do deploy.

**IDs de requisito:** STR-07, STR-08

### Operador — observabilidade (P2 / STR)

Como **operador**, quero health checks e correlation IDs em integration-service, incluindo dependências de DB, registry de módulos, reference-service e catalog-service.

**IDs de requisito:** STR-08, STR-09

---

## Capacidades centrais

### F1 — integration-service (MVP)

Dono de orquestração de pagamento/frete, registry de plugins, persistência de configuração de integração do merchant (DB compartilhado) e APIs internas de pagamento/cotação. Porta 8086. Stateless em relação a Order.

### F2 — sm-integration-core (MVP)

Módulo de domínio thin: `PaymentOrchestrator`, `ShippingOrchestrator`, implementações de plugins movidas, regras de empacotamento, criptografia de credenciais.

### F3 — Strangler BFF (MVP)

Adaptadores HTTP para facades de configuração de pagamento/frete; wiring de checkout para `OrderPaymentApi` / `OrderShippingApi`; properties `wave5.*` coexistindo com wave1–4.

### F4 — Testes de contrato e Compose (Fase 2)

Pact provider/consumer; `docker-compose-wave5.yml`; gates JaCoCo.

---

## Experiência do usuário

| Persona | Objetivo |
| ------- | ---- |
| Admin | Configurar módulos Stripe/PayPal/frete sem notar mudança de runtime |
| Cliente | Ver opções de frete e concluir pagamento como hoje |
| Engenheiro de plataforma | Alternar strangler; observar health; confiar nos gates Pact |

**Restrições de UX:** Sem telas novas; paridade comportamental é a barra. p95 endpoints públicos ≤ 2× baseline do monólito.

---

## Restrições técnicas de alto nível

- Integrar com **reference-service** (Onda 1) para resolução de país/idioma em frete.
- Integrar com **API de leitura do catalog-service** (Onda 4 parcial) para snapshots de peso/dimensão de produto.
- Preservar **caminhos REST congelados** (STR-06).
- Sem entidades JPA em respostas JSON migradas.
- **Banco operacional compartilhado** durante extração (AD-003 herdado).
- JWT em `/private/**` equivalente às ondas anteriores.
- Execute bloqueado até **Onda 3 + Onda 4 parcial** completas.

---

## Não-objetivos

| Excluído | Por quê |
| -------- | --- |
| Execute antes de Onda 3 + Onda 4 parcial | Gates rígidos — contratos e leituras de catálogo |
| Extração de order / shopping cart service | Onda 6 |
| Novos provedores de gateway de pagamento | Fora de escopo |
| Split database-per-service | AD-003 |
| Feign/WebClient/service mesh | Padrão AD-005 RestTemplate |
| Corrigir stubs null de payment/shipping em `ConfigurationsApi` | Legado incompleto |
| Extração CRUD completa de catálogo | Onda 4 |
| Substituir todas as transações globais de checkout | Fundação saga Onda 3; saga completa de pedido Onda 6 |

---

## Rollout faseado

### MVP (Fase 1) — histórias P1

- integration-service: CRUD de config, cotações, APIs internas de pagamento stateless.
- sm-integration-core com plugins movidos.
- Adaptadores Strangler + wiring de checkout.
- Profile do monólito preservado para rollback.

**Critérios de saída:** endpoints P1 saudáveis; sem writes em Order a partir de integration-service; pact verde para P1.

### Fase 2

- Suite Pact completa; Docker Compose wave5; gates JaCoCo verify.

### Fase 3

- STATE/ROADMAP atualizados; GAP-INT documentado; padrão reutilizável para Onda 6.

---

## Métricas de sucesso

| Métrica | Meta |
| ------ | ------ |
| Disponibilidade endpoints P1 | integration-service + caminhos BFF respondem |
| Paridade de contrato | Pact verde config pagamento + cotação frete |
| Vazamento de entidade | Zero tipos JPA em JSON migrado |
| Quebra de ciclo | Sem chamada `OrderService` no caminho de pagamento de integration |
| Precisão de cotação | Opções retornadas para módulos UPS/custom configurados |
| Latência | p95 ≤ 2× monólito |
| Disciplina de gate | Sem Execute Onda 5 antes de Onda 3 + Onda 4 parcial |

---

## Riscos e mitigações

| Risco | Mitigação |
| ---- | ---------- |
| Atraso da Onda 3 bloqueia calendário | Manter docs Compozy prontos; congelar Execute |
| Snapshot de catálogo incompleto para empacotamento | Fallback GAP-INT-01 documentado |
| Plugins legados resistem contratos V2 | Ponte adaptadora AD-017 |
| Merchants notam regressões de pagamento | Profile Strangler rollback; pact |
| Scope creep em extração de order | Não-objetivos explícitos; ADR stateless |

---

## Registros de decisão arquitetural

- [ADR-001: Workflow Compozy único para integration-service](adrs/adr-001.md)
- [ADR-002: Orquestração de pagamento stateless — sem ownership de Order](adrs/adr-002.md)
- [ADR-003: MySQL compartilhado para configuração de integration](adrs/adr-003.md)
- [ADR-004: PaymentModuleV2 / ShippingQuoteModuleV2 da Onda 3](adrs/adr-004.md)
- [ADR-005: Registry de plugins in-process em integration-service](adrs/adr-005.md)
- [ADR-006: APIs de checkout permanecem no BFF com application service de checkout](adrs/adr-006.md)
- [ADR-007: Leitura HTTP de catálogo para snapshots de produto de frete](adrs/adr-007.md)

---

## Questões em aberto

Todas as OQ-01…OQ-06 do TLC **resolvidas** em `.specs/features/onda-5-integration-service/context.md`. Não restam ambiguidades de produto bloqueadoras.

Residual: conjunto exato de campos de snapshot de catálogo para empacotamento — confirmar na revisão do gate Onda 4 parcial.
