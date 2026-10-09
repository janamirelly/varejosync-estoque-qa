package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Métodos usados por todas as Pages: esperar, digitar, clicar e ler a
 * mensagem de feedback. A espera é de até 10 segundos.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    // Elemento da mensagem de feedback, o mesmo em todas as telas.
    private static final By TEXTO_FEEDBACK = By.id("feedbackText");

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Espera o campo ficar visível, limpa e digita. O clear() evita juntar o
     * texto novo com o valor que já estava no campo.
     */
    protected void digitar(By campo, String texto) {
        WebElement elemento = wait.until(
                ExpectedConditions.visibilityOfElementLocated(campo)
        );

        elemento.clear();
        elemento.sendKeys(texto);
    }

    /** Espera o botão ficar clicável, rola até ele e clica. */
    protected void clicar(By botao) {
        WebElement elemento = wait.until(
                ExpectedConditions.elementToBeClickable(botao)
        );

        new Actions(driver)
                .scrollToElement(elemento)
                .perform();

        elemento.click();
    }

    /**
     * Espera a mensagem esperada aparecer e devolve o texto que está na tela.
     *
     * Recebe o texto esperado porque a mensagem do passo anterior pode ainda
     * estar na tela. Se o tempo acabar, devolve o texto atual em vez de lançar
     * exceção, e o assert do teste mostra o que veio.
     */
    public String lerFeedback(String textoEsperado) {
        try {
            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                    TEXTO_FEEDBACK, textoEsperado
            ));
        } catch (TimeoutException erro) {
            // O assert do teste mostra o texto que veio.
        }

        return driver.findElement(TEXTO_FEEDBACK).getText();
    }

    // URL e título da página atual.

    public String urlAtual() {
        return driver.getCurrentUrl();
    }

    public String tituloDaPagina() {
        return driver.getTitle();
    }
}
