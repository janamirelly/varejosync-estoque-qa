package tests;

import core.BaseTest;
import org.junit.Test;
import pages.CadastroProdutoPage;
import pages.DashboardPage;
import variaveis.VariaveisEstoque;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Testes de navegação do módulo de estoque (CT-EST-NAV-xxx).
 *
 * Se o menu ou a tela inicial quebrarem, os outros testes também falham.
 * Estes dois mostram se o problema está na navegação. Não consultam o banco.
 */
public class NavegacaoEstoqueTest extends BaseTest {

    @Test
    public void CT_EST_NAV_001_validarTelaInicialDoEstoque() {

        DashboardPage dashboardPage = new DashboardPage(driver);

        // Dado: que o usuário acesse a página inicial do módulo estoque
        // (o @Before já abriu a aplicação)

        // Então: a URL exibida deve ser a esperada
        assertEquals(
                VariaveisEstoque.URL_ESTOQUE,
                dashboardPage.urlAtual()
        );

        // E: o título da página deve ser o esperado
        assertEquals(
                VariaveisEstoque.TITULO_ESTOQUE,
                dashboardPage.tituloDaPagina()
        );

        // E: o Dashboard deve estar visível
        assertTrue(
                "O Dashboard não está visível na tela inicial.",
                dashboardPage.estaVisivel()
        );
    }

    @Test
    public void CT_EST_NAV_002_acessarCadastroPeloMenu() {

        DashboardPage dashboardPage = new DashboardPage(driver);
        CadastroProdutoPage cadastroPage = new CadastroProdutoPage(driver);

        // Dado: que o usuário esteja na página inicial
        assertTrue(
                "Pré-condição falhou: o Dashboard não está visível.",
                dashboardPage.estaVisivel()
        );

        // Quando: clicar em Cadastrar Produto no menu
        menuPage.irParaCadastroProduto();

        // Então: a página de cadastro deve ficar ativa
        assertTrue(
                "A tela de cadastro de produto não ficou ativa após clicar no menu.",
                cadastroPage.estaAtiva()
        );
    }
}
