# CT-EST-CAD-004 — Cadastrar produto com dados válidos

## Identificação

* **Módulo:** Estoque
* **Funcionalidade:** Cadastro de produto
* **Camada:** UI
* **Tipo de teste:** Funcional positivo
* **Técnica de teste:** Particionamento de Equivalência — classe válida
* **Regra relacionada:** RN-010 — Cadastro de produto com variação válida
* **Critério de aceite relacionado:** CA-010
* **Ambiente:** Desenvolvimento local
* **Automação:** Sim
* **Status:** Passou
* **Data da execução:** 09/08/2026

## Objetivo

Verificar se o sistema permite o cadastro de um produto quando os campos obrigatórios e os dados da variação são preenchidos com valores válidos.

## Pré-condições

* Aplicação disponível em ambiente de desenvolvimento local.
* Backend/API em execução.
* Tela **Cadastrar Produto** acessível.
* SKU `CAM-VO-G` não cadastrado previamente no banco de dados.

## Massa de teste

| Campo              | Valor             |
| ------------------ | ----------------- |
| Nome do produto    | `Camiseta Básica` |
| Cor                | `VERDE OLIVA`     |
| Tamanho            | `G`               |
| SKU                | `CAM-VO-G`        |
| Preço              | `69.90`           |
| Quantidade inicial | `0`               |
| Estoque mínimo     | `10`              |

## Passos

1. Acessar a aplicação VarejoSync — Módulo de Estoque.
2. Navegar até a tela **Cadastrar Produto**.
3. Consultar o banco de dados e confirmar que o SKU `CAM-VO-G` não está cadastrado.
4. Preencher o formulário conforme a massa de teste.
5. Clicar em **Cadastrar produto**.
6. Verificar a mensagem apresentada pela aplicação.
7. Consultar o estoque pelo SKU utilizado.
8. Consultar o banco de dados utilizando o SKU `CAM-VO-G`.

## Resultado esperado

* O sistema deve concluir o cadastro.
* O sistema deve exibir a mensagem `Produto cadastrado com sucesso`.
* O produto deve ser localizado na consulta de estoque.
* O registro correspondente ao SKU `CAM-VO-G` deve estar persistido no banco de dados.

## Resultado obtido

* O sistema concluiu o cadastro.
* A mensagem `Produto cadastrado com sucesso` foi exibida.
* O produto foi localizado na consulta de estoque.
* A consulta no banco de dados pelo SKU `CAM-VO-G` retornou o produto cadastrado, confirmando sua persistência.

## Evidências

* **EVD-CT-EST-CAD-004-01 — Pré-condição no banco:** consulta realizada antes da execução, confirmando que o SKU `CAM-VO-G` não estava cadastrado.
  [Ver evidência](../../evidencias/ct-est-cad-004/01-pre-condicao-banco.png)

* **EVD-CT-EST-CAD-004-02 — Massa de teste na UI:** formulário preenchido com os dados válidos utilizados na execução.
  [Ver evidência](../../evidencias/ct-est-cad-004/02-cadastro-dados-validos.png)

* **EVD-CT-EST-CAD-004-03 — Mensagem de sucesso:** sistema exibindo a mensagem `Produto cadastrado com sucesso`.
  [Ver evidência](../../evidencias/ct-est-cad-004/03-mensagem-sucesso-exibida.png)

* **EVD-CT-EST-CAD-004-04 — Consulta de estoque:** produto cadastrado localizado na consulta do módulo de estoque.
  [Ver evidência](../../evidencias/ct-est-cad-004/04-consulta-estoque-ui.png)

* **EVD-CT-EST-CAD-004-05 — Pós-condição no banco:** consulta confirmando a persistência do produto e da variação cadastrada.
  [Ver evidência](../../evidencias/ct-est-cad-004/05-pos-condicao-banco.png)

## Automação relacionada

* **Classe:** `CadastroProdutoTest`
* **Método:** `CT_EST_CAD_004_cadastrarProdutoComDadosValidos()`
* **Status da última execução:** Passou

A automação preenche o formulário com dados válidos, realiza o cadastro, valida a mensagem de sucesso e confirma a persistência do SKU no banco de dados.