package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Dashboard: a tela inicial do módulo de estoque. É a menor Page do projeto —
 * o tamanho de uma Page acompanha o da tela que ela representa.
 */
public class DashboardPage extends BasePage {

    private static final By PG_DASHBOARD_ATIVA =
            By.cssSelector("#page-dashboard.active");

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    /** Espera o dashboard aparecer e devolve se conseguiu. Reporta, não julga. */
    public boolean estaVisivel() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(PG_DASHBOARD_ATIVA));
            return true;
        } catch (TimeoutException erro) {
            return false;
        }
    }
}
