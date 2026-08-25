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