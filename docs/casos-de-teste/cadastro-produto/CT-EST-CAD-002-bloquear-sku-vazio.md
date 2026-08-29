# CT-EST-CAD-002 — Bloquear cadastro com SKU vazio

## Identificação

* **Módulo:** Estoque
* **Funcionalidade:** Cadastro de produto
* **Camada:** UI
* **Tipo de teste:** Funcional negativo
* **Técnica de teste:** Particionamento de Equivalência — classe inválida
* **Regra relacionada:** RN-004 — SKU da variação deve ser obrigatório e válido
* **Critério de aceite relacionado:** CA-004
* **Ambiente:** Desenvolvimento local
* **Automação:** Sim
* **Status:** Passou (por automação; evidência manual pendente)
* **Data da execução:** 19/08/2026

## Objetivo

Verificar se o sistema impede o cadastro quando o campo **SKU da variação** não é preenchido, conforme a obrigatoriedade definida na RN-004.

## Pré-condições

* Aplicação disponível em ambiente de desenvolvimento local.
* Backend/API em execução.
* Banco de dados disponível.
* Tela **Cadastrar Produto** acessível.
* Demais campos obrigatórios preenchidos com valores válidos.

## Massa de teste

| Campo | Valor |
| --- | --- |
| Nome do produto | Gerado dinamicamente |
| Cor | `Verde Oliva` |
| Tamanho | `P` |
| SKU | vazio |
| Preço | `69.90` |
| Quantidade inicial | `0` |
| Estoque mínimo | `10` |

## Passos

1. Acessar a aplicação VarejoSync — Módulo de Estoque.
2. Navegar até **Cadastrar Produto**.
3. Preencher os dados do produto com valores válidos.
4. Manter o campo **SKU da variação** vazio.
5. Preencher os demais campos obrigatórios com valores válidos.
6. Clicar em **Cadastrar produto**.
7. Verificar a mensagem apresentada pelo sistema.

## Resultado esperado

* O sistema deve impedir a conclusão do cadastro.
* O SKU vazio deve ser considerado inválido.
* O sistema deve apresentar a mensagem de validação correspondente ao SKU inválido.
* A variação não deve ser cadastrada.

## Resultado obtido

* O sistema impediu a conclusão do cadastro.
* A mensagem de validação referente ao SKU inválido foi apresentada.
* O comportamento ficou de acordo com a RN-004 e o CA-004.

## Status

**Passou**

## Evidências

**Pendente de captura.**

Este caso é executado por automação e o resultado é registrado pelo JUnit, mas ainda não possui evidência manual capturada. Enquanto ela não existir, o status **Passou** apoia-se apenas na execução automatizada.

Para completar a evidência, ver o [padrão de evidências](../../padrao-evidencias.md). São necessárias:

| # | Momento | O que capturar |
| :---: | --- | --- |
| 01 | Pré-condição | consulta ao banco pelo SKU da massa, retornando 0 linhas |
| 02 | Ação | formulário preenchido, com o campo inválido visível |
| 03 | Resultado na tela | mensagem de erro exibida pelo sistema |
| 04 | Pós-condição | consulta ao banco pelo mesmo SKU, ainda retornando 0 linhas |

A evidência 04 é a que realmente prova o caso: mensagem de erro na tela não demonstra que o back-end recusou o cadastro.

Destino dos arquivos: `docs/evidencias/ct-est-cad-002/`

## Automação relacionada

* **Classe:** `CadastroProdutoNegativoTest`
* **Método:** `CT_EST_CAD_002_bloquearCadastroComSkuVazio()`
* **Status da última execução:** Passou

A automação preenche o formulário mantendo o SKU vazio, utiliza valores válidos nos demais campos, executa a tentativa de cadastro e valida a mensagem de bloqueio esperada.