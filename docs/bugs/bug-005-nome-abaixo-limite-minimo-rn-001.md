# BUG-005 — Nome de produto abaixo do limite mínimo é aceito no cadastro e na edição

> **Registro retroativo.** Este defeito foi encontrado em junho de 2026 e corrigido no mesmo mês, antes de o repositório adotar o padrão de documentação de defeitos. O identificador segue a ordem de documentação, não a de descoberta: cronologicamente, este é o primeiro defeito do projeto. Foi registrado em 02/09/2026 para que a correção tivesse rastreabilidade até o achado que a originou.

| | |
| --- | --- |
| **Status** | Corrigido e retestado |
| **Severidade sugerida** | Média — integridade de dados |
| **Ambiente** | Desenvolvimento local |
| **Camada** | API — cadastro e edição de produto · UI — formulário de cadastro |
| **Regra** | RN-001 — Nome do produto deve ser obrigatório e válido |
| **Encontrado em** | Teste exploratório de API · junho/2026 |
| **Corrigido em** | Commit `b142f22` · 26/06/2026 |
| **Retestado em** | `CT-EST-CAD-005` (API) e `CT-EST-CAD-003` (UI) |

## Reproduzir

Reprodutível apenas em commits anteriores à correção: `git checkout b142f22^`.

`POST {{base_url}}/produtos` com o payload abaixo. Apenas o campo `nome` está inválido; os demais atendem às respectivas regras.

```json
{
  "nome": "CB",
  "cor": "PRETO",
  "tamanho": "M",
  "sku": "CAM-PRETO-M",
  "tipo": "ENTRADA",
  "observacao": "",
  "preco": 59.90,
  "quantidade": 2,
  "estoque_min": 10
}
```

## Esperado x obtido

| | |
| --- | --- |
| **Esperado** | `400 Bad Request`, nome rejeitado, nada persistido |
| **Obtido** | `201 Created`, produto persistido com nome de 2 caracteres |

`CB` tem 2 caracteres. A RN-001 exige entre 3 e 30.

## Impacto

O nome é o identificador do produto na consulta de estoque e na verificação de duplicidade (`normalizarTexto(item.nome) === normalizarTexto(nome)`). Um nome de 1 ou 2 caracteres atravessa essa verificação como qualquer outro e permanece na base: a exclusão do sistema é lógica, não há remoção física de produto.

Pelo mesmo caminho passavam nomes formados apenas por números ou por caracteres especiais, já que nenhuma das condições de conteúdo da regra era verificada.

## Evidência

**O registro original da resposta `201 Created` não foi capturado.** O achado é anterior ao padrão de evidências do projeto e a requisição que o produziu não foi salva em collection. A comprovação disponível hoje é indireta:

- o código anterior à correção, onde a única verificação sobre o nome é de campo vazio — `git show b142f22^:backend/src/controllers/produtos.controller.js`;
- o diff da correção, que introduz exatamente as condições ausentes — `git show b142f22`;
- a evidência do reteste posterior à correção — `docs/evidencias/ct-est-cad-005/01-post-produtos-nome-2-caracteres-400.png`.

Registrar essa ausência é parte do defeito: achado sem requisição salva não é reproduzível por terceiros.

---
---

# Investigação

> Leitura opcional. O necessário para entender o defeito e o reteste está acima.

## Onde a validação parava

Em `backend/src/controllers/produtos.controller.js`, antes de `b142f22`, o nome era normalizado na entrada:

```js
const nome = String(req.body.nome || "").trim();
```

E a única verificação sobre ele era de preenchimento:

```js
if (!nome) {
  return res.status(400).json({
    message: "Nome do produto é obrigatório.",
  });
}
```

`"CB"` é uma string não vazia. Passa.

Não era o caso de "validação apenas no frontend": `validarFormularioProduto()`, em `frontend/js/app.js`, fazia a mesma verificação de preenchimento. As duas camadas tinham a mesma lacuna.

## Condições da RN-001

| Condição | Antes de `b142f22` | Depois |
| --- | :---: | :---: |
| Remover espaços no início e no fim | ✅ | ✅ |
| Ser obrigatório | ✅ | ✅ |
| Conter entre 3 e 30 caracteres | ❌ | ✅ |
| Conter pelo menos uma letra | ❌ | ✅ |
| Aceitar apenas letras, números, espaços, acentos e `- / . ( ) %` | ❌ | ✅ |
| Rejeitar valores só de números, espaços ou caracteres especiais | ❌ | ✅ |

Duas de seis condições implementadas. O `CT-EST-CAD-001` passava porque cobre a obrigatoriedade — uma das duas.

## Correção

O commit `b142f22` introduziu `nomeProdutoValido()` nas duas camadas, com implementação idêntica:

```js
function nomeProdutoValido(nome) {
  const nomeTratado = String(nome || "").trim();

  return (
    nomeTratado.length >= 3 &&
    nomeTratado.length <= 30 &&
    /[\p{L}]/u.test(nomeTratado) &&
    /^[\p{L}\p{N} \-/.()%]+$/u.test(nomeTratado)
  );
}
```

Pontos da correção que valem registro:

- foi aplicada em `criarProduto()` **e** em `editarProduto()`. A edição tinha o mesmo defeito e teria permanecido aberta se a correção olhasse apenas o cadastro;
- a mensagem mudou de `Nome do produto é obrigatório.` para `Informe um nome de produto válido.`, que cobre todas as condições da regra e não só a de campo vazio;
- a validação roda depois do `trim`, preservando a normalização que a própria RN-001 exige antes de validar.

## Reteste

| Caso | Camada | Resultado |
| --- | --- | --- |
| `CT-EST-CAD-005` | API — `POST /produtos` com nome de 2 caracteres | `400 Bad Request` · `Informe um nome de produto válido.` |
| `CT-EST-CAD-003` | UI — cadastro com nome abaixo do limite mínimo | Cadastro bloqueado |

Os dois passam. A validação está no backend, e não apenas no formulário — é o `CT-EST-CAD-005` que prova isso: mensagem de erro na tela não demonstra que a API recusou.

## Pendências

- Os limites exatos da regra, 3 e 30 caracteres, não têm caso de teste. Estão declarados como planejados na matriz de cobertura, e são os valores onde a comparação costuma trocar `>=` por `>`.
- A condição "conter pelo menos uma letra" não tem caso de teste próprio.
- Saneamento: verificar se há nomes fora do padrão persistidos na base antes de `b142f22`.
