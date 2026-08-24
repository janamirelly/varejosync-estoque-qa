import database.ProdutoDAO;
import io.github.bonigarcia.wdm.WebDriverManager;
import massas.MassaCadastroProduto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CadastroProdutoNegativoTest {
    private WebDriver driver;

    private static final String MS_PRODUTO_INVALIDO =
            "Informe um nome de produto válido";

    private static  final String MS_SKU_INVALIDO = "Informe um sku válido para a variação";

    @Before
    public void inicializa() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();

        WebDriverWait wait = new WebDriverWait
                (driver, Duration.ofSeconds(5)
                );

        driver.get(VariaveisEstoque.URL_ESTOQUE);
        wait.until(ExpectedConditions.elementToBeClickable
                (ElementosEstoque.BOTAO_CADASTRO)).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated
                (ElementosEstoque.PG_CADASTRO_PRODUTO_ATIVA));

        assertTrue(driver.findElement
                (ElementosEstoque
                        .PG_CADASTRO_PRODUTO_ATIVA).isDisplayed());
    }

    @After
    public void finalizarTeste() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    @Test
    public void CT_EST_CAD_001_bloquearCadastroComNomeVazio(){
        //Dado: que o usuário esteja na tela de cadastro
        String nomeProduto = MassaCadastroProduto.nomeVazio();
        String cor = MassaCadastroProduto.corValida();
        String sku = MassaCadastroProduto.skuValido();
        String tamanho = MassaCadastroProduto.tamanhoValido();
        String preco = MassaCadastroProduto.precoValido();
        String qtd = MassaCadastroProduto.quantidadeInicialValida();
        String estoqueMinimo = MassaCadastroProduto
                .estoqueMinimoValido();

        //Quando: preencher formulário com nome vazio
        preencherFormularioProduto(
                nomeProduto,
                cor,
                sku,
                tamanho,
                preco,
                qtd,
                estoqueMinimo
        );

        //E: clicar no botão cadastrar
        clicarBotaoCadastrarProduto();


        //Então: o sistema deve exibir a mensagem de erro
        validarMensFeedback(MS_PRODUTO_INVALIDO);

    }

    @Test
    public void CT_EST_CAD_002_bloquearCadastroComSkuVazio(){
        //Dado: que o usuário esteja na tela de cadastro
        String nomeProduto = MassaCadastroProduto.nomeProdutoValido();
        String cor = MassaCadastroProduto.corValida();
        String sku = MassaCadastroProduto.skuVazio();
        String tamanho = MassaCadastroProduto.tamanhoValido();
        String preco = MassaCadastroProduto.precoValido();
        String qtd = MassaCadastroProduto.quantidadeInicialValida();
        String estoqueMinimo = MassaCadastroProduto
                .estoqueMinimoValido();

        //Quando: preencher formulário com o SKU vazio
        preencherFormularioProduto(
                nomeProduto,
                cor,
                sku,
                tamanho,
                preco,
                qtd,
                estoqueMinimo
        );


        clicarBotaoCadastrarProduto();

        //Então: O sistema deve exibir a mensagem de erro
        validarMensFeedback(MS_SKU_INVALIDO);

    }

    @Test
    public void CT_EST_CAD_003_bloquearCadastroComNomeAbaixoLimiteMinimo(){
        //Dado: que o usuário esteja na tela de cadastro
        String nomeProduto = MassaCadastroProduto.nomeAbaixoMinimo();
        String cor = MassaCadastroProduto.corValida();
        String sku = MassaCadastroProduto.skuValido();
        String tamanho = MassaCadastroProduto.tamanhoValido();
        String preco = MassaCadastroProduto.precoValido();
        String qtd = MassaCadastroProduto.quantidadeInicialValida();
        String estoqueMinimo = MassaCadastroProduto.estoqueMinimoValido();

        //Quando: preencher campo nome do produto abaixo do limite mínimo
        preencherFormularioProduto(
                nomeProduto,
                cor,
                sku,
                tamanho,
                preco,
                qtd,
                estoqueMinimo
        );
        clicarBotaoCadastrarProduto();

        //Então o sistema deve exibir a mensagem de erro
        validarMensFeedback(MS_PRODUTO_INVALIDO);

        assertFalse("O produto com nome abaixo do limite mínimo foi persistido no banco.",
                 ProdutoDAO.existeProdutoPorSku(sku));

    }

    private void preencherFormularioProduto(
            String nomeProduto,
            String cor,
            String sku,
            String tamanho,
            String preco,
            String qtd,
            String estoqueMinimo
    ){
        driver.findElement(CadastroProduto.
                INPUT_NOME).sendKeys(nomeProduto);

        driver.findElement(CadastroProduto.
                INPUT_COR).sendKeys(cor);

        driver.findElement(CadastroProduto.
                INPUT_TAMANHO).sendKeys(tamanho);

        driver.findElement(CadastroProduto.INPUT_SKU)
                .sendKeys(sku);

        driver.findElement(CadastroProduto.INPUT_QTD)
                .sendKeys(qtd);

        driver.findElement(CadastroProduto.INPUT_PRECO)
                .sendKeys(preco);

        driver.findElement(CadastroProduto
                .INPUT_ESTOQUE_MIN).clear();

        driver.findElement(CadastroProduto
                .INPUT_ESTOQUE_MIN).sendKeys(estoqueMinimo);
    }

    private void clicarBotaoCadastrarProduto(){
        WebDriverWait wait =
                new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement botaoCadastrarProduto =
                wait.until(ExpectedConditions.elementToBeClickable
                        (ElementosEstoque.BOTAO_CADASTRAR));

        new Actions(driver)
                .scrollToElement(botaoCadastrarProduto)
                .perform();
        botaoCadastrarProduto.click();

    }

private void validarMensFeedback(String mensagemEsperada){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.
                textToBePresentInElementLocated
                        (ElementosEstoque.TEXTO_FEEDBACK,  mensagemEsperada));
        WebElement mensagemErro =
                driver.findElement(ElementosEstoque.TEXTO_FEEDBACK);
        assertTrue(mensagemErro.getText().contains(mensagemEsperada));
    }


}
