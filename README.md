# VarejoSync — Módulo de Estoque | Portfólio QA

Portfólio de Quality Assurance desenvolvido sobre o módulo de estoque do **VarejoSync**, uma aplicação web voltada à gestão de varejo.

O projeto demonstra a aplicação prática de testes funcionais, análise de regras de negócio, critérios de aceite, documentação de casos de teste, investigação de defeitos, validações em banco de dados e automação de interface com Java, Selenium WebDriver e JUnit.

A estratégia de testes utiliza rastreabilidade entre:

`Regra de Negócio → Critério de Aceite → Caso de Teste → Execução → Evidência`

---

## Destaques do portfólio

| Artefato | Conteúdo |
| --- | --- |
| [Cobertura de testes](./docs/cobertura-testes.md) | Matriz RN → CA → CT, cobertura atual e gaps |
| [Caso de teste — Cadastro válido](./docs/casos-de-teste/cadastro-produto/CT-EST-CAD-004-cadastrar-produto-valido.md) | Fluxo positivo de cadastro e persistência |
| [Caso de teste — Vínculo produto/variações](./docs/casos-de-teste/variacao-produto/CT-EST-VAR-001-vincular-variacoes-mesmo-produto.md) | Validação do relacionamento entre produto e variações |
| [Caso de teste — Inativação de variação](./docs/casos-de-teste/inativacao-variacao/CT-EST-EXC-001-inativar-variacao.md) | Inativação lógica individual e reteste de defeito |
| [BUG-001 — Vínculo incorreto entre produto e variações](./docs/bugs/bug-001-variacoes-mesmo-produto-ids-distintos.md) | Investigação do relacionamento de dados |
| [BUG-002 — Inativação indevida do produto](./docs/bugs/bug-002-inativacao-variacao-inativa-produto.md) | Defeito investigado, corrigido e retestado |
| [Automação — Java, Selenium e JUnit](./selenium-tests/testes-varejosync-estoque-qa-) | Testes automatizados de interface e validação em banco |

---

## Cobertura atual

| Indicador | Situação |
| --- | ---: |
| Regras de Negócio formalizadas | 13 |
| Critérios de Aceite formalizados | 13 |
| RNs com pelo menos um CT executado | 6 de 13 |
| Cobertura por RN | ~46% |
| Casos de teste funcionais catalogados | 7 |
| Casos funcionais automatizados | 7 |
| Casos funcionais documentados | 7 |
| Testes de navegação / smoke | 2 |
| Total de testes automatizados identificados | 9 |

A porcentagem de cobertura por RN representa regras que possuem ao menos um caso de teste formalmente associado e executado.

Ela **não representa cobertura exaustiva de todas as combinações possíveis**.

[Ver matriz de cobertura completa](./docs/cobertura-testes.md)

---

## Funcionalidades validadas

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

As demais condições das regras de SKU e outras regras ainda não cobertas estão explicitamente registradas no documento de cobertura.

---

## Estratégia de testes

Os testes são elaborados a partir das regras de negócio e critérios de aceite.

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

Quando o comportamento apresentado pela interface não é suficiente para determinar o resultado da operação, a investigação continua pelas demais camadas:

```text
Interface
   ↓
Backend / API
   ↓
Banco de dados
```

Essa abordagem é utilizada principalmente nos cenários de persistência, edição, vínculo entre produto e variações e inativação lógica.

---

## Visualização do sistema

### Dashboard de estoque

![Dashboard de estoque do VarejoSync](assets/screenshots/dashboard-estoque.png)

### Consulta de estoque

![Consulta de produtos, variações e saldos](assets/screenshots/consulta-estoque.png)

### Cadastro de produto

![Cadastro de produto e variação](assets/screenshots/cadastro-produto.png)

> A aplicação é executada atualmente em ambiente de desenvolvimento local.
>
> As evidências do repositório registram os comportamentos validados na interface e no banco de dados.

---

## Casos de teste automatizados

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

### Navegação / Smoke

| ID | Cenário | Resultado |
| --- | --- | --- |
| `CT-EST-NAV-001` | Validar tela inicial do estoque | Passou |
| `CT-EST-NAV-002` | Acessar cadastro pelo menu | Passou |

[Ver código da automação](./selenium-tests/testes-varejosync-estoque-qa)

---

## Automação de interface

A automação utiliza:

**Java + Selenium WebDriver + JUnit**

Os testes incluem validações na interface e, nos cenários em que a persistência é relevante, consultas ao banco SQLite.

A estrutura foi separada por responsabilidades:

```text
src/test/java/
│
├── CadastroProdutoTest
├── CadastroProdutoNegativoTest
├── NavegacaoEstoqueTest
│
├── database/
│   └── ProdutoDAO
│
├── massas/
│   └── MassaCadastroProduto
│
└── variaveis/
    ├── CadastroProduto
    ├── ElementosEstoque
    └── VariaveisEstoque
```

São utilizadas esperas explícitas com `WebDriverWait` e assertions do JUnit para validação dos resultados.

As massas que precisam ser únicas são geradas dinamicamente, reduzindo colisões entre execuções.

---

## Validação em banco de dados

O módulo utiliza **SQLite**.

As consultas ao banco são utilizadas para confirmar estados que não podem ser determinados somente pela interface, como:

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

Entre as validações realizadas estão persistência após cadastro, alteração de estoque mínimo, vínculo entre variações e produto e estado lógico dos registros após uma inativação.

---

## Defeito em destaque — BUG-002

### Inativação de uma variação afetava o produto inteiro

Durante a execução do `CT-EST-EXC-001`, foi identificado que a ação destinada a uma única variação causava o desaparecimento de todas as variações do produto na consulta de estoque.

### Investigação

A interface enviava corretamente o identificador da variação selecionada.

A análise do backend demonstrou que o fluxo utilizava o `id_produto` associado e executava a inativação sobre o produto de origem:

```sql
UPDATE produto
SET ativo = 0
WHERE id_produto = ?;
```

Como a consulta de estoque considera apenas produtos e variações ativas, todas as variações deixavam de ser apresentadas.

### Correção

A operação passou a atuar diretamente sobre a variação:

```sql
UPDATE variacao_produto
SET ativo = 0
WHERE id_variacao = ?;
```

Após a correção:

```text
Produto de origem            ativo = 1
Variação selecionada         ativo = 0
Outra variação               ativo = 1
```

O cenário foi reexecutado manualmente e por automação.

**Resultado do reteste: Passou.**

[Ver BUG-002 completo](./docs/bugs/bug-002-inativacao-variacao-inativa-produto.md)

[Ver CT-EST-EXC-001](./docs/casos-de-teste/inativacao-variacao/CT-EST-EXC-001-inativar-variacao.md)

---

## Outro defeito investigado — BUG-001

Durante os testes de cadastro de variações, foi identificado que diferentes variações pertencentes ao mesmo produto eram vinculadas a registros de produto distintos.

O comportamento esperado era:

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

As variações devem possuir identificadores próprios, mas compartilhar o mesmo `id_produto`.

Após a correção, o cenário passou a ser coberto pelo `CT-EST-VAR-001`, que valida no banco:

```text
id_produto(P) = id_produto(M)
id_variacao(P) != id_variacao(M)
```

[Ver BUG-001 completo](./docs/bugs/bug-001-variacoes-mesmo-produto-ids-distintos.md)

[Ver CT-EST-VAR-001](./docs/casos-de-teste/variacao-produto/CT-EST-VAR-001-vincular-variacoes-mesmo-produto.md)

---

## Regras de negócio e critérios de aceite

A especificação funcional do módulo está documentada separadamente dos casos de teste.

| Documento | Conteúdo |
| --- | --- |
| [Regras de negócio — Cadastro de produto](./docs/regras-negocio/cadastro-produto.md) | RN-001 a RN-012 |
| [Regra — Inativação de variação](./docs/regras-negocio/inativacao-variacao.md) | RN-013 |
| [Critérios de aceite — Cadastro](./docs/criterios-aceite/cadastro-produto.md) | CA-001 a CA-012 |
| [Critério — Inativação de variação](./docs/criterios-aceite/inativacao-variacao.md) | CA-013 |

---

## Documentação e evidências

Os artefatos de QA estão organizados em:

```text
docs/
│
├── regras-negocio/
├── criterios-aceite/
├── casos-de-teste/
├── bugs/
├── evidencias/
└── cobertura-testes.md
```

Os casos de teste registram objetivo, pré-condições, massa, passos, resultado esperado, resultado obtido, status, automação relacionada e evidências quando aplicável.

Os defeitos documentados mantêm o vínculo com a regra, critério de aceite e caso de teste que originou a investigação.

---

## Tecnologias

### QA e automação

| Tecnologia / ferramenta | Uso |
| --- | --- |
| Java | implementação da automação |
| Selenium WebDriver | automação da interface |
| JUnit | execução e assertions |
| SQL / SQLite | validação de persistência e investigação |
| Postman | apoio em validações de API REST |
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

---

## Cobertura ainda planejada

O projeto mantém os gaps explicitamente registrados na matriz de cobertura.

Entre as próximas regras previstas para expansão estão:

- `RN-001` — demais valores limite do nome;
- `RN-004` — demais validações estruturais do SKU;
- `RN-005` — unicidade de SKU;
- `RN-008` — duplicidade produto/cor/tamanho;
- `RN-002`, `RN-003`, `RN-006`, `RN-007` e `RN-009`.

Funcionalidades de movimentação, histórico e alertas de estoque também podem ser incorporadas posteriormente.

A prioridade atual do portfólio é manter a cobertura existente consistente, rastreável e baseada em evidências, sem apresentar cenários ainda não validados como cobertura concluída.

---

## Estrutura do projeto

```text
varejosync-estoque-qa/
│
├── frontend/
├── backend/
├── selenium-tests/
├── docs/
├── assets/
└── README.md
```

O frontend e o backend permitem executar localmente a aplicação utilizada nos testes, enquanto `selenium-tests` concentra a automação e `docs` reúne os artefatos de QA.
