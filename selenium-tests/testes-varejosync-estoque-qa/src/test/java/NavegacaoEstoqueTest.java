import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import variaveis.ElementosEstoque;
import variaveis.VariaveisEstoque;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NavegacaoEstoqueTest {
    private WebDriver driver;

    @Before
    public void iniciarTeste() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @After
    public void finalizarTeste() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void CT_EST_NAV_001_validarTelaInicialDoEstoque() {

        // Dado: que o usuário acesse a página inicial do módulo estoque
        driver.get(VariaveisEstoque.URL_ESTOQUE);

        // Então: a URL exibida deve ser a esperada
        assertEquals(
                VariaveisEstoque.URL_ESTOQUE,
                driver.getCurrentUrl()
        );

        // E: o título da página deve ser o esperado
        assertEquals(
                VariaveisEstoque.TITULO_ESTOQUE,
                driver.getTitle()
        );

        // E: o Dashboard deve estar visível
        assertTrue(
                "O Dashboard não está visível na tela inicial.",
                driver.findElement(
                        ElementosEstoque.PG_DASHBOARD_ATIVA
                ).isDisplayed()
        );

    }

    @Test
    public void CT_EST_NAV_002_acessarCadastroPeloMenu() {
        // Dado: que o usuário esteja na página inicial
        driver.get(VariaveisEstoque.URL_ESTOQUE);

        assertTrue(
                driver.findElement(
                        ElementosEstoque.PG_DASHBOARD_ATIVA
                ).isDisplayed()
        );

        // Quando: clicar em Cadastrar Produto
        driver.findElement(
                ElementosEstoque.BOTAO_CADASTRO
        ).click();

        // Então: a página de cadastro deve ficar ativa
        assertTrue(
                driver.findElement(
                        ElementosEstoque.PG_CADASTRO_PRODUTO_ATIVA
                ).isDisplayed()
        );
    }

}

