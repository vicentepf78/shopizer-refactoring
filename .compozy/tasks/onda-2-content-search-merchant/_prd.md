# PRD: Onda 2 — Content, Search, Merchant

**Slug da feature:** `onda-2-content-search-merchant`  
**Fonte da verdade:** TLC em `.specs/features/onda-2-content-search-merchant/` (Opção A — autoritativa; escopo congelado)  
**Status:** Pronto para TechSpec  
**Data:** 2026-07-18

---

## Visão geral

Após a Onda 1 validar o padrão Strangler em Reference e Tax, a Onda 2 extrai três domínios de comércio de risco médio-baixo para que administradores de loja, visitantes da vitrine e operadores da plataforma possam gerenciar conteúdo CMS, buscar produtos e operar lojas multi-tenant sem que esses domínios permaneçam presos no runtime do monólito.

O problema hoje é operacional e arquitetural: metadados de conteúdo e blobs estão divididos entre runtimes; consultas e indexação de busca de produtos estão coladas ao grafo JPA de produto; as APIs de merchant store estão prontas para sair, mas historicamente bloqueadas por uma dependência morta de ProductType. Sem um limite de produto formal, os times ou extraem por intuição (padrão falho da Fase 3) ou adiam a busca até o ProductSnapshot completo (Onda 3), atrasando a prova de que o padrão da Onda 1 escala.

Este PRD define o **o quê e o porquê de negócio** de uma entrega da Onda 2: três serviços independentemente implantáveis atrás do BFF shop existente, com caminhos REST congelados e sem quebras de API voltadas ao usuário. O como técnico fica para a TechSpec e os ADRs.

**Usuários primários:** administradores de loja, retailers/superadmins, visitantes da vitrine e o time de plataforma que opera o rollout Strangler.

---

## Objetivos

- Entregar capacidades implantáveis de **content**, **search** e **merchant** como serviços independentes, enquanto o monólito permanece como BFF voltado ao cliente.
- Preservar as jornadas existentes de vitrine e admin (páginas, boxes, arquivos, busca, autocomplete, CRUD de loja, config pública, logo) com respostas equivalentes às do monólito de hoje.
- Colocalizar a propriedade de metadados de conteúdo com a propriedade de blobs para que operações CMS deixem de depender de dois runtimes descoordenados.
- Permitir que visitantes busquem e usem autocomplete de produtos sem que o caminho de consulta dependa do cliente OpenSearch do monólito.
- Permitir que operadores gerenciem lojas e configuração pública da loja sem puxar o gerenciamento de product-type do catálogo para esta onda.
- Provar estabilidade de contrato (checagens consumer/provider) para todas as superfícies P1 migradas antes de declarar a Onda 2 concluída.
- Reutilizar os pacotes compartilhados de contrato de API estabelecidos na Onda 1 para novos DTOs.
- Permanecer bloqueado na **conclusão da Onda 1** — a Onda 2 não inicia Execute até que esse gate passe.

### Resultados de negócio

| Resultado | Indicador |
| --------- | --------- |
| Decomposição incremental mais segura | Três domínios saem do monólito sem reescrever ~450 call sites do resolver de merchant |
| Continuidade para merchants | Sem breaking change nos caminhos REST públicos/privados usados pela UI admin e pela vitrine |
| Independência da busca | Query/autocomplete de propriedade da capability de search; indexação não exige mais SearchService in-process |
| Postura de integridade de conteúdo | Metadados + blobs de propriedade conjunta; orquestração de logo consistente com registros de loja |

---

## Histórias de usuário

### Administrador de loja — páginas CMS, boxes e arquivos (P1 / CNT)

Como **administrador de loja**, quero gerenciar páginas CMS, content boxes e arquivos estáticos (imagens, CSS) pelas mesmas APIs que uso hoje, para personalizar a vitrine sem que o monólito seja dono dos metadados de conteúdo e dos blobs.

**Aceite (negócio):**

1. Listagens paginadas de páginas e boxes e leituras localizadas retornam DTOs de conteúdo legíveis, não formatos internos de persistência.
2. Create/update de páginas e boxes exige códigos únicos por loja.
3. Delete de página/box remove registros de conteúdo como hoje (blobs órfãos possíveis — comportamento atual preservado).
4. Upload, listagem, rename e remoção de arquivos funcionam para assets CMS com escopo de loja.
5. A resolução de idioma usa a capability de reference da Onda 1 — não um catálogo local de idiomas dentro de content.
6. O tenant é identificado pelo store code como hoje.

**IDs de requisito:** CNT-01…CNT-07, CNT-09

### Visitante da vitrine — busca de produtos e autocomplete (P1 / SRCH)

Como **visitante da vitrine**, quero buscar produtos e receber sugestões de autocomplete pelos endpoints de busca existentes, para encontrar produtos mesmo quando a busca roda fora do processo do monólito.

**Aceite (negócio):**

1. Respostas de search e autocomplete batem com os schemas e headers de hoje (`store` / `lang`).
2. A busca fica indisponível com falha clara quando o backend de busca está fora — sem fallback silencioso para catálogo vazio.
3. Reindex disparado pelo admin permanece disponível como ação de plataforma (a orquestração pode ficar no BFF inicialmente).
4. Atualizações de índice por mudanças de catálogo continuam sem exigir que a busca carregue grafos completos de persistência de produto.

**IDs de requisito:** SRCH-01…SRCH-06, SRCH-08

### Superadmin / admin retailer — lojas e config pública (P1 / MCH)

Como **superadmin ou admin retailer**, quero criar e gerenciar lojas, idiomas suportados e configuração pública da loja, para que operações multi-tenant não dependam do runtime merchant do monólito.

**Aceite (negócio):**

1. Leituras públicas e privadas de loja, create/update/delete, checagens de unicidade e listagens da hierarquia de retailer se comportam como hoje.
2. O `/config` público continua expondo flags da loja e configurações sociais.
3. Upload/remoção de logo atualiza a associação de logo da loja e o asset de imagem armazenado de forma consistente.
4. A resolução de country/zone/language/currency usa a capability de reference da Onda 1.
5. A loja default permanece protegida contra exclusão.
6. O gerenciamento de product-type **não** faz parte desta experiência na Onda 2.

**IDs de requisito:** MCH-01…MCH-08

### Time de plataforma — Strangler BFF (P1 / STR)

Como **engenheiro de plataforma**, quero delegação HTTP configurável para as facades de content, search e merchant, para validarmos a extração sem reescrever resolvers e centenas de call sites de merchant-store.

**Aceite (negócio):**

1. Com as URLs remotas da Onda 2 habilitadas, as facades delegam aos três serviços; com o profile monólito, o comportamento legado in-process permanece.
2. Eventos de mudança de produto no catálogo que antes indexavam in-process passam a notificar a capability de search.
3. Falha remota aparece como service unavailable com identidade de correlação — sem fallback silencioso para in-process.
4. A postura de produção da Onda 2 usa adapters HTTP por padrão.

**IDs de requisito:** STR-01, STR-04, STR-06

### Visitante — assets estáticos após a extração de content (P2 / CNT)

Como **visitante da vitrine**, quero que URLs existentes de arquivos estáticos e logos continuem funcionando, para que as vitrines não quebrem quando content sair do monólito.

**IDs de requisito:** CNT-08

### Desenvolvedor — confiança em contratos (P2 / STR)

Como **desenvolvedor**, quero testes de contrato para as superfícies P1 de content, search e merchant, para que mudanças breaking em DTOs falhem no CI antes do deploy.

**IDs de requisito:** STR-02, SRCH-07

### Operador — observabilidade (P3 / STR)

Como **operador**, quero health checks e correlation IDs nos três serviços, para ver o status de database, search engine, blob backend e dependências HTTP.

**IDs de requisito:** STR-05

---

## Funcionalidades principais

### F1 — Content service (MVP)

Gerenciar páginas, boxes e arquivos CMS de uma loja; ser dono tanto dos registros de conteúdo quanto dos backends de blob (infinispan/local/aws/gcp conforme configurado). Caminhos públicos e privados de content permanecem congelados. Stubs/endpoints deprecated mantêm o comportamento null/no-op de hoje.

### F2 — Search service (MVP)

Ser dono de query e autocomplete de produtos contra o índice de busca; aceitar payloads de índice versionados do lado do catálogo para upsert/delete de documentos; preservar a semântica do entrypoint de reindex admin (o BFF pode orquestrar o trabalho em bulk).

### F3 — Merchant service (MVP)

CRUD de loja, hierarquia de retailer, idiomas, config pública e orquestração de logo via content. Exclui explicitamente APIs de product-type.

### F4 — Strangler BFF (MVP)

Adapters HTTP com feature flag para as facades de content, search e merchant; producer de índice a partir de eventos de catálogo; a resolução do argumento merchant store permanece no monólito chamando merchant remotamente.

### F5 — Serving estático e clientes de blob do catálogo (Fase 2)

Proxy fino para o legado `/static/files/**`; uploads de imagem de option/variant do catálogo chamam content remotamente.

### F6 — Contratos e observabilidade (Fases 2–3)

Cobertura Pact para endpoints P1; schema versionado do payload de índice; health do actuator e propagação de correlação.

---

## Experiência do usuário

### Personas e objetivos

| Persona | Objetivo |
| ------- | -------- |
| Admin de loja | Editar páginas/boxes/arquivos sem perceber a mudança de runtime |
| Visitante | Buscar, usar autocomplete e ver imagens/logos como antes |
| Retailer/superadmin | Gerenciar lojas, config, logos |
| Engenheiro de plataforma | Alternar flags strangler; observar health; confiar nos gates de contrato |

### Fluxos principais

1. **Edição CMS:** Admin autentica → lista páginas → cria página localizada → faz upload de imagem → a vitrine renderiza o conteúdo.
2. **Busca:** Visitante posta search/autocomplete → recebe itens/sugestões → outage do OpenSearch produz resposta clara de unavailable.
3. **Setup de loja:** Superadmin cria loja com endereço/refs → define config → faz upload de logo → endpoints públicos de loja e config refletem as mudanças.
4. **Cutover Strangler:** Plataforma habilita URLs remotas da Onda 2 → mesmos caminhos do cliente → 503 remoto em falha de dependência com correlation id.

### Restrições de UX

- Sem novas telas de admin ou vitrine nesta onda — clientes existentes continuam chamando os mesmos caminhos.
- Acessibilidade e redesign de UI estão fora de escopo; paridade comportamental é a barra de UX.
- Meta de performance na perspectiva do usuário: p95 de endpoints públicos ≤ 2× a baseline do monólito.

---

## Restrições técnicas de alto nível

Limites de produto que moldam a entrega sem prescrever implementação:

- Deve integrar com a capability de **reference da Onda 1** para resolução de language/country/zone/currency usada por content e merchant.
- Deve preservar os **contratos de caminho REST existentes** consumidos pela UI admin e pela vitrine (STR-04).
- Não deve expor entidades internas de persistência nas respostas JSON migradas.
- Deve manter o **banco operacional compartilhado** durante a extração de runtime (decisão herdada de decomposição) — split físico de DB é onda posterior.
- Não deve iniciar Execute até que a **Onda 1 esteja completa**.
- Rotas privadas protegidas por JWT permanecem autorizadas de forma equivalente às APIs privadas de hoje.
- Orçamento de latência p95: ≤ 2× o monólito atual para endpoints públicos P1.

---

## Não-objetivos (fora de escopo)

| Excluído | Por quê |
| -------- | ------- |
| **Qualquer Execute antes da Onda 1 completa** | Pré-requisito rígido — padrões, contratos e gates da Onda 1 precisam existir |
| `ProductTypeApi` / seeding de product-type | Domínio de catálogo; MCH-06 / AD-010 |
| Pipeline de imagens de produto (`productFileManager`, `/static/products/**`) | CMS de catálogo; adiado para onda posterior |
| `ProductSnapshot` completo / snapshots sistêmicos | Contratos da Onda 3 |
| Reescrita sistêmica de `MerchantStoreArgumentResolver` (~450 refs) | Resolver fica no BFF; chama merchant remotamente |
| Bootstrap greenfield de DB / `InitializationDatabaseImpl` | Serviços assumem DB populado |
| Stubs de config de payment/shipping e stubs de signup de marketplace | Incompletos; adiar |
| Tax, checkout, order, CRUD de catálogo | Ondas 4–6 |
| Split de database por serviço | Decisão herdada de schema compartilhado |
| Saga/outbox para dual-write de metadados+blob de content | Complexidade posterior; Onda 2 usa colocalização + APIs idempotentes |
| Corrigir todas as lacunas conhecidas de reindex (reviews, inventory-only, etc.) | Documentar na Onda 2; corrigir só se trivial |
| Rewrites quick-win de Mapper/Populator | Paralelos, não bloqueantes |

---

## Plano de rollout por fases

### MVP (Fase 1) — histórias P1

- Content service: páginas, boxes, arquivos; idioma via reference.
- Search service: query, autocomplete, intake de índice; feature flags para modos no-index.
- Merchant service: CRUD de loja, config, logo via content; sem product types.
- Adapters Strangler do BFF + producer de índice; profile monólito/in-process mantido para rollback.

**Critérios de saída:** Todos os endpoints P1 saudáveis em integração; sem entidades JPA no JSON migrado; pact verde para P1; caminhos de logo + payload de índice de busca funcionando.

### Fase 2

- Proxy de arquivos estáticos e clientes de blob do catálogo para imagens de option/variant.
- Suite Pact completa; versionamento de `ProductIndexPayload` enforced.
- Topologia Docker Compose para Onda 2 local.

**Critérios de saída:** URLs estáticas e uploads de imagem do catálogo funcionam via content; pacts consumer/provider passam o gate Full.

### Fase 3

- Indicadores de health e propagação de correlation id entre serviços.
- Lacunas de indexação de busca documentadas; STATE/rastreabilidade atualizados.

**Critérios de saída:** Operadores conseguem ver health das dependências; padrão da Onda 2 documentado para reúso na Onda 3.

---

## Métricas de sucesso

| Métrica | Meta |
| ------- | ---- |
| Disponibilidade de endpoints P1 em integração | Os três serviços + caminhos do BFF respondem |
| Paridade de contrato | Pact verde para content, search, merchant P1 |
| Vazamento de entidade | Zero tipos de entidade JPA nas respostas JSON migradas |
| Backends de content | Upload/leitura verificados em ≥ 2 backends CMS (local + uma cloud) |
| Search | Query funciona com índice populado via index payload |
| Merchant | CRUD de loja + logo + `/config` sem regressão do resolver |
| Latência | p95 público ≤ 2× baseline do monólito |
| Disciplina de pré-requisito | Nenhum commit de Execute da Onda 2 até a Onda 1 completa |

---

## Riscos e mitigações

| Risco | Mitigação |
| ----- | --------- |
| Onda 1 incompleta bloqueia o calendário | Manter docs Compozy prontos; congelar Execute até o gate T32/Onda 1 |
| Merchants notam regressões de CMS/busca | Caminhos congelados + pact + dual profile strangler para rollback |
| Inconsistência logo/metadados após falhas de upload | Decisão de produto: blob-first com compensação (ver ADRs) — operadores recebem falha consistente, não half-state silencioso |
| Lacunas de frescor da busca (sales conhecidos de inventory/review) | Aceitar lacunas documentadas na Onda 2; não expandir escopo para redesign completo de eventos de catálogo |
| Pressão de escopo para incluir ProductType ou imagens de produto | Não-objetivos explícitos; AD-010 |
| Entrega multi-serviço maior mais difícil de staffar | Três trilhas paralelas com marcos compartilhados de contratos/Strangler |

---

## Registros de decisão de arquitetura

- [ADR-001: Um workflow Compozy para Content + Search + Merchant na Onda 2](adrs/adr-001.md) — Um único workflow para três serviços implantáveis + Strangler BFF; bloqueado na Onda 1; TLC autoritativa.
- [ADR-002: Contrato intermediário de índice ProductIndexPayload](adrs/adr-002.md) — Payload HTTP versionado de índice para extrair search antes dos snapshots da Onda 3.
- [ADR-003: search-service sem JPA/MySQL](adrs/adr-003.md) — Search é dono apenas do OpenSearch; sem acesso ao DB compartilhado.
- [ADR-004: Módulos thin sm-content-core / sm-merchant-core](adrs/adr-004.md) — JARs intermediários extraem subsets de sm-core sem arrastar o monólito.
- [ADR-005: APIs internas e X-Internal-Token](adrs/adr-005.md) — `/internal/v1/**` isolado por rede com token para APIs de índice de search.
- [ADR-006: Upload de logo blob-first com compensação](adrs/adr-006.md) — Ordem de orquestração de logo merchant e compensate-on-DB-fail.
- [ADR-007: Sem ProductType na Onda 2](adrs/adr-007.md) — Extração merchant exclui APIs de product-type.
- [ADR-008: Colocalização de content e escopo contentFileManager-only](adrs/adr-008.md) — Mitigar split-brain; excluir product file managers.
- [ADR-009: Thin proxy no monólito para arquivos estáticos legados](adrs/adr-009.md) — Manter `/static/files/**` no BFF, proxy para content.
- [ADR-010: SearchItem permanece em shopizer-commons](adrs/adr-010.md) — Pact contra commons até a migração da Onda 3.
- [ADR-011: Preservar comportamento de stubs e endpoints deprecated](adrs/adr-011.md) — Paridade byte-a-byte null/no-op/deprecated para contratos.

---

## Questões em aberto

Todas as questões em aberto do TLC (OQ-01…OQ-06) estão **resolvidas** em `.specs/features/onda-2-content-search-merchant/context.md`. Não restam ambiguidades de produto bloqueantes para este PRD.

Acompanhamento residual (não bloqueante):

- Data exata de início do Execute após a Onda 1 T32 — agendamento, não escopo.
- Se algum item GAP-SRCH se tornar correção trivial durante a implementação — o padrão permanece documentar-only, salvo se for trivialmente seguro.
