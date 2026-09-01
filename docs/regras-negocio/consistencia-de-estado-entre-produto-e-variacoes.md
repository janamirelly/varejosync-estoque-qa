# RN-014 — Consistência de estado entre produto e variações

## Objetivo

Garantir a consistência do estado lógico entre um produto e suas variações, impedindo que uma variação ativa permaneça vinculada a um produto inativo.

## Regra de negócio

Cada variação deve estar vinculada ao seu produto de origem por meio do `id_produto` e possuir identificação própria por `id_variacao`.

Enquanto existir pelo menos uma variação ativa vinculada a um produto, o produto de origem deve permanecer ativo.

Uma variação ativa não pode estar vinculada a um produto inativo.

O produto somente pode ser inativado quando não existir nenhuma variação ativa vinculada ao seu `id_produto`.

## Condições

* A operação de inativação deve atuar sobre a variação identificada por `id_variacao`.
* A inativação de uma variação não deve alterar o estado das demais variações do mesmo produto.
* Enquanto existir pelo menos uma variação com `variacao_produto.ativo = 1`, o produto deve permanecer com `produto.ativo = 1`.
* O produto pode ser inativado somente quando todas as suas variações estiverem inativas.
* Uma operação em massa deve inativar somente as variações explicitamente selecionadas.
* A operação em massa não deve inativar o produto caso permaneça pelo menos uma outra variação ativa.
* Os registros devem permanecer fisicamente armazenados no banco de dados.
* O histórico de movimentações não deve ser removido pela inativação do produto ou da variação.

## Estado válido

### Produto com pelo menos uma variação ativa

```text
produto.ativo = 1

variacao_produto.ativo = 1
```

### Produto sem nenhuma variação ativa

```text
produto.ativo = 0

variacao_produto.ativo = 0
```

## Estado inválido

O sistema não deve permitir, em nenhuma circunstância, a combinação abaixo:

```text
produto.ativo = 0
+
variacao_produto.ativo = 1
```

Uma variação ativa vinculada a um produto inativo permanece registrada no banco com SKU e saldo, mas deixa de estar disponível operacionalmente, porque as consultas consideram o estado do produto e o da variação.

O registro existe, o usuário não o encontra e o sistema não sinaliza o motivo.

## Transição de estado

O estado do produto é consequência do estado das suas variações, nunca uma decisão isolada da operação.

| Situação após a operação | `produto.ativo` |
| --- | --- |
| Permanece pelo menos uma variação ativa | permanece `1` |
| Nenhuma variação ativa restante | passa para `0` |

A verificação deve ocorrer **após** a inativação das variações selecionadas, considerando o estado resultante — e não a intenção da operação.

## Exemplo

Considere o produto:

`Calça Jeans — id_produto = 3`

Com as seguintes variações ativas:

| id_variacao | SKU | Tamanho | Estado |
| --- | --- | --- | --- |
| 17 | `CAL-AZUL-P` | P | Ativa |
| 18 | `CAL-AZUL-M` | M | Ativa |
| 19 | `CAL-AZUL-G` | G | Ativa |
| 20 | `CAL-AZUL-GG` | GG | Ativa |

### Inativação parcial

Ao inativar somente `CAL-AZUL-M` e `CAL-AZUL-G`:

| Entidade | Estado esperado |
| --- | --- |
| Produto `id_produto = 3` | Permanece ativo |
| `CAL-AZUL-P` — id_variacao 17 | Permanece ativa |
| `CAL-AZUL-M` — id_variacao 18 | Inativa |
| `CAL-AZUL-G` — id_variacao 19 | Inativa |
| `CAL-AZUL-GG` — id_variacao 20 | Permanece ativa |

O produto permanece ativo porque restam as variações 17 e 20.

### Inativação da última variação ativa

Partindo do estado acima, ao inativar também `CAL-AZUL-P` e `CAL-AZUL-GG`:

| Entidade | Estado esperado |
| --- | --- |
| Produto `id_produto = 3` | Inativo |
| Todas as variações | Inativas |

O produto é inativado como **consequência** de não restar nenhuma variação ativa.

## Rastreabilidade

Esta regra foi formalizada a partir da investigação do **BUG-003 — Exclusão em massa pode inativar produto com variações ainda ativas**, que identificou na base 96 variações ativas vinculadas a 86 produtos inativos.

| Artefato | Referência |
| --- | --- |
| Defeito de origem | [BUG-003](../bugs/bug-003-exclusao-massa-inativa-produto-com-variacoes-ativas.md) |
| Regra complementar | RN-013 — Inativação lógica individual de variação |
| Critério de aceite | CA-014 — Consistência de estado entre produto e variações |

A RN-013 trata da inativação **individual** de uma variação e declara como fora do escopo o comportamento ao inativar a última variação ativa. A RN-014 cobre justamente esse ponto e a inativação **em massa**.

## Fora do escopo

Esta regra não define:

* as validações de cadastro de produto e variação;
* a reativação de produtos ou variações;
* a exclusão física de registros;
* o saneamento de registros que já se encontram em estado inconsistente na base;
* o comportamento de movimentações de estoque após a inativação.

Esses comportamentos devem possuir regras específicas caso sejam implementados.
