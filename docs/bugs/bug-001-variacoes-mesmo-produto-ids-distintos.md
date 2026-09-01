# BUG-001 — Variações do mesmo produto são vinculadas a produtos distintos

| | |
| --- | --- |
| **Status** | Corrigido — validação manual aprovada |
| **Severidade** | Alta — integridade de dados |
| **Ambiente** | Desenvolvimento local |
| **Camada** | UI + Banco de dados |
| **Regra** | RN-012 — Variações do mesmo produto devem permanecer vinculadas ao produto de origem |
| **Teste de regressão** | CT-EST-VAR-001 |
| **Identificado / Corrigido** | 10/08/2026 · 11/08/2026 |

## Reproduzir

1. Cadastrar o produto `Calça Jeans` com uma variação válida.
2. Cadastrar novas variações para o **mesmo produto**, com SKUs diferentes.
3. Consultar no banco o `id_produto` associado a cada SKU.

## Esperado x obtido

| | |
| --- | --- |
| **Esperado** | As três variações vinculadas ao **mesmo** `id_produto`, cada uma com seu `id_variacao` |
| **Obtido** | Três `id_produto` distintos — `CAL-PRETA-P` → 10, `CAL-PRETA-G` → 11, `CAL-PRETA-M` → 13 |

Cada variação passou a ser tratada no banco como um produto separado.

## Impacto

Quebra o relacionamento produto ↔ variações, do qual dependem edição, inativação, agrupamento, consulta de estoque, relatórios e movimentações.

## Resolução

O cadastro passou a reaproveitar o produto existente quando já há um ativo com o mesmo nome, em vez de criar um novo a cada variação. Validado com `Blusa Canelada`: `BLU-PRETA-P` e `BLU-PRETA-M` vinculadas ao `id_produto = 217`, com `id_variacao` próprios (234 e 235).

**Evidência:** [`bug-001/03-correcao-validada-banco.png`](../evidencias/bug-001/03-correcao-validada-banco.png)

---
---

# Investigação

> Leitura opcional. O necessário para reproduzir e entender o defeito está acima.

## Consulta utilizada

```sql
SELECT
    p.id_produto,
    p.nome,
    p.ativo AS produto_ativo,
    vp.sku,
    vp.ativo AS variacao_ativa
FROM produto p
INNER JOIN variacao_produto vp
    ON vp.id_produto = p.id_produto
WHERE vp.sku IN (
    'CAL-PRETA-G',
    'CAL-PRETA-M',
    'CAL-PRETA-P'
)
ORDER BY vp.sku;
```

## Estado obtido no banco

| Produto | SKU | id_produto |
| --- | --- | --- |
| Calça Jeans | CAL-PRETA-G | 11 |
| Calça Jeans | CAL-PRETA-M | 13 |
| Calça Jeans | CAL-PRETA-P | 10 |

## Causa-raiz confirmada

> Documentada retroativamente em 01/09/2026, a partir do commit `a0da543` (11/08/2026). O registro original continha apenas a validação da correção.

Em `criarProduto()`, no `produtos.controller.js`, o fluxo executava `INSERT INTO produto` **incondicionalmente** a cada cadastro:

```js
const produtoCriado = await run(
  `INSERT INTO produto (nome, descricao, ativo) VALUES (?, NULL, 1)`,
  [nome],
);
```

Em seguida usava o `produtoCriado.lastID` — o identificador do registro recém-inserido — como `id_produto` da variação.

Não havia, em nenhum ponto do fluxo, consulta que verificasse se já existia produto ativo com aquele nome. Toda variação cadastrada criava, portanto, um produto novo, independentemente de o nome já existir na base.

## Correção aplicada

Commit `a0da543` — *fix: corrige vinculo entre produto e variacoes*.

**1. Busca do produto existente**, antes de abrir a transação:

```js
const produtosExistentes = await all(`
  SELECT id_produto, nome
  FROM produto
  WHERE ativo = 1
`);

const produtoExistente = produtosExistentes.find(
  (item) => normalizarTexto(item.nome) === normalizarTexto(nome),
);
```

A comparação usa o nome **normalizado**, não a igualdade literal, para que diferenças de caixa e acentuação não gerem produtos duplicados.

**2. Reaproveitamento do identificador**, dentro da transação:

```js
let idProduto;
let produtoFoiCriado = false;

if (produtoExistente) {
  idProduto = produtoExistente.id_produto;
} else {
  const produtoCriado = await run(/* INSERT INTO produto ... */);
  idProduto = produtoCriado.lastID;
  produtoFoiCriado = true;
}
```

A variável `idProduto` substituiu `produtoCriado.lastID` em todos os pontos do fluxo: no vínculo da variação, no registro de auditoria e no corpo da resposta.

**3. Correção da auditoria**, efeito colateral identificado junto:

```js
produtoFoiCriado ? "PRODUTO_CRIADO" : "VARIACAO_CRIADA"
```

Antes, todo cadastro registrava `PRODUTO_CRIADO`. O registro era coerente com o comportamento defeituoso — já que um produto realmente era criado a cada vez — mas passou a estar errado assim que o vínculo foi corrigido. Cadastrar uma variação de produto existente agora registra `VARIACAO_CRIADA`, com o alvo correspondente.

## Estado após a correção

| id_produto | Produto | id_variacao | SKU |
| --- | --- | --- | --- |
| 217 | Blusa Canelada | 234 | BLU-PRETA-P |
| 217 | Blusa Canelada | 235 | BLU-PRETA-M |

**Resultado da validação:** Aprovado.

## Teste de regressão

Commit `6176d49`, no mesmo dia — *test: adiciona regressao para vinculo entre produto e variacoes*.

O cenário virou o `CT-EST-VAR-001`, que valida no banco que duas variações do mesmo produto compartilham o `id_produto`, possuem SKUs diferentes, mantêm `id_variacao` próprios e não geram um produto novo para cada variação.

## Evidências

* **EVD-001 — Variações na interface:** diferentes SKUs apresentadas para `Calça Jeans`.
  [Ver evidência](../evidencias/bug-001/01-variacoes-mesmo-produto-ui.png)

* **EVD-002 — Inconsistência no banco:** variações associadas a valores distintos de `id_produto`.
  [Ver evidência](../evidencias/bug-001/02-ids-produto-distintos-banco.png)

* **EVD-003 — Consulta após a correção:** duas variações sob o mesmo `id_produto`.
  [Ver evidência](../evidencias/bug-001/03-correcao-validada-banco.png)
