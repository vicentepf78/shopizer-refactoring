# Onda 4 — Tarefas de Catálogo + Cliente

**Design:** `.specs/features/onda-4-catalog-customer/design.md`
**Spec:** `.specs/features/onda-4-catalog-customer/spec.md`
**Status:** Aprovado — Execução bloqueada até conclusão da Onda 3
**Testes:** `.specs/codebase/TESTING.md`
**Pré-requisito:** Onda 3 Execute concluída (`ProductSnapshot`, `CustomerSnapshot`, `LanguageCode`, `MerchantStoreId`)

---

## Plano de execução

### Fase 1: Contratos + Configuração Wave4 (Sequencial → Paralelo)

```
Onda3-gate ──→ T1 ──┬──→ T2 [P]
                    └──→ T3 [P]
T1,T2,T3 ──→ T4
```

### Fase 2: Extração do Core (2 trilhas paralelas)

```
T4 ──┬──→ T5 ──→ T6 ──→ T7 ──→ T8 ──→ T9 ──→ T10 ──→ T11 ──→ T12 ──→ T13
     │
     └──→ T14 ──→ T15 ──→ T16 ──→ T17 ──→ T18 ──→ T19 ──→ T20
```

**Trilha A (Catálogo):** T5–T13
**Trilha B (Cliente):** T14–T20

### Fase 3: Transversal (Sequencial)

```
T10,T13,T20 ──→ T21 ──→ T22 ──→ T23 ──→ T24 ──→ T25 ──→ T26
```

### Fase 4: Strangler + migração de busca (Sequencial)

```
T26 ──→ T27 ──→ T28 ──→ T29 ──→ T30 ──→ T31
```

### Fase 5: Integração e Gate (Cauda sequencial)

```
T31 ──→ T32 ──┬──→ T33 [P providers]
              └──→ T34
T33,T34 ──→ T35 ──→ T36 ──→ T37 ──→ T38
```

---

## Mapa de execução paralela

```
Fase 1:
  Onda3-gate → T1 → (T2 ∥ T3) → T4

Fase 2 (2 tracks após T4):
  Catalog:   T5 → T6 → T7 → T8 → T9 → T10 → T11 → T12 → T13
  Customer:  T14 → T15 → T16 → T17 → T18 → T19 → T20

Fase 3:
  T21 → T22 → T23 → T24 → T25 → T26

Fase 4:
  T27 → T28 → T29 → T30 → T31

Fase 5:
  T32 → (T33 ∥ subagents) → T34 → T35 → T36 → T37 → T38
```

**Marco `CAT-ready`:** T11 concluída (leitura pública de catálogo + snapshot interno).
**Marco `CUS-ready`:** T19 concluída (REST de perfil de cliente + snapshot interno).
**Regra de subagente:** `[P]` → tarefas paralelas na mesma fase. Trilhas Catálogo/Cliente na Fase 2 → **2 subagentes** por ordinal quando ambas estiverem prontas.

---

## Decomposição de tarefas

### T1: Fundação de contratos Wave 4 — verificação de tipos de valor

**O quê:** Verificar `LanguageCode`, `MerchantStoreId` da Onda 3 nos contratos; adicionar constantes `schemaVersion` para snapshots.
**Onde:** `shopizer-api-contracts/.../common/`
**Depende de:** gate da Onda 3
**Requisito:** STR-06, AD-021

**Concluído quando:**
- [ ] Tipos de valor compilam sem JPA
- [ ] `./mvnw compile -pl shopizer-api-contracts` passa

**Testes:** serialização unitária
**Gate:** `./mvnw compile -pl shopizer-api-contracts`

---

### T2: DTOs de catálogo + `CatalogServiceClient` [P]

**O quê:** Migrar/adicionar `ReadableProduct*`, `ReadableCategory*`, `ProductSnapshot`; interface `CatalogServiceClient` (leitura + snapshot).
**Onde:** `shopizer-api-contracts/.../catalog/`, `.../client/`
**Depende de:** Onda 3 T-product-snapshot (Wave 3)
**Reutiliza:** `sm-shop-model/.../model/catalog/`
**Requisito:** CAT-02, CAT-03, CAT-08, STR-04

**Concluído quando:**
- [ ] DTOs compilam sem `com.salesmanager.core.model`
- [ ] `ProductSnapshot.schemaVersion` padrão 2

**Testes:** nenhum
**Gate:** `./mvnw compile -pl shopizer-api-contracts`

---

### T3: DTOs de cliente + `CustomerServiceClient` [P]

**O quê:** Migrar DTOs de cliente; criar `CustomerSnapshot`; `CustomerServiceClient` com `getSnapshot`.
**Onde:** `shopizer-api-contracts/.../customer/`, `.../client/`
**Depende de:** Onda 3 T-customer-snapshot
**Requisito:** CUS-02, CUS-08, STR-04

**Concluído quando:**
- [ ] `CustomerSnapshot` serializável; `schemaVersion` padrão 1

**Testes:** nenhum
**Gate:** `./mvnw compile -pl shopizer-api-contracts`

---

### T4: Propriedades Strangler Wave4 + RestTemplate

**O quê:** Perfil `strangler-wave4`; `wave4.*.base-url`, `wave4.strangler.enabled`; beans RestTemplate; clientes stub.
**Onde:** `sm-shop/.../strangler/config/Wave4ClientConfig.java`
**Depende de:** T1, T2, T3
**Requisito:** STR-01

**Concluído quando:**
- [ ] Propriedades coexistem com `wave1.*`, `wave2.*`
- [ ] `./mvnw test -pl sm-shop -Dtest=Wave4ClientConfigTest`

**Testes:** unitário
**Gate:** `./mvnw test -pl sm-shop -Dtest=Wave4ClientConfigTest`

---

### T5: Scaffold do módulo `sm-catalog-core`

**O quê:** Criar módulo Maven; mover repositórios de leitura de produto, categoria, fabricante.
**Onde:** `sm-catalog-core/pom.xml`, `.../repositories/`
**Depende de:** T2
**Requisito:** CAT-12

**Concluído quando:**
- [ ] Módulo no `pom.xml` raiz
- [ ] `./mvnw compile -pl sm-catalog-core`

**Testes:** nenhum
**Gate:** `./mvnw compile -pl sm-catalog-core`

---

### T6: Mover serviços de leitura de catálogo para `sm-catalog-core`

**O quê:** Extrair métodos de leitura de `ProductService`, `CategoryService`, `ManufacturerService`, `ProductInventoryService`, `PricingService` (subconjunto somente leitura).
**Onde:** `sm-catalog-core/.../services/catalog/`
**Depende de:** T5
**Requisito:** CAT-12

**Concluído quando:**
- [ ] Métodos de escrita permanecem em `sm-core` ou stubs deprecados delegam ao monólito
- [ ] Testes unitários dos serviços de leitura passam

**Testes:** unitário
**Gate:** `./mvnw test -pl sm-catalog-core`

---

### T7: Mappers/populadores de catálogo em `sm-catalog-core`

**O quê:** `ReadableProductMapper` ou equivalentes populadores para o caminho de leitura; mappers de árvore de categorias.
**Onde:** `sm-catalog-core/.../mappers/`
**Depende de:** T6
**Requisito:** CAT-06

**Concluído quando:**
- [ ] Mappers não vazam entidades para a camada de API

**Testes:** unitário
**Gate:** `./mvnw test -pl sm-catalog-core`

---

### T8: Conectar `sm-core` → `sm-catalog-core` (delegação de leitura)

**O quê:** Serviços de catálogo do `sm-core` delegam chamadas de leitura ao thin core; escritas admin inalteradas no sm-core.
**Onde:** `sm-core/pom.xml`, implementações de serviço
**Depende de:** T6, T7
**Requisito:** AD-020

**Concluído quando:**
- [ ] Testes de integração existentes do monólito para leitura de produto ainda passam in-process

**Testes:** integração (sm-core)
**Gate:** `./mvnw test -pl sm-core -Dtest=*Product*Test -DfailIfNoTests=false`

---

### T9: Scaffold Boot `catalog-service` (:8086)

**O quê:** Aplicação Spring Boot, config JPA com banco compartilhado, package scan `sm-catalog-core`.
**Onde:** `catalog-service/`
**Depende de:** T8
**Requisito:** CAT-01

**Concluído quando:**
- [ ] Contexto da aplicação inicia com Testcontainers MySQL

**Testes:** integração
**Gate:** `./mvnw test -pl catalog-service -Dtest=*Application*Test`

---

### T10: `catalog-service` — clientes HTTP reference + merchant

**O quê:** `ReferenceServiceClient`, `MerchantServiceClient` para resolução de idioma/loja.
**Onde:** `catalog-service/.../client/`
**Depende de:** T9, T4
**Requisito:** CAT-07, STR-06

**Concluído quando:**
- [ ] Resolução de idioma usa apenas HTTP

**Testes:** unitário + wiremock
**Gate:** `./mvnw test -pl catalog-service -Dtest=*Client*Test`

---

### T11: `catalog-service` — controllers REST de leitura pública (`CAT-ready`)

**O quê:** Portarar handlers GET de `ProductApi`, `CategoryApi`, fabricante, inventário, preço, APIs de grupo.
**Onde:** `catalog-service/.../api/v1/`
**Depende de:** T10
**Requisito:** CAT-02…CAT-05, STR-04

**Concluído quando:**
- [ ] Teste de paridade vs baseline do monólito para lista GET de produtos
- [ ] Marco **CAT-ready**

**Testes:** integração
**Gate:** `./mvnw test -pl catalog-service -Dtest=*ProductApi*Test,*CategoryApi*Test`

---

### T12: `catalog-service` — API interna ProductSnapshot

**O quê:** `InternalProductSnapshotController`; restrito à rede; 422 em schemaVersion inválido.
**Onde:** `catalog-service/.../api/internal/`
**Depende de:** T11
**Requisito:** CAT-08

**Concluído quando:**
- [ ] GET snapshot retorna `ProductSnapshot` v2

**Testes:** integração
**Gate:** `./mvnw test -pl catalog-service -Dtest=*Snapshot*Test`

---

### T13: `catalog-service` — JWT + segurança para rotas de leitura privadas

**O quê:** Replicar padrão de cadeia de filtros JWT do merchant-service para rotas que exigem autenticação hoje.
**Onde:** `catalog-service/.../security/`
**Depende de:** T11
**Requisito:** CAT-06

**Concluído quando:**
- [ ] Rotas de leitura privadas (se houver) rejeitam anônimo

**Testes:** integração
**Gate:** `./mvnw test -pl catalog-service -Dtest=*Security*Test`

---

### T14: Scaffold do módulo `sm-customer-core` [trilha P]

**O quê:** Módulo Maven; repositórios de cliente.
**Onde:** `sm-customer-core/`
**Depende de:** T3
**Requisito:** CUS-01

**Concluído quando:**
- [ ] `./mvnw compile -pl sm-customer-core`

**Testes:** nenhum
**Gate:** `./mvnw compile -pl sm-customer-core`

---

### T15: Mover serviços de cliente para `sm-customer-core`

**O quê:** `CustomerService`, `CustomerOptinService`, serviços de atributos (excluir helpers de criação exclusivos de pedido).
**Onde:** `sm-customer-core/.../services/customer/`
**Depende de:** T14
**Requisito:** CUS-01

**Concluído quando:**
- [ ] OrderService ainda usa cópia do monólito ou bridge para criação de cliente no checkout (documentar bridge)

**Testes:** unitário
**Gate:** `./mvnw test -pl sm-customer-core`

---

### T16: Mappers de cliente em `sm-customer-core`

**O quê:** ReadableCustomer, populadores/mappers de endereço.
**Onde:** `sm-customer-core/.../mappers/`
**Depende de:** T15
**Requisito:** CUS-05

**Concluído quando:**
- [ ] Mapper de snapshot produz `CustomerSnapshot`

**Testes:** unitário
**Gate:** `./mvnw test -pl sm-customer-core -Dtest=*Mapper*Test`

---

### T17: Conectar `sm-core` → `sm-customer-core`

**O quê:** Delegar leituras/escritas de perfil de cliente ao thin core onde for seguro; manter caminhos de pedido no sm-core.
**Onde:** `sm-core`
**Depende de:** T15, T16
**Requisito:** GAP-CUS-01

**Concluído quando:**
- [ ] Testes unitários de perfil passam in-process

**Testes:** unitário
**Gate:** `./mvnw test -pl sm-core -Dtest=*Customer*Test -DfailIfNoTests=false`

---

### T18: Scaffold Boot `customer-service` (:8087)

**O quê:** Spring Boot + JPA + sm-customer-core.
**Onde:** `customer-service/`
**Depende de:** T17
**Requisito:** CUS-01

**Concluído quando:**
- [ ] Aplicação inicia com Testcontainers MySQL

**Testes:** integração
**Gate:** `./mvnw test -pl customer-service -Dtest=*Application*Test`

---

### T19: `customer-service` — REST de perfil, endereço, optin (`CUS-ready`)

**O quê:** Portarar seções do CustomerApi (não auth); cliente HTTP reference.
**Onde:** `customer-service/.../api/v1/`
**Depende de:** T18, T4
**Requisito:** CUS-02…CUS-04, CUS-06

**Concluído quando:**
- [ ] Teste de integração de atualização de perfil passa
- [ ] Marco **CUS-ready**

**Testes:** integração
**Gate:** `./mvnw test -pl customer-service -Dtest=*CustomerApi*Test`

---

### T20: `customer-service` — CustomerSnapshot interno + JWT

**O quê:** Controller de snapshot interno; segurança JWT para rotas privadas.
**Onde:** `customer-service/.../api/internal/`, `.../security/`
**Depende de:** T19
**Requisito:** CUS-08

**Concluído quando:**
- [ ] Endpoint de snapshot retorna JSON v1

**Testes:** integração
**Gate:** `./mvnw test -pl customer-service -Dtest=*Snapshot*Test,*Security*Test`

---

### T21: `ProductSnapshotBuilder` no monólito (substitui ProductIndexPayloadBuilder)

**O quê:** Construir snapshots v2 a partir do modelo de leitura de catálogo; deprecar builder v1.
**Onde:** `sm-core/.../search/` ou `sm-shop/.../search/`
**Depende de:** T12, T2
**Requisito:** CAT-09, STR-07

**Concluído quando:**
- [ ] Saída do builder corresponde à API de snapshot interno para produtos de fixture

**Testes:** unitário
**Gate:** `./mvnw test -pl sm-core -Dtest=*ProductSnapshotBuilder*Test`

---

### T22: `search-service` — aceitar ProductSnapshot v2

**O quê:** Estender serviço de índice para desserializar v2; mapear para doc OpenSearch; v1 ainda aceito.
**Onde:** `search-service/.../services/`
**Depende de:** T21
**Requisito:** CAT-09

**Concluído quando:**
- [ ] POST snapshot v2 indexa com sucesso

**Testes:** integração
**Gate:** `./mvnw test -pl search-service -Dtest=*Index*Test`

---

### T23: Atualizar `SearchIndexProducerHttp` para enviar ProductSnapshot

**O quê:** Produtor do monólito usa v2; semântica do endpoint bulk inalterada.
**Onde:** `sm-shop/.../strangler/`
**Depende de:** T22
**Requisito:** STR-07

**Concluído quando:**
- [ ] Evento de índice produz payload v2 no teste de integração

**Testes:** integração
**Gate:** `./mvnw test -pl sm-shop -Dtest=*SearchIndexProducer*Test`

---

### T24: Merge de carrinho — `CustomerSnapshot` em `ShoppingCartService`

**O quê:** Refatorar `mergeShoppingCarts` para aceitar snapshot/id; remover dependência rígida da entidade `Customer` carregada onde possível.
**Onde:** `sm-core/.../shoppingcart/ShoppingCartServiceImpl.java`
**Depende de:** T20
**Requisito:** CUS-09, STR-08

**Concluído quando:**
- [ ] Teste de integração de merge usa entrada de snapshot

**Testes:** integração
**Gate:** `./mvnw test -pl sm-core -Dtest=*ShoppingCart*Merge*Test`

---

### T25: Orquestração de merge do `CustomerFacade` via snapshot HTTP

**O quê:** No login, buscar snapshot do customer-service antes do merge.
**Onde:** `sm-shop/.../facade/customer/`
**Depende de:** T24, T20
**Requisito:** CUS-08

**Concluído quando:**
- [ ] Teste E2E de login+merge com mock strangler wave4

**Testes:** integração
**Gate:** `./mvnw test -pl sm-shop -Dtest=*CustomerFacade*Merge*Test`

---

### T26: Imagens de produto — endpoints de arquivo de produto no content-service (P2)

**O quê:** Estender content-service para uploads de `productFileManager`; conectar facades de catálogo/monólito ao `ContentServiceClient`.
**Onde:** `content-service`, `sm-shop/.../product/`
**Depende de:** T11 (CAT-ready), conteúdo da Onda 2
**Requisito:** CAT-10

**Concluído quando:**
- [ ] Upload de imagem de opção atinge HTTP do content-service

**Testes:** integração
**Gate:** `./mvnw test -pl content-service,sm-shop -Dtest=*ProductImage*Test -DfailIfNoTests=false`

---

### T27: `CatalogFacadeHttpAdapter` — strangler de leitura de produto/categoria

**O quê:** Adaptador HTTP para métodos de leitura de `ProductFacade`, `ProductCommonFacade`, `CategoryFacade`.
**Onde:** `sm-shop/.../strangler/catalog/`
**Depende de:** T13 (CAT-ready), T4
**Requisito:** STR-01, STR-04

**Concluído quando:**
- [ ] Strangler ativo: GET produto delega HTTP; POST produto permanece local

**Testes:** integração
**Gate:** `./mvnw test -pl sm-shop -Dtest=*CatalogFacadeHttp*Test`

---

### T28: `CustomerFacadeHttpAdapter` — strangler de perfil

**O quê:** Adaptador HTTP para perfil/endereço/optin; métodos de auth permanecem locais.
**Onde:** `sm-shop/.../strangler/customer/`
**Depende de:** T20 (CUS-ready), T4
**Requisito:** STR-01

**Concluído quando:**
- [ ] GET perfil delega; Authenticate permanece local

**Testes:** integração
**Gate:** `./mvnw test -pl sm-shop -Dtest=*CustomerFacadeHttp*Test`

---

### T29: Delegação de leitura do `ProductFacadeV2`

**O quê:** Caminhos de leitura V2 usam o mesmo `CatalogServiceClient`.
**Onde:** `sm-shop/.../facade/product/ProductFacadeV2Impl.java`
**Depende de:** T27
**Requisito:** OQ-05

**Concluído quando:**
- [ ] GET produto V2 usa HTTP quando strangler ativo

**Testes:** integração
**Gate:** `./mvnw test -pl sm-shop -Dtest=*ProductFacadeV2*Test`

---

### T30: Wiring condicional — escritas admin nunca strangler

**O quê:** Garantir `@ConditionalOnProperty` nos adaptadores exclui facades de escrita; documentar no código.
**Onde:** `sm-shop/.../strangler/`
**Depende de:** T27, T28
**Requisito:** AD-020

**Concluído quando:**
- [ ] ArchUnit ou teste de integração prova que POST privado de produto não é roteado

**Testes:** unitário/arch
**Gate:** `./mvnw test -pl sm-shop -Dtest=*Wave4Wiring*Test`

---

### T31: Correlation ID + health indicators Wave4

**O quê:** Filtros em catalog/customer; health para db, reference, merchant (catalog).
**Onde:** ambos os serviços + interceptor sm-shop
**Depende de:** T9, T18
**Requisito:** STR-05

**Concluído quando:**
- [ ] `/actuator/health` exibe componentes de dependência

**Testes:** integração
**Gate:** `./mvnw test -pl catalog-service,customer-service -Dtest=*Health*Test`

---

### T32: Gates de cobertura JaCoCo dos módulos Wave4

**O quê:** Adicionar thresholds JaCoCo na fase verify seguindo padrão das Ondas 1–2.
**Onde:** `catalog-service/pom.xml`, `customer-service/pom.xml`, thin cores
**Depende de:** T31
**Requisito:** quality gate

**Concluído quando:**
- [ ] `./mvnw verify -pl catalog-service,customer-service,sm-catalog-core,sm-customer-core`

**Testes:** verify
**Gate:** `./mvnw verify -pl catalog-service,customer-service`

---

### T33: Pact providers — catalog + customer [P]

**O quê:** Testes pact de provider em ambos os serviços para endpoints P1.
**Onde:** `*/src/test/java/**/pact/`
**Depende de:** T11, T19
**Requisito:** CAT-11, CUS-10, STR-02

**Concluído quando:**
- [ ] Testes de provider publicam pacts

**Testes:** pact
**Gate:** `./mvnw test -pl catalog-service,customer-service -Dtest=*ProviderPact*Test`

---

### T34: Pact consumer — `Wave4ConsumerPactTest` no sm-shop

**O quê:** Testes consumer para leitura de catálogo + perfil de cliente + snapshots.
**Onde:** `sm-shop/src/test/java/.../pact/`
**Depende de:** T27, T28, T33
**Requisito:** STR-02

**Concluído quando:**
- [ ] Consumer pact passa contra stubs de provider

**Testes:** pact
**Gate:** `./mvnw test -pl sm-shop -Dtest=Wave4ConsumerPactTest`

---

### T35: `docker-compose-wave4.yml`

**O quê:** Topologia completa com catalog + customer; variáveis de ambiente documentadas.
**Onde:** raiz do repositório
**Depende de:** T31
**Requisito:** deploy

**Concluído quando:**
- [ ] `docker compose -f docker-compose-wave4.yml config` exit 0

**Testes:** config
**Gate:** `docker compose -f docker-compose-wave4.yml config`

---

### T36: Suite de integração Wave4

**O quê:** Consolidar smoke `*Wave4*Test`: leitura de catálogo, perfil de cliente, merge, índice search v2.
**Onde:** `sm-shop/src/test/java/`
**Depende de:** T35, T34
**Requisito:** integration

**Concluído quando:**
- [ ] Suite verde com Testcontainers / perfil compose

**Testes:** integração
**Gate:** `./mvnw test -pl sm-shop -Dtest=*Wave4*Integration*Test`

---

### T37: Gate do reator `./mvnw clean install`

**O quê:** Reator completo incluindo módulos das Ondas 1–4.
**Depende de:** T36
**Requisito:** gate

**Concluído quando:**
- [ ] Install conclui sem falha

**Testes:** completo
**Gate:** `./mvnw clean install`

---

### T38: Rastreabilidade + atualização do STATE.md

**O quê:** Marcar 30 requisitos como Verified; atualizar ROADMAP/STATE; status Execute completo no design.
**Onde:** `.specs/project/STATE.md`, tabela de rastreabilidade da spec
**Depende de:** T37
**Requisito:** documentation

**Concluído quando:**
- [ ] 30/30 requisitos Verified
- [ ] STATE.md registra data do gate da Onda 4

**Testes:** nenhum
**Gate:** revisão de checklist

---

### T39: Dockerfile do catalog-service + smoke de container

**O quê:** Adicionar Dockerfile para catalog-service espelhando padrão do merchant-service; cópia de JAR de `target/`.
**Onde:** `catalog-service/Dockerfile`
**Depende de:** T11
**Requisito:** deploy

**Concluído quando:**
- [ ] Imagem constrói a partir de JAR pré-compilado
- [ ] Container inicia com env DB_URL

**Testes:** smoke manual
**Gate:** `docker build -f catalog-service/Dockerfile catalog-service`

---

### T40: Dockerfile do customer-service + smoke de container

**O quê:** Dockerfile para customer-service.
**Onde:** `customer-service/Dockerfile`
**Depende de:** T19
**Requisito:** deploy

**Concluído quando:**
- [ ] Imagem constrói e health responde no compose

**Testes:** smoke manual
**Gate:** `docker build -f customer-service/Dockerfile customer-service`

---

### T41: Testes de paridade de fixture ReadableProduct

**O quê:** Testes golden-file ou snapshot comparando GET produto monólito vs catalog-service para SKUs de fixture.
**Onde:** `catalog-service/src/test/java/`
**Depende de:** T11
**Requisito:** CAT-02

**Concluído quando:**
- [ ] ≥3 produtos de fixture correspondem campo a campo (excluindo timestamps voláteis)

**Testes:** integração
**Gate:** `./mvnw test -pl catalog-service -Dtest=*Parity*Test`

---

### T42: Testes de paridade de árvore de categorias

**O quê:** Comparação profunda de árvore de categorias vs baseline do monólito.
**Onde:** `catalog-service/src/test/java/`
**Depende de:** T11
**Requisito:** CAT-03

**Concluído quando:**
- [ ] Estrutura da árvore + contagens correspondem para loja DEFAULT

**Testes:** integração
**Gate:** `./mvnw test -pl catalog-service -Dtest=*CategoryTree*Test`

---

### T43: Paridade de validação de endereço de cliente

**O quê:** Erros de bean validation nos endpoints de endereço correspondem aos status codes do monólito.
**Onde:** `customer-service/src/test/java/`
**Depende de:** T19
**Requisito:** CUS-03

**Concluído quando:**
- [ ] Código postal inválido retorna o mesmo formato 400 do monólito

**Testes:** integração
**Gate:** `./mvnw test -pl customer-service -Dtest=*AddressValidation*Test`

---

### T44: Cache TTL opcional no adaptador de leitura de catálogo

**O quê:** Caffeine ou cache simples no CatalogFacadeHttpAdapter GET produto por id (TTL configurável).
**Onde:** `sm-shop/.../strangler/catalog/`
**Depende de:** T27
**Requisito:** performance

**Concluído quando:**
- [ ] `wave4.catalog-service.cache.ttl-seconds` respeitado
- [ ] Strangler desativado desabilita cache

**Testes:** unitário
**Gate:** `./mvnw test -pl sm-shop -Dtest=*CatalogCache*Test`

---

### T45: Cache de snapshot de cliente no caminho de merge

**O quê:** Cache de TTL curto para CustomerSnapshot no merge de login para evitar HTTP duplicado.
**Onde:** `sm-shop/.../strangler/customer/`
**Depende de:** T25
**Requisito:** CUS-08

**Concluído quando:**
- [ ] Segunda chamada de merge dentro do TTL não atinge HTTP (verificação por mock)

**Testes:** unitário
**Gate:** `./mvnw test -pl sm-shop -Dtest=*SnapshotCache*Test`

---

### T46: Endpoints de leitura de avaliações de cliente

**O quê:** Portarar endpoints GET de lista de avaliações para customer-service (escrita pode permanecer no monólito).
**Onde:** `customer-service/.../api/v1/customer/review/`
**Depende de:** T19
**Requisito:** CUS-07

**Concluído quando:**
- [ ] GET reviews retorna lista ReadableCustomerReview

**Testes:** integração
**Gate:** `./mvnw test -pl customer-service -Dtest=*Review*Test`

---

### T47: Documentar GAP-CAT / GAP-CUS no apêndice do design

**O quê:** Adicionar tabela explícita de gaps ao design.md ou `docs/decomposition/GAP-WAVE4.md`.
**Onde:** `.specs/features/onda-4-catalog-customer/design.md`
**Depende de:** T21, T24
**Requisito:** documentation

**Concluído quando:**
- [ ] GAP-CAT-01..02 e GAP-CUS-01..02 documentados com onda responsável

**Testes:** nenhum
**Gate:** revisão de documentação

---

### T48: Atualização de status da Onda 4 no ROADMAP.md (pré-Execute)

**O quê:** Definir status TLC da Onda 4 como Tasks approved no ROADMAP.md.
**Onde:** `.specs/project/ROADMAP.md`
**Depende de:** T38 (ou paralelo após aprovação das tarefas)
**Requisito:** documentation

**Concluído quando:**
- [ ] ROADMAP exibe Specify/Design/Tasks ✅ para Onda 4

**Testes:** nenhum
**Gate:** checklist

---

## Mapeamento Requisito → Tarefa

| Req | Tarefas |
| --- | ----- |
| CAT-01 | T9 |
| CAT-02 | T11, T27 |
| CAT-03 | T11, T27 |
| CAT-04 | T11 |
| CAT-05 | T11 |
| CAT-06 | T7, T13 |
| CAT-07 | T10 |
| CAT-08 | T12 |
| CAT-09 | T21, T22, T23 |
| CAT-10 | T26 |
| CAT-11 | T33 |
| CAT-12 | T5, T6, T8 |
| CUS-01 | T14, T15, T18 |
| CUS-02 | T19, T28 |
| CUS-03 | T19, T28 |
| CUS-04 | T19 |
| CUS-05 | T16 |
| CUS-06 | T19 |
| CUS-07 | T19 (fase de leitura) |
| CUS-08 | T20, T25 |
| CUS-09 | T24 |
| CUS-10 | T33 |
| STR-01 | T4, T27, T28 |
| STR-02 | T33, T34 |
| STR-03 | (herdado AD-022) |
| STR-04 | T2, T3, T11, T19 |
| STR-05 | T31 |
| STR-06 | T10, T19 |
| STR-07 | T21, T23 |
| STR-08 | T24, T25 |

**Cobertura:** 30 requisitos → 48 tarefas, 0 sem mapeamento ✅
