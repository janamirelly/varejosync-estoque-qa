package tests;

import core.BaseTest;
import database.ProdutoDAO;
import massas.MassaProduto;
import massas.Produto;
import org.junit.Test;
import pages.CadastroProdutoPage;

import static org.junit.Assert.assertTrue;

/**
 * Testes de cadastro de produto (CT-EST-CAD-xxx).
 *
 * Aqui só existe @Test: nenhum método auxiliar, nenhuma espera, nenhum
 * findElement. O que se lê é o que executa, de cima para baixo.
 */
public class CadastroProdutoTest extends BaseTest {

    private static final String MSG_PRODUTO_CADASTRADO =
            "Produto cadastrado com sucesso";

    @Test
    public void CT_EST_CAD_004_cadastrarProdutoComDadosValidos() {

        CadastroProdutoPage cadastroPage = new CadastroProdutoPage(driver);

        // Dado: que o usuário esteja na tela de cadastro
        menuPage.irParaCadastroProduto();

        // E: tenha um produto com dados válidos
        // cujo SKU ainda não esteja cadastrado
        Produto produto = MassaProduto.valido();
        skusCriadosNoTeste.add(produto.sku());

        // Quando: preencher todos os campos com dados válidos
        cadastroPage.preencherFormulario(produto);

        // E: clicar no botão cadastrar produto
        cadastroPage.clicarCadastrar();

        // Então: o sistema deve exibir a mensagem de sucesso
        String feedback = cadastroPage.lerFeedback(MSG_PRODUTO_CADASTRADO);

        assertTrue(
                "Esperava conter '" + MSG_PRODUTO_CADASTRADO + "', mas veio: '" + feedback + "'",
                feedback.contains(MSG_PRODUTO_CADASTRADO)
        );

        // E: o produto deve ser persistido no banco de dados
        assertTrue(
                "O produto cadastrado não foi encontrado no banco de dados.",
                ProdutoDAO.aguardarProdutoPorSku(produto.sku())
        );
    }
}
