# BUG-002 — Inativação de uma variação inativa o produto de origem

## Identificação

* **ID:** BUG-002
* **Módulo:** Estoque
* **Funcionalidade:** Inativação de variação
* **Tipo:** Defeito funcional
* **Severidade:** Alta
* **Prioridade:** Alta
* **Status:** Fechado
* **Ambiente:** Desenvolvimento local
* **Data de identificação:** 18/08/2026
* **Data da correção:** 19/08/2026
* **Data do reteste:** 19/08/2026
* **Resultado do reteste:** Passou
* **Regra relacionada:** RN-013 — Inativação lógica individual de variação
* **Critério de aceite relacionado:** CA-013 — Inativar somente a variação selecionada
* **Caso de teste relacionado:** CT-EST-EXC-001 — Inativar somente a variação selecionada

## Título resumido

Ao excluir uma variação específica pela consulta de estoque, o sistema inativa o produto de origem em vez da variação selecionada.

## Descrição

Ao executar a ação **Excluir** sobre uma variação específica de um produto que possui mais de uma variação ativa, o sistema não inativa a variação selecionada.

A operação altera o estado do **produto de origem** para inativo.

Como consequência, todas as variações vinculadas ao produto deixam de ser apresentadas na consulta de estoque, embora permaneçam com estado ativo na tabela `variacao_produto`.

O comportamento diverge da RN-013 e do CA-013, segundo os quais somente a variação selecionada deve ser inativada.

## Pré-condições

* Aplicação disponível em ambiente de desenvolvimento local.
* Backend/API em execução.
* Banco de dados disponível.
* Produto `Blusa Canelada` cadastrado e ativo.
* Produto identificado por `id_produto = 217`.
* Produto contendo duas variações ativas vinculadas ao mesmo `id_produto`.

### Estado inicial

| Entidade   | Identificador                       | Estado                       |
| ---------- | ----------------------------------- | ---------------------------- |
| Produto    | `id_produto = 217`                  | `produto.ativo = 1`          |
| Variação P | `id_variacao = 234` / `BLU-PRETA-P` | `variacao_produto.ativo = 1` |
| Variação M | `id_variacao = 235` / `BLU-PRETA-M` | `variacao_produto.ativo = 1` |

## Passos para reprodução

1. Acessar o VarejoSync — Módulo de Estoque.
2. Navegar até **Consultar Estoque**.
3. Pesquisar pelo produto `Blusa Canelada`.
4. Confirmar que as variações `BLU-PRETA-P` e `BLU-PRETA-M` estão disponíveis.
5. Na linha correspondente à SKU `BLU-PRETA-M`, clicar em **Excluir**.
6. Confirmar que a mensagem de confirmação identifica a SKU `BLU-PRETA-M`.
7. Confirmar a operação.
8. Observar a mensagem apresentada pela aplicação.
9. Pesquisar novamente por `Blusa Canelada`.
10. Consultar no banco o estado do produto `id_produto = 217`.
11. Consultar no banco o estado da variação `BLU-PRETA-M`.
12. Consultar no banco o estado da variação `BLU-PRETA-P`.
13. Comparar o estado do produto e das duas variações após a operação.

## Resultado esperado

Ao confirmar a operação sobre:

`BLU-PRETA-M — id_variacao = 235`

o sistema deve:

* manter o produto `id_produto = 217` ativo;
* manter a variação `BLU-PRETA-P` ativa;
* inativar somente a variação `BLU-PRETA-M`;
* manter fisicamente os registros no banco de dados;
* remover somente `BLU-PRETA-M` da consulta de variações ativas;
* continuar apresentando `BLU-PRETA-P`;
* exibir a mensagem `Variação excluída com sucesso.`

### Estado esperado no banco

| Entidade               | Estado esperado              |
| ---------------------- | ---------------------------- |
| Produto `217`          | `produto.ativo = 1`          |
| `BLU-PRETA-P` / id 234 | `variacao_produto.ativo = 1` |
| `BLU-PRETA-M` / id 235 | `variacao_produto.ativo = 0` |

## Resultado obtido

Após confirmar a exclusão de `BLU-PRETA-M`:

* o sistema exibiu a mensagem `Produto excluído com sucesso.`;
* o produto `Blusa Canelada` deixou de ser apresentado corretamente com suas variações ativas na consulta;
* o banco apresentou `produto.ativo = 0`;
* `BLU-PRETA-M` permaneceu com `variacao_produto.ativo = 1`;
* `BLU-PRETA-P` também permaneceu com `variacao_produto.ativo = 1`.

### Estado obtido no banco

| Entidade               | Esperado    | Obtido      | Resultado |
| ---------------------- | ----------- | ----------- | --------- |
| Produto `217`          | `ativo = 1` | `ativo = 0` | ❌         |
| `BLU-PRETA-P` / id 234 | `ativo = 1` | `ativo = 1` | ✅         |
| `BLU-PRETA-M` / id 235 | `ativo = 0` | `ativo = 1` | ❌         |

## Comparação do comportamento

### Esperado

```text
Produto 217
ativo = 1

├── BLU-PRETA-P
│   ativo = 1
│
└── BLU-PRETA-M
    ativo = 0
```

### Obtido

```text
Produto 217
ativo = 0

├── BLU-PRETA-P
│   ativo = 1
│
└── BLU-PRETA-M
    ativo = 1
```

## Impacto

O defeito permite que a ação destinada a uma única variação afete o produto de origem.

Em produtos que possuem múltiplas variações, a exclusão de apenas uma SKU pode fazer com que todas as demais variações vinculadas deixem de ser apresentadas na consulta de estoque.

Isso pode indisponibilizar variações que deveriam permanecer ativas e operacionais.

## Evidências

* **EVD-BUG-002-01 — Pré-condição no banco:** produto `id_produto = 217` ativo, contendo `BLU-PRETA-P` e `BLU-PRETA-M`, ambas ativas.
  [Ver evidência](../evidencias/ct-est-exc-001/falha/001-pre-condicao-banco.png)

* **EVD-BUG-002-02 — Estado inicial na UI:** `BLU-PRETA-P` e `BLU-PRETA-M` apresentadas antes da operação.
  [Ver evidência](../evidencias/ct-est-exc-001/falha/002-estado-inicial-ui.png)

* **EVD-BUG-002-03 — Confirmação da operação:** diálogo identificando especificamente `Blusa Canelada — BLU-PRETA-M`.
  [Ver evidência](../evidencias/ct-est-exc-001/falha/003-confirmacao-exclusao.png)

* **EVD-BUG-002-04 — Mensagem após a operação:** aplicação exibindo `Produto excluído com sucesso.`
  [Ver evidência](../evidencias/ct-est-exc-001/falha/004-mensagem-exclusao-sucesso.png)

* **EVD-BUG-002-05 — Pós-condição na UI:** consulta após a operação demonstrando o impacto sobre as variações do produto.
  [Ver evidência](../evidencias/ct-est-exc-001/falha/005-pos-condicao-ui.png)

* **EVD-BUG-002-06 — Pós-condição da variação selecionada:** `BLU-PRETA-M` permanece com `variacao_ativa = 1`, enquanto o produto apresenta `produto_ativo = 0`.
  [Ver evidência](../evidencias/ct-est-exc-001/falha/006-pos-condicao-banco-variacao-m.png)

* **EVD-BUG-002-07 — Pós-condição da variação não selecionada:** `BLU-PRETA-P` permanece com `variacao_ativa = 1`, enquanto o mesmo produto apresenta `produto_ativo = 0`.
  [Ver evidência](../evidencias/ct-est-exc-001/falha/007-pos-condicao-banco-variacao-p.png)

## Causa-raiz confirmada

A interface enviava corretamente o `id_variacao` correspondente à variação selecionada.

O backend localizava essa variação, obtinha o `id_produto` relacionado e executava a inativação sobre a tabela `produto`:

`UPDATE produto SET ativo = 0`

Dessa forma, a operação destinada a uma única variação inativava o produto de origem.

Como a consulta de estoque considera somente registros cujo produto e variação estão ativos, todas as variações vinculadas ao produto deixavam de ser apresentadas na interface, mesmo permanecendo com `variacao_produto.ativo = 1`.

## Correção aplicada

O fluxo foi alterado para realizar a inativação lógica diretamente sobre a variação selecionada:

`UPDATE variacao_produto SET ativo = 0 WHERE id_variacao = ?`

O estado do produto de origem não é mais alterado pela inativação individual de uma variação.

Também foram ajustados:

* a mensagem de confirmação da operação;
* a mensagem de sucesso para `Variação excluída com sucesso.`;
* o registro de auditoria;
* a automação do CT-EST-EXC-001;
* as validações de persistência no banco de dados.

## Observação adicional

A mensagem `Produto excluído com sucesso.` também diverge da operação definida na RN-013.

Para a inativação individual da variação, a mensagem esperada é:

`Variação excluída com sucesso.`

Essa divergência é secundária ao defeito funcional principal e deve ser reavaliada após a correção do fluxo de inativação.

## Reteste

Após a correção, o CT-EST-EXC-001 foi reexecutado utilizando um produto com duas variações ativas vinculadas ao mesmo `id_produto`.

O reteste confirmou que:

* o produto de origem permaneceu ativo;
* somente a variação selecionada foi inativada;
* a outra variação permaneceu ativa;
* somente a variação inativada deixou de aparecer na consulta;
* a mensagem `Variação excluída com sucesso.` foi apresentada;
* o comportamento esperado foi confirmado no banco de dados;
* a automação foi atualizada e executada com sucesso.

**Resultado do reteste:** Passou.

## Encerramento

O defeito foi considerado corrigido após o reteste manual e a execução bem-sucedida do `CT-EST-EXC-001`.

**Status final: Fechado**
