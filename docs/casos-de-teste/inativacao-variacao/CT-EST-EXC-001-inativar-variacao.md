# CT-EST-EXC-001 — Inativar somente a variação selecionada

## Identificação

- **Módulo:** Estoque
- **Funcionalidade:** Inativação de variação
- **Camada:** UI
- **Tipo de teste:** Funcional positivo
- **Técnica de teste:** Transição de Estado
- **Regra relacionada:** RN-013 — Inativação lógica individual de variação
- **Critério de aceite relacionado:** CA-013 — Inativar somente a variação selecionada
- **Ambiente:** Desenvolvimento local
- **Automação:** Sim
- **Status:** Passou
- **Data da última execução:** 19/08/2026
- **Defeito relacionado:** BUG-002 — Corrigido e retestado

## Objetivo

Verificar se, ao solicitar a exclusão de uma variação específica na consulta de estoque, o sistema realiza a inativação lógica somente da variação selecionada, preservando o produto de origem e as demais variações vinculadas ao mesmo `id_produto`.

## Pré-condições

- Aplicação disponível em ambiente de desenvolvimento local.
- Backend/API em execução.
- Banco de dados disponível.
- Produto ativo contendo pelo menos duas variações ativas.
- As variações devem estar vinculadas ao mesmo `id_produto`.
- As variações devem possuir SKUs distintos.
- Ambas as variações devem estar disponíveis na consulta de estoque.

## Massa de teste — Reteste

| Entidade | Identificador | Valor |
| --- | --- | --- |
| Produto | `id_produto` | `222` |
| Produto | Nome | `Macacão Fitness Longo` |
| Variação não selecionada | `id_variacao` | `242` |
| Variação não selecionada | SKU | `MAC-PRETO-LONGO-M` |
| Variação selecionada | `id_variacao` | `243` |
| Variação selecionada | SKU | `MAC-PRETO-LONGO-P` |

### Estado inicial

| Entidade | Estado |
| --- | --- |
| Produto `id_produto = 222` | `produto.ativo = 1` |
| `MAC-PRETO-LONGO-M` | `variacao_produto.ativo = 1` |
| `MAC-PRETO-LONGO-P` | `variacao_produto.ativo = 1` |

## Passos

1. Consultar o banco de dados e confirmar que as duas variações estão vinculadas ao mesmo produto.
2. Confirmar que o produto está ativo.
3. Confirmar que as duas variações estão ativas.
4. Acessar a aplicação VarejoSync — Módulo de Estoque.
5. Navegar até **Consultar Estoque**.
6. Pesquisar pelo produto.
7. Confirmar que as duas variações são apresentadas.
8. Clicar em **Excluir** na linha correspondente à variação selecionada.
9. Verificar se a confirmação identifica corretamente a variação.
10. Confirmar a operação.
11. Verificar a mensagem apresentada pelo sistema.
12. Pesquisar novamente pelo produto.
13. Verificar quais variações permanecem disponíveis.
14. Consultar novamente o banco de dados.
15. Verificar o estado do produto.
16. Verificar o estado da variação selecionada.
17. Verificar o estado da variação não selecionada.

## Resultado esperado

Após confirmar a inativação de `MAC-PRETO-LONGO-P`:

- somente a variação `MAC-PRETO-LONGO-P` deve ser inativada;
- `MAC-PRETO-LONGO-P` deve possuir `variacao_produto.ativo = 0`;
- o produto `Macacão Fitness Longo` deve permanecer com `produto.ativo = 1`;
- `MAC-PRETO-LONGO-M` deve permanecer com `variacao_produto.ativo = 1`;
- `MAC-PRETO-LONGO-P` não deve mais aparecer na consulta de variações ativas;
- `MAC-PRETO-LONGO-M` deve continuar disponível na consulta;
- os registros devem permanecer armazenados fisicamente no banco de dados;
- o sistema deve exibir a mensagem `Variação excluída com sucesso.`

## Resultado obtido

Após a correção do BUG-002, o caso de teste foi reexecutado.

- o sistema solicitou confirmação da variação selecionada;
- o sistema exibiu a mensagem `Variação excluída com sucesso.`;
- o produto de origem permaneceu ativo;
- a variação selecionada foi inativada;
- a variação não selecionada permaneceu ativa;
- somente a variação inativada deixou de ser apresentada na consulta;
- a outra variação permaneceu disponível na interface;
- os registros permaneceram armazenados no banco de dados.

### Estado obtido após o reteste

| Entidade | Esperado | Obtido |
| --- | --- | --- |
| Produto `id_produto = 222` | `ativo = 1` | `ativo = 1` ✅ |
| `MAC-PRETO-LONGO-M` — id 242 | `ativo = 1` | `ativo = 1` ✅ |
| `MAC-PRETO-LONGO-P` — id 243 | `ativo = 0` | `ativo = 0` ✅ |
| `MAC-PRETO-LONGO-P` na UI | Não apresentada | Não apresentada ✅ |
| `MAC-PRETO-LONGO-M` na UI | Apresentada | Apresentada ✅ |
| Mensagem | `Variação excluída com sucesso.` | Conforme esperado ✅ |

## Status

**Passou**

## Evidências do reteste

- **EVD-CT-EST-EXC-001-RT-01 — Pré-condição da variação selecionada no banco:** consulta da SKU `MAC-PRETO-LONGO-P`, confirmando que o produto de origem estava ativo (`produto_ativo = 1`) e a variação estava ativa (`variacao_ativa = 1`) antes da operação.  
  [Ver evidência](../../evidencias/ct-est-exc-001/reteste/001-pre-condicao-banco-variacao-p.png)

- **EVD-CT-EST-EXC-001-RT-02 — Pré-condição da variação não selecionada no banco:** consulta da SKU `MAC-PRETO-LONGO-M`, confirmando `produto_ativo = 1` e `variacao_ativa = 1` antes da operação.  
  [Ver evidência](../../evidencias/ct-est-exc-001/reteste/002-pre-condicao-banco-variacao-m.png)

- **EVD-CT-EST-EXC-001-RT-03 — Confirmação da inativação:** consulta de estoque apresentando as variações `MAC-PRETO-LONGO-M` e `MAC-PRETO-LONGO-P`, com o sistema solicitando confirmação para excluir especificamente a variação `MAC-PRETO-LONGO-P`.  
  [Ver evidência](../../evidencias/ct-est-exc-001/reteste/003-confirmacao-inativacao-ui.png)

- **EVD-CT-EST-EXC-001-RT-04 — Mensagem de sucesso:** sistema exibindo a mensagem `Variação excluída com sucesso.` após a confirmação da operação.  
  [Ver evidência](../../evidencias/ct-est-exc-001/reteste/004-mensagem-sucesso-ui.png)

- **EVD-CT-EST-EXC-001-RT-05 — Pós-condição na UI:** após a inativação de `MAC-PRETO-LONGO-P`, a consulta apresenta somente `MAC-PRETO-LONGO-M`, confirmando que a variação não selecionada permaneceu disponível.  
  [Ver evidência](../../evidencias/ct-est-exc-001/reteste/005-pos-condicao-ui-variacao-mantida.png)

- **EVD-CT-EST-EXC-001-RT-06 — Pós-condição da variação selecionada no banco:** consulta da SKU `MAC-PRETO-LONGO-P`, confirmando que o produto permaneceu ativo (`produto_ativo = 1`) e somente a variação selecionada foi inativada (`variacao_ativa = 0`).  
  [Ver evidência](../../evidencias/ct-est-exc-001/reteste/006-pos-condicao-banco-variacao-inativada-p.png)

- **EVD-CT-EST-EXC-001-RT-07 — Pós-condição da variação não selecionada no banco:** consulta da SKU `MAC-PRETO-LONGO-M`, confirmando que o produto permaneceu ativo (`produto_ativo = 1`) e a variação não selecionada permaneceu ativa (`variacao_ativa = 1`).  
  [Ver evidência](../../evidencias/ct-est-exc-001/reteste/007-pos-condicao-banco-variacao-mantida-m.png)

## Automação relacionada

- **Classe:** `tests.ExclusaoProdutoTest`
- **Método:** `CT_EST_EXC_001_inativarSomenteVariacaoSelecionada()`
- **Status da última execução:** Passou

A automação valida:

- criação de duas variações vinculadas ao mesmo produto;
- estado ativo do produto antes da operação;
- estado ativo das duas variações antes da operação;
- inativação somente da variação selecionada;
- permanência do produto de origem como ativo;
- permanência da outra variação como ativa;
- ausência da variação inativada na interface;
- permanência da variação não selecionada na interface;
- mensagem de sucesso da operação.

## Histórico de execução

| Execução | Resultado | Observação |
| --- | --- | --- |
| Execução inicial | Falhou | Comportamento incorreto registrado no BUG-002 |
| Reteste após correção | Passou | Comportamento conforme RN-013 e CA-013 |

### Execução inicial

Na execução inicial, a aplicação inativava o produto de origem em vez da variação selecionada.

O comportamento foi registrado no **BUG-002 — Inativação de uma variação inativa o produto de origem**.

As evidências da falha permanecem vinculadas ao registro do defeito para preservação da rastreabilidade.