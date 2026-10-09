# Suíte de automação | Módulo de Estoque

Testes automatizados de interface do VarejoSync em **Java, Selenium WebDriver e JUnit**, com validação no banco **SQLite**.

São **10 casos automatizados**: 8 funcionais e 2 de navegação. A documentação de cada caso (regra de negócio, critério de aceite, passos e evidência) fica em [`docs/casos-de-teste`](../../docs/casos-de-teste).

← [Voltar ao README do projeto](../../README.md)

---

## Como rodar

**Pré-requisitos**

| Item | Detalhe |
| --- | --- |
| JDK | 17 ou superior |
| Maven | qualquer versão 3.x |
| Chrome | instalado (o WebDriverManager baixa o driver) |
| Frontend no ar | `http://127.0.0.1:5500/frontend/index.html`, com o Live Server do VS Code na raiz do projeto |
| Banco | `backend/db/estoque_qa_lab.db`, criado com `npm run seed` dentro de `backend/` |

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

## Organização

| Pasta | O que tem | Exemplo |
| --- | --- | --- |
| `tests/` | os testes | `assertTrue(..., ProdutoDAO.aguardarProdutoPorSku(produto.sku()))` |
| `pages/` | locators e ações de cada tela | `digitar(INPUT_SKU, produto.sku())` |
| `massas/` | dados usados nos testes | `MassaProduto.semNome()` |
| `database/` | consultas para conferir se gravou no banco | `SELECT ... FROM variacao_produto WHERE sku = ?` |
| `core/` | classe base dos testes e configurações | `Configuracao.rodarHeadless()` |

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
│   └── MassaProduto        massas de cada cenário: valido(), semNome(), ...
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
    ├── ExclusaoProdutoTest          CT-EST-EXC-001, 002
    ├── VariacaoProdutoTest          CT-EST-VAR-001
    └── NavegacaoEstoqueTest         CT-EST-NAV-001, 002
```

O nome da classe segue o prefixo do caso de teste: o `CT-EST-EDT-001` está em `EdicaoProdutoTest`.

---

## Exemplo de teste

```java
@Test
public void CT_EST_CAD_004_cadastrarProdutoComDadosValidos() {

    CadastroProdutoPage cadastroPage = new CadastroProdutoPage(driver);

    // Dado: que o usuário esteja na tela de cadastro
    menuPage.irParaCadastroProduto();

    // E: tenha um produto com dados válidos
    // cujo SKU ainda não esteja cadastrado
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

O teste não usa `findElement` nem `WebDriverWait` direto: isso fica nas Pages.

---

## Decisões de projeto

- As Pages não têm assert. `lerFeedback()` devolve o texto que a tela mostrou, e o teste compara com o esperado. A falha mostra o esperado e o que veio, em vez de um `TimeoutException`.
- A massa negativa parte da massa válida e troca um campo: `semNome()` é `valido().comNome("")`. Se o cadastro for recusado, dá para saber qual campo causou a recusa.
- O formulário recebe um objeto `Produto` em vez de sete `String`. Com sete `String`, trocar a ordem de dois argumentos compilaria sem erro.
- A espera é explícita: `WebDriverWait` na tela e `ProdutoDAO.aguardarProdutoPorSku()` no banco, que consulta a cada 300 ms, por no máximo 10 segundos.
- A janela tem tamanho fixo, 1366x768, em vez de `maximize()`, para o teste rodar igual em qualquer monitor.
- A massa criada é apagada no `@After`: cada teste guarda os SKUs em `skusCriadosNoTeste`, e o `ProdutoDAO` apaga esses registros junto com estoque, movimentações e auditoria.

---

## Configuração

O que muda entre a máquina local e um servidor fica em `core/Configuracao`. Ordem de leitura: `-D` na linha de comando, depois variável de ambiente, depois o padrão local.

| O quê | Propriedade | Variável | Padrão |
| --- | --- | --- | --- |
| Caminho do banco | `-Dbanco.caminho` | `BANCO_CAMINHO` | `C:/varejosync-estoque-qa/backend/db/estoque_qa_lab.db` |
| Navegador sem janela | `-Dheadless` | `HEADLESS` | segue a variável `CI` (o GitHub Actions define `CI=true`) |

Pelo IntelliJ, sem configurar nada, vale o padrão.

> **Para rodar em CI:** o arquivo `.db` não está no repositório (`*.db` está no `.gitignore`). O pipeline precisa criar o banco com `npm run seed`, a partir de `schema.sql` e `seed.sql`, e passar o caminho em `-Dbanco.caminho`.
