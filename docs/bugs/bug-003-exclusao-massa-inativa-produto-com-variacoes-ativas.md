# BUG-003 — Exclusão em massa pode inativar produto com variações ainda ativas

## Identificação

* **ID:** BUG-003
* **Módulo:** Estoque
* **Funcionalidade:** Inativação em massa de variações
* **Tipo:** Defeito funcional / integridade de dados
* **Severidade:** Alta
* **Prioridade:** Alta
* **Status:** Fechado
* **Data da correção:** 23/08/2026
* **Data do reteste:** 23/08/2026
* **Resultado do reteste:** Passou
* **Ambiente:** Desenvolvimento local
* **Data de identificação:** 23/08/2026
* **Regra relacionada:** RN-014 — Consistência de estado entre produto e variações

## Título resumido

A inativação em massa pode alterar o estado do produto de origem para inativo mesmo quando permanecem variações ativas vinculadas ao produto.

## Descrição

Durante a investigação de inconsistências entre cadastro, consulta de estoque e banco de dados, foram identificadas variações com estado ativo vinculadas a produtos com estado inativo.

O estado encontrado foi:

```text
produto.ativo = 0
variacao_produto.ativo = 1
```
## Pré-condições

* Aplicação disponível em ambiente de desenvolvimento local.
* Backend/API em execução.
* Banco de dados SQLite disponível.
* Existência de um produto com duas ou mais variações ativas vinculadas ao mesmo `id_produto`.
* Produto de origem inicialmente com `produto.ativo = 1`.
* Variações inicialmente com `variacao_produto.ativo = 1`.
* Fluxo de inativação em massa disponível por meio do endpoint correspondente.

## Passos para reprodução — fluxo anterior

A possibilidade de reprodução foi confirmada por análise do fluxo anterior da aplicação e do estado persistido no banco.

Na implementação anterior:

1. Utilizar um produto contendo duas ou mais variações ativas vinculadas ao mesmo `id_produto`.
2. Solicitar a inativação em massa informando somente uma das `id_variacao` ativas.
3. O backend localizar os produtos relacionados às variações selecionadas.
4. O fluxo executar a inativação sobre a tabela `produto`, utilizando os `id_produto` encontrados.
5. Consultar novamente o estado do produto e das suas variações no banco de dados.
6. Verificar que o produto pode apresentar `produto.ativo = 0` mesmo permanecendo outra variação com `variacao_produto.ativo = 1`.
7. Pesquisar a variação ainda ativa na tela **Consultar Estoque**.
8. Verificar que a variação deixa de ser apresentada porque o produto de origem está inativo.

> A reprodução do comportamento defeituoso não foi novamente executada antes da correção para evitar ampliar a inconsistência já existente na base. A existência do fluxo capaz de gerar o estado inválido foi confirmada pela análise da implementação anterior.

## Resultado esperado

Ao realizar uma inativação em massa:

* somente as `id_variacao` explicitamente selecionadas devem ser inativadas;
* as variações não selecionadas devem permanecer com seu estado inalterado;
* o produto de origem deve permanecer ativo enquanto existir pelo menos uma variação ativa vinculada ao mesmo `id_produto`;
* o produto somente deve ser inativado quando não existir nenhuma variação ativa vinculada a ele;
* as variações que permanecerem ativas devem continuar disponíveis nas consultas operacionais.

O sistema não deve permitir o estado:

```text
produto.ativo = 0
variacao_produto.ativo = 1
```

## Resultado obtido

Durante a investigação foi identificado na base o seguinte estado inconsistente:

```text
produto.ativo = 0
variacao_produto.ativo = 1
```

Um dos casos identificados foi o produto `Calça Jeans`, `id_produto = 3`, cujo produto de origem estava inativo enquanto suas variações permaneciam ativas.

Entre as variações encontradas estavam:

```text
id_variacao = 17 | SKU = CAL-AZUL-P  | ativo = 1
id_variacao = 18 | SKU = CAL-AZUL-M  | ativo = 1
id_variacao = 19 | SKU = CAL-AZUL-G  | ativo = 1
id_variacao = 20 | SKU = CAL-AZUL-GG | ativo = 1
```

Apesar de permanecerem ativas no banco, essas variações não eram apresentadas na tela **Consultar Estoque**, pois o produto de origem possuía `produto.ativo = 0`.

A análise geral da base identificou:

```text
96 variações ativas vinculadas a produtos inativos
86 produtos afetados
```

Essa quantidade representa a abrangência da inconsistência existente na base e não deve ser atribuída integralmente ao fluxo de exclusão em massa, pois parte dos registros possui origem em comportamentos legados anteriores.

## Impacto

O defeito compromete a integridade entre produto e variações.

Uma variação pode permanecer ativa, possuir SKU e saldo registrados no banco e, mesmo assim, deixar de estar disponível operacionalmente porque o produto de origem foi inativado indevidamente.

Como as consultas atuais consideram o estado do produto e da variação, essa inconsistência pode afetar:

* consulta de estoque;
* disponibilidade operacional das variações;
* alertas de estoque;
* indicadores do dashboard;
* validações relacionadas à existência de SKUs;
* confiabilidade dos dados apresentados ao usuário.

A severidade foi classificada como **Alta** porque o problema mantém registros aparentemente ativos no banco, porém indisponíveis para uso na aplicação.

## Investigação realizada

A investigação teve início durante uma tentativa de cadastro do produto `Calça Jeans`.

Ao informar uma SKU já existente, o cadastro foi corretamente bloqueado com a mensagem:

`SKU já cadastrado para outra variação.`

Em seguida, a mesma SKU foi pesquisada na tela **Consultar Estoque**, porém nenhum produto foi apresentado.

A validação prosseguiu pelas diferentes fontes de dados:

1. Foi consultado o banco de dados e confirmado que a SKU existia.
2. Foi confirmado que a respectiva `variacao_produto.ativo` permanecia igual a `1`.
3. Foi identificado que o produto de origem possuía `produto.ativo = 0`.
4. A API foi consultada diretamente e confirmou simultaneamente `produto_ativo = 0` e `variacao_ativa = 1`.
5. Foi executada uma consulta de integridade em toda a base.
6. A consulta identificou 96 variações ativas vinculadas a 86 produtos inativos.
7. Foi analisado o fluxo de inativação existente no backend.
8. A análise da implementação de exclusão em massa identificou um caminho ainda ativo capaz de inativar o produto de origem sem verificar se permaneciam outras variações ativas.

A investigação permitiu separar o impacto visível na interface da inconsistência persistida no banco e identificar um fluxo de backend capaz de recriar o estado inválido.

## Evidências

* **EVD-BUG-003-01 — Extensão da inconsistência:** consulta identificando 96 variações ativas vinculadas a produtos inativos, distribuídas em 86 produtos.
  [Ver evidência](../evidencias/bug-003/001-total-inconsistencias-96-variacoes-86-produtos.png)

* **EVD-BUG-003-02 — Estado inconsistente no banco:** produto inativo contendo variações com `variacao_produto.ativo = 1`.
  [Ver evidência](../evidencias/bug-003/002-produto-inativo-com-variacoes-ativas.png)

* **EVD-BUG-003-03 — Impacto na consulta de estoque:** SKU existente e ativa no banco não apresentada em **Consultar Estoque** devido ao produto de origem estar inativo.
  [Ver evidência](../evidencias/bug-003/003-sku-nao-retornada-consulta-estoque.png)

* **EVD-BUG-003-04 — Estado confirmado pela API:** resposta demonstrando simultaneamente `produto_ativo = 0` e `variacao_ativa = 1`.
  [Ver evidência](../evidencias/bug-003/004-produto-inativo-variacao-ativa-api.png)

* **EVD-BUG-003-05 — Reteste com outra variação ainda ativa:** após a inativação de uma das variações selecionadas, o produto permaneceu ativo e a outra variação permaneceu ativa.
  [Ver evidência](../evidencias/bug-003/005-reteste-variacao-inativada-produto-permanece-ativo.png)

* **EVD-BUG-003-06 — Reteste da última variação ativa:** após a inativação da última variação ativa, o produto e todas as suas variações ficaram inativos.
  [Ver evidência](../evidencias/bug-003/006-reteste-ultima-variacao-inativa-produto-inativado.png)

## Causa-raiz confirmada

A implementação anterior do fluxo de inativação em massa recebia uma lista de `id_variacao`, localizava os respectivos produtos de origem e utilizava os `id_produto` encontrados para executar a inativação na tabela `produto`.

O fluxo realizava, conceitualmente:

```sql
UPDATE produto
SET ativo = 0
WHERE id_produto IN (...);
```

A operação não verificava se ainda existiam outras variações ativas vinculadas ao mesmo produto.

Dessa forma, ao selecionar apenas uma variação de um produto com múltiplas variações, era possível inativar o produto de origem e manter outras variações com:

```text
variacao_produto.ativo = 1
```

produzindo o estado inválido:

```text
produto.ativo = 0
+
variacao_produto.ativo = 1
```

A causa-raiz do BUG-003 foi localizada no backend, no tratamento da inativação em massa.

## Correção aplicada

O fluxo de inativação em massa foi alterado para atuar diretamente sobre as `id_variacao` selecionadas.

A operação passou a executar a inativação lógica sobre `variacao_produto`:

```sql
UPDATE variacao_produto
SET ativo = 0
WHERE id_variacao IN (...);
```

Após a inativação das variações selecionadas, o sistema verifica se ainda existe alguma variação ativa vinculada ao produto.

Quando ainda existir pelo menos uma variação ativa:

```text
produto.ativo permanece = 1
```

Quando não existir nenhuma variação ativa:

```text
produto.ativo passa para = 0
```

Também foi ajustado o registro de auditoria da operação em massa.

A correção implementa o comportamento definido pela **RN-014 — Consistência de estado entre produto e variações**.