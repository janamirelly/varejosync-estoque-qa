# Cobertura de Testes — Módulo de Estoque

Este documento consolida a cobertura de testes do módulo de Estoque do VarejoSync e mantém a rastreabilidade entre:

- Regras de Negócio (RN);
- Critérios de Aceite (CA);
- Casos de Teste (CT);
- testes automatizados;
- status das execuções.

O documento representa a situação atual da cobertura funcional e identifica explicitamente os cenários ainda não cobertos.

**Última atualização:** 19/08/2026

---

## 1. Convenção de identificação dos casos de teste

Os casos de teste seguem o padrão:

`CT-[MÓDULO]-[FUNCIONALIDADE]-[SEQUÊNCIA]`

### Identificadores

| Código | Significado |
| --- | --- |
| `CT` | Caso de Teste |
| `EST` | Módulo de Estoque |
| `CAD` | Cadastro |
| `EDT` | Edição |
| `EXC` | Exclusão / Inativação |
| `VAR` | Vínculo produto / variações |
| `NAV` | Navegação |
| `001` | Número sequencial dentro da funcionalidade |

### Exemplos

- `CT-EST-CAD-001`
- `CT-EST-EDT-001`
- `CT-EST-EXC-001`
- `CT-EST-VAR-001`
- `CT-EST-NAV-001`

---

## 2. Critérios utilizados para medir a cobertura

A existência de um teste automatizado não significa, isoladamente, cobertura completa de uma Regra de Negócio.

Neste documento:

- **Coberta:** a regra possui caso de teste executado que valida o comportamento principal definido pela RN/CA.
- **Cobertura parcial:** existe teste associado, porém somente parte das condições da regra está contemplada.
- **Não coberta:** não existe caso de teste formalmente associado à regra.
- **Automação Sim:** existe teste automatizado correspondente.
- **Passou:** a última execução registrada apresentou o comportamento esperado.

A porcentagem de cobertura por regra representa quantas RNs possuem pelo menos um caso de teste associado e executado.

Ela não representa a porcentagem de todas as combinações, entradas ou cenários possíveis do sistema.

---

## 3. Resumo da cobertura atual

| Indicador | Situação atual |
| --- | --- |
| Regras de Negócio formalizadas | 14 |
| Critérios de Aceite formalizados | 14 |
| RNs com pelo menos um CT executado | 7 de 14 |
| CAs com pelo menos um CT executado | 7 de 14 |
| Casos de teste funcionais catalogados | 8 |
| Casos de teste funcionais automatizados | 8 |
| Casos funcionais com status Passou | 8 |
| Casos funcionais documentados individualmente | 8 |
| Testes automatizados de navegação / smoke | 2 |
| Total de testes automatizados identificados | 10 |

### Cobertura por Regra de Negócio

`7 / 14 = 50%`

A cobertura atual por RN é, portanto, de **50%**, considerando como coberta uma regra que possui pelo menos um caso de teste formalmente associado e executado.

---

## 4. Catálogo de casos de teste funcionais

| ID | RN | CA | Funcionalidade | Cenário | Tipo | Automação | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| CT-EST-CAD-001 | RN-001 | CA-001 | Cadastro | Bloquear nome vazio | Negativo | Sim | Passou |
| CT-EST-CAD-002 | RN-004 | CA-004 | Cadastro | Bloquear SKU vazio | Negativo | Sim | Passou |
| CT-EST-CAD-003 | RN-001 | CA-001 | Cadastro | Bloquear nome com 2 caracteres | Negativo | Sim | Passou |
| CT-EST-CAD-004 | RN-010 | CA-010 | Cadastro | Cadastrar produto com dados válidos | Positivo | Sim | Passou |
| CT-EST-EDT-001 | RN-011 | CA-011 | Edição | Alterar estoque mínimo da variação | Positivo | Sim | Passou |
| CT-EST-VAR-001 | RN-012 | CA-012 | Vínculo produto/variações | Manter variações do mesmo produto vinculadas | Positivo | Sim | Passou |
| CT-EST-EXC-001 | RN-013 | CA-013 | Inativação de variação | Inativar somente a variação selecionada | Positivo | Sim | Passou |
| CT-EST-EXC-002 | RN-014 | CA-014 | Inativação de variação | Inativar a última variação ativa inativa o produto | Positivo | Sim | Passou |

---

## 5. Matriz de rastreabilidade RN → CA → CT

| RN | CA | Caso de teste | Situação |
| --- | --- | --- | --- |
| RN-001 | CA-001 | CT-EST-CAD-001 / CT-EST-CAD-003 | Cobertura parcial |
| RN-002 | CA-002 | — | Não coberta |
| RN-003 | CA-003 | — | Não coberta |
| RN-004 | CA-004 | CT-EST-CAD-002 | Cobertura parcial |
| RN-005 | CA-005 | — | Não coberta |
| RN-006 | CA-006 | — | Não coberta |
| RN-007 | CA-007 | — | Não coberta |
| RN-008 | CA-008 | — | Não coberta |
| RN-009 | CA-009 | — | Não coberta |
| RN-010 | CA-010 | CT-EST-CAD-004 | Coberta |
| RN-011 | CA-011 | CT-EST-EDT-001 | Coberta |
| RN-012 | CA-012 | CT-EST-VAR-001 | Coberta |
| RN-013 | CA-013 | CT-EST-EXC-001 | Coberta |
| RN-014 | CA-014 | CT-EST-EXC-002 | Cobertura parcial |

> **RN-014 — cobertura parcial.** O `CT-EST-EXC-002` cobre, pela interface, a inativação da última variação ativa e a invariante de integridade em toda a base. O cenário de inativação **em massa** permanece descoberto: não existe tela para ele, o fluxo é exposto somente pelo endpoint `PATCH /produtos/exclusao-massa`, e foi justamente esse caminho que originou o BUG-003. É o gap de maior risco da suíte e depende de teste de API.

---

## 6. Cobertura da RN-001 — Nome do produto deve ser obrigatório e válido

**Critério de Aceite relacionado:** `CA-001`

A RN-001 possui cobertura automatizada parcial.

### Condições da regra

| Condição | Técnica | Caso de teste | Situação |
| --- | --- | --- | --- |
| Nome obrigatório | PE | CT-EST-CAD-001 — Nome vazio | Executado, documentado e automatizado |
| Nome com menos de 3 caracteres | AVL | CT-EST-CAD-003 — Nome com 2 caracteres | Executado, documentado e automatizado |
| Nome com exatamente 3 caracteres | AVL | Não criado | Planejado |
| Nome com exatamente 30 caracteres | AVL | Não criado | Planejado |
| Nome com mais de 30 caracteres | AVL | Não criado | Planejado |
| Nome deve conter pelo menos uma letra | PE | Não criado — somente números | Planejado |
| Espaços no início e no fim devem ser desconsiderados | PE | Não criado | Planejado |

### Técnicas utilizadas

**PE — Particionamento de Equivalência**

Divide os valores de entrada em classes que devem apresentar comportamento equivalente.

**AVL — Análise de Valor Limite**

Valida valores localizados nos limites estabelecidos pela regra e imediatamente antes ou depois desses limites.

### Situação da RN-001

Atualmente estão automatizadas:

- obrigatoriedade do nome;
- nome abaixo do limite mínimo de 3 caracteres.

Ainda permanecem planejados:

- limite mínimo válido de 3 caracteres;
- limite máximo válido de 30 caracteres;
- valor acima do limite máximo;
- nome formado somente por números;
- tratamento de espaços no início e no fim.

**Situação:** Cobertura parcial.

---

## 7. Cobertura da RN-004 — SKU da variação deve ser obrigatório e válido

**Critério de Aceite relacionado:** `CA-004`

A RN-004 possui cobertura automatizada parcial.

### Condições da regra

| Condição | Caso de teste | Situação |
| --- | --- | --- |
| SKU obrigatório | CT-EST-CAD-002 — SKU vazio | Executado, documentado e automatizado |
| Mínimo de três blocos | Não criado | Planejado |
| Uso de hífen como separador | Não criado | Planejado |
| Todos os blocos devem possuir conteúdo | Não criado | Planejado |
| Apenas letras e números dentro dos blocos | Não criado | Planejado |
| SKU não deve conter espaços | Não criado | Planejado |
| Não aceitar outros separadores | Não criado | Planejado |
| Não iniciar com hífen | Não criado | Planejado |
| Não terminar com hífen | Não criado | Planejado |
| Não possuir hífens consecutivos | Não criado | Planejado |
| Normalização para letras maiúsculas | Não criado | Planejado |

### Situação da RN-004

Atualmente a automação comprova a obrigatoriedade do SKU.

As demais condições estruturais definidas pela RN-004 ainda não possuem casos de teste formalmente catalogados.

**Situação:** Cobertura parcial.

---

## 8. Cobertura das RN-010 a RN-013

### RN-010 — Cadastro de produto com variação válida

**CA relacionado:** `CA-010`  
**Caso de teste:** `CT-EST-CAD-004`

O caso valida o cadastro utilizando dados válidos e confirma a persistência do registro.

**Automação:** Sim  
**Status:** Passou  
**Situação:** Coberta - cenário principal

---

### RN-011 — Alteração do estoque mínimo na edição da variação

**CA relacionado:** `CA-011`  
**Caso de teste:** `CT-EST-EDT-001`

O caso valida a alteração do estoque mínimo de uma variação existente e confirma a persistência do novo valor no banco de dados.

**Automação:** Sim  
**Status:** Passou  
**Situação:** Coberta - cenário principal

---

### RN-012 — Vínculo produto/variações

**CA relacionado:** `CA-012`  
**Caso de teste:** `CT-EST-VAR-001`

O caso valida que duas variações cadastradas para o mesmo produto:

- compartilham o mesmo `id_produto`;
- possuem SKUs diferentes;
- possuem identificadores `id_variacao` próprios;
- não provocam a criação de um novo produto para cada variação.

A validação inclui consulta ao banco de dados.

**Automação:** Sim  
**Status:** Passou  
**Situação:** Coberta - cenário principal

---

### RN-013 — Inativação lógica individual de variação

**CA relacionado:** `CA-013`  
**Caso de teste:** `CT-EST-EXC-001`

O caso valida que a inativação de uma variação:

- altera somente a variação selecionada para `ativo = 0`;
- mantém o produto de origem ativo;
- mantém as demais variações ativas;
- remove somente a variação inativada da consulta de estoque.

O caso originou o `BUG-002`, posteriormente corrigido e retestado.

Após a correção, o cenário passou tanto no reteste manual quanto na automação.

**Automação:** Sim  
**Status:** Passou  
**Defeito relacionado:** BUG-002 — Corrigido  
**Situação:** Coberta - cenário principal

---

## 9. Regras ainda sem cobertura formal

As seguintes regras possuem especificação e critério de aceite, porém ainda não possuem caso de teste formalmente associado no catálogo atual:

| RN | CA | Regra | Situação |
| --- | --- | --- | --- |
| RN-002 | CA-002 | Cor da variação deve ser obrigatória e válida | Não coberta |
| RN-003 | CA-003 | Tamanho da variação deve ser obrigatório e válido | Não coberta |
| RN-005 | CA-005 | SKU da variação deve ser único | Não coberta |
| RN-006 | CA-006 | Preço da variação deve ser obrigatório e válido | Não coberta |
| RN-007 | CA-007 | Quantidade inicial da variação deve ser opcional | Não coberta |
| RN-008 | CA-008 | Duplicidade de cadastro da variação por produto, cor e tamanho | Não coberta |
| RN-009 | CA-009 | Estoque mínimo da variação deve ser obrigatório | Não coberta |

A ausência de cobertura nesta matriz não significa necessariamente ausência de validações no código da aplicação.

Significa que ainda não existe um caso de teste formalmente catalogado e rastreado para essas regras.

---

## 10. Testes de navegação / Smoke

Existem também testes automatizados voltados à navegação básica do módulo.

Esses testes não estão vinculados diretamente a uma Regra de Negócio ou Critério de Aceite funcional e, por isso, são mantidos separadamente da matriz RN → CA → CT.

| ID padronizado | Classe | Cenário | Categoria | RN / CA |
| --- | --- | --- | --- | --- |
| CT-EST-NAV-001 | NavegacaoEstoqueTest | Validar tela inicial do módulo de estoque | Smoke / Navegação | — |
| CT-EST-NAV-002 | NavegacaoEstoqueTest | Acessar cadastro pelo menu lateral | Smoke / Navegação | — |

Os métodos Java correspondentes utilizam os mesmos identificadores definidos neste documento:

- `CT_EST_NAV_001_validarTelaInicialDoEstoque()`
- `CT_EST_NAV_002_acessarCadastroPeloMenu()`

Os dois cenários estão automatizados e foram executados com sucesso.

---

## 11. Situação da documentação dos casos de teste

| Caso de teste | Documento individual | Situação |
| --- | --- | --- |
| CT-EST-CAD-001 | `casos-de-teste/cadastro-produto/CT-EST-CAD-001-bloquear-nome-vazio.md` | Documentado |
| CT-EST-CAD-002 | `casos-de-teste/cadastro-produto/CT-EST-CAD-002-bloquear-sku-vazio.md` | Documentado |
| CT-EST-CAD-003 | `casos-de-teste/cadastro-produto/CT-EST-CAD-003-nome-abaixo-limite-minimo.md` | Documentado |
| CT-EST-CAD-004 | `casos-de-teste/cadastro-produto/CT-EST-CAD-004-cadastrar-produto-valido.md` | Documentado |
| CT-EST-EDT-001 | `casos-de-teste/edicao-produto/CT-EST-EDT-001-alterar-estoque-minimo.md` | Documentado |
| CT-EST-EXC-001 | `casos-de-teste/inativacao-variacao/CT-EST-EXC-001-inativar-variacao.md` | Documentado |
| CT-EST-VAR-001 | `casos-de-teste/variacao-produto/CT-EST-VAR-001-vincular-variacoes-mesmo-produto.md` | Documentado |

---

## 12. Situação da rastreabilidade

Os casos de teste atualmente catalogados possuem identificação padronizada entre documentação e automação.

Os testes funcionais seguem a convenção:

`CT-EST-[FUNCIONALIDADE]-[SEQUÊNCIA]`

Os testes de navegação também foram padronizados utilizando o identificador `NAV`.

Não permanecem, nesta etapa, métodos automatizados catalogados utilizando a convenção legada de identificação.

A rastreabilidade atual está estruturada entre:

`RN → CA → CT → Automação → Status`

As regras ainda sem cobertura permanecem explicitamente identificadas na seção **Regras ainda sem cobertura formal**.

## 13. Estratégia de evolução da cobertura

A expansão da cobertura deve ser realizada progressivamente, priorizando as regras com maior impacto sobre integridade de dados e comportamento funcional.

A ordem recomendada para evolução da cobertura é:

1. ampliar a cobertura de valores limite da RN-001;
2. ampliar as validações estruturais da RN-004;
3. criar cobertura para RN-005 — unicidade de SKU;
4. criar cobertura para RN-008 — duplicidade de produto, cor e tamanho;
5. criar cobertura para RN-002 — validação da cor;
6. criar cobertura para RN-003 — validação do tamanho;
7. criar cobertura para RN-006 — validação do preço;
8. criar cobertura para RN-007 — quantidade inicial;
9. criar cobertura para RN-009 — estoque mínimo;
10. manter a matriz de rastreabilidade atualizada a cada novo CT ou alteração de regra.

---

## 14. Situação geral

A cobertura atual demonstra validações funcionais automatizadas sobre:

- obrigatoriedade e limite mínimo do nome do produto;
- obrigatoriedade do SKU;
- cadastro válido;
- edição do estoque mínimo;
- vínculo entre produto e múltiplas variações;
- inativação individual de variação.

Também existem testes automatizados de navegação básica do módulo.

As regras ainda não cobertas permanecem explicitamente registradas como gaps, permitindo evolução incremental da suíte sem apresentar uma cobertura superior à efetivamente implementada.

A matriz deverá ser atualizada sempre que uma RN, CA, CT ou automação for criada, alterada, executada ou descontinuada.
