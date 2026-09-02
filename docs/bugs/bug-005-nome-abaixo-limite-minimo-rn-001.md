# BUG-005 — Nome de produto abaixo do limite mínimo é aceito no cadastro e na edição

> **Registro retroativo.** Este defeito foi encontrado em maio de 2026 e corrigido em junho, antes de o repositório adotar o padrão de documentação de defeitos. O identificador segue a ordem de documentação, não a de descoberta: cronologicamente, este é o primeiro defeito do projeto. Foi registrado em 02/09/2026 para que a correção tivesse rastreabilidade até o achado que a originou.

| | |
| --- | --- |
| **Status** | Corrigido e retestado |
| **Severidade sugerida** | Média — integridade de dados |
| **Ambiente** | Desenvolvimento local |
| **Camada** | API — cadastro e edição de produto · UI — formulário de cadastro |
| **Regra** | RN-001 — Nome do produto deve ser obrigatório e válido |
| **Encontrado em** | Teste exploratório em três camadas · 15/05/2026, reproduzido em 18/05 |
| **Corrigido em** | Commit `b142f22` · 26/06/2026 |
| **Retestado em** | `CT-EST-CAD-005` (API) e `CT-EST-CAD-003` (UI) |

## Reproduzir

Reprodutível apenas em commits anteriores à correção: `git checkout b142f22^`.

`POST {{base_url}}/produtos` com o payload abaixo. Apenas o campo `nome` está inválido; os demais atendem às respectivas regras.

```json
{
  "nome": "CB",
  "cor": "ROXA",
  "tamanho": "P",
  "sku": "CAM-ROXA-P",
  "preco": 59.90,
  "quantidade": 0,
  "estoque_min": 10
}
```

## Esperado x obtido

| | |
| --- | --- |
| **Esperado** | `400 Bad Request`, nome rejeitado, nada persistido |
| **Obtido** | `201 Created` em 26 ms, 511 B — produto `id_produto 40` e variação `id_variacao 57` persistidos com `ativo = 1` |

`CB` tem 2 caracteres. A RN-001 exige entre 3 e 30. Este é o payload exato do achado original; o reteste posterior usa `CA` com a massa do `CT-EST-CAD-005`, e os dois valem porque violam o mesmo limite.

## Impacto

O nome é o identificador do produto na consulta de estoque e na verificação de duplicidade (`normalizarTexto(item.nome) === normalizarTexto(nome)`). Um nome de 1 ou 2 caracteres atravessa essa verificação como qualquer outro e permanece na base: a exclusão do sistema é lógica, não há remoção física de produto.

Pelo mesmo caminho passavam nomes formados apenas por números ou por caracteres especiais, já que nenhuma das condições de conteúdo da regra era verificada.

## Evidência

O achado foi registrado nas três camadas no momento em que ocorreu:

| Camada | O que prova | Arquivo |
| --- | --- | --- |
| API | `201 Created` · 26 ms · 511 B, e o corpo da resposta devolvendo `id_produto 40`, `nome "CB"`, `ativo 1` e `id_variacao 57` | [01](../evidencias/bug-005/01-api-post-produtos-nome-2-caracteres-201.png) |
| Front | o formulário aceitou `CB` no campo Nome do produto e não bloqueou o envio | [02](../evidencias/bug-005/02-front-formulario-nao-bloqueou.png) |
| Banco | `SELECT * FROM produto WHERE nome = 'CB'` retornou duas linhas — `id_produto 40` (15/05/2026 23:17) e `id_produto 42` (18/05/2026 11:51), ambas com `ativo = 1` | [03](../evidencias/bug-005/03-banco-produto-cb-persistido.png) |

A evidência de banco é a que fecha o caso: o defeito não parou na resposta da API, o dado inválido chegou à base e ficou.

O reteste posterior à correção está em `docs/evidencias/ct-est-cad-005/01-post-produtos-nome-2-caracteres-400.png`, e o antes e o depois do código podem ser comparados com `git show b142f22`.

> **Sobre estas três imagens.** São os cards de evidência publicados originalmente, recuperados e trazidos para o repositório em 02/09/2026. O card 01 estampa "RN01: nome do produto deve ter de 3 a 80 caracteres" — valor incorreto, citado de memória na época; a RN-001 sempre exigiu entre 3 e 30. O defeito vale nas duas leituras, porque 2 é menor que 3 em ambas. A imagem foi mantida como está por ser o registro do que de fato foi observado e publicado; a correção fica declarada aqui e no próprio post.

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

`"CA"` é uma string não vazia. Passa.

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
- Saneamento: a evidência de banco já mostra dois registros inválidos remanescentes — `id_produto 40` e `id_produto 42`, ambos com `nome = "CB"` e `ativo = 1`. Não foram saneados, e a exclusão do sistema é lógica: continuam na base. Verificar se há outros nomes fora do padrão anteriores a `b142f22`.
