# CT-EST-EXC-002 — Inativar a última variação ativa inativa o produto de origem

## Identificação

- **Módulo:** Estoque
- **Funcionalidade:** Inativação de variação
- **Camada:** UI + Banco de dados
- **Tipo de teste:** Funcional positivo / Integridade de dados
- **Técnica de teste:** Transição de Estado
- **Regra relacionada:** RN-014 — Consistência de estado entre produto e variações
- **Critério de aceite relacionado:** CA-014 — Consistência de estado entre produto e variações
- **Ambiente:** Desenvolvimento local
- **Automação:** Sim
- **Status:** Passou
- **Data da última execução:** 28/08/2026
- **Defeito relacionado:** BUG-003 — Corrigido e retestado

## Objetivo

Verificar se, ao inativar a **última variação ativa** de um produto, o sistema inativa também o produto de origem, e se em nenhum momento a base passa a conter uma variação ativa vinculada a um produto inativo.

Este caso é o complemento do `CT-EST-EXC-001`. Os dois validam a mesma regra por lados opostos:

| Caso | Situação | Comportamento esperado do produto |
| --- | --- | --- |
| `CT-EST-EXC-001` | produto com **duas** variações, uma inativada | permanece **ativo** |
| `CT-EST-EXC-002` | produto com **uma** variação, ela inativada | passa a **inativo** |

O estado do produto é **consequência** do estado das suas variações — nunca uma decisão isolada da operação.

## Pré-condições

- Aplicação disponível em ambiente de desenvolvimento local.
- Backend/API em execução.
- Banco de dados disponível.
- Produto ativo contendo **exatamente uma** variação ativa.
- A variação deve estar disponível na consulta de estoque.

## Massa de teste

Gerada dinamicamente pela automação, em `MassaProduto.valido()`:

| Campo | Origem |
| --- | --- |
| Nome | `Camiseta` + número aleatório |
| SKU | `CAM` + timestamp + `-VO-P` |
| Cor / Tamanho | `Verde Oliva` / `P` |

O SKU único a cada execução garante que o caso possa ser reexecutado sem colisão com massa anterior.

### Estado inicial

| Entidade | Estado |
| --- | --- |
| Produto | `produto.ativo = 1` |
| Variação única | `variacao_produto.ativo = 1` |
| Base | nenhuma variação ativa vinculada a produto inativo |

## Passos

1. Acessar a aplicação VarejoSync — Módulo de Estoque.
2. Navegar até **Cadastrar Produto**.
3. Cadastrar um produto com uma única variação, usando dados válidos.
4. Consultar o banco e confirmar que a variação foi persistida.
5. Confirmar no banco que o produto está ativo.
6. Confirmar no banco que a variação está ativa.
7. Navegar até **Consultar Estoque**.
8. Pesquisar pelo SKU cadastrado.
9. Confirmar que a variação é apresentada na tabela.
10. Clicar em **Excluir** na linha correspondente.
11. Confirmar a operação.
12. Verificar a mensagem apresentada pelo sistema.
13. Consultar o banco e verificar o estado da variação.
14. Consultar o banco e verificar o estado do produto de origem.
15. Executar a consulta de integridade em toda a base.
16. Pesquisar novamente pelo SKU na consulta de estoque.
17. Verificar que a variação não é mais apresentada.

## Resultado esperado

Após confirmar a inativação da única variação:

- o sistema deve exibir a mensagem `Variação excluída com sucesso.`;
- a variação deve possuir `variacao_produto.ativo = 0`;
- o produto de origem deve possuir `produto.ativo = 0`, por não restar nenhuma variação ativa;
- a consulta de integridade da base deve retornar `0`;
- a variação não deve mais aparecer na consulta de estoque;
- os registros devem permanecer armazenados fisicamente no banco de dados.

### Consulta de integridade

```sql
SELECT COUNT(*)
FROM variacao_produto vp
INNER JOIN produto p
        ON p.id_produto = vp.id_produto
WHERE vp.ativo = 1
  AND p.ativo = 0;
```

Resultado esperado: `0`.

Esta é a mesma consulta que identificou o BUG-003, quando retornou 96 variações ativas distribuídas em 86 produtos inativos.

## Resultado obtido

O caso foi executado por automação em 28/08/2026 e todas as verificações foram atendidas.

- o sistema exibiu a mensagem `Variação excluída com sucesso.`;
- a variação foi inativada (`variacao_produto.ativo = 0`);
- o produto de origem foi inativado (`produto.ativo = 0`), por não restar nenhuma variação ativa;
- a consulta de integridade retornou `0` em toda a base;
- a variação deixou de ser apresentada na consulta de estoque;
- os registros permaneceram armazenados fisicamente no banco de dados.

### Estado obtido

| Verificação | Esperado | Obtido |
| --- | --- | --- |
| Variação após a exclusão | `ativo = 0` | `ativo = 0` ✅ |
| Produto de origem após a exclusão | `ativo = 0` | `ativo = 0` ✅ |
| Variações ativas com produto inativo (base) | `0` | `0` ✅ |
| Variação na consulta de estoque | Não apresentada | Não apresentada ✅ |
| Mensagem | `Variação excluída com sucesso.` | Conforme esperado ✅ |

## Status

**Passou**

## Automação relacionada

- **Classe:** `tests.ExclusaoProdutoTest`
- **Método:** `CT_EST_EXC_002_inativarUltimaVariacaoAtivaInativaProdutoDeOrigem()`
- **Status da última execução:** Passou

A automação valida:

- cadastro de um produto com uma única variação;
- estado ativo do produto antes da operação;
- estado ativo da variação antes da operação;
- presença da variação na consulta de estoque antes da exclusão;
- mensagem de sucesso da operação;
- inativação da variação;
- inativação do produto de origem, como consequência;
- ausência de variações ativas vinculadas a produtos inativos em toda a base;
- ausência da variação inativada na interface.

## Cobertura do CA-014

| Cenário do CA-014 | Coberto por este CT | Observação |
| --- | :---: | --- |
| 1 — Inativação em massa parcial mantém o produto ativo | ❌ | Não existe tela para inativação em massa; o fluxo é exposto apenas pelo endpoint `PATCH /produtos/exclusao-massa`. Requer teste de API. |
| 2 — Inativação da última variação ativa inativa o produto | ✅ | Coberto integralmente pela UI |
| 3 — O estado inválido nunca deve ocorrer | ✅ | Coberto pela consulta de integridade |

O `CT-EST-EXC-001` cobre, pela UI, o comportamento equivalente ao cenário 1 no fluxo **individual**: inativar uma variação de um produto que possui outras ativas não altera o produto.

O cenário 1 do CA-014, no fluxo **em massa**, permanece descoberto por automação. Como o caminho existe no backend e foi a origem do BUG-003, este é o gap de maior risco da regra e está registrado na matriz de cobertura.

## Fora do escopo

Este caso de teste não valida:

- a inativação em massa por meio do endpoint;
- a reativação de produtos ou variações;
- a exclusão física de registros;
- o saneamento de registros já inconsistentes na base;
- o comportamento de movimentações após a inativação.

## Histórico de execução

| Execução | Resultado | Observação |
| --- | --- | --- |
| 28/08/2026 — primeira execução | Passou | Caso criado a partir do CA-014, após a formalização da RN-014 |

A evidência deste caso é a própria execução automatizada. Diferente dos casos retestados manualmente após defeito, não há captura de tela associada: a verificação de integridade da base é uma consulta SQL executada dentro do teste, e o resultado é registrado pelo JUnit.
