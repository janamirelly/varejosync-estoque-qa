package tests;

import core.BaseTest;
import database.ProdutoDAO;
import massas.MassaProduto;
import massas.Produto;
import org.junit.Test;
import pages.CadastroProdutoPage;
import pages.ConsultarEstoquePage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Testes de edição de produto (CT-EST-EDT-xxx).
 *
 * Ficam num arquivo próprio porque editar não é cadastrar. O nome da classe
 * acompanha o prefixo do caso de teste, então achar o código pelo ID é direto.
 */
public class EdicaoProdutoTest extends BaseTest {

    private static final String MSG_ALTERACAO_SALVA =
            "Alteração salva com sucesso";

    @Test
    public void CT_EST_EDT_001_alterarEstoqueMinimoDaVariacao() {

        CadastroProdutoPage cadastroPage = new CadastroProdutoPage(driver);
        ConsultarEstoquePage estoquePage = new ConsultarEstoquePage(driver);

        // ----------------------------------------------------------------
        // Dado: que exista um produto cadastrado.
        //
        // A pré-condição fica visível no teste, e não escondida num auxiliar:
        // se ela falhar, vê-se em qual linha parou. Usa cadastrar() porque
        // cadastrar não é o que está sendo testado aqui.
        // ----------------------------------------------------------------
        Produto produto = MassaProduto.valido();
        skusCriadosNoTeste.add(produto.sku());

        menuPage.irParaCadastroProduto();
        cadastroPage.cadastrar(produto);

        assertTrue(
                "Pré-condição falhou: o produto não foi cadastrado.",
                ProdutoDAO.aguardarProdutoPorSku(produto.sku())
        );

        // E: registrar a quantidade em estoque ANTES da edição
        int quantidadeAntes = ProdutoDAO.obterQuantidadePorSku(produto.sku());

        // Quando: buscar o produto na consulta de estoque
        menuPage.irParaConsultarEstoque();
        estoquePage.buscarPorSku(produto.sku());

        // E: clicar em Editar
        estoquePage.clicarEditar();

        assertTrue(
                "Pré-condição falhou: a tela de cadastro não abriu em modo edição.",
                cadastroPage.estaAtiva()
        );

        // E: alterar o estoque mínimo
        String novoEstoqueMinimo = MassaProduto.novoEstoqueMinimo();
        cadastroPage.alterarEstoqueMinimo(novoEstoqueMinimo);

        // E: clicar em Salvar alterações
        cadastroPage.clicarSalvarAlteracoes();

        // Então: o sistema deve exibir a mensagem de sucesso
        String feedback = cadastroPage.lerFeedback(MSG_ALTERACAO_SALVA);

        assertTrue(
                "Esperava conter '" + MSG_ALTERACAO_SALVA + "', mas veio: '" + feedback + "'",
                feedback.contains(MSG_ALTERACAO_SALVA)
        );

        // E: o novo estoque mínimo deve estar persistido no banco
        assertTrue(
                "O novo estoque mínimo não foi persistido no banco de dados.",
                ProdutoDAO.existeProdutoComEstoqueMinimo(produto.sku(), novoEstoqueMinimo)
        );

        // ----------------------------------------------------------------
        // E: a quantidade em estoque deve continuar igual.
        //
        // É o assert que dá valor ao teste: verificar que o estoque mínimo
        // mudou prova apenas que a tela salvou algo; verificar que a
        // quantidade não mudou prova que a edição não teve efeito colateral.
        // ----------------------------------------------------------------
        int quantidadeDepois = ProdutoDAO.obterQuantidadePorSku(produto.sku());

        assertEquals(
                "A quantidade em estoque foi alterada indevidamente durante a edição do estoque mínimo.",
                quantidadeAntes,
                quantidadeDepois
        );
    }
}
