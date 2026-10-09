package tests;

import core.BaseTest;
import database.ProdutoDAO;
import massas.MassaProduto;
import massas.Produto;
import org.junit.Test;
import pages.CadastroProdutoPage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Testes negativos de cadastro de produto (CT-EST-CAD-xxx).
 *
 * Cada teste confere a mensagem de erro na tela e se o produto ficou fora do
 * banco. A mensagem sozinha não mostra se o back-end recusou o cadastro.
 */
public class CadastroProdutoNegativoTest extends BaseTest {

    private static final String MSG_NOME_INVALIDO =
            "Informe um nome de produto válido";

    private static final String MSG_SKU_INVALIDO =
            "Informe um sku válido para a variação";

    @Test
    public void CT_EST_CAD_001_bloquearCadastroComNomeVazio() {

        CadastroProdutoPage cadastroPage = new CadastroProdutoPage(driver);

        // Dado: que o usuário esteja na tela de cadastro
        menuPage.irParaCadastroProduto();

        // E: tenha um produto válido, exceto pelo nome vazio
        Produto produto = MassaProduto.semNome();
        skusCriadosNoTeste.add(produto.sku());

        // Quando: preencher o formulário
        cadastroPage.preencherFormulario(produto);

        // E: clicar no botão cadastrar produto
        cadastroPage.clicarCadastrar();

        // Então: o sistema deve exibir a mensagem de erro
        String feedback = cadastroPage.lerFeedback(MSG_NOME_INVALIDO);

        assertTrue(
                "Esperava conter '" + MSG_NOME_INVALIDO + "', mas veio: '" + feedback + "'",
                feedback.contains(MSG_NOME_INVALIDO)
        );

        // E: o produto não deve ser persistido no banco de dados
        assertFalse(
                "O produto com nome vazio foi persistido no banco.",
                ProdutoDAO.existeProdutoPorSku(produto.sku())
        );
    }

    @Test
    public void CT_EST_CAD_002_bloquearCadastroComSkuVazio() {

        CadastroProdutoPage cadastroPage = new CadastroProdutoPage(driver);

        // Dado: que o usuário esteja na tela de cadastro
        menuPage.irParaCadastroProduto();

        // E: tenha um produto válido, exceto pelo SKU vazio
        Produto produto = MassaProduto.comSkuVazio();

        // Quando: preencher o formulário
        cadastroPage.preencherFormulario(produto);

        // E: clicar no botão cadastrar produto
        cadastroPage.clicarCadastrar();

        // Então: o sistema deve exibir a mensagem de erro
        String feedback = cadastroPage.lerFeedback(MSG_SKU_INVALIDO);

        assertTrue(
                "Esperava conter '" + MSG_SKU_INVALIDO + "', mas veio: '" + feedback + "'",
                feedback.contains(MSG_SKU_INVALIDO)
        );

        // E: nenhuma variação deve ter sido criada com SKU vazio
        assertFalse(
                "Foi persistida no banco uma variação com SKU vazio.",
                ProdutoDAO.existeProdutoPorSku("")
        );
    }

    @Test
    public void CT_EST_CAD_003_bloquearCadastroComNomeAbaixoLimiteMinimo() {

        CadastroProdutoPage cadastroPage = new CadastroProdutoPage(driver);

        // Dado: que o usuário esteja na tela de cadastro
        menuPage.irParaCadastroProduto();

        // E: tenha um produto válido, exceto pelo nome abaixo do mínimo
        Produto produto = MassaProduto.comNomeAbaixoMinimo();
        skusCriadosNoTeste.add(produto.sku());

        // Quando: preencher o formulário
        cadastroPage.preencherFormulario(produto);

        // E: clicar no botão cadastrar produto
        cadastroPage.clicarCadastrar();

        // Então: o sistema deve exibir a mensagem de erro
        String feedback = cadastroPage.lerFeedback(MSG_NOME_INVALIDO);

        assertTrue(
                "Esperava conter '" + MSG_NOME_INVALIDO + "', mas veio: '" + feedback + "'",
                feedback.contains(MSG_NOME_INVALIDO)
        );

        // E: o produto não deve ser persistido no banco de dados
        assertFalse(
                "O produto com nome abaixo do limite mínimo foi persistido no banco.",
                ProdutoDAO.existeProdutoPorSku(produto.sku())
        );
    }
}
