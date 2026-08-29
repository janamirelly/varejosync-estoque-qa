package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Tela de Consultar Estoque: busca, leitura da tabela e os botões de editar
 * e excluir. Como nas demais Pages, nenhum assert aqui.
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

    /**
     * Devolve o texto inteiro da tabela. A mesma leitura serve para "apareceu"
     * e para "não apareceu" — quem decide é o @Test.
     */
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

    /**
     * Aceita o alert nativo do navegador. Alert nativo não é elemento da
     * página e não pode ser localizado por By — daí o tratamento diferente.
     */
    public void confirmarExclusao() {
        wait.until(ExpectedConditions.alertIsPresent()).accept();
    }
}
