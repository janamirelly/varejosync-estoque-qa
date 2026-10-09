package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Tela de consulta de estoque: busca, tabela e botões de editar e excluir.
 */
public class ConsultarEstoquePage extends BasePage {

    private static final By INPUT_BUSCA   = By.id("estoqueBusca");
    private static final By BOTAO_BUSCAR  = By.id("btnBuscarEstoque");
    private static final By TABELA        = By.id("estoqueTabela");

    private static final By BOTAO_EDITAR  = By.cssSelector(".btn-editar-produto");
    private static final By BOTAO_EXCLUIR = By.cssSelector(".btn-excluir-produto");

    public ConsultarEstoquePage(WebDriver driver) {
        super(driver);
    }

    public void buscarPorSku(String sku) {
        digitar(INPUT_BUSCA, sku);
        clicar(BOTAO_BUSCAR);
    }

    /** Devolve o texto da tabela de estoque. */
    public String lerTabela() {
        return wait.until(
                ExpectedConditions.visibilityOfElementLocated(TABELA)
        ).getText();
    }

    public void clicarEditar() {
        clicar(BOTAO_EDITAR);
    }

    public void clicarExcluir() {
        clicar(BOTAO_EXCLUIR);
    }

    /** Aceita o alert de confirmação do navegador. */
    public void confirmarExclusao() {
        wait.until(ExpectedConditions.alertIsPresent()).accept();
    }
}
