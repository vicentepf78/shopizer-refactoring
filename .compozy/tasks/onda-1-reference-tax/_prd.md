# PRD: Wave 1 — Extração dos serviços Reference + Tax

**Feature slug:** `onda-1-reference-tax`  
**Fonte de verdade:** pacote TLC `.specs/features/onda-1-reference-tax/` (ADR-001)  
**Date:** 2026-07-18

---

## Visão geral

O Shopizer hoje executa dados de referência (countries, zones, languages, currencies) e administração de tax (tax classes e rates) no mesmo runtime do monolito que catalog, order e checkout. Esse acoplamento impede o deploy independente de domínios de baixo risco e deixa as APIs públicas vazando modelos internos de persistência.

A Wave 1 extrai **reference** e **tax admin** como capacidades independentemente deployáveis, para que o time de plataforma prove um padrão seguro de migração antes de enfrentar domínios de alto acoplamento (catalog, order). Clientes de storefront e admin mantêm as mesmas URLs e a mesma experiência de auth; operadores ganham dois serviços que podem ser health-checked, escalados e contratados de forma independente. O **cálculo** de tax usado no checkout permanece no monolito para não colocar em risco os totais de order.

**Para quem é**

- Times de plataforma / engenharia que executam o roadmap de decomposição
- Administradores de loja que gerenciam tax classes e rates
- Consumidores das APIs de storefront e admin que já chamam endpoints de reference e tax privados

**Por que é valioso**

- Valida a migração Strangler com os domínios de menor risco (reference ~3/10, tax admin ~4/10)
- Estabelece padrões reutilizáveis de contrato HTTP e testes consumer-driven para as Waves 2–6
- Remove o vazamento de entidades JPA das respostas públicas/privadas da Wave 1 sem quebrar clients

---

## Objetivos

1. **Deployabilidade independente** — `reference-service` e `tax-service` rodam como aplicações standalone e passam health checks em um ambiente de integração (REF-01, TAX-01).
2. **Experiência transparente para o client** — Os paths REST existentes de reference e tax privado continuam funcionando sem breaking change para os clients da API (STR-04, REF-02, TAX-02).
3. **Contratos de resposta limpos** — Endpoints migrados retornam payloads no formato DTO apenas; sem entidades JPA (`Language`, `Currency`, `TaxClass`, `TaxRate`) no JSON (REF-04, REF-05, REF-06).
4. **Cutover seguro do monolito** — Quando o modo Strangler da Wave 1 está habilitado, o monolito delega as boundaries definidas de Reference/Tax admin aos serviços extraídos; quando os serviços estão fora, os clients veem falha clara (HTTP 503), não um fallback silencioso in-process (REF-09, TAX-10, STR-01).
5. **Rede de segurança de contratos** — Testes automatizados consumer/provider cobrem todos os endpoints migrados da Wave 1 para que drift de schema falhe no CI antes do deploy (STR-02, TAX-08).
6. **Continuidade do checkout** — O cálculo de tax de order continua funcionando in-process com zero regressão nos totais de order (TAX-07).
7. **Template piloto** — Padrão documentado e repetível para ondas posteriores (critérios de sucesso do plano mestre).

**Milestone:** Wave 1 completa quando features P1 e gates de contrato estão verdes; observabilidade operacional (P3) é obrigatória antes de tráfego de produção.

---

## Histórias de usuário

### Primária — Consumidor da API (integração storefront / admin)

**Como** consumidor da API do Shopizer,  
**eu quero** endpoints estáveis para countries, zones, languages, currencies e measures,  
**para que** a configuração da loja e do checkout não dependa de modelos internos de persistência.

Intenção de aceite (REF-01..REF-06, REF-10):

- Listas de countries localizadas com zones aninhadas
- Zones por country code; languages e currencies como listas de DTO
- Measures inalterados em relação ao contrato de hoje
- Sem campos só-JPA nas respostas

### Primária — Administrador da loja

**Como** administrador autenticado da loja,  
**eu quero** criar, listar, atualizar e excluir tax classes e tax rates pelas APIs privadas existentes,  
**para que** eu possa configurar a tributação da loja sem depender do wiring interno de services do monolito.

Intenção de aceite (TAX-01..TAX-06, TAX-09):

- CRUD completo de tax classes com escopo da loja autenticada
- Tax rates com descriptions localizadas; country/zone codes resolvidos corretamente
- Checks `exists` retornam existência booleana, não falhas duras para codes ausentes
- Acesso não autorizado ou de loja errada rejeitado como hoje (401/403)

### Primária — Time de plataforma

**Como** engenheiro de plataforma,  
**eu quero** que o monolito delegue o tráfego de Reference e Tax admin aos serviços extraídos quando o modo Wave 1 estiver ligado,  
**para que** validemos a extração sem reescrever de uma vez todos os callers in-process de language/country.

Intenção de aceite (STR-01, REF-09, TAX-10):

- Liga/desliga configurável entre comportamento remoto vs. local (local para o dia a dia de desenvolvimento)
- Produção Wave 1 usa delegação remota
- Timeouts e falhas são visíveis (erros amigáveis a correlação)

### Secundária — Desenvolvedor / QA

**Como** desenvolvedor,  
**eu quero** testes de contrato automatizados entre o monolito e os serviços extraídos,  
**para que** mudanças de DTO que quebrem compatibilidade falhem o build antes do merge.

### Secundária — Operador

**Como** operador,  
**eu quero** status de health e identificadores de correlação entre monolito e serviços da Wave 1,  
**para que** eu possa monitorar com segurança a primeira extração em produção.

---

## Funcionalidades principais

### F1 — Dados públicos de reference (P1 / MVP) — REF-*

Oferece acesso de leitura a dados geográficos e de localização via paths públicos existentes:

- Countries (nomes localizados, zones aninhadas)
- Zones por country ISO code
- Languages e currencies como DTOs legíveis (incluindo um DTO adequado de currency; sem entidade crua)
- Measures / size references

**Por que é importante:** Domínio de menor risco; desbloqueia criação de tax rate que depende de country/zone codes; piloto Strangler ideal.

### F2 — Administração privada de tax (P1 / MVP) — TAX-*

Oferece CRUD admin autenticado para:

- Tax classes (com escopo de store)
- Tax rates (com descriptions i18n; country/zone codes validados)

**Garantias comportamentais:**

- Country/zone codes inválidos → erro de validação do client; sem associações órfãs
- Delete de tax class ainda referenciada por products → conflict (não pode excluir em uso)
- Code de tax-rate ausente no check de existência → `{exists: false}` sem lançar exceção

**Por que é importante:** Superfície admin isolada com auth clara; compartilha a onda com reference porque a criação de rate precisa de resolução country/zone.

### F3 — Monolito como porta de entrada Strangler (P1 / MVP) — STR-*

Clients continuam chamando o monolito. No modo Wave 1, o monolito encaminha as boundaries definidas das facades de Reference e Tax admin aos serviços remotos. O comportamento local in-process permanece disponível para desenvolvimento quando as URLs remotas não estão configuradas.

**Por que é importante:** Evita rewrite big-bang de 60+ callers de language e preserva concerns de BFF (auth, resolução store/lang).

### F4 — Testes de contrato (P2)

Contratos consumer-driven para todos os endpoints migrados P1, para que mudanças de schema do provider falhem a verificação do consumer no CI.

### F5 — Anti-corrupção DTO em reference (P2)

Respostas de reference expõem apenas os campos pretendidos (ex.: language: id, code, sortOrder). Assinaturas internas das facades do monolito ainda podem carregar tipos de entidade legados até uma onda posterior.

### F6 — Observabilidade (P3)

Endpoints de health e propagação de correlation id entre monolito ↔ serviços; visibilidade básica de latência antes do cutover de produção.

---

## Experiência do usuário

### Personas e objetivos

| Persona | Objetivo |
| ------- | -------- |
| Integrador de storefront | Carregar countries/zones/languages/currencies para forms e checkout |
| Admin da loja | Gerenciar tax classes/rates na UI admin sem perceber a divisão de backend |
| Engenheiro de plataforma | Alternar modo remoto Wave 1; comparar paridade; avançar/reverter |
| Operador | Ver health do serviço e rastrear uma request falha entre hops |

### Fluxos principais

1. **Lookup de reference** — Client chama a URL pública de reference existente → recebe lista DTO localizada → configura store/checkout.
2. **Admin de tax class/rate** — Admin autentica (login permanece no monolito) → chama URLs privadas de tax existentes → CRUD só sucede para a loja dele.
3. **Modo remoto Wave 1** — Plataforma habilita delegação remota Wave 1 → mesmas URLs do client → monolito encaminha → serviços extraídos respondem → client vê payloads equivalentes.
4. **Visibilidade de falha** — Se um serviço Wave 1 estiver indisponível sob modo remoto → client recebe erro estruturado de service-unavailable (sem fallback local silencioso).

### Considerações de UX / acessibilidade

- Sem novas telas de UI para o usuário final na Wave 1; integrações existentes de admin e storefront mantêm URLs e formatos de payload.
- Corpos de erro para novos casos de conflict (tax class em uso) devem ser estruturados e legíveis para ferramentas admin.
- Preservar a semântica atual de resolução language/store para defaults de query/header.

### Onboarding / descoberta

- Operadores aprendem a topologia Wave 1 via docs de deployment (compose/ambiente de integração).
- Desenvolvedores descobrem expectativas de contrato via falhas Pact no CI, não via incidentes em produção.

---

## Restrições técnicas de alto nível

Apenas boundaries (implementação pertence ao TechSpec):

- Deve integrar-se à superfície de API existente do Shopizer; sem renomear paths voltados ao client (STR-04).
- Deve preservar a semântica de autenticação admin nas rotas privadas de tax (TAX-05).
- Não deve exigir split de schema de banco na Wave 1 (STR-03 / decisão de produto).
- Não deve mudar o ownership do cálculo de tax do checkout na Wave 1 (TAX-07).
- Ambientes greenfield ainda dependem do bootstrap/seed do monolito antes que os serviços Wave 1 assumam dados de reference/tax populados.
- Meta de performance voltada ao usuário: p95 dos endpoints de reference ≤ 2× o baseline atual do monolito (objetivo operacional P3).
- Segurança: tax privado permanece autenticado; sem enfraquecer o isolamento por store.

---

## Não-objetivos (fora de escopo)

| Excluído | Motivo |
| -------- | ------ |
| Cálculo de tax / engine de totais de order | Alto acoplamento; permanece no monolito (TAX-07, AD-002) |
| Mover o bootstrap multi-domínio de database para fora do monolito | Hub cross-domain; onda posterior (AD-004) |
| Substituir ~60 callers in-process de language em catalog/order/customer | Precisa de identidade sistêmica de language; Wave 3 |
| Remover foreign keys cross-domain no schema compartilhado | Evolução de schema após Wave 3 |
| APIs de GeoZone / GeoZoneDescription | Sem service layer hoje (OQ-01 / AD-007) |
| Schemas de database dedicados por serviço | Explicitamente adiado (AD-003) |
| APIs de TaxConfiguration | Hoje armazenado em merchant/system configuration |
| Exposição da hierarquia piggyback/parent de TaxRate | Campo existe; não está na API atual |
| Quick wins de consolidação de mapper/populator da fase 1 | Paralelo, não bloqueante |

---

## Plano de rollout por fases

### MVP (Fase 1) — Features P1

- Serviços deployáveis de reference e tax-admin
- Delegação Strangler do monolito para as facades definidas
- Respostas só-DTO nos endpoints migrados
- Feature flag / profile para local vs. remoto (dev vs. prod Wave 1)

**Critérios de sucesso para avançar:** Health do ambiente de integração verde; checks de paridade entre modos local e remoto para endpoints P1; suite de regressão de cálculo de tax ainda verde.

### Fase 2 — Endurecimento de contratos

- Pact (ou equivalente) para todos os endpoints P1
- Gate de CI: verificação consumer + provider
- Garantias de campos DTO de reference enforced nos contracts

**Critérios de sucesso para avançar:** Quebra intencional de schema falha no CI; gate de build completo inclui contracts.

### Fase 3 — Prontidão para produção

- Indicadores de health (incluindo dependência tax → reference)
- Correlation id entre hops
- Métricas de latência/erro para paths de HTTP server e client
- Runbook operacional para ordem de startup e dependência de seed

**Sucesso de longo prazo:** Padrão reutilizado na Wave 2; dívida residual da Wave 1 (DB compartilhado, callers in-process de language, calc de tax) rastreada para ondas posteriores.

---

## Métricas de sucesso

| Métrica | Meta |
| ------- | ---- |
| Serviços deployáveis | Ambos os serviços Wave 1 saudáveis e respondendo a todos os endpoints P1 |
| Compatibilidade de client | Zero mudanças breaking de path/contrato para clients existentes |
| Pureza da resposta | Zero campos de entidade JPA no JSON dos endpoints migrados |
| Segurança do checkout | Regressão de cálculo de tax: zero falhas nos totais de order |
| Qualidade de contrato | Todos os endpoints P1 cobertos; CI falha em quebra de schema do provider |
| Latência (ops) | Reference p95 ≤ 2× baseline do monolito |
| Modo de falha | Modo remoto + dependência fora → 503 erro estruturado (sem fallback silencioso) |
| Valor de template | Padrão documentado para reuso na Wave 2 |

---

## Riscos e mitigações

| Risco | Impacto | Mitigação |
| ----- | ------- | --------- |
| Stakeholders esperam extração completa do domínio tax | Entrega mal escopada / atraso no trabalho de checkout | Não-objetivos explícitos; TAX-07 destacado nos critérios de sucesso |
| DB compartilhado percebido como “não são microsserviços de verdade” | Pressão política para expandir a Wave 1 | ADR-001 / ADR-002: extração runtime-first é intencional |
| Confusão do admin com o novo comportamento 409 no delete | Tickets de suporte | Documentar como correção intencional de produto; mensagem de erro clara |
| Deploys greenfield perdem a dependência de seed | Dados vazios de reference/tax | Dependência operacional: seed do monolito primeiro |
| Testes de contrato pulados sob pressão de prazo | Drift silencioso de DTO em ondas posteriores | Gate da Fase 2 obrigatório antes de produção |
| Adoção do modo remoto atrasada | Valor do piloto não realizado | Profile prod Wave 1 default usa remoto; local só para dev |

---

## Registros de decisão de arquitetura

- [ADR-001: TLC-sourced Compozy PRD for Wave 1 Reference+Tax](adrs/adr-001.md) — Usar TLC onda-1-reference-tax como fonte de verdade de produto/técnica; piloto Strangler apenas para reference + tax admin.
- [ADR-002: Shared DB schema for Wave 1](adrs/adr-002.md) — Manter schema compartilhado; sem split de ownership de tabelas na Wave 1.
- [ADR-003: Tax admin only; calculation stays in monolith](adrs/adr-003.md) — Extrair admin de TaxClass/TaxRate; deixar calculateTax in-process.
- [ADR-004: RestTemplate Strangler clients](adrs/adr-004.md) — Usar RestTemplate para monolito↔serviços e tax→reference.
- [ADR-005: Contract DTOs, no JPA in REST responses, Pact](adrs/adr-005.md) — Módulo de contracts livre de JPA; testes Pact consumer/provider.
- [ADR-006: GeoZone excluded from Wave 1](adrs/adr-006.md) — Sem API/serviço GeoZone nesta onda.
- [ADR-007: InitializationDatabaseImpl stays in monolith](adrs/adr-007.md) — Bootstrap/seed permanece multi-domínio no monolito.
- [ADR-008: JWT replication for tax-service](adrs/adr-008.md) — tax-service valida JWT com secret compartilhado e paridade de auth por store.

---

## Questões em aberto

Todas as questões abertas TLC OQ-01 até OQ-06 estão **resolvidas** (ver `.specs/features/onda-1-reference-tax/context.md` e design). Nenhuma questão de produto bloqueante permanece para a Wave 1.

**Backlog residual (não aberto para a Wave 1):**

- Expor hierarquia piggyback/parent de TaxRate via API
- API REST para TaxConfiguration (hoje via merchant configuration)
- Uso de `ReadableTaxRateFull` com multi-description
- OpenFeign / service discovery (adiado para Wave 2+)
- Refactor sistêmico LanguageCode / MerchantStoreId (Wave 3)

---

*Próximo passo: revisar este PRD e então usar `_techspec.md` (cy-create-techspec) como input de design de implementação para cy-create-tasks.*
