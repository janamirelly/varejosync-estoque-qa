# CA-014 — Consistência de estado entre produto e variações

**Regra relacionada:** RN-014 — Consistência de estado entre produto e variações

## Objetivo

Definir as condições de aceite para a inativação em massa de variações, garantindo que o estado do produto de origem seja sempre consequência do estado das suas variações, e que nunca exista uma variação ativa vinculada a um produto inativo.

---

## Pré-condições

- deve existir um produto ativo;
- o produto deve possuir três ou mais variações ativas;
- as variações devem estar vinculadas ao mesmo `id_produto`;
- cada variação deve possuir seu próprio `id_variacao` e SKU;
- as variações devem estar disponíveis na consulta de estoque ativo;
- o fluxo de inativação em massa deve estar disponível.

---

## Cenário 1 — Inativação em massa parcial mantém o produto ativo

- **Dado** que exista um produto ativo com três ou mais variações ativas vinculadas ao mesmo `id_produto`
- **Quando** o usuário solicitar a inativação em massa de parte das variações
- **E** confirmar a operação
- **Então** somente as variações explicitamente selecionadas devem ser inativadas
- **E** as variações não selecionadas devem permanecer com seus estados inalterados
- **E** o produto de origem deve permanecer ativo
- **E** as variações que permaneceram ativas devem continuar disponíveis na consulta de estoque
- **E** as variações inativadas não devem mais ser apresentadas nas consultas de estoque ativo
- **E** os registros devem permanecer fisicamente armazenados no banco de dados

## Cenário 2 — Inativação da última variação ativa inativa o produto

- **Dado** que exista um produto ativo com exatamente uma variação ativa
- **Quando** o usuário solicitar a inativação dessa variação
- **E** confirmar a operação
- **Então** a variação deve ser inativada
- **E** o produto de origem deve ser inativado, por não restar nenhuma variação ativa
- **E** o produto e a variação não devem mais ser apresentados nas consultas de estoque ativo
- **E** os registros devem permanecer fisicamente armazenados no banco de dados

## Cenário 3 — O estado inválido nunca deve ocorrer

- **Dado** qualquer operação de inativação, individual ou em massa
- **Quando** a operação for concluída
- **Então** não deve existir na base nenhuma variação com `variacao_produto.ativo = 1` vinculada a um produto com `produto.ativo = 0`

---

## Estado esperado no banco de dados

O estado do produto é **consequência** do estado das suas variações, verificado após a inativação:

| Situação após a operação | `produto.ativo` | `variacao_produto.ativo` |
| --- | --- | --- |
| Permanece pelo menos uma variação ativa | `1` | `0` nas selecionadas, inalterado nas demais |
| Nenhuma variação ativa restante | `0` | `0` em todas |

Estado que **nunca** pode ocorrer:

```text
produto.ativo = 0
+
variacao_produto.ativo = 1
```

Consulta de verificação da integridade em toda a base:

```sql
SELECT COUNT(*)
FROM variacao_produto vp
INNER JOIN produto p
        ON p.id_produto = vp.id_produto
WHERE vp.ativo = 1
  AND p.ativo = 0;
```

O resultado esperado é `0`.

A operação deve ser lógica. Nenhum registro pode ser removido fisicamente do banco de dados, e o histórico de movimentações deve ser preservado.

---

## Estado esperado na interface

| Elemento | Resultado esperado |
| --- | --- |
| Variações selecionadas para inativação | Não devem aparecer na consulta de estoque |
| Variações não selecionadas | Devem continuar disponíveis na consulta |
| Produto com variações ativas remanescentes | Deve continuar disponível |
| Produto sem nenhuma variação ativa | Não deve aparecer na consulta de estoque ativo |

---

## Critérios para aprovação

O cenário será considerado **Passou** somente se:

- somente as variações explicitamente selecionadas forem inativadas;
- as variações não selecionadas permanecerem com seus estados inalterados;
- o produto permanecer ativo enquanto existir pelo menos uma variação ativa vinculada a ele;
- o produto for inativado somente quando não restar nenhuma variação ativa;
- a consulta de integridade retornar `0` após a operação;
- as variações remanescentes continuarem disponíveis nas consultas operacionais;
- os registros permanecerem fisicamente armazenados no banco de dados;
- o histórico de movimentações for preservado.

Se qualquer uma dessas condições não for atendida, o cenário deve ser considerado **Falhou**.

---

## Rastreabilidade

| Artefato | Referência |
| --- | --- |
| Regra de negócio | [RN-014 — Consistência de estado entre produto e variações](../regras-negocio/consistência-de-estado-entre-produto-e-variações.md) |
| Defeito de origem | [BUG-003 — Exclusão em massa pode inativar produto com variações ainda ativas](../bugs/bug-003-exclusao-massa-inativa-produto-com-variacoes-ativas.md) |
| Critério complementar | CA-013 — Inativar somente a variação selecionada |
| Caso de teste | pendente — `CT-EST-EXC-002` |

O reteste do BUG-003 está registrado nas evidências `EVD-BUG-003-05` e `EVD-BUG-003-06`, porém ainda **não existe caso de teste formalizado** cobrindo esta regra. Enquanto o `CT-EST-EXC-002` não for criado e automatizado, não há proteção contra a reincidência do defeito.

---

## Fora do escopo deste critério

Este critério não define:

- as validações de cadastro de produto e variação;
- a reativação de produtos ou variações;
- a exclusão física de registros;
- o saneamento de registros já inconsistentes existentes na base;
- o comportamento de movimentações de estoque após a inativação.

Esses comportamentos dependem de regras de negócio específicas.
