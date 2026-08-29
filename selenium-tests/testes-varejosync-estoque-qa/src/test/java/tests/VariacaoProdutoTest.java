package tests;

import core.BaseTest;
import database.ProdutoDAO;
import massas.MassaProduto;
import massas.ParDeVariacoes;
import massas.Produto;
import org.junit.Test;
import pages.CadastroProdutoPage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Testes de variação de produto (CT-EST-VAR-xxx).
 *
 * Quase todas as verificações são no banco, e isso é proposital: o vínculo
 * entre variações não aparece na tela. A interface mostra duas linhas
 * parecidas — se pertencem ao mesmo produto ou a dois produtos duplicados,
 * só o id_produto responde. Testar pela tela daria falso positivo.
 */
public class VariacaoProdutoTest extends BaseTest {

    private static final String MSG_PRODUTO_CADASTRADO =
            "Produto cadastrado com sucesso";

    @Test
    public void CT_EST_VAR_001_manterVariacoesDoMesmoProdutoVinculadas() {

        CadastroProdutoPage cadastroPage = new CadastroProdutoPage(driver);

        // Dado: duas variações do mesmo produto (mesmo nome e cor)
        ParDeVariacoes variacoes = MassaProduto.duasVariacoesDoMesmoProduto();

        Produto variacaoP = variacoes.tamanhoP();
        Produto variacaoM = variacoes.tamanhoM();

        skusCriadosNoTeste.add(variacaoP.sku());
        skusCriadosNoTeste.add(variacaoM.sku());

        // Quando: cadastrar a primeira variação
        menuPage.irParaCadastroProduto();
        cadastroPage.preencherFormulario(variacaoP);
        cadastroPage.clicarCadastrar();

        String feedback = cadastroPage.lerFeedback(MSG_PRODUTO_CADASTRADO);

        assertTrue(
                "Esperava conter '" + MSG_PRODUTO_CADASTRADO + "', mas veio: '" + feedback + "'",
                feedback.contains(MSG_PRODUTO_CADASTRADO)
        );

        assertTrue(
                "A primeira variação não foi persistida no banco.",
                ProdutoDAO.aguardarProdutoPorSku(variacaoP.sku())
        );

        // ----------------------------------------------------------------
        // E: cadastrar a segunda variação. Voltar ao menu limpa o formulário.
        //
        // Aqui não há assert de feedback, de propósito: a mensagem de sucesso
        // é a mesma do cadastro anterior e ainda pode estar na tela, então um
        // assert nela passaria sem provar nada. Quem prova é o banco — e por
        // isso a espera pela gravação precisa ser explícita
        // (aguardarProdutoPorSku), e não efeito colateral de outra coisa.
        // ----------------------------------------------------------------
        menuPage.irParaCadastroProduto();
        cadastroPage.preencherFormulario(variacaoM);
        cadastroPage.clicarCadastrar();

        assertTrue(
                "A segunda variação não foi persistida no banco.",
                ProdutoDAO.aguardarProdutoPorSku(variacaoM.sku())
        );

        // Então: as duas devem estar vinculadas ao MESMO id_produto
        int idProdutoDaVariacaoP = ProdutoDAO.obterIdProdutoPorSku(variacaoP.sku());
        int idProdutoDaVariacaoM = ProdutoDAO.obterIdProdutoPorSku(variacaoM.sku());

        assertEquals(
                "As variações do mesmo produto foram vinculadas a produtos diferentes.",
                idProdutoDaVariacaoP,
                idProdutoDaVariacaoM
        );

        // E: cada uma deve ter o SEU PRÓPRIO id_variacao
        int idVariacaoP = ProdutoDAO.obterIdVariacaoPorSku(variacaoP.sku());
        int idVariacaoM = ProdutoDAO.obterIdVariacaoPorSku(variacaoM.sku());

        assertTrue("A primeira variação não possui um id_variacao válido.", idVariacaoP > 0);
        assertTrue("A segunda variação não possui um id_variacao válido.", idVariacaoM > 0);

        assertNotEquals(
                "As duas variações receberam o mesmo id_variacao.",
                idVariacaoP,
                idVariacaoM
        );
    }
}
