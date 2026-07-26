# AGENTS.md

## Visão do projeto

O Shopizer é uma plataforma de comércio eletrônico headless em Java. O sistema
opera hoje como um monólito Maven, com partes do domínio já organizadas em
módulos internos.

O projeto está em uma reestruturação incremental para uma arquitetura modular.
A primeira etapa dessa transformação já foi executada: os domínios de
referência e administração tributária passaram a ter aplicações Spring Boot
próprias, mantendo compatibilidade com o monólito e o banco compartilhado.

As próximas etapas ampliarão essa modularização gradualmente. Preserve esse
processo: não trate os módulos extraídos como um sistema totalmente
independente, não remova integrações do monólito sem especificação aprovada e
não introduza mudanças de fronteira arquitetural fora do escopo da tarefa.

Antes de alterar limites entre módulos, contratos HTTP ou persistência, leia
`.specs/project/STATE.md` e a especificação aplicável em `.specs/features/`.

## Arquitetura atual e direção da reestruturação

### Estrutura legada

O fluxo principal ainda está concentrado no monólito:

```text
sm-shop                         API REST, facades e BFF
├── sm-shop-model               DTOs e interfaces de facade
├── sm-core                     regras de negócio, repositórios e integrações
│   ├── sm-reference-core       domínio de referência extraível
│   └── sm-tax-core             domínio tributário administrativo extraível
├── sm-core-model               entidades JPA compartilhadas
└── sm-core-modules             contratos de plugins e integrações
```

### Estrutura modular em evolução

Os módulos a seguir existem como aplicações independentes, mas ainda convivem
com o monólito:

```text
shopizer-api-contracts          DTOs e contratos HTTP sem dependência JPA
reference-service               API pública de países, zonas, idiomas,
                                moedas e medidas (porta 8081)
tax-service                     API privada de administração tributária e JWT
                                (porta 8082)
sm-shop                         monólito, BFF e autoridade de login (porta 8080)
```

Na topologia local, MySQL atende os três processos:

```text
MySQL → reference-service → tax-service → sm-shop
```

O banco `SALESMANAGER` é compartilhado intencionalmente durante a transição.
O `sm-shop` continua responsável pelo bootstrap de uma base nova. O cálculo de
impostos no checkout permanece no monólito; `tax-service` cobre apenas a
administração tributária.

## Navegação no repositório

Leia `.rtt/context.txt` antes de abrir arquivos-fonte. Esse índice estrutural
mostra assinaturas, imports, classes e métodos do repositório; use-o para
localizar os arquivos relevantes antes de inspecionar implementações.

Se o índice estiver ausente ou parecer desatualizado, peça ao usuário para
executar `rtt update`.

Use `.specs/` como fonte de verdade para escopo, decisões arquiteturais e
trabalho planejado. As regras locais em `.cursor/rules/` também se aplicam a
todas as alterações.

## Tecnologias e organização

- Java 11 como versão de compilação, Spring Boot 2.5.12 e Maven Wrapper.
- MySQL 8 em execução; H2 nas configurações de teste.
- Spring MVC, Spring Data JPA, Spring Security, MapStruct, JUnit 4
  predominante, JUnit 5 parcial, JaCoCo e Pact JVM.
- Pacotes sob `com.salesmanager`.
- Código Maven em `src/main/java` e `src/main/resources`; testes em
  `src/test/java` e `src/test/resources`.

Use `./mvnw`, não o Maven instalado no sistema.

## Desenvolvimento e execução

Compile todo o reator:

```bash
./mvnw clean install
```

Execute uma aplicação com seus módulos dependentes:

```bash
./mvnw -pl sm-shop -am spring-boot:run
./mvnw -pl reference-service -am spring-boot:run
./mvnw -pl tax-service -am spring-boot:run
```

`-am` também compila os módulos Maven dos quais o módulo selecionado depende.
Use-o em execuções isoladas quando essas dependências não tiverem sido
instaladas previamente.

Para iniciar a topologia modular local, gere os JARs antes de subir os
contêineres:

```bash
./mvnw -pl reference-service,tax-service,sm-shop -am package -DskipTests
docker compose -f docker-compose-wave1.yml up --build
```

Os Dockerfiles copiam JARs já compilados; `docker compose ... up --build` não
compila as aplicações. As URLs entre serviços devem ser configuradas por
`WAVE1_REFERENCE_BASE_URL` e `WAVE1_TAX_BASE_URL`, nunca codificadas no fonte.

## Testes e validação

Durante o desenvolvimento, execute o menor recorte que cobre a mudança:

```bash
./mvnw -pl <modulo> -am test
./mvnw -pl <modulo> -am verify
```

Antes de entregar alterações que atravessam módulos, contratos ou a
configuração de build, execute:

```bash
./mvnw clean install
```

Ao filtrar um teste com `-am`, inclua `-DfailIfNoTests=false`, pois módulos
dependentes podem não conter o teste solicitado:

```bash
./mvnw -pl sm-shop -am test \
  -Dtest=Wave1ConsumerPactTest -DfailIfNoTests=false
```

Para validar os contratos entre o monólito e os módulos extraídos:

```bash
./mvnw -pl sm-shop,reference-service,tax-service -am test \
  -Dtest=Wave1ConsumerPactTest,ReferenceProviderPactTest,ReferenceProviderDriftProofTest,TaxProviderPactTest \
  -DfailIfNoTests=false
```

`reference-service`, `tax-service`, `sm-reference-core`, `sm-tax-core`,
`shopizer-api-contracts` e o pacote Strangler de `sm-shop` possuem gates de
cobertura JaCoCo em `verify`. Nomeie testes como `*Test.java` ou
`*IntegrationTest.java`.

Valide a configuração de contêineres antes de iniciá-la:

```bash
docker compose -f docker-compose-wave1.yml config
```

Depois de iniciar a topologia local, consulte `/actuator/health` nas portas
8081, 8082 e 8080.

## Convenções de implementação

- Mantenha `shopizer-api-contracts` livre de entidades JPA, repositórios e
  mapeadores. Coloque mapeamento e população de dados no módulo proprietário.
- Use o padrão HTTP existente: `RestTemplate` configurado. Não introduza
  Feign, WebClient ou descoberta de serviços sem uma decisão arquitetural
  aprovada.
- Preserve compatibilidade nas fronteiras do monólito. O caminho para
  funcionalidades extraídas passa pelos adaptadores Strangler de `sm-shop`,
  salvo autorização explícita na especificação.
- A opção `wave1.strangler.enabled` fica desativada por padrão. A topologia
  Docker local habilita o perfil `strangler`.
- Preserve `X-Correlation-Id` nas chamadas entre serviços e atualize testes
  Pact quando houver mudança de contrato.
- Trate URLs, segredos JWT e credenciais como configuração. Não versione
  segredos de produção; os valores do Compose são apenas para desenvolvimento
  local.
- Não existe formatter ou linter obrigatório no repositório. Siga a
  indentação e o estilo do arquivo próximo e evite alterações somente de
  formatação.

## Fluxo de alteração

1. Identifique o módulo Maven afetado e consulte o índice `.rtt`.
2. Leia a configuração e os testes do módulo antes de implementar.
3. Para mudanças arquiteturais, confirme o escopo em `.specs/`.
4. Adicione ou atualize testes focados; inclua Pact para mudanças de contrato.
5. Execute a validação proporcional à mudança e o reator completo quando a
   alteração atravessar módulos.
6. Não versione `target/`, saídas Pact, caches locais ou arquivos gerados.

## CI e entrega

- O CircleCI usa uma imagem Java 11 e chama um script externo que não está
  neste repositório. O gate local equivalente é `./mvnw clean install`.
- Os Dockerfiles usam imagens Temurin 11 JRE e esperam os JARs já construídos.
- `pom.xml` define a versão e a compatibilidade Java. O README e tags de
  imagens legadas podem conter versões diferentes.
