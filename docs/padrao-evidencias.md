# Padrão de evidências

Como capturar, nomear e organizar as evidências dos casos de teste deste projeto.

---

## 1. O que uma evidência precisa provar

Uma evidência não é um registro de que o teste foi executado. É a prova de que o **resultado esperado aconteceu** — e uma prova que outra pessoa consegue conferir sem você.

Para isso ela precisa responder três perguntas:

| | Pergunta | Momento |
| :---: | --- | --- |
| 1 | **O que era antes?** | pré-condição |
| 2 | **O que eu fiz?** | ação, com a massa visível |
| 3 | **O que ficou depois?** | pós-condição |

> Print de mensagem de sucesso, sozinho, não prova nada: prova que a tela escreveu um texto. É o passo 2 sem o 1 e sem o 3.

### O teste diz onde está a prova

A pergunta que define quais capturas são obrigatórias é: **qual assert dá valor a este teste?**

| Se o assert que importa está... | A evidência obrigatória é... |
| --- | --- |
| na tela (mensagem, elemento visível) | captura da tela |
| no banco (persistiu, estado, vínculo) | **consulta ao banco, antes e depois** |
| na ausência de efeito colateral | uma consulta que mostre, **junto**, o que mudou e o que não podia mudar |

O terceiro caso é o mais forte, e vale copiar: a pós-condição do `CT-EST-EDT-001` traz `estoque_min = 12` (o que mudou) e `quantidade = 4` (o que não podia mudar) **na mesma consulta**. Uma imagem, duas provas.

---

## 2. O mínimo por tipo de caso

### Caso positivo — algo deve ser criado ou alterado

| # | Captura | Por quê |
| :---: | --- | --- |
| 01 | banco, antes: consulta pelo SKU retornando **0 linhas** | prova que não existia |
| 02 | tela: formulário preenchido com a massa | mostra o que foi enviado |
| 03 | tela: mensagem de sucesso | o sistema confirmou |
| 04 | banco, depois: a linha criada, com os campos | **prova que persistiu** |

### Caso negativo — algo deve ser bloqueado

| # | Captura | Por quê |
| :---: | --- | --- |
| 01 | banco, antes: 0 linhas | ponto de partida |
| 02 | tela: formulário com o campo inválido visível | mostra o que foi tentado |
| 03 | tela: mensagem de erro | o sistema recusou na interface |
| 04 | banco, depois: **ainda 0 linhas** | **prova que o back-end também recusou** |

A 04 é a que dá valor ao caso. Mensagem de erro na tela não demonstra que a API recusou — o JavaScript pode barrar enquanto o back-end aceita.

### Caso de edição — algo muda e algo não pode mudar

Igual ao positivo, com uma exigência a mais: a pré e a pós-condição no banco devem trazer, **na mesma consulta**, o campo alterado e o campo que precisa permanecer intacto.

### Reteste de defeito

Guardar as duas execuções, em pastas separadas:

```text
ct-est-exc-001/
├── falha/      as capturas de quando o defeito existia
└── reteste/    as capturas depois da correção
```

O `CT-EST-EXC-001` já segue isso. É o melhor conjunto do projeto: mostra a evolução, não só o resultado final.

---

## 3. Como capturar

**Deixe a consulta SQL visível no print.** Não recorte só o resultado. A consulta é o que permite a outra pessoa refazer a verificação — sem ela, o print é uma tabela sem procedência.

**Deixe o relógio do sistema visível** no canto da captura. Resolve a datação sem esforço: sem carimbo de tempo, a evidência não se liga a nenhuma execução específica.

**Uma captura, uma afirmação.** Se precisa explicar duas coisas, faça duas capturas.

**Destaque o que importa** quando o print tiver muita informação. A evidência do `CT-EST-VAR-001` usa setas vermelhas apontando o `id_produto` idêntico nas duas linhas — o leitor entende em dois segundos.

---

## 4. Nomes e pastas

```text
docs/evidencias/
├── ct-est-cad-004/          minúsculo, o mesmo id do caso de teste
│   ├── 01-pre-condicao-banco.png
│   ├── 02-cadastro-dados-validos.png
│   ├── 03-mensagem-sucesso-exibida.png
│   ├── 04-consulta-estoque-ui.png
│   └── 05-pos-condicao-banco.png
│
├── ct-est-exc-001/
│   ├── falha/
│   └── reteste/
│
└── bug-003/                 defeitos usam o id do bug
```

**Regras de nome:**

- pasta em minúsculo, igual ao id do caso: `ct-est-cad-004`
- arquivo começa com número de dois dígitos, na ordem de execução: `01-`, `02-`
- resto do nome em minúsculo, separado por hífen, descrevendo o que a imagem mostra
- **sem acento e sem espaço** — acento em nome de arquivo exige codificação de URL e quebra links no GitHub
- nunca deixar arquivo solto na raiz de `evidencias/`

---

## 5. Como citar no caso de teste

```markdown
## Evidências

* **EVD-CT-EST-CAD-004-01 — Pré-condição no banco:** consulta realizada antes da
  execução, confirmando que o SKU `CAM-VO-G` não estava cadastrado.
  [Ver evidência](./evidencias/ct-est-cad-004/01-pre-condicao-banco.png)
```

Cada item traz: **identificador**, **o que a imagem prova** e o **link**. A descrição precisa dizer o que a imagem demonstra, não o que ela mostra — *"confirmando que o SKU não estava cadastrado"* vale mais que *"print da consulta"*.

---

## 6. Conferência antes de fechar um caso

- [ ] Existe pré-condição no banco?
- [ ] Existe pós-condição no banco?
- [ ] A massa citada no documento é **a mesma** que aparece nas imagens?
- [ ] A consulta SQL está visível nos prints de banco?
- [ ] Todos os links abrem?
- [ ] Nenhum nome de arquivo tem acento ou espaço?
- [ ] Se o caso afirma "X não mudou", existe captura provando o valor de X antes?

> O terceiro item é o que mais falha, e é o mais caro: quando uma evidência não bate com o documento, o leitor deixa de confiar em **todas** as outras.

---

## 7. Situação atual

| Caso | Antes | Ação | Depois | Situação |
| --- | :---: | :---: | :---: | --- |
| `CT-EST-CAD-001` | ❌ | ❌ | ❌ | pendente de captura |
| `CT-EST-CAD-002` | ❌ | ❌ | ❌ | pendente de captura |
| `CT-EST-CAD-003` | ✅ | ✅ | ✅ | completo |
| `CT-EST-CAD-004` | ✅ | ✅ | ✅ | completo |
| `CT-EST-EDT-001` | ⚠️ só tela | ✅ | ✅ | falta a pré-condição no banco |
| `CT-EST-VAR-001` | ❌ | ❌ | ✅ | prova o vínculo, não prova que o teste o criou |
| `CT-EST-EXC-001` | ✅ | ✅ | ✅ | completo, com falha e reteste |
| `CT-EST-EXC-002` | — | — | — | automatizado, sem captura manual (declarado no caso) |
