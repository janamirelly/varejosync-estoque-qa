# CT-EST-VAR-001 — Manter variações do mesmo produto vinculadas

## Identificação

- **Módulo:** Estoque
- **Funcionalidade:** Cadastro de variações
- **Camada:** UI + Banco de dados
- **Tipo de teste:** Funcional positivo
- **Técnica de teste:** Transição de Estado
- **Regra relacionada:** RN-012 — Vínculo produto/variações
- **Critério de aceite relacionado:** CA-012 — Deve manter as variações vinculadas ao mesmo produto
- **Ambiente:** Desenvolvimento local
- **Automação:** Sim
- **Status:** Passou
- **Data da execução:** 19/08/2026

## Objetivo

Verificar se, ao cadastrar uma nova variação válida para um produto já existente, o sistema reutiliza o registro do produto de origem e vincula a nova variação ao mesmo `id_produto`, sem criar um novo registro de produto.

## Pré-condições

- Aplicação disponível em ambiente de desenvolvimento local.
- Backend/API em execução.
- Banco de dados disponível.
- Tela **Cadastrar Produto** acessível.
- Dados válidos disponíveis para o cadastro.
- As SKUs utilizadas no teste não devem existir previamente.

## Massa de teste

A massa é gerada dinamicamente pela automação.

As duas variações devem utilizar:

- o mesmo nome de produto;
- a mesma cor;
- tamanhos diferentes;
- SKUs diferentes;
- demais campos obrigatórios com valores válidos.

### Exemplo conceitual

| Campo | Primeira variação | Segunda variação |
| --- | --- | --- |
| Produto | Blusa Canelada | Blusa Canelada |
| Cor | PRETA | PRETA |
| Tamanho | P | M |
| SKU | SKU única P | SKU única M |
| Preço | Válido | Válido |
| Quantidade inicial | Válida | Válida |
| Estoque mínimo | Válido | Válido |

## Passos

1. Acessar a tela **Cadastrar Produto**.
2. Informar os dados válidos do produto e da primeira variação.
3. Cadastrar a primeira variação.
4. Confirmar a mensagem de sucesso.
5. Confirmar a persistência da primeira SKU no banco de dados.
6. Acessar novamente a tela de cadastro.
7. Informar o mesmo nome de produto.
8. Informar uma segunda variação válida, com tamanho e SKU diferentes.
9. Cadastrar a segunda variação.
10. Confirmar a mensagem de sucesso.
11. Confirmar a persistência da segunda SKU.
12. Consultar o `id_produto` associado à primeira SKU.
13. Consultar o `id_produto` associado à segunda SKU.
14. Comparar os identificadores obtidos.

## Resultado esperado

- a primeira variação deve ser cadastrada com sucesso;
- a segunda variação deve ser cadastrada com sucesso;
- cada variação deve possuir sua própria SKU;
- cada variação deve possuir seu próprio `id_variacao`;
- o cadastro da segunda variação não deve criar um novo produto;
- ambas as variações devem estar associadas ao mesmo `id_produto`;
- o produto deve permanecer representado por um único registro na tabela `produto`.

### Estado esperado

```text
produto
id_produto = X
      │
      ├── variação P
      │   id_variacao = A
      │   SKU = SKU-P
      │
      └── variação M
          id_variacao = B
          SKU = SKU-M
```
## Resultado obtido

O caso de teste foi executado com sucesso utilizando massa gerada dinamicamente pela automação.

Foram cadastradas duas variações distintas para o mesmo produto, utilizando:

- o mesmo nome de produto;
- a mesma cor;
- tamanhos diferentes;
- SKUs diferentes.

Após os cadastros:

- a primeira variação foi persistida com sucesso;
- a segunda variação foi persistida com sucesso;
- ambas ficaram vinculadas ao mesmo `id_produto`;
- cada variação recebeu seu próprio `id_variacao`;
- os valores de `id_variacao` eram diferentes entre si;
- não foi criado um segundo registro de produto para a nova variação.

### Validações realizadas

| Validação | Resultado |
| --- | --- |
| Primeira SKU persistida | Conforme ✅ |
| Segunda SKU persistida | Conforme ✅ |
| Mesmo `id_produto` para as duas variações | Conforme ✅ |
| `id_variacao` válido para a primeira variação | Conforme ✅ |
| `id_variacao` válido para a segunda variação | Conforme ✅ |
| `id_variacao` diferentes entre as variações | Conforme ✅ |

## Status

**Passou**
## Evidências

- **EVD-CT-EST-VAR-001-01 — Vínculo produto/variações no banco:** consulta confirmando duas variações distintas, com SKUs, tamanhos e `id_variacao` próprios, vinculadas ao mesmo `id_produto = 225`.
  [Ver evidência](../../evidencias/ct-est-var-001/001-vinculo-variacoes-banco.png)

## Automação relacionada

- **Classe:** `VariacaoProdutoTest`
- **Método:** `CT_EST_VAR_001_manterVariacoesDoMesmoProdutoVinculadas()`
- **Status da última execução:** Passou