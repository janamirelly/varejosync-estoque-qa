package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Menu lateral: o único lugar do projeto que sabe navegar entre as telas.
 *
 * Cada método espera a tela de destino ficar ativa, de modo que o teste possa
 * seguir para a linha seguinte sem escrever espera nenhuma.
 */
public class MenuPage extends BasePage {

    private static final By BOTAO_CADASTRO =
            By.cssSelector(".menu-button[data-page='produtos']");

    private static final By BOTAO_CONSULTAR_ESTOQUE =
            By.cssSelector(".menu-button[data-page='estoque']");

    private static final By PG_CADASTRO_ATIVA =
            By.cssSelector("#page-produtos.active");

    private static final By PG_ESTOQUE_ATIVA =
            By.cssSelector("#page-estoque.active");

    public MenuPage(WebDriver driver) {
        super(driver);
    }

    public void irParaCadastroProduto() {
        clicar(BOTAO_CADASTRO);
        wait.until(ExpectedConditions.visibilityOfElementLocated(PG_CADASTRO_ATIVA));
    }

    public void irParaConsultarEstoque() {
        clicar(BOTAO_CONSULTAR_ESTOQUE);
        wait.until(ExpectedConditions.visibilityOfElementLocated(PG_ESTOQUE_ATIVA));
    }
}
