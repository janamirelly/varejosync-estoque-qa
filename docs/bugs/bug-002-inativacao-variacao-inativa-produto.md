# BUG-002 — Inativação de uma variação inativa o produto de origem

| | |
| --- | --- |
| **Status** | Fechado |
| **Severidade** | Alta |
| **Ambiente** | Desenvolvimento local |
| **Camada** | UI + Banco de dados |
| **Regra** | RN-013 — Inativação lógica individual de variação · CA-013 |
| **Caso de teste** | CT-EST-EXC-001 |
| **Identificado / Corrigido / Retestado** | 18/08/2026 · 19/08/2026 · 19/08/2026 — Passou |

## Reproduzir

Produto `Blusa Canelada` (`id_produto = 217`) com duas variações ativas: `BLU-PRETA-P` (234) e `BLU-PRETA-M` (235).

1. Acessar **Consultar Estoque** e pesquisar `Blusa Canelada`.
2. Na linha da SKU `BLU-PRETA-M`, clicar em **Excluir**.
3. Confirmar a operação — o diálogo identifica corretamente `BLU-PRETA-M`.
4. Consultar no banco o estado do produto `217` e das duas variações.

## Esperado x obtido

| Entidade | Esperado | Obtido | |
| --- | --- | --- | :---: |
| Produto `217` | `ativo = 1` | `ativo = 0` | ❌ |
| `BLU-PRETA-M` / 235 — selecionada | `ativo = 0` | `ativo = 1` | ❌ |
| `BLU-PRETA-P` / 234 — não selecionada | `ativo = 1` | `ativo = 1` | ✅ |
| Mensagem | `Variação excluída com sucesso.` | `Produto excluído com sucesso.` | ❌ |

A operação destinada a uma variação inativou o **produto**, e a variação selecionada permaneceu ativa.

## Impacto

Em produtos com múltiplas variações, excluir uma única SKU faz todas as demais desaparecerem da consulta de estoque — elas permanecem `ativo = 1` no banco, mas a consulta considera o estado do produto. Variações que deveriam continuar operacionais ficam indisponíveis.

## Resolução

O fluxo passou a inativar diretamente a variação selecionada:

```sql
UPDATE variacao_produto SET ativo = 0 WHERE id_variacao = ?
```

O estado do produto de origem não é mais alterado pela inativação individual. Reteste do `CT-EST-EXC-001` aprovado.

**Evidência:** [`ct-est-exc-001/falha/`](../evidencias/ct-est-exc-001/falha/) e [`ct-est-exc-001/reteste/`](../evidencias/ct-est-exc-001/reteste/)

---
---

# Investigação

> Leitura opcional. O necessário para reproduzir e corrigir está acima.

## Estado inicial

| Entidade | Identificador | Estado |
| --- | --- | --- |
| Produto | `id_produto = 217` | `produto.ativo = 1` |
| Variação P | `id_variacao = 234` / `BLU-PRETA-P` | `variacao_produto.ativo = 1` |
| Variação M | `id_variacao = 235` / `BLU-PRETA-M` | `variacao_produto.ativo = 1` |

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

## Causa-raiz confirmada

A interface enviava corretamente o `id_variacao` da variação selecionada.

O backend localizava essa variação, obtinha o `id_produto` relacionado e executava a inativação sobre a tabela `produto`:

```sql
UPDATE produto SET ativo = 0
```

Como a consulta de estoque considera somente registros cujo produto **e** variação estão ativos, todas as variações vinculadas deixavam de ser apresentadas na interface, mesmo permanecendo com `variacao_produto.ativo = 1`.

## Correção aplicada

Inativação lógica direta sobre a variação selecionada:

```sql
UPDATE variacao_produto SET ativo = 0 WHERE id_variacao = ?
```

Também foram ajustados:

* a mensagem de confirmação da operação;
* a mensagem de sucesso, para `Variação excluída com sucesso.`;
* o registro de auditoria;
* a automação do `CT-EST-EXC-001`;
* as validações de persistência no banco.

## Observação secundária

A mensagem `Produto excluído com sucesso.` divergia da operação definida na RN-013, que para a inativação individual espera `Variação excluída com sucesso.` A divergência era secundária ao defeito funcional e foi corrigida junto com o fluxo.

## Reteste

Reexecutado o `CT-EST-EXC-001` com um produto de duas variações ativas vinculadas ao mesmo `id_produto`. Confirmou-se que:

* o produto de origem permaneceu ativo;
* somente a variação selecionada foi inativada;
* a outra variação permaneceu ativa;
* somente a variação inativada deixou de aparecer na consulta;
* a mensagem `Variação excluída com sucesso.` foi apresentada;
* o comportamento foi confirmado no banco;
* a automação foi atualizada e executada com sucesso.

**Resultado do reteste:** Passou.

## Evidências

* **EVD-BUG-002-01 — Pré-condição no banco:** produto `217` ativo, com `BLU-PRETA-P` e `BLU-PRETA-M` ativas.
  [Ver evidência](../evidencias/ct-est-exc-001/falha/001-pre-condicao-banco.png)

* **EVD-BUG-002-02 — Estado inicial na UI:** as duas variações apresentadas antes da operação.
  [Ver evidência](../evidencias/ct-est-exc-001/falha/002-Estado-inicial-ui.png)

* **EVD-BUG-002-03 — Confirmação da operação:** diálogo identificando `Blusa Canelada — BLU-PRETA-M`.
  [Ver evidência](../evidencias/ct-est-exc-001/falha/003-confirmacao-exclusao.png)

* **EVD-BUG-002-04 — Mensagem após a operação:** `Produto excluído com sucesso.`
  [Ver evidência](../evidencias/ct-est-exc-001/falha/004-mensagem-exclusao-sucesso.png)

* **EVD-BUG-002-05 — Pós-condição na UI:** consulta demonstrando o impacto sobre as variações.
  [Ver evidência](../evidencias/ct-est-exc-001/falha/005-pos-condicao-ui.png)

* **EVD-BUG-002-06 — Pós-condição da variação selecionada:** `BLU-PRETA-M` com `variacao_ativa = 1` e `produto_ativo = 0`.
  [Ver evidência](../evidencias/ct-est-exc-001/falha/006-pos-condicao-banco-variacao-m.png)

* **EVD-BUG-002-07 — Pós-condição da variação não selecionada:** `BLU-PRETA-P` com `variacao_ativa = 1` e `produto_ativo = 0`.
  [Ver evidência](../evidencias/ct-est-exc-001/falha/007-pos-condicao-banco-variacao-p.png)

## Encerramento

Defeito considerado corrigido após o reteste manual e a execução bem-sucedida do `CT-EST-EXC-001`.

**Status final: Fechado**
