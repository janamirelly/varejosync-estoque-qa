# CT-EST-EDT-001 — Alterar estoque mínimo de uma variação com valor válido

## Identificação

* **Módulo:** Estoque
* **Funcionalidade:** Edição de produto
* **Camada:** UI + Banco de dados
* **Tipo de teste:** Funcional positivo
* **Técnica de teste:** Particionamento de Equivalência — classe válida
* **Regra relacionada:** RN-011 — Alteração do estoque mínimo na edição da variação
* **Critério de aceite relacionado:** CA-011
* **Ambiente:** Desenvolvimento local
* **Automação:** Sim
* **Status:** Passou
* **Data da última execução:** 20/08/2026

## Objetivo

Verificar se o sistema permite alterar o estoque mínimo de uma variação existente quando é informado um valor válido, se o novo valor é persistido corretamente e se a quantidade atual permanece inalterada quando não é modificada pelo usuário durante a edição.

## Pré-condições

* Aplicação disponível em ambiente de desenvolvimento local.
* Backend/API em execução.
* Existência de um produto válido cadastrado para a execução do teste.
* Produto localizável na consulta de estoque por meio do SKU.
* Banco de dados disponível para consulta.

## Massa de teste

A massa de teste do produto é gerada pelos dados válidos definidos em `MassaCadastroProduto`.

O novo estoque mínimo utilizado na automação é obtido por:

`MassaCadastroProduto.novoEstoqueMinimoValidoEdicao()`.

## Passos

1. Acessar a aplicação VarejoSync — Módulo de Estoque.
2. Garantir a existência de um produto válido cadastrado para o teste.
3. Consultar a quantidade atual da variação pelo SKU e registrar o valor inicial.
4. Navegar até a tela **Consultar estoque**.
5. Pesquisar o produto pelo SKU.
6. Clicar em **Editar**.
7. Alterar somente o campo **Estoque mínimo** para um valor válido.
8. Manter os demais campos sem alteração.
9. Clicar em **Salvar alterações**.
10. Verificar a mensagem apresentada pela aplicação.
11. Consultar o banco de dados e verificar o novo estoque mínimo pelo SKU.
12. Consultar novamente a quantidade atual da variação.
13. Comparar a quantidade antes e depois da edição.


## Resultado esperado

* O sistema deve permitir a alteração do estoque mínimo.
* O sistema deve exibir a mensagem `Alteração salva com sucesso`.
* O novo valor de estoque mínimo deve ser persistido para a variação correspondente.
* A quantidade atual em estoque deve permanecer igual ao valor existente antes da edição, pois esse campo não foi modificado pelo usuário.

## Resultado obtido

* O sistema permitiu a alteração do estoque mínimo da variação.
* O estoque mínimo foi alterado de `10` para `12`.
* A mensagem `Alteração salva com sucesso` foi exibida.
* A consulta de estoque apresentou o novo estoque mínimo igual a `12`.
* A consulta no banco de dados confirmou a persistência do estoque mínimo igual a `12`.
* A quantidade atual foi consultada antes e depois da edição.
* A quantidade permaneceu com o mesmo valor após a alteração do estoque mínimo, confirmando que não ocorreu alteração colateral nesse campo.

## Status

**Passou**


## Evidências

* **EVD-CT-EST-EDT-001-01 — Estado inicial da variação:** tela de edição da variação CAM-AZUL-M, apresentando estoque mínimo igual a 10 antes da alteração.
[Ver evidência](../../evidencias/ct-est-edt-001/01-estoque-minimo-antes-edicao.png)

* **EVD-CT-EST-EDT-001-02 — Novo estoque mínimo informado:** tela de edição da variação CAM-AZUL-M com o estoque mínimo alterado de 10 para 12, antes de salvar a alteração.
[Ver evidência](../../evidencias/ct-est-edt-001/02-novo-estoque-minimo.png)

* **EVD-CT-EST-EDT-001-03 — Confirmação da alteração:** sistema exibindo a mensagem Alteração salva com sucesso. após a operação.
[Ver evidência](../../evidencias/ct-est-edt-001/03-mensagem-alteracao-sucesso.png)

* **EVD-CT-EST-EDT-001-04 — Validação na consulta de estoque:** busca pela SKU CAM-AZUL-M após a edição, apresentando estoque mínimo igual a 12.
[Ver evidência](../../evidencias/ct-est-edt-001/04-estoque-minimo-atualizado-ui.png)

* **EVD-CT-EST-EDT-001-05 — Persistência no banco de dados:** consulta pelo SKU CAM-AZUL-M confirmando estoque mínimo igual a 12.
[Ver evidência](../../evidencias/ct-est-edt-001/05-pos-condicao-banco.png)


## Automação relacionada

* **Classe:** `CadastroProdutoTest`
* **Método:** `CT_EST_EDT_001_alterarEstoqueMinimoDaVariacao()`
* **Status da última execução:** Passou

A automação registra a quantidade atual da variação antes da edição, altera somente o estoque mínimo, valida a mensagem de sucesso, confirma a persistência do novo estoque mínimo no banco de dados e compara a quantidade antes e depois da operação.

A comparação da quantidade antes e depois da edição é realizada por consulta direta ao banco de dados por meio de `ProdutoDAO.obterQuantidadePorSku()`.



