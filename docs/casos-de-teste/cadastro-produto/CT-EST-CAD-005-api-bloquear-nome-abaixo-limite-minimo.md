# CT-EST-CAD-005 — API bloqueia cadastro com nome abaixo do limite mínimo

## Identificação

- **Módulo:** Estoque
- **Funcionalidade:** Cadastro de produto
- **Camada:** API
- **Tipo de teste:** Funcional negativo
- **Técnica de teste:** Análise de Valor Limite
- **Regra relacionada:** RN-001 — Nome do produto deve ser obrigatório e válido
- **Critério de aceite relacionado:** CA-001
- **Ambiente:** Desenvolvimento local
- **Automação:** Não — execução manual via Postman
- **Status:** Passou
- **Data da última execução:** 31/08/2026
- **Defeito relacionado:** Nenhum

## Objetivo

Verificar se o endpoint de cadastro rejeita um nome com 2 caracteres — valor imediatamente abaixo do limite mínimo de 3 definido na RN-001 — quando a requisição é enviada **diretamente à API**, sem passar pelo formulário.

Este caso é o par do `CT-EST-CAD-003` na camada de baixo:

| Caso | Camada | Caminho da requisição |
| --- | --- | --- |
| `CT-EST-CAD-003` | UI | formulário **Cadastrar Produto** |
| `CT-EST-CAD-005` | API | `POST {{base_url}}/produtos` |

A distinção importa: uma validação implementada apenas no frontend produz um `CT-EST-CAD-003` aprovado e, ainda assim, permite a entrada de dados inválidos por qualquer cliente que não seja o formulário. Este caso existe para determinar se a regra é da aplicação ou apenas da tela.

## Pré-condições

- Backend/API em execução no ambiente de desenvolvimento local.
- Banco de dados SQLite disponível.
- Variável `base_url` configurada na collection `varejosync-estoque-qa - API`.
- SKU `CAM-VINHO-G` não cadastrado previamente.

## Requisição

- **Método:** `POST`
- **Endpoint:** `{{base_url}}/produtos`
- **Body:** `raw` / `JSON`

```json
{
  "nome": "CA",
  "cor": "VINHO",
  "tamanho": "G",
  "sku": "CAM-VINHO-G",
  "tipo": "ENTRADA",
  "observacao": "",
  "preco": 59.90,
  "quantidade": 2,
  "estoque_min": 10
}
```

### Massa de teste

| Campo | Valor | Observação |
| --- | --- | --- |
| `nome` | `CA` | **2 caracteres** — abaixo do mínimo de 3 da RN-001 |
| `cor` | `VINHO` | válido |
| `tamanho` | `G` | válido |
| `sku` | `CAM-VINHO-G` | válido |
| `preco` | `59.90` | válido |
| `quantidade` | `2` | válido |
| `estoque_min` | `10` | válido |

Apenas o campo `nome` está inválido. Todos os demais atendem às respectivas regras, para que a rejeição possa ser atribuída inequivocamente à RN-001.

## Passos

1. Subir o backend no ambiente de desenvolvimento local.
2. Abrir a collection `varejosync-estoque-qa - API`, requisição **Cadastrar Produtos**.
3. Confirmar que a variável `base_url` aponta para o ambiente local.
4. Consultar o banco e confirmar que o SKU `CAM-VINHO-G` não está cadastrado.
5. Selecionar o método `POST` e o endpoint `/produtos`.
6. Informar no corpo da requisição o JSON da massa de teste.
7. Enviar a requisição.
8. Verificar o status code da resposta.
9. Verificar o corpo da resposta.
10. Consultar novamente o banco pelo SKU `CAM-VINHO-G`.

## Resultado esperado

- A API deve rejeitar a requisição.
- O status code deve ser `400 Bad Request`.
- O corpo da resposta deve conter a mensagem `Informe um nome de produto válido.`
- Nenhum registro correspondente ao SKU `CAM-VINHO-G` deve ser persistido.
- A mensagem retornada pela API deve ser a mesma apresentada pela interface no `CT-EST-CAD-003`.

## Resultado obtido

- A API rejeitou a requisição.
- O status code retornado foi `400 Bad Request`, em 5 ms, com 325 B de resposta.
- O corpo da resposta foi:

```json
{
  "message": "Informe um nome de produto válido."
}
```

- Nenhum registro foi persistido para o SKU `CAM-VINHO-G`.
- A mensagem coincide com a apresentada pela interface no `CT-EST-CAD-003`.

### Estado obtido

| Verificação | Esperado | Obtido |
| --- | --- | --- |
| Status code | `400 Bad Request` | `400 Bad Request` ✅ |
| Mensagem da resposta | `Informe um nome de produto válido.` | Conforme esperado ✅ |
| Persistência do SKU `CAM-VINHO-G` | Nenhum registro | Nenhum registro ✅ |
| Consistência da mensagem entre UI e API | Mesma mensagem | Mesma mensagem ✅ |

## Status

**Passou**

## Conclusão

A validação da RN-001 está implementada **no backend**, e não apenas no formulário da interface.

Esse é o resultado relevante do caso. O `CT-EST-CAD-003` sozinho não permitia essa conclusão: uma interface que bloqueia o envio produz o mesmo resultado aprovado tanto num sistema que valida no servidor quanto num que não valida. Só a chamada direta ao endpoint distingue os dois cenários.

A resposta também é adequada em forma, não apenas em efeito: o status `400` classifica corretamente o erro como falha de validação da entrada, e a mensagem é idêntica à da interface, o que indica origem única da regra em vez de duas implementações paralelas que podem divergir com o tempo.

## Cobertura da RN-001

Este caso **não amplia** a cobertura da RN-001 em número de condições — a condição "nome abaixo do limite mínimo" já era coberta pelo `CT-EST-CAD-003`. O que ele acrescenta é a verificação da mesma condição em uma segunda camada.

| Condição da RN-001 | UI | API |
| --- | :---: | :---: |
| Nome obrigatório | `CT-EST-CAD-001` | ❌ não coberto |
| Nome com menos de 3 caracteres | `CT-EST-CAD-003` | `CT-EST-CAD-005` ✅ |
| Nome com exatamente 3 caracteres | ❌ planejado | ❌ planejado |
| Nome com exatamente 30 caracteres | ❌ planejado | ❌ planejado |
| Nome com mais de 30 caracteres | ❌ planejado | ❌ planejado |
| Nome apenas com números | ❌ planejado | ❌ planejado |
| Espaços no início e no fim | ❌ planejado | ❌ planejado |

A RN-001 permanece com **cobertura parcial**.

## Fora do escopo

Este caso de teste não valida:

- as demais condições da RN-001 (limite máximo, composição do nome, tratamento de espaços);
- a validação dos outros campos do payload;
- o comportamento do endpoint com nome ausente ou nulo, em vez de curto;
- o endpoint `PATCH /produtos/exclusao-massa`, que permanece como gap declarado da RN-014.

## Histórico de execução

| Execução | Resultado | Observação |
| --- | --- | --- |
| 31/08/2026 — primeira execução | Passou | Primeiro caso de teste da suíte na camada API |

## Evidências

* **EVD-CT-EST-CAD-005-01 — Requisição e resposta:** `POST /produtos` com `"nome": "CA"` retornando `400 Bad Request` e a mensagem `Informe um nome de produto válido.`
  [Ver evidência](../../evidencias/ct-est-cad-005/01-post-produtos-nome-2-caracteres-400.png)
