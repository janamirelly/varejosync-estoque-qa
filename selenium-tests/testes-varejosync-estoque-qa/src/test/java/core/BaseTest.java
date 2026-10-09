package core;

import database.ProdutoDAO;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import pages.MenuPage;
import variaveis.VariaveisEstoque;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe base dos testes: abre o navegador antes de cada teste, apaga a massa
 * criada e fecha o navegador no final.
 */
public abstract class BaseTest {

    protected WebDriver driver;
    protected MenuPage menuPage;

    // SKUs criados pelo teste. O @After apaga esses registros do banco.
    protected final List<String> skusCriadosNoTeste = new ArrayList<>();

    // Abre o navegador na tela inicial. Cada teste navega até a tela que usa.
    @Before
    public void abrirAplicacao() {
        WebDriverManager.chromedriver().setup();

        driver = new ChromeDriver(montarOpcoesDoChrome());

        driver.get(VariaveisEstoque.URL_ESTOQUE);

        menuPage = new MenuPage(driver);
    }

    // Janela com tamanho fixo (VariaveisEstoque) para o teste rodar igual em
    // qualquer monitor. O modo headless vem de core.Configuracao.
    private static ChromeOptions montarOpcoesDoChrome() {
        ChromeOptions opcoes = new ChromeOptions();

        opcoes.addArguments(
                "--window-size="
                        + VariaveisEstoque.LARGURA_TELA
                        + ","
                        + VariaveisEstoque.ALTURA_TELA
        );

        if (Configuracao.rodarHeadless()) {
            opcoes.addArguments("--headless=new");

            // Opções para rodar o Chrome em servidor Linux (container).
            opcoes.addArguments("--no-sandbox");
            opcoes.addArguments("--disable-dev-shm-usage");
        }

        return opcoes;
    }

    // Apaga a massa e fecha o navegador. Se algum SKU continuar no banco,
    // mostra um aviso no console em vez de falhar um teste que passou.
    // O quit() fica no finally para fechar o navegador mesmo se der erro.
    @After
    public void limparMassaEFecharNavegador() {
        try {
            ProdutoDAO.removerDadosTestePorSkus(skusCriadosNoTeste);

            for (String sku : skusCriadosNoTeste) {
                if (ProdutoDAO.existeProdutoPorSku(sku)) {
                    System.out.println(
                            "[CLEANUP] ATENÇÃO: a massa não foi removida para o SKU: " + sku
                    );
                }
            }

        } finally {
            skusCriadosNoTeste.clear();

            if (driver != null) {
                driver.quit();
            }
        }
    }
}
