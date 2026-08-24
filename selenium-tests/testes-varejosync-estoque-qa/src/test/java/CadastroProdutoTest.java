import database.ProdutoDAO;
import io.github.bonigarcia.wdm.WebDriverManager;
import massas.MassaCadastroProduto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import java.util.ArrayList;
import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import variaveis.CadastroProduto;
import variaveis.ElementosEstoque;
import variaveis.VariaveisEstoque;

import java.time.Duration;

import static org.junit.Assert.*;

public class CadastroProdutoTest {
    private WebDriver driver;
    private final List<String> skusCriadosNoTeste = new ArrayList<>();

    private static final String MSG_PRODUTO_CADASTRADO =
            "Produto cadastrado com sucesso";

    private static final String MSG_ALTERACAO_SALVA =
            "Alteração salva com sucesso";

    private static final String MSG_VARIACAO_EXCLUIDA =
            "Variação excluída com sucesso";

    @Before
    public void iniciarTeste() {
        WebDriverManager.chromedriver().setup();

        driver = new ChromeDriver();
        driver.manage().window().maximize();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get(VariaveisEstoque.URL_ESTOQUE);

        wait.until(ExpectedConditions.elementToBeClickable(
                ElementosEstoque.BOTAO_CADASTRO
        )).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                ElementosEstoque.PG_CADASTRO_PRODUTO_ATIVA
        ));

        assertTrue(
                driver.findElement(ElementosEstoque.PG_CADASTRO_PRODUTO_ATIVA)
                        .isDisplayed()
        );
    }

    @After
    public void finalizarTeste() {

        try {
            ProdutoDAO.removerDadosTestePorSkus(skusCriadosNoTeste);

            for (String sku : skusCriadosNoTeste) {
                System.out.println(
                        "[CLEANUP] Validando remoção do SKU: " + sku
                );

                if (ProdutoDAO.existeProdutoPorSku(sku)) {
                    throw new AssertionError(
                            "A massa de teste não foi removida para o SKU: " + sku
                    );
                }
            }

        } finally {

            if (driver != null) {
                driver.quit();
            }

            skusCriadosNoTeste.clear();
        }
    }

    @Test
    public void CT_EST_CAD_004_cadastrarProdutoComDadosValidos() {
        // Dado: que o usuário esteja na tela de cadastro
        String nomeProduto = MassaCadastroProduto.nomeProdutoValido();
        String cor = MassaCadastroProduto.corValida();
        String tamanho = MassaCadastroProduto.tamanhoValido();
        String sku = MassaCadastroProduto.skuValido();
        skusCriadosNoTeste.add(sku);
        String preco = MassaCadastroProduto.precoValido();
        String quantidadeInicial =
                MassaCadastroProduto.quantidadeInicialValida();
        String estoqueMinimo =
                MassaCadastroProduto.estoqueMinimoValido();

        // Quando: preencher os dados válidos do produto
        preencherFormularioProduto(
                nomeProduto,
                cor,
                tamanho,
                sku,
                preco,
                quantidadeInicial,
                estoqueMinimo
        );

        //E: Clicar no botão cadastrar produto
        clicarBotaoCadastrarProduto();

        // Então: o sistema deve exibir mensagem de sucesso
        validarMensagemFeedback(MSG_PRODUTO_CADASTRADO);

        // E: o produto deve ser persistido no banco de dados
        assertTrue(
                "O produto cadastrado não foi encontrado no banco de dados.",
                ProdutoDAO.existeProdutoPorSku(sku)
        );
    }

    @Test
    public void CT_EST_EDT_001_alterarEstoqueMinimoDaVariacao() {
        // Dado: que exista um produto cadastrado
        String sku = cadastrarProdutoParaTeste();

        // E: registrar a quantidade atual antes da edição
        int quantidadeAntes =
                ProdutoDAO.obterQuantidadePorSku(sku);


        String novoEstoqueMinimo = MassaCadastroProduto.novoEstoqueMinimoValidoEdicao();

        // Quando: buscar o produto cadastrado
        acessarTelaConsultarEstoque();
        buscarProdutoPorSku(sku);

        // E: clicar em Editar
        clicarBotaoEditarProduto();

        aguardarTelaCadastroProduto();

        // E: alterar o estoque mínimo
        alterarEstoqueMinimo(novoEstoqueMinimo);

        // E: clicar em Salvar alterações
        clicarBotaoSalvarAlteracoes();

        // Então: o sistema deve exibir mensagem de sucesso
        validarMensagemFeedback(MSG_ALTERACAO_SALVA);

        // E: o novo estoque mínimo deve ser persistido no banco
        assertTrue(
                "O novo estoque mínimo não foi persistido no banco de dados.",
                ProdutoDAO.existeProdutoComEstoqueMinimo(sku, novoEstoqueMinimo)
        );


        // E: consultar novamente a quantidade após a edição
        int quantidadeDepois =
                ProdutoDAO.obterQuantidadePorSku(sku);

        // E: a quantidade deve permanecer inalterada,
        // pois somente o estoque mínimo foi modificado pelo usuário
        assertEquals(
                "A quantidade atual foi alterada indevidamente durante a edição do estoque mínimo.",
                quantidadeAntes,
                quantidadeDepois
        );


    }

    @Test
    public void CT_EST_EXC_001_inativarSomenteVariacaoSelecionada() {

        // Dado: um produto com duas variações ativas
        String sufixo = String.valueOf(System.currentTimeMillis());

        String nomeProduto = "Blusa " + sufixo;
        String cor = "PRETA";

        String skuVariacaoP =
                "BLU" + sufixo + "-PRETA-P";

        String skuVariacaoM =
                "BLU" + sufixo + "-PRETA-M";

        skusCriadosNoTeste.add(skuVariacaoP);
        skusCriadosNoTeste.add(skuVariacaoM);

        String preco = MassaCadastroProduto.precoValido();
        String quantidadeInicial =
                MassaCadastroProduto.quantidadeInicialValida();
        String estoqueMinimo =
                MassaCadastroProduto.estoqueMinimoValido();


        // Cadastrar primeira variação - P
        preencherFormularioProduto(
                nomeProduto,
                cor,
                "P",
                skuVariacaoP,
                preco,
                quantidadeInicial,
                estoqueMinimo
        );

        clicarBotaoCadastrarProduto();

        validarMensagemFeedback(MSG_PRODUTO_CADASTRADO);

        assertTrue(
                "A variação P não foi persistida no banco.",
                ProdutoDAO.existeProdutoPorSku(skuVariacaoP)
        );


        // Cadastrar segunda variação - M
        acessarTelaCadastrarProduto();

        preencherFormularioProduto(
                nomeProduto,
                cor,
                "M",
                skuVariacaoM,
                preco,
                quantidadeInicial,
                estoqueMinimo
        );

        clicarBotaoCadastrarProduto();

        validarMensagemFeedback(MSG_PRODUTO_CADASTRADO);

        assertTrue(
                "A variação M não foi persistida no banco.",
                ProdutoDAO.existeProdutoPorSku(skuVariacaoM)
        );


        // Confirmar que ambas pertencem ao mesmo produto
        int idProdutoVariacaoP =
                ProdutoDAO.obterIdProdutoPorSku(skuVariacaoP);

        int idProdutoVariacaoM =
                ProdutoDAO.obterIdProdutoPorSku(skuVariacaoM);

        assertEquals(
                "As variações não estão vinculadas ao mesmo produto.",
                idProdutoVariacaoP,
                idProdutoVariacaoM
        );


        // Confirmar pré-condições no banco
        assertTrue(
                "O produto de origem deveria estar ativo antes da exclusão.",
                ProdutoDAO.produtoEstaAtivoPorSku(skuVariacaoM)
        );

        assertTrue(
                "A variação P deveria estar ativa antes da exclusão.",
                ProdutoDAO.variacaoEstaAtivaPorSku(skuVariacaoP)
        );

        assertTrue(
                "A variação M deveria estar ativa antes da exclusão.",
                ProdutoDAO.variacaoEstaAtivaPorSku(skuVariacaoM)
        );


        // Quando: excluir somente a variação M
        acessarTelaConsultarEstoque();

        buscarProdutoPorSku(skuVariacaoM);

        validarProdutoExibidoNaTabela(skuVariacaoM);

        clicarBotaoExcluirProduto();

        confirmarExclusaoProduto();


        // Então: deve apresentar mensagem correta
        validarMensagemFeedback(MSG_VARIACAO_EXCLUIDA);


        // E: o produto pai deve permanecer ativo
        assertTrue(
                "O produto de origem foi inativado indevidamente.",
                ProdutoDAO.produtoEstaAtivoPorSku(skuVariacaoM)
        );


        // E: somente a variação M deve ficar inativa
        assertFalse(
                "A variação selecionada permaneceu ativa.",
                ProdutoDAO.variacaoEstaAtivaPorSku(skuVariacaoM)
        );


        // E: a variação P deve continuar ativa
        assertTrue(
                "A variação não selecionada foi inativada indevidamente.",
                ProdutoDAO.variacaoEstaAtivaPorSku(skuVariacaoP)
        );


        // E: a variação M não deve mais aparecer na UI
        buscarProdutoPorSku(skuVariacaoM);

        validarProdutoNaoExibidoNaTabela(skuVariacaoM);


        // E: a variação P deve continuar disponível na UI
        buscarProdutoPorSku(skuVariacaoP);

        validarProdutoExibidoNaTabela(skuVariacaoP);
    }

    @Test
    public void CT_EST_VAR_001_manterVariacoesDoMesmoProdutoVinculadas() {

        // Dado: duas variações diferentes pertencentes ao mesmo produto
        String sufixo = String.valueOf(System.currentTimeMillis());

        String nomeProduto = "Blusa Canelada " + sufixo;
        String cor = "PRETA";

        String tamanhoPrimeiraVariacao = "P";
        String tamanhoSegundaVariacao = "M";

        String skuPrimeiraVariacao =
                "BLU" + sufixo + "-PRETA-P";

        String skuSegundaVariacao =
                "BLU" + sufixo + "-PRETA-M";

        skusCriadosNoTeste.add(skuPrimeiraVariacao);
        skusCriadosNoTeste.add(skuSegundaVariacao);

        String preco = MassaCadastroProduto.precoValido();
        String quantidadeInicial =
                MassaCadastroProduto.quantidadeInicialValida();
        String estoqueMinimo =
                MassaCadastroProduto.estoqueMinimoValido();


        // Quando: cadastrar a primeira variação
        preencherFormularioProduto(
                nomeProduto,
                cor,
                tamanhoPrimeiraVariacao,
                skuPrimeiraVariacao,
                preco,
                quantidadeInicial,
                estoqueMinimo
        );

        clicarBotaoCadastrarProduto();
        validarMensagemFeedback(MSG_PRODUTO_CADASTRADO);

        assertTrue(
                "A primeira variação não foi persistida no banco.",
                ProdutoDAO.existeProdutoPorSku(skuPrimeiraVariacao)
        );


        // Preparar o formulário para a segunda variação
        acessarTelaCadastrarProduto();


        // E: cadastrar uma segunda variação para o mesmo produto
        preencherFormularioProduto(
                nomeProduto,
                cor,
                tamanhoSegundaVariacao,
                skuSegundaVariacao,
                preco,
                quantidadeInicial,
                estoqueMinimo
        );

        clicarBotaoCadastrarProduto();
        validarMensagemFeedback(MSG_PRODUTO_CADASTRADO);

        assertTrue(
                "A segunda variação não foi persistida no banco.",
                ProdutoDAO.existeProdutoPorSku(skuSegundaVariacao)
        );


        // Então: ambas devem estar vinculadas ao mesmo id_produto
        int idProdutoPrimeiraVariacao =
                ProdutoDAO.obterIdProdutoPorSku(skuPrimeiraVariacao);

        int idProdutoSegundaVariacao =
                ProdutoDAO.obterIdProdutoPorSku(skuSegundaVariacao);

        assertEquals(
                "As variações do mesmo produto foram vinculadas a produtos diferentes.",
                idProdutoPrimeiraVariacao,
                idProdutoSegundaVariacao
        );

        int idVariacaoP =
                ProdutoDAO.obterIdVariacaoPorSku(skuPrimeiraVariacao);

        int idVariacaoM =
                ProdutoDAO.obterIdVariacaoPorSku(skuSegundaVariacao);

        assertTrue(
                "A primeira variação não possui um id_variacao válido.",
                idVariacaoP > 0
        );

        assertTrue(
                "A segunda variação não possui um id_variacao válido.",
                idVariacaoM > 0
        );

        assertNotEquals(
                "As duas variações receberam o mesmo id_variacao.",
                idVariacaoP,
                idVariacaoM
        );
    }


    private String cadastrarProdutoParaTeste() {
        String nomeProduto = MassaCadastroProduto.nomeProdutoValido();
        String cor = MassaCadastroProduto.corValida();
        String tamanho = MassaCadastroProduto.tamanhoValido();
        String sku = MassaCadastroProduto.skuValido();
        String preco = MassaCadastroProduto.precoValido();
        String quantidadeInicial =
                MassaCadastroProduto.quantidadeInicialValida();
        String estoqueMinimo =
                MassaCadastroProduto.estoqueMinimoValido();

        preencherFormularioProduto(
                nomeProduto,
                cor,
                tamanho,
                sku,
                preco,
                quantidadeInicial,
                estoqueMinimo
        );

        clicarBotaoCadastrarProduto();

        validarMensagemFeedback(MSG_PRODUTO_CADASTRADO);

        assertTrue(
                "Produto não foi cadastrado para preparação do teste.",
                ProdutoDAO.existeProdutoPorSku(sku)
        );

        return sku;
    }

    private void preencherFormularioProduto(
            String nomeProduto,
            String cor,
            String tamanho,
            String sku,
            String preco,
            String quantidadeInicial,
            String estoqueMinimo
    ) {
        driver.findElement(CadastroProduto.INPUT_NOME)
                .sendKeys(nomeProduto);

        driver.findElement(CadastroProduto.INPUT_COR)
                .sendKeys(cor);

        driver.findElement(CadastroProduto.INPUT_TAMANHO)
                .sendKeys(tamanho);

        driver.findElement(CadastroProduto.INPUT_SKU)
                .sendKeys(sku);

        driver.findElement(CadastroProduto.INPUT_PRECO)
                .sendKeys(preco);

        driver.findElement(CadastroProduto.INPUT_QTD).clear();
        driver.findElement(CadastroProduto.INPUT_QTD)
                .sendKeys(quantidadeInicial);

        driver.findElement(CadastroProduto.INPUT_ESTOQUE_MIN).clear();
        driver.findElement(CadastroProduto.INPUT_ESTOQUE_MIN)
                .sendKeys(estoqueMinimo);
    }

    private void clicarBotaoCadastrarProduto() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement botaoCadastrar = wait.until(
                ExpectedConditions.elementToBeClickable(
                        ElementosEstoque.BOTAO_CADASTRAR
                )
        );

        new Actions(driver)
                .scrollToElement(botaoCadastrar)
                .perform();

        botaoCadastrar.click();
    }

    private void acessarTelaConsultarEstoque() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.elementToBeClickable(
                ElementosEstoque.BOTAO_CONSULTAR_ESTOQUE
        )).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                ElementosEstoque.PG_CONSULTAR_ESTOQUE_ATIVA
        ));
    }

    private void buscarProdutoPorSku(String sku) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement campoBusca = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        ElementosEstoque.INPUT_BUSCA_ESTOQUE
                )
        );

        campoBusca.clear();
        campoBusca.sendKeys(sku);

        wait.until(ExpectedConditions.elementToBeClickable(
                ElementosEstoque.BOTAO_BUSCAR_ESTOQUE
        )).click();
    }

    private void validarProdutoExibidoNaTabela(String sku) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement tabela = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        ElementosEstoque.TABELA_ESTOQUE
                )
        );

        assertTrue(
                "O produto buscado não apareceu na tabela.",
                tabela.getText().contains(sku)
        );
    }

    private void validarProdutoNaoExibidoNaTabela(String sku) {
        WebDriverWait wait =
                new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement tabela = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        ElementosEstoque.TABELA_ESTOQUE
                )
        );

        assertFalse(
                "A variação inativada continua sendo exibida na tabela.",
                tabela.getText().contains(sku)
        );
    }

    private void clicarBotaoEditarProduto() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement botaoEditar = wait.until(
                ExpectedConditions.elementToBeClickable(
                        ElementosEstoque.BOTAO_EDITAR_PRODUTO
                )
        );

        new Actions(driver)
                .scrollToElement(botaoEditar)
                .perform();

        botaoEditar.click();
    }

    private void clicarBotaoExcluirProduto() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement botaoExcluir = wait.until(
                ExpectedConditions.elementToBeClickable(
                        ElementosEstoque.BOTAO_EXCLUIR_PRODUTO
                )
        );

        new Actions(driver)
                .scrollToElement(botaoExcluir)
                .perform();

        botaoExcluir.click();
    }

    private void confirmarExclusaoProduto() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        Alert alerta = wait.until(ExpectedConditions.alertIsPresent());

        alerta.accept();
    }

    private void aguardarTelaCadastroProduto() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                ElementosEstoque.PG_CADASTRO_PRODUTO_ATIVA
        ));
    }

    private void alterarEstoqueMinimo(String novoEstoqueMinimo) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement campoEstoqueMinimo = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        CadastroProduto.INPUT_ESTOQUE_MIN
                )
        );

        campoEstoqueMinimo.clear();
        campoEstoqueMinimo.sendKeys(novoEstoqueMinimo);
    }

    private void clicarBotaoSalvarAlteracoes() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement botaoSalvarAlteracoes = wait.until(
                ExpectedConditions.elementToBeClickable(
                        ElementosEstoque.BOTAO_SALVAR_ALTERACOES
                )
        );

        new Actions(driver)
                .scrollToElement(botaoSalvarAlteracoes)
                .perform();

        botaoSalvarAlteracoes.click();
    }

    private void validarMensagemFeedback(String mensagemEsperada) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(
                ExpectedConditions.textToBePresentInElementLocated(
                        ElementosEstoque.TEXTO_FEEDBACK,
                        mensagemEsperada
                )
        );

        WebElement mensagemSucesso =
                driver.findElement(ElementosEstoque.TEXTO_FEEDBACK);

        assertTrue(
                mensagemSucesso.getText().contains(mensagemEsperada)
        );
    }
    private void acessarTelaCadastrarProduto() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.elementToBeClickable(
                ElementosEstoque.BOTAO_CADASTRO
        )).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                ElementosEstoque.PG_CADASTRO_PRODUTO_ATIVA
        ));
    }
}


