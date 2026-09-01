# BUG-003 — Exclusão em massa pode inativar produto com variações ainda ativas

| | |
| --- | --- |
| **Status** | Fechado |
| **Severidade** | Alta — integridade de dados |
| **Ambiente** | Desenvolvimento local |
| **Camada** | API + Banco de dados |
| **Regra** | RN-014 — Consistência de estado entre produto e variações |
| **Origem** | Investigação de inconsistência na base — sem caso de teste associado |
| **Identificado / Corrigido / Retestado** | 23/08/2026 · 23/08/2026 · 23/08/2026 — Passou |

## Reproduzir

Fluxo anterior, com um produto contendo duas ou mais variações ativas sob o mesmo `id_produto`:

1. Solicitar a inativação em massa informando **apenas uma** das `id_variacao` ativas.
2. Consultar no banco o estado do produto e de todas as suas variações.
3. Pesquisar na tela **Consultar Estoque** a variação que permaneceu ativa.

> A reprodução não foi reexecutada antes da correção, para não ampliar a inconsistência já existente na base. A existência do fluxo capaz de gerar o estado inválido foi confirmada por análise da implementação anterior.

## Esperado x obtido

| | |
| --- | --- |
| **Esperado** | Somente as variações selecionadas inativadas; o produto permanece ativo enquanto restar ao menos uma variação ativa |
| **Obtido** | Produto inativado com variações ainda ativas — o estado `produto.ativo = 0` + `variacao_produto.ativo = 1` |

Caso concreto: `Calça Jeans`, `id_produto = 3`, inativo, com `CAL-AZUL-P`, `CAL-AZUL-M`, `CAL-AZUL-G` e `CAL-AZUL-GG` ativas. A consulta de integridade encontrou **96 variações ativas em 86 produtos inativos** na base.

## Impacto

Uma variação pode permanecer ativa, com SKU e saldo registrados, e ainda assim ficar indisponível operacionalmente porque o produto de origem foi inativado indevidamente. Afeta consulta de estoque, alertas, indicadores do dashboard e validações de existência de SKU.

## Resolução

A inativação em massa passou a atuar sobre as variações selecionadas:

```sql
UPDATE variacao_produto SET ativo = 0 WHERE id_variacao IN (...);
```

Em seguida o sistema verifica se restam variações ativas: se sim, `produto.ativo` permanece `1`; se não, passa a `0`. Reteste aprovado nos dois sentidos.

**Evidência:** [`bug-003/`](../evidencias/bug-003/)

---
---

# Investigação

> Leitura opcional. O necessário para reproduzir e corrigir está acima.

## Como o defeito apareceu

A investigação começou por acaso, durante uma tentativa de cadastro do produto `Calça Jeans`. Ao informar uma SKU já existente, o cadastro foi corretamente bloqueado:

`SKU já cadastrado para outra variação.`

Em seguida, a mesma SKU foi pesquisada em **Consultar Estoque** e nenhum produto foi apresentado. O sistema afirmava simultaneamente que a SKU existia e que ela não existia.

## Passos da investigação

1. Consulta ao banco confirmou que a SKU existia.
2. Confirmado que `variacao_produto.ativo = 1`.
3. Identificado que o produto de origem possuía `produto.ativo = 0`.
4. A API foi consultada diretamente e confirmou simultaneamente `produto_ativo = 0` e `variacao_ativa = 1`.
5. Executada consulta de integridade em toda a base.
6. A consulta identificou 96 variações ativas vinculadas a 86 produtos inativos.
7. Analisado o fluxo de inativação no backend.
8. A análise da exclusão em massa identificou um caminho ainda ativo capaz de inativar o produto de origem sem verificar se restavam outras variações ativas.

A investigação permitiu separar o impacto visível na interface da inconsistência persistida no banco, e identificar o fluxo capaz de recriar o estado inválido.

## Estado encontrado

```text
produto.ativo = 0
variacao_produto.ativo = 1
```

Variações do `id_produto = 3`:

```text
id_variacao = 17 | SKU = CAL-AZUL-P  | ativo = 1
id_variacao = 18 | SKU = CAL-AZUL-M  | ativo = 1
id_variacao = 19 | SKU = CAL-AZUL-G  | ativo = 1
id_variacao = 20 | SKU = CAL-AZUL-GG | ativo = 1
```

Abrangência na base:

```text
96 variações ativas vinculadas a produtos inativos
86 produtos afetados
```

Essa quantidade não deve ser atribuída integralmente ao fluxo de exclusão em massa: parte dos registros tem origem em comportamentos legados anteriores.

## Causa-raiz confirmada

A implementação anterior recebia uma lista de `id_variacao`, localizava os respectivos produtos de origem e usava os `id_produto` encontrados para executar a inativação na tabela `produto`:

```sql
UPDATE produto
SET ativo = 0
WHERE id_produto IN (...);
```

A operação não verificava se ainda existiam outras variações ativas vinculadas ao mesmo produto. Selecionando apenas uma variação de um produto com múltiplas, era possível inativar o produto e manter as demais com `variacao_produto.ativo = 1`.

## Correção aplicada

Inativação lógica direta sobre as variações selecionadas:

```sql
UPDATE variacao_produto
SET ativo = 0
WHERE id_variacao IN (...);
```

Após a inativação, o sistema verifica se ainda existe alguma variação ativa vinculada ao produto:

| Situação | `produto.ativo` |
| --- | --- |
| Ainda existe variação ativa | permanece `1` |
| Não existe nenhuma variação ativa | passa a `0` |

Também foi ajustado o registro de auditoria da operação em massa. A correção implementa o comportamento definido pela RN-014.

## Evidências

* **EVD-BUG-003-01 — Extensão da inconsistência:** 96 variações ativas vinculadas a produtos inativos, em 86 produtos.
  [Ver evidência](../evidencias/bug-003/001-total-inconsistencias-96-variacoes-86-produtos.png)

* **EVD-BUG-003-02 — Estado inconsistente no banco:** produto inativo com variações `ativo = 1`.
  [Ver evidência](../evidencias/bug-003/002-produto-inativo-com-variacoes-ativas.png)

* **EVD-BUG-003-03 — Impacto na consulta:** SKU existente e ativa não apresentada em **Consultar Estoque**.
  [Ver evidência](../evidencias/bug-003/003-sku-nao-retornada-consulta-estoque.png)

* **EVD-BUG-003-04 — Estado confirmado pela API:** resposta com `produto_ativo = 0` e `variacao_ativa = 1`.
  [Ver evidência](../evidencias/bug-003/004-produto-inativo-variacao-ativa-api.png)

* **EVD-BUG-003-05 — Reteste com outra variação ativa:** produto permaneceu ativo e a outra variação também.
  [Ver evidência](../evidencias/bug-003/005-reteste-variacao-inativada-produto-permanece-ativo.png)

* **EVD-BUG-003-06 — Reteste da última variação ativa:** produto e variações ficaram inativos.
  [Ver evidência](../evidencias/bug-003/006-reteste-ultima-variacao-inativa-produto-inativado.png)

## Cobertura posterior

O `CT-EST-EXC-002` foi criado a partir da RN-014 e cobre, pela interface, a inativação da última variação ativa e a invariante de integridade em toda a base.

O cenário de inativação **em massa** permanece descoberto por automação: não existe tela para ele, o fluxo é exposto somente pelo endpoint `PATCH /produtos/exclusao-massa`, e depende de teste de API. É o gap de maior risco da suíte e está declarado na matriz de cobertura.