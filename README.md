# VarejoSync | Módulo de Estoque | Portfólio QA

Portfólio de QA sobre o módulo de estoque de uma aplicação web de varejo. Tem regras de negócio, casos de teste ligados a cada regra, cinco defeitos investigados no código e no banco de dados, testes de API no Postman e automação de interface em Java.

> **Escrevi a aplicação e a suíte de testes.** Os cinco defeitos documentados apareceram testando o meu próprio código.

![Dashboard de estoque do VarejoSync](assets/screenshots/dashboard-estoque.png)

| Regras de negócio | Testes automatizados | Defeitos investigados | Evidências |
| :---: | :---: | :---: | :---: |
| **14** | **10** | **5** | **44** |

`Java` · `Selenium WebDriver` · `JUnit` · `SQL / SQLite` · `Postman` · `Node.js` · `Git`

---

## Índice

1. [O que é este projeto](#1-o-que-é-este-projeto)
2. [O sistema testado](#2-o-sistema-testado)
3. [Como eu testo](#3-como-eu-testo)
4. [Defeitos encontrados](#4-defeitos-encontrados)
5. [Automação de testes](#5-automação-de-testes)
6. [Validação em banco de dados](#6-validação-em-banco-de-dados)
7. [Cobertura atual](#7-cobertura-atual)
8. [Documentação completa](#8-documentação-completa)
9. [Tecnologias](#9-tecnologias)
10. [Próximos passos](#10-próximos-passos)

---

## 1. O que é este projeto

O **VarejoSync** é uma aplicação web de gestão de varejo. Este repositório tem o trabalho de QA sobre o **módulo de estoque**, da regra de negócio até a automação e a evidência.

Não havia especificação nem área de produto para consultar. Algumas regras eu escrevi antes de testar. Outras levantei a partir do comportamento do sistema e decidi o que ele deveria fazer; quando ele fazia diferente, registrei defeito. Em todos os casos, a regra virou critério de aceite e depois caso de teste.

O que tem aqui:

- 14 regras de negócio e 14 critérios de aceite;
- casos de teste positivos, negativos e de integridade de dados, com pré-condições, massa, passos e resultado esperado;
- 5 defeitos investigados na interface, no backend e no banco: 4 corrigidos e retestados, 1 aberto;
- 10 testes automatizados em Java, Selenium e JUnit, com validação no banco via SQL;
- cada teste ligado à regra de origem e à evidência;
- a aplicação (HTML, JavaScript, Node.js, SQLite) e a suíte que a testa no mesmo repositório.

Preferi cobrir menos regras e comprovar cada uma. O que ainda falta está na [matriz de cobertura](./docs/cobertura-testes.md).

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

> A aplicação roda em ambiente de desenvolvimento local. As evidências mostram o que foi validado na interface e no banco de dados.

---

## 3. Como eu testo

Cada caso de teste parte de uma regra de negócio e termina numa evidência:

```text
Regra de negócio → Critério de aceite → Caso de teste → Execução → Esperado x obtido → Evidência
```

Quando a tela não basta para saber se a operação deu certo, confiro também a API e o banco de dados. Faço isso nos cenários de persistência, edição, vínculo entre produto e variações e inativação.

As regras e os critérios ficam em documentos separados dos casos de teste:

| Documento | Conteúdo |
| --- | --- |
| [Regras de negócio: cadastro de produto](./docs/regras-negocio/cadastro-produto.md) | RN-001 a RN-012 |
| [Regra: inativação de variação](./docs/regras-negocio/inativacao-variacao.md) | RN-013 |
| [Regra: consistência de estado entre produto e variações](./docs/regras-negocio/consistencia-de-estado-entre-produto-e-variacoes.md) | RN-014 |
| [Critérios de aceite: cadastro](./docs/criterios-aceite/cadastro-produto.md) | CA-001 a CA-012 |
| [Critério: inativação de variação](./docs/criterios-aceite/inativacao-variacao.md) | CA-013 |
| [Critério: consistência de estado entre produto e variações](./docs/criterios-aceite/consistencia-estado-produto-variacoes.md) | CA-014 |

---

## 4. Defeitos encontrados

Encontrei cinco defeitos de integridade de dados e investiguei cada um até a causa. Quatro foram corrigidos e retestados. O BUG-004 continua aberto.

| ID | Defeito | Severidade | Status |
| --- | --- | --- | --- |
| [BUG-001](./docs/bugs/bug-001-variacoes-mesmo-produto-ids-distintos.md) | Variações do mesmo produto vinculadas a produtos distintos | Alta | Fechado |
| [BUG-002](./docs/bugs/bug-002-inativacao-variacao-inativa-produto.md) | Inativar uma variação inativava o produto inteiro | Alta | Fechado |
| [BUG-003](./docs/bugs/bug-003-exclusao-massa-inativa-produto-com-variacoes-ativas.md) | Exclusão em massa inativava produto com variações ainda ativas | Alta | Fechado |
| [BUG-004](./docs/bugs/bug-004-api-aceita-sku-fora-padrao-rn-004.md) | API aceita SKU fora do padrão estrutural da RN-004 | Alta | **Aberto** |
| [BUG-005](./docs/bugs/bug-005-nome-abaixo-limite-minimo-rn-001.md) | Nome abaixo do limite mínimo aceito no cadastro e na edição | Média | Fechado |

### BUG-002: inativar uma variação afetava o produto inteiro

Na execução do `CT-EST-EXC-001`, excluir uma única variação fazia **todas** as variações do produto sumirem da consulta de estoque.

**Investigação.** A interface enviava o id da variação selecionada. No backend, o fluxo pegava o `id_produto` dessa variação e inativava o produto:

```sql
UPDATE produto
SET ativo = 0
WHERE id_produto = ?;
```

Como a consulta de estoque mostra só produtos e variações ativos, todas as variações sumiam da tela. A causa estava no backend.

**Correção.** A inativação passou a ser feita direto na variação:

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

### BUG-001: vínculo incorreto entre produto e variações

Nos testes de cadastro, variações do mesmo produto ficavam ligadas a registros de produto diferentes.

O esperado:

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

Cada variação tem o seu `id_variacao`, mas as duas devem ter o mesmo `id_produto`. Na tela, as duas linhas pareciam corretas. Só a consulta ao banco mostrou o produto duplicado.

Depois da correção, o `CT-EST-VAR-001` passou a cobrir esse cenário, conferindo no banco:

```text
id_produto(P) = id_produto(M)
id_variacao(P) != id_variacao(M)
```

[Ver BUG-001 completo](./docs/bugs/bug-001-variacoes-mesmo-produto-ids-distintos.md) · [Ver CT-EST-VAR-001](./docs/casos-de-teste/variacao-produto/CT-EST-VAR-001-vincular-variacoes-mesmo-produto.md)

---

## 5. Automação de testes

**10 testes automatizados** em Java, Selenium WebDriver e JUnit, com Page Object.

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

### Organização do código

| Pasta | O que tem |
| --- | --- |
| `tests/` | os testes, uma classe por prefixo de caso de teste |
| `pages/` | locators e ações de cada tela |
| `massas/` | dados usados nos testes |
| `database/` | consultas ao banco para conferir se gravou |
| `core/` | classe base dos testes e configurações (caminho do banco, modo headless) |

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

- As Pages não têm assert: devolvem o que a tela mostrou, e o teste compara com o esperado. Assim a mensagem de falha mostra o valor esperado e o que veio.
- Na tela, a espera é explícita, com `WebDriverWait`. No banco, o `ProdutoDAO` consulta a cada 300 ms até achar o registro, por no máximo 10 segundos.
- A massa que precisa ser única é gerada a cada execução. Cada teste guarda os SKUs que criou, e o `ProdutoDAO` apaga esses registros no final, junto com estoque, movimentações e auditoria. Assim o teste pode rodar de novo.

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

A mensagem de sucesso na tela não garante que o dado foi gravado certo. Por isso os testes consultam o banco, com consultas como esta:

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

O que é conferido no banco:

- se o produto foi gravado depois do cadastro;
- se o estoque mínimo mudou na edição, e se a quantidade em estoque continuou igual;
- se as variações estão no mesmo produto (`id_produto` igual, `id_variacao` diferentes);
- o estado (ativo ou inativo) dos registros depois de uma inativação.

Os BUG-001, BUG-002 e BUG-003 foram investigados por esse caminho: pela tela não dava para ver a causa. O BUG-004 e o BUG-005 apareceram chamando a API direto, na própria resposta.

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

Os 50% contam as regras com pelo menos um caso de teste executado. Não quer dizer que todas as combinações de cada regra foram testadas.

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
├── evidencias/          44 registros de execução
└── cobertura-testes.md  matriz RN → CA → CT
```

Cada caso de teste registra objetivo, pré-condições, massa, passos, resultado esperado, resultado obtido, status, automação relacionada e evidências. Cada defeito tem o vínculo com a regra, o critério de aceite e o caso de teste de onde veio.

**Para começar:**

- [Matriz de cobertura](./docs/cobertura-testes.md): o que existe e o que falta;
- [CT-EST-CAD-004: cadastrar produto válido](./docs/casos-de-teste/cadastro-produto/CT-EST-CAD-004-cadastrar-produto-valido.md): um caso de teste completo;
- [BUG-002: inativação indevida do produto](./docs/bugs/bug-002-inativacao-variacao-inativa-produto.md): uma investigação completa.

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

Próximas regras a cobrir:

- `RN-001`: demais valores limite do nome;
- `RN-004`: demais validações estruturais do SKU;
- `RN-005`: unicidade de SKU;
- `RN-008`: duplicidade produto / cor / tamanho;
- `RN-002`, `RN-003`, `RN-006`, `RN-007` e `RN-009`.

Também previstos:

- automatizar pela API a inativação em massa (`PATCH /produtos/exclusao-massa`). Foi nesse fluxo que apareceu o BUG-003, e é a única parte da `RN-014` sem teste, porque não tem tela;
- cobertura de movimentação, histórico e alertas de estoque;
- execução da suíte em pipeline de integração contínua.

---

**Janayna Mirelly** | Quality Assurance

[LinkedIn](https://www.linkedin.com/in/janayna-mirelly-dev)
