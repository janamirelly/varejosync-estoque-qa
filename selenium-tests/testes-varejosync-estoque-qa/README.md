# Suíte de automação — Módulo de Estoque

Testes automatizados de interface do VarejoSync, em **Java + Selenium WebDriver + JUnit**, com validação de persistência direto no banco **SQLite**.

São **9 casos automatizados**: 7 funcionais e 2 de navegação. A documentação de cada caso (regra de negócio, critério de aceite, passos e evidência) fica em [`docs/casos-de-teste`](../../docs/casos-de-teste).

← [Voltar ao README do projeto](../../README.md)

---

## Como rodar

**Pré-requisitos**

| Item | Detalhe |
| --- | --- |
| JDK | 17 ou superior |
| Maven | qualquer versão 3.x |
| Chrome | instalado (o driver é baixado sozinho pelo WebDriverManager) |
| Frontend no ar | `http://127.0.0.1:5500/frontend/index.html` — Live Server do VS Code na raiz do projeto |
| Banco | `backend/db/estoque_qa_lab.db` — criado com `npm run seed` dentro de `backend/` |

**Executar**

```bash
# a suíte inteira
mvn test

# uma classe
mvn test -Dtest=CadastroProdutoTest

# sem abrir janela do navegador
mvn test -Dheadless=true
```

Pelo IntelliJ: botão direito na pasta `tests` → *Run tests in 'tests'*.

---

## Arquitetura

O princípio é um só: **cada camada responde uma pergunta, e só uma**.

| Camada | Responde | Exemplo |
| --- | --- | --- |
| `tests/` | **o quê** estou validando | `assertTrue(ProdutoDAO.existeProdutoPorSku(...))` |
| `pages/` | **como** a tela funciona | `driver.findElement(INPUT_SKU).sendKeys(sku)` |
| `massas/` | **quais dados** eu uso | `MassaProduto.semNome()` |
| `database/` | **se persistiu** de verdade | `SELECT ... FROM variacao_produto WHERE sku = ?` |
| `core/` | **o que muda** entre local e CI | `Configuracao.rodarHeadless()` |

```text
src/test/java/
│
├── core/
│   ├── BaseTest            abre e fecha o navegador, limpa a massa criada
│   └── Configuracao        caminho do banco e modo headless
│
├── pages/                  uma classe por tela, com os locators dentro
│   ├── BasePage            wait, digitar(), clicar(), lerFeedback()
│   ├── MenuPage            navegação entre telas
│   ├── DashboardPage       tela inicial
│   ├── CadastroProdutoPage formulário de cadastro e edição
│   └── ConsultarEstoquePage busca, tabela, editar e excluir
│
├── massas/
│   ├── Produto             o objeto de dados (record)
│   ├── ParDeVariacoes      duas variações do mesmo produto
│   └── MassaProduto        fábrica de cenários: valido(), semNome(), ...
│
├── database/
│   └── ProdutoDAO          consultas de validação e limpeza da massa
│
├── variaveis/
│   └── VariaveisEstoque    URL, título e tamanho de tela
│
└── tests/                  uma classe por prefixo de caso de teste
    ├── CadastroProdutoTest          CT-EST-CAD-004
    ├── CadastroProdutoNegativoTest  CT-EST-CAD-001, 002, 003
    ├── EdicaoProdutoTest            CT-EST-EDT-001
    ├── ExclusaoProdutoTest          CT-EST-EXC-001
    ├── VariacaoProdutoTest          CT-EST-VAR-001
    └── NavegacaoEstoqueTest         CT-EST-NAV-001, 002
```

Como o nome da classe de teste acompanha o prefixo do caso, achar o código a partir do ID é direto: `CT-EST-EDT-001` está em `EdicaoProdutoTest`.

---

## Como um teste se lê

```java
@Test
public void CT_EST_CAD_004_cadastrarProdutoComDadosValidos() {

    CadastroProdutoPage cadastroPage = new CadastroProdutoPage(driver);

    // Dado: que o usuário esteja na tela de cadastro
    menuPage.irParaCadastroProduto();

    // E: tenha um produto com dados válidos
    Produto produto = MassaProduto.valido();
    skusCriadosNoTeste.add(produto.sku());

    // Quando: preencher todos os campos com dados válidos
    cadastroPage.preencherFormulario(produto);

    // E: clicar no botão cadastrar produto
    cadastroPage.clicarCadastrar();

    // Então: o sistema deve exibir a mensagem de sucesso
    String feedback = cadastroPage.lerFeedback(MSG_PRODUTO_CADASTRADO);
    assertTrue(
            "Esperava conter '" + MSG_PRODUTO_CADASTRADO + "', mas veio: '" + feedback + "'",
            feedback.contains(MSG_PRODUTO_CADASTRADO)
    );

    // E: o produto deve ser persistido no banco de dados
    assertTrue(
            "O produto cadastrado não foi encontrado no banco de dados.",
            ProdutoDAO.aguardarProdutoPorSku(produto.sku())
    );
}
```

Nenhum método privado, nenhum `findElement`, nenhum `WebDriverWait`. O que se lê é o que executa, de cima para baixo.

---

## Decisões de projeto

**A Page reporta, o teste julga.**
Nenhuma Page contém `assert`. `lerFeedback()` devolve o texto que a tela mostrou; quem compara com o esperado é o `@Test`. Assim a falha aparece como *"esperava X, mas veio Y"* em vez de `TimeoutException`.

**Cenário negativo é o cenário válido com um campo trocado.**
`semNome()` é `valido().comNome("")`. Se a massa inválida fosse montada do zero e o cadastro fosse recusado, não daria para saber qual campo causou a recusa.

**O formulário recebe um objeto, não sete parâmetros.**
`preencherFormulario(Produto)` em vez de sete `String` posicionais — que compilam na ordem errada sem o compilador reclamar.

**Duplicação entre cenários é aceitável; duplicação de mecânica não é.**
Os testes negativos se parecem entre si de propósito: cada um é uma história completa. O que não se repete é o `sendKeys` — esse mora na Page, escrito uma vez.

**Espera explícita, nunca `Thread.sleep` fixo.**
A tela responde antes do banco gravar. `ProdutoDAO.aguardarProdutoPorSku()` consulta em intervalos e retorna assim que encontra, com teto de 10 segundos.

**Tamanho de tela fixo em 1366x768, não `maximize()`.**
`maximize()` dá um tamanho por máquina. Um teste que passa num monitor grande e falha num pequeno não achou defeito — só mudou de tela.

**Toda massa criada é removida no `@After`.**
Cada teste registra os SKUs que criou em `skusCriadosNoTeste`; o `ProdutoDAO` os apaga junto com estoque, movimentações e auditoria. Sem isso os testes deixam de ser repetíveis.

---

## Configuração

O que muda entre a máquina local e um servidor fica todo em `core/Configuracao`, sempre nesta ordem: **`-D` na linha de comando → variável de ambiente → padrão local**.

| O quê | Propriedade | Variável | Padrão |
| --- | --- | --- | --- |
| Caminho do banco | `-Dbanco.caminho` | `BANCO_CAMINHO` | `C:/varejosync-estoque-qa/backend/db/estoque_qa_lab.db` |
| Navegador sem janela | `-Dheadless` | `HEADLESS` | segue `CI` (o GitHub Actions define `CI=true` sozinho) |

Rodando pelo IntelliJ sem configurar nada, tudo cai no padrão.

> **Nota para o CI:** o arquivo `.db` não é versionado (`*.db` está no `.gitignore`, e está certo — binário de banco não vai para o repositório). Um pipeline precisa **criar** o banco antes de rodar a suíte, com `npm run seed` a partir de `schema.sql` e `seed.sql`, e então apontar `-Dbanco.caminho` para o arquivo gerado.
