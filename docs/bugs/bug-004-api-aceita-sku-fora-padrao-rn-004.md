# BUG-004 — API aceita SKU fora do padrão da RN-004

| | |
| --- | --- |
| **Status** | Aberto |
| **Severidade sugerida** | Alta — integridade de dados |
| **Ambiente** | Desenvolvimento local |
| **Camada** | API — `POST /produtos` |
| **Regra** | RN-004 — SKU da variação deve ser obrigatório e válido |
| **Encontrado em** | Teste exploratório de API · 31/08/2026 |

## Reproduzir

`POST {{base_url}}/produtos` com o payload abaixo. Apenas o campo `sku` está inválido; os demais atendem às respectivas regras.

```json
{
  "nome": "Camiseta Básica",
  "cor": "VINHO",
  "tamanho": "M",
  "sku": "CAMVINHOM",
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
| **Esperado** | `400 Bad Request`, SKU rejeitado, nada persistido |
| **Obtido** | `201 Created` em 32 ms, variação persistida com `ativo = 1` |

`CAMVINHOM` não possui separador. A RN-004 exige no mínimo três blocos separados por hífen e lista `CAMAZULM` — mesma estrutura — entre os exemplos inválidos.

## Impacto

O SKU é a chave de negócio da variação (`UNIQUE` no schema, usado em busca e consulta de estoque). `CAM-VINHO-M` e `CAMVINHOM` designam a mesma variação e coexistem sem violar a restrição.

**Evidência:** `docs/evidencias/bug-004/01-post-produtos-sku-sem-separador-201.png`

**Evidência:** `docs/evidencias/bug-004/02-consulta-banco.png`

---
---

# Investigação

> Leitura opcional. O necessário para reproduzir e corrigir está acima.

## Onde a validação para

A validação estrutural da RN-004 não existe em nenhuma camada.

Em `backend/src/controllers/produtos.controller.js` há duas funções validadoras — `nomeProdutoValido()` (linha 33) e `corVariacaoValida()` (linha 44). Não há equivalente para o SKU. A única verificação é a linha 126:

```js
if (!sku) {
  return res.status(400).json({
    message: "SKU da variação é obrigatório.",
  });
}
```

A verificação seguinte, na linha 160, é de unicidade — atende à RN-005, não à RN-004.

O `trim` e o `toUpperCase` exigidos pela regra **estão** implementados (linhas 88–90). É a lista de condições estruturais que não foi.

No frontend, `sku` aparece apenas em exibição e busca, sem `pattern` no input. No schema, a coluna é `TEXT NOT NULL UNIQUE`, sem `CHECK` de formato.

## Condições da RN-004

| Condição | Implementada |
| --- | :---: |
| Ser obrigatório | ✅ |
| Mínimo três blocos | ❌ |
| Hífen como separador | ❌ |
| Conteúdo em todos os blocos | ❌ |
| Apenas letras e números por bloco | ❌ |
| Não conter espaços | ❌ |
| Não usar outros separadores | ❌ |
| Não iniciar ou terminar com hífen | ❌ |
| Sem hífens consecutivos | ❌ |

Não é o padrão de “validação apenas no frontend”: nenhuma das duas camadas valida. O `CT-EST-CAD-002` passa porque cobre a obrigatoriedade — a única condição implementada.

## Pontos de atenção para quem for corrigir

- `CAM-AZUL-MARINHO-G` é **válido** pela RN-004: a cor pode ocupar mais de um bloco. Uma validação que exija exatamente três blocos rejeita um valor legítimo.
- A validação precisa rodar **depois** do `trim`/`toUpperCase` das linhas 88–90, para preservar a normalização.
- Os sete exemplos inválidos da própria RN-004 servem como massa de reteste: `CAMAZULM`, `CAM_AZUL_M`, `CAM AZUL M`, `CAM-AZUL`, `CAM--M`, `-CAM-AZUL-M`, `CAM-AZUL-M-`.
- O `CT-EST-CAD-002` deve continuar passando.

## Pendências

- Saneamento: verificar SKUs fora do padrão já existentes na base, incluindo o `CAMVINHOM` criado nesta reprodução.
- Criar caso de teste de API cobrindo as condições estruturais da RN-004, hoje declaradas como planejadas na matriz de cobertura.