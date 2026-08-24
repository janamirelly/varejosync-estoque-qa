# BUG-003 — Exclusão em massa pode inativar produto com variações ainda ativas

## Identificação

* **ID:** BUG-003
* **Módulo:** Estoque
* **Funcionalidade:** Inativação em massa de variações
* **Tipo:** Defeito funcional / integridade de dados
* **Severidade:** Alta
* **Prioridade:** Alta
* **Status:** Em reteste
* **Ambiente:** Desenvolvimento local
* **Data de identificação:** 23/08/2026
* **Regra relacionada:** RN-014 — Consistência de estado entre produto e variações

## Título resumido

A inativação em massa pode alterar o estado do produto de origem para inativo mesmo quando permanecem variações ativas vinculadas ao produto.

## Descrição

Durante a investigação de inconsistências entre cadastro, consulta de estoque e banco de dados, foram identificadas variações com estado ativo vinculadas a produtos com estado inativo.

O estado encontrado foi:

```text
produto.ativo = 0
variacao_produto.ativo = 1