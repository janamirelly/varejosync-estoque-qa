# CT-EST-CAD-003 — Bloquear cadastro com nome abaixo do limite mínimo

## Identificação

* **Módulo:** Estoque
* **Funcionalidade:** Cadastro de produto
* **Camada:** UI
* **Tipo de teste:** Funcional negativo
* **Técnica de teste:** Análise de Valor Limite
* **Regra relacionada:** RN-001 — Nome do produto deve ser obrigatório e válido
* **Critério de aceite relacionado:** CA-001
* **Ambiente:** Desenvolvimento local
* **Status:** Passou
* **Data da execução:** 07/08/2026

## Objetivo

Verificar se o sistema impede o cadastro de um produto quando o campo **Nome do produto** contém 2 caracteres, valor imediatamente abaixo do limite mínimo válido de 3 caracteres definido na RN-001.

## Pré-condições

* Aplicação disponível em ambiente de desenvolvimento local.
* Backend/API em execução.
* Tela **Cadastrar Produto** acessível.
* SKU `CAM-VO-P` não cadastrado previamente no banco de dados.

## Massa de teste

| Campo              | Valor         |
| ------------------ | ------------- |
| Nome do produto    | `CA`          |
| Cor                | `VERDE OLIVA` |
| Tamanho            | `P`           |
| SKU                | `CAM-VO-P`    |
| Preço              | `69.90`       |
| Quantidade inicial | `0`           |
| Estoque mínimo     | `10`          |

## Passos

1. Acessar a aplicação Estoque QA Lab.
2. Navegar até a tela **Cadastrar Produto**.
3. Consultar o banco de dados e confirmar que o SKU `CAM-VO-P` não está cadastrado.
4. Preencher o formulário conforme a massa de teste.
5. Clicar em **Cadastrar produto**.
6. Verificar a mensagem apresentada pela aplicação.
7. Consultar novamente o banco de dados utilizando o SKU `CAM-VO-P`.

## Resultado esperado

* O sistema deve impedir a conclusão do cadastro.
* O sistema deve exibir a mensagem `Informe um nome de produto válido`.
* Nenhum registro correspondente ao SKU `CAM-VO-P` deve ser persistido no banco de dados.

## Resultado obtido

* O sistema impediu a conclusão do cadastro.
* A mensagem `Informe um nome de produto válido` foi exibida.
* A consulta pelo SKU `CAM-VO-P` retornou **0 registros**, confirmando que o produto não foi persistido.

## Evidências

* **EVD-CT-EST-CAD-003-01 — Pré-condição:** consulta no banco antes da execução, confirmando que o SKU `CAM-VO-P` não estava cadastrado.
  [Ver evidência](../../evidencias/EV-EST-CAD-003/01-pre-condicao-banco.png)

* **EVD-CT-EST-CAD-003-02 — Massa de teste:** formulário preenchido com os dados utilizados na execução.
  [Ver evidência](../../evidencias/EV-EST-CAD-003/02-massa-teste-ui.png)

* **EVD-CT-EST-CAD-003-03 — Validação UI:** sistema bloqueando o cadastro e exibindo a mensagem `Informe um nome de produto válido`.
  [Ver evidência](../../evidencias/EV-EST-CAD-003/03-bloqueio-nome-abaixo-minimo.png)

* **EVD-CT-EST-CAD-003-04 — Pós-condição:** consulta no banco após a execução retornando 0 registros para o SKU `CAM-VO-P`.
  [Ver evidência](../../evidencias/EV-EST-CAD-003/04-pos-condicao-banco.png)
