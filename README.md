# VarejoSync — Módulo de Estoque | Portfólio QA

**Portfólio de Quality Assurance sobre um módulo de gestão de estoque.** Regras de negócio formalizadas, casos de teste rastreáveis da regra até a evidência, cinco defeitos investigados até a causa raiz no código e no banco de dados, testes de API e automação de interface em Java.

> **Escrevi a aplicação e a suíte de testes.** As regras de negócio foram levantadas a partir do comportamento do sistema, não recebidas prontas — e os cinco defeitos documentados não foram plantados: apareceram testando o meu próprio código.

![Dashboard de estoque do VarejoSync](assets/screenshots/dashboard-estoque.png)

| Regras de negócio | Testes automatizados | Defeitos investigados | Evidências |
| :---: | :---: | :---: | :---: |
| **14** | **10** | **5** | **41** |

`Java` · `Selenium WebDriver` · `JUnit` · `SQL / SQLite` · `Node.js` · `Git`

---

## Índice

| | Seção | O que você encontra |
| :---: | --- | --- |
| 1 | [O que é este projeto](#1-o-que-é-este-projeto) | contexto em 30 segundos |
| 2 | [O sistema testado](#2-o-sistema-testado) | telas e funcionalidades |
| 3 | [Como eu testo](#3-como-eu-testo) | a estratégia e a rastreabilidade |
| 4 | [Defeitos encontrados](#4-defeitos-encontrados) | 5 defeitos — quatro retestados, um em aberto |
| 5 | [Automação de testes](#5-automação-de-testes) | arquitetura do código e como rodar |
| 6 | [Validação em banco de dados](#6-validação-em-banco-de-dados) | o que a interface não prova |
| 7 | [Cobertura atual](#7-cobertura-atual) | números e o que ainda falta |
| 8 | [Documentação completa](#8-documentação-completa) | todos os artefatos |
| 9 | [Tecnologias](#9-tecnologias) | stack de QA e da aplicação |
| 10 | [Próximos passos](#10-próximos-passos) | o que vem a seguir |

---

## 1. O que é este projeto

O **VarejoSync** é uma aplicação web de gestão de varejo. Este repositório documenta o trabalho de QA sobre o seu **módulo de estoque**: um ciclo completo de qualidade, do entendimento da regra de negócio até a automação do cenário e o registro da evidência.

**Escrevi a aplicação e a suíte de testes, e isso define o escopo do trabalho.** Não havia especificação pronta nem área de produto para consultar: cada regra de negócio foi levantada a partir do comportamento observado no sistema, escrita, transformada em critério de aceite e só então em caso de teste.

Os cinco defeitos documentados apareceram testando o meu próprio código, e foram investigados até a causa antes de serem tratados — quatro já corrigidos e retestados, um ainda aberto. Testar o que se construiu exige sair da cabeça de quem escreveu a regra e entrar na de quem procura onde ela não se sustenta — é a parte do trabalho que este repositório registra com mais detalhe.

O que este portfólio demonstra:

- **Análise funcional** — 14 regras de negócio e 14 critérios de aceite formalizados a partir do comportamento do sistema;
- **Desenho de casos de teste** — cenários positivos, negativos e de integridade de dados, documentados com pré-condições, massa, passos e resultado esperado;
- **Investigação de defeitos** — cinco bugs encontrados, analisados através de interface, backend e banco — quatro corrigidos e retestados, um ainda aberto;
- **Automação de interface** — 10 casos automatizados em Java, Selenium e JUnit, com validação de persistência via SQL;
- **Rastreabilidade** — cada teste tem origem em uma regra e destino em uma evidência;
- **Visão das duas pontas** — o mesmo repositório traz a aplicação (HTML, JavaScript, Node.js, SQLite) e a suíte que a testa.

A prioridade do projeto é **cobertura consistente e comprovada**, não cobertura ampla. Os gaps estão registrados abertamente na [matriz de cobertura](./docs/cobertura-testes.md).

---

## 2. O sistema testado

O módulo de estoque permite cadastrar produtos com variações (cor e tamanho), consultar saldos, editar parâmetros de reposição e inativar variações individualmente.

### Dashboard de estoque

![Dashboard de estoque do VarejoSync](assets/screenshots/dashboard-estoque.png)

### Consulta de estoque

![Consulta de produtos, variações e saldos](assets/screenshots/consulta-estoque.png)

### Cadastro de produto

![Cadastro de produto e variação](assets/screenshots/cadastro-produto.png)

### Funcionalidades validadas

| Área | Cobertura desenvolvida |
| --- | --- |
| Cadastro | nome obrigatório, limite mínimo e cadastro com dados válidos |
| SKU | obrigatoriedade do campo |
| Edição | alteração e persistência do estoque mínimo |
| Produto / variações | vínculo de múltiplas variações ao mesmo produto |
| Inativação | inativação lógica somente da variação selecionada |
| Banco de dados | persistência, estado dos registros e relacionamento entre entidades |
| Navegação | tela inicial e acesso ao cadastro pelo menu |
| Investigação de defeitos | análise de comportamento entre UI, backend e banco |

> A aplicação é executada em ambiente de desenvolvimento local. As evidências do repositório registram os comportamentos validados na interface e no banco de dados.

---

## 3. Como eu testo

Nenhum teste nasce de improviso. Todo caso tem origem em uma regra de negócio e termina em uma evidência registrada:

```text
Regra de Negócio
        ↓
Critério de Aceite
        ↓
Caso de Teste
        ↓
Execução
        ↓
Resultado esperado x obtido
        ↓
Evidência
```

Quando o comportamento da interface **não é suficiente** para determinar o resultado da operação, a investigação continua pelas camadas de baixo:

```text
Interface
   ↓
Backend / API
   ↓
Banco de dados
```

Essa descida é o que diferencia "a tela mostrou sucesso" de "a operação realmente aconteceu". Ela é usada nos cenários de persistência, edição, vínculo entre produto e variações e inativação lógica — e foi ela que revelou os três primeiros defeitos da seção seguinte.

A especificação funcional fica separada dos casos de teste:

| Documento | Conteúdo |
| --- | --- |
| [Regras de negócio — Cadastro de produto](./docs/regras-negocio/cadastro-produto.md) | RN-001 a RN-012 |
| [Regra — Inativação de variação](./docs/regras-negocio/inativacao-variacao.md) | RN-013 |
| [Regra — Consistência de estado entre produto e variações](./docs/regras-negocio/consistencia-de-estado-entre-produto-e-variacoes.md) | RN-014 |
| [Critérios de aceite — Cadastro](./docs/criterios-aceite/cadastro-produto.md) | CA-001 a CA-012 |
| [Critério — Inativação de variação](./docs/criterios-aceite/inativacao-variacao.md) | CA-013 |
| [Critério — Consistência de estado entre produto e variações](./docs/criterios-aceite/consistencia-estado-produto-variacoes.md) | CA-014 |

---

## 4. Defeitos encontrados

Cinco defeitos de integridade de dados foram identificados e investigados até a causa. Quatro foram corrigidos e retestados; o BUG-004 permanece aberto.

| ID | Defeito | Severidade | Status |
| --- | --- | --- | --- |
| [BUG-001](./docs/bugs/bug-001-variacoes-mesmo-produto-ids-distintos.md) | Variações do mesmo produto vinculadas a produtos distintos | Alta | Fechado |
| [BUG-002](./docs/bugs/bug-002-inativacao-variacao-inativa-produto.md) | Inativar uma variação inativava o produto inteiro | Alta | Fechado |
| [BUG-003](./docs/bugs/bug-003-exclusao-massa-inativa-produto-com-variacoes-ativas.md) | Exclusão em massa inativava produto com variações ainda ativas | Alta | Fechado |
| [BUG-004](./docs/bugs/bug-004-api-aceita-sku-fora-padrao-rn-004.md) | API aceita SKU fora do padrão estrutural da RN-004 | Alta | **Aberto** |
| [BUG-005](./docs/bugs/bug-005-nome-abaixo-limite-minimo-rn-001.md) | Nome abaixo do limite mínimo aceito no cadastro e na edição | Média | Fechado |

### BUG-002 — Inativação de uma variação afetava o produto inteiro

Durante a execução do `CT-EST-EXC-001`, a ação destinada a uma única variação fazia **todas** as variações do produto sumirem da consulta de estoque.

**Investigação.** A interface enviava corretamente o identificador da variação selecionada. A análise do backend mostrou que o fluxo usava o `id_produto` associado e executava a inativação sobre o produto de origem:

```sql
UPDATE produto
SET ativo = 0
WHERE id_produto = ?;
```

Como a consulta de estoque considera apenas produtos e variações ativos, todas as variações deixavam de ser apresentadas — um sintoma na tela cuja causa estava duas camadas abaixo.

**Correção.** A operação passou a atuar diretamente sobre a variação:

```sql
UPDATE variacao_produto
SET ativo = 0
WHERE id_variacao = ?;
```

Estado após a correção:

```text
Produto de origem            ativo = 1
Variação selecionada         ativo = 0
Outra variação               ativo = 1
```

O cenário foi reexecutado manualmente e por automação. **Resultado do reteste: Passou.**

[Ver BUG-002 completo](./docs/bugs/bug-002-inativacao-variacao-inativa-produto.md) · [Ver CT-EST-EXC-001](./docs/casos-de-teste/inativacao-variacao/CT-EST-EXC-001-inativar-variacao.md)

### BUG-001 — Vínculo incorreto entre produto e variações

Nos testes de cadastro, variações do mesmo produto eram vinculadas a registros de produto distintos.

O comportamento esperado:

```text
Produto
id_produto = X
      │
      ├── Variação P
      │   id_variacao = A
      │
      └── Variação M
          id_variacao = B
```

As variações devem ter identificadores próprios, mas compartilhar o mesmo `id_produto`. Esse é o tipo de defeito **invisível na interface**: a tela mostrava duas linhas corretas, e só a consulta ao banco revelou a duplicação do produto.

Após a correção, o cenário passou a ser coberto pelo `CT-EST-VAR-001`, que valida no banco:

```text
id_produto(P) = id_produto(M)
id_variacao(P) != id_variacao(M)
```

[Ver BUG-001 completo](./docs/bugs/bug-001-variacoes-mesmo-produto-ids-distintos.md) · [Ver CT-EST-VAR-001](./docs/casos-de-teste/variacao-produto/CT-EST-VAR-001-vincular-variacoes-mesmo-produto.md)

---

## 5. Automação de testes

**10 casos automatizados** em Java, Selenium WebDriver e JUnit, seguindo o padrão **Page Object**.

### Funcionais

| ID | Cenário | Resultado |
| --- | --- | --- |
| `CT-EST-CAD-001` | Bloquear cadastro com nome vazio | Passou |
| `CT-EST-CAD-002` | Bloquear cadastro com SKU vazio | Passou |
| `CT-EST-CAD-003` | Bloquear nome abaixo do limite mínimo | Passou |
| `CT-EST-CAD-004` | Cadastrar produto com dados válidos | Passou |
| `CT-EST-EDT-001` | Alterar estoque mínimo da variação | Passou |
| `CT-EST-VAR-001` | Manter variações vinculadas ao mesmo produto | Passou |
| `CT-EST-EXC-001` | Inativar somente a variação selecionada | Passou |
| `CT-EST-EXC-002` | Inativar a última variação ativa inativa o produto | Passou |

### Navegação / Smoke

| ID | Cenário | Resultado |
| --- | --- | --- |
| `CT-EST-NAV-001` | Validar tela inicial do estoque | Passou |
| `CT-EST-NAV-002` | Acessar cadastro pelo menu | Passou |

### Arquitetura

Cada camada responde **uma** pergunta:

| Camada | Responde |
| --- | --- |
| `tests/` | o quê está sendo validado |
| `pages/` | como a tela funciona |
| `massas/` | quais dados são usados |
| `database/` | se persistiu de verdade |
| `core/` | o que muda entre a máquina local e um servidor |

```text
selenium-tests/testes-varejosync-estoque-qa/src/test/java/
│
├── core/       BaseTest · Configuracao
├── pages/      BasePage · MenuPage · DashboardPage
│               CadastroProdutoPage · ConsultarEstoquePage
├── massas/     Produto · ParDeVariacoes · MassaProduto
├── database/   ProdutoDAO
├── variaveis/  VariaveisEstoque
└── tests/      uma classe por prefixo de caso de teste
                CadastroProduto · CadastroProdutoNegativo · Edicao
                Exclusao · Variacao · Navegacao
```

Os locators ficam dentro da Page da tela a que pertencem, e **nenhuma Page contém assertions**: a Page reporta o que a tela mostrou, o teste decide se está correto. Assim a falha aparece como *"esperava X, mas veio Y"* em vez de um erro genérico de timeout.

São usadas esperas explícitas com `WebDriverWait` — nunca `Thread.sleep` fixo — inclusive para a persistência no banco, que conclui depois da resposta da interface.

A massa que precisa ser única é gerada dinamicamente. Cada teste registra os SKUs que criou, e o `ProdutoDAO` remove esses registros ao final da execução, junto com estoque, movimentações e auditoria — sem isso os testes deixariam de ser repetíveis.

### Como executar

**Pré-requisitos:** Node.js, JDK 17+, Maven e Chrome instalados.

```bash
# 1. Banco e API
cd backend
npm install
npm run seed          # cria o estoque_qa_lab.db a partir do schema
npm start             # API em http://localhost:3001

# 2. Frontend
# Live Server do VS Code na raiz do projeto
# → http://127.0.0.1:5500/frontend/index.html

# 3. Testes
cd selenium-tests/testes-varejosync-estoque-qa
mvn test                        # a suíte inteira
mvn test -Dheadless=true        # sem abrir janela do navegador
```

[Detalhes da suíte de automação](./selenium-tests/testes-varejosync-estoque-qa)

---

## 6. Validação em banco de dados

A interface confirma que a ação foi aceita. **Ela não confirma que o dado ficou correto.** As consultas ao banco cobrem exatamente essa lacuna:

```sql
SELECT
    p.id_produto,
    p.nome,
    vp.id_variacao,
    vp.sku,
    vp.ativo
FROM produto p
INNER JOIN variacao_produto vp
    ON vp.id_produto = p.id_produto
WHERE vp.sku = ?;
```

Entre as validações realizadas:

- persistência do produto após o cadastro;
- alteração e persistência do estoque mínimo, **e** confirmação de que a quantidade em estoque não foi alterada junto;
- vínculo entre variações e produto (`id_produto` compartilhado, `id_variacao` distintos);
- estado lógico dos registros após uma inativação.

Os três primeiros defeitos da seção 4 foram identificados por esse caminho — nenhum deles seria visível olhando apenas a tela. O BUG-004 e o BUG-005 vieram por outra via: chamada direta à API, onde o defeito estava visível na própria resposta.

---

## 7. Cobertura atual

| Indicador | Situação |
| --- | ---: |
| Regras de Negócio formalizadas | 14 |
| Critérios de Aceite formalizados | 14 |
| RNs com pelo menos um CT executado | 7 de 14 |
| Cobertura por RN | 50% |
| Casos de teste funcionais catalogados | 9 |
| Casos funcionais automatizados | 8 |
| Casos de teste por camada | 8 UI · 1 API |
| Casos funcionais documentados | 9 |
| Testes de navegação / smoke | 2 |
| Total de testes automatizados | 10 |

A porcentagem representa regras que possuem **ao menos um** caso de teste formalmente associado e executado. Ela **não representa cobertura exaustiva** de todas as combinações possíveis.

[Ver matriz de cobertura completa](./docs/cobertura-testes.md)

---

## 8. Documentação completa

```text
docs/
│
├── regras-negocio/      RN-001 a RN-014
├── criterios-aceite/    CA-001 a CA-014
├── casos-de-teste/      9 casos funcionais documentados
├── bugs/                BUG-001 a BUG-005
├── evidencias/          41 registros de execução
└── cobertura-testes.md  matriz RN → CA → CT
```

Cada caso de teste registra objetivo, pré-condições, massa, passos, resultado esperado, resultado obtido, status, automação relacionada e evidências. Cada defeito mantém o vínculo com a regra, o critério de aceite e o caso de teste que originou a investigação.

**Comece por aqui:**

- [Matriz de cobertura](./docs/cobertura-testes.md) — a visão geral do que existe e do que falta
- [CT-EST-CAD-004 — Cadastrar produto válido](./docs/casos-de-teste/cadastro-produto/CT-EST-CAD-004-cadastrar-produto-valido.md) — um caso de teste completo
- [BUG-002 — Inativação indevida do produto](./docs/bugs/bug-002-inativacao-variacao-inativa-produto.md) — uma investigação completa

---

## 9. Tecnologias

### QA e automação

| Tecnologia / ferramenta | Uso |
| --- | --- |
| Java | implementação da automação |
| Selenium WebDriver | automação da interface |
| JUnit | execução e assertions |
| SQL / SQLite | validação de persistência e investigação |
| Postman | execução dos casos de teste de camada API |
| IntelliJ IDEA | desenvolvimento dos testes |
| Git / GitHub | versionamento e documentação |

### Aplicação

| Tecnologia | Uso |
| --- | --- |
| HTML | interface |
| CSS | apresentação |
| JavaScript | comportamento do frontend |
| Node.js | backend |
| Express | API REST |
| SQLite | banco de dados |

### Estrutura do repositório

```text
varejosync-estoque-qa/
│
├── frontend/         interface da aplicação
├── backend/          API REST e banco SQLite
├── selenium-tests/   automação de testes
├── docs/             artefatos de QA
├── assets/           imagens do projeto
└── README.md
```

---

## 10. Próximos passos

Os gaps estão registrados abertamente na matriz de cobertura. As próximas regras previstas:

- `RN-001` — demais valores limite do nome;
- `RN-004` — demais validações estruturais do SKU;
- `RN-005` — unicidade de SKU;
- `RN-008` — duplicidade produto / cor / tamanho;
- `RN-002`, `RN-003`, `RN-006`, `RN-007` e `RN-009`.

Também previstos:

- automatizar por **API** o cenário de inativação em massa (`PATCH /produtos/exclusao-massa`) — é o caminho que originou o BUG-003 e o único da `RN-014` sem cobertura, por não possuir tela;
- cobertura de movimentação, histórico e alertas de estoque;
- execução da suíte em pipeline de integração contínua.

A prioridade atual é manter a cobertura existente **consistente, rastreável e baseada em evidências**, sem apresentar como concluído aquilo que ainda não foi validado.

---

**Janayna Mirelly** — Quality Assurance

[LinkedIn](https://www.linkedin.com/in/janayna-mirelly-dev)
