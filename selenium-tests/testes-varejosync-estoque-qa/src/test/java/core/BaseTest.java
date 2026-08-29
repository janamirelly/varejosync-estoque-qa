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
 * Tudo que acontece antes e depois de qualquer teste: abrir o navegador e a
 * aplicação, limpar a massa criada e fechar o navegador.
 *
 * Toda classe de teste herda isso, de modo que o @Test contenha só o cenário.
 */
public abstract class BaseTest {

    protected WebDriver driver;
    protected MenuPage menuPage;

    /**
     * Cada teste registra aqui os SKUs que criou, e o @After os apaga do
     * banco. Sem isso a execução deixa lixo e os testes deixam de ser
     * repetíveis.
     */
    protected final List<String> skusCriadosNoTeste = new ArrayList<>();

    /**
     * Abre o navegador e a aplicação, e para por aí. Navegar até a tela do
     * cenário é responsabilidade do @Test: assim o "Dado" fica visível no
     * próprio teste, em vez de escondido no setup.
     */
    @Before
    public void abrirAplicacao() {
        WebDriverManager.chromedriver().setup();

        driver = new ChromeDriver(montarOpcoesDoChrome());

        driver.get(VariaveisEstoque.URL_ESTOQUE);

        menuPage = new MenuPage(driver);
    }

    /**
     * Monta as opções do Chrome.
     *
     * O tamanho é fixo em vez de maximize() porque maximize() entrega uma
     * área diferente em cada máquina: um teste que passa num monitor grande e
     * falha num pequeno não achou defeito, só mudou de tela. Além disso,
     * maximize() não é confiável em headless, onde não há janela.
     *
     * Se roda com ou sem janela é decisão de core.Configuracao.
     */
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

            // Os dois abaixo são específicos de servidor Linux em container:
            // sem eles o Chrome costuma morrer no meio do pipeline.
            opcoes.addArguments("--no-sandbox");
            opcoes.addArguments("--disable-dev-shm-usage");
        }

        return opcoes;
    }

    /**
     * Limpa a massa e fecha o navegador.
     *
     * A conferência da limpeza é um aviso no console, e não um AssertionError,
     * porque um erro lançado no @After marcaria como falho um teste cujo
     * cenário passou. O driver.quit() fica no finally para que o navegador
     * feche mesmo se a limpeza do banco quebrar.
     */
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
