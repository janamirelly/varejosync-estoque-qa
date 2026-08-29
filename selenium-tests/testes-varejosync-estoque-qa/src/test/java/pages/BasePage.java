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
 * O que toda página do sistema sabe fazer: esperar, digitar, clicar e ler o
 * feedback. Concentrar isso aqui mantém a espera com um único tempo-limite e
 * um único comportamento em toda a suíte.
 *
 * É abstract porque não representa nenhuma tela real — só existe para ser
 * herdada.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    /** O feedback é o mesmo elemento em todas as telas, por isso mora aqui. */
    private static final By TEXTO_FEEDBACK = By.id("feedbackText");

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Espera o campo ficar visível, limpa e digita.
     *
     * O clear() é sempre feito: sem ele, um campo com valor padrão concatena
     * em vez de substituir, e o teste passa a depender de qual tela veio antes.
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
     * Espera o feedback exibir o texto esperado e devolve o que está na tela.
     *
     * Recebe o texto esperado porque o feedback é um elemento único,
     * reaproveitado por toda ação do sistema: num teste de dois passos, ler
     * sem saber o que esperar pode devolver a mensagem do passo anterior, que
     * ainda não sumiu. Esperar pelo texto certo elimina essa corrida.
     *
     * O catch vazio é proposital: em vez de estourar TimeoutException, o
     * método devolve o que estiver na tela, para que o assert do teste possa
     * reportar "esperava X, mas veio Y". A decisão continua sendo do @Test —
     * esta Page apenas reporta.
     */
    public String lerFeedback(String textoEsperado) {
        try {
            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                    TEXTO_FEEDBACK, textoEsperado
            ));
        } catch (TimeoutException erro) {
            // Silencioso de propósito: o assert do teste é quem reporta.
        }

        return driver.findElement(TEXTO_FEEDBACK).getText();
    }

    // URL e título não pertencem a uma tela específica: toda página tem os
    // dois. Continuam apenas reportando — quem compara é o @Test.

    public String urlAtual() {
        return driver.getCurrentUrl();
    }

    public String tituloDaPagina() {
        return driver.getTitle();
    }
}
