package tests;

import core.BaseTest;
import database.ProdutoDAO;
import massas.MassaProduto;
import massas.ParDeVariacoes;
import massas.Produto;
import org.junit.Test;
import pages.CadastroProdutoPage;
import pages.ConsultarEstoquePage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Testes de exclusão de variação (CT-EST-EXC-xxx).
 *
 * O tema é efeito colateral: a operação precisa mudar exatamente o que foi
 * pedido e nada além. Por isso cada teste verifica tanto o que deve mudar
 * quanto o que não pode mudar.
 */
public class ExclusaoProdutoTest extends BaseTest {

    private static final String MSG_VARIACAO_EXCLUIDA =
            "Variação excluída com sucesso";

    @Test
    public void CT_EST_EXC_001_inativarSomenteVariacaoSelecionada() {

        CadastroProdutoPage cadastroPage = new CadastroProdutoPage(driver);
        ConsultarEstoquePage estoquePage = new ConsultarEstoquePage(driver);

        // ================================================================
        // Dado: um produto com duas variações ativas.
        // Toda esta seção é pré-condição.
        // ================================================================
        ParDeVariacoes variacoes = MassaProduto.duasVariacoesDoMesmoProduto();

        Produto variacaoP = variacoes.tamanhoP();
        Produto variacaoM = variacoes.tamanhoM();

        skusCriadosNoTeste.add(variacaoP.sku());
        skusCriadosNoTeste.add(variacaoM.sku());

        menuPage.irParaCadastroProduto();
        cadastroPage.cadastrar(variacaoP);

        menuPage.irParaCadastroProduto();
        cadastroPage.cadastrar(variacaoM);

        // Pré-condição: as duas foram criadas
        assertTrue(
                "Pré-condição falhou: a variação P não foi cadastrada.",
                ProdutoDAO.aguardarProdutoPorSku(variacaoP.sku())
        );

        assertTrue(
                "Pré-condição falhou: a variação M não foi cadastrada.",
                ProdutoDAO.aguardarProdutoPorSku(variacaoM.sku())
        );

        // Pré-condição: as duas pertencem ao mesmo produto
        assertEquals(
                "Pré-condição falhou: as variações não estão no mesmo produto.",
                ProdutoDAO.obterIdProdutoPorSku(variacaoP.sku()),
                ProdutoDAO.obterIdProdutoPorSku(variacaoM.sku())
        );

        // ----------------------------------------------------------------
        // Pré-condição: tudo ativo antes da exclusão. Sem estes três asserts,
        // os do final não provam nada — "a variação P continua ativa" só tem
        // sentido se ela estava ativa antes.
        // ----------------------------------------------------------------
        assertTrue(
                "Pré-condição falhou: o produto de origem deveria estar ativo.",
                ProdutoDAO.produtoEstaAtivoPorSku(variacaoM.sku())
        );

        assertTrue(
                "Pré-condição falhou: a variação P deveria estar ativa.",
                ProdutoDAO.variacaoEstaAtivaPorSku(variacaoP.sku())
        );

        assertTrue(
                "Pré-condição falhou: a variação M deveria estar ativa.",
                ProdutoDAO.variacaoEstaAtivaPorSku(variacaoM.sku())
        );

        // ================================================================
        // Quando: excluir SOMENTE a variação M
        // ================================================================
        menuPage.irParaConsultarEstoque();
        estoquePage.buscarPorSku(variacaoM.sku());

        assertTrue(
                "A variação M não apareceu na tabela antes da exclusão.",
                estoquePage.lerTabela().contains(variacaoM.sku())
        );

        estoquePage.clicarExcluir();
        estoquePage.confirmarExclusao();

        // ================================================================
        // Então: o sistema deve exibir a mensagem correta
        // ================================================================
        String feedback = estoquePage.lerFeedback(MSG_VARIACAO_EXCLUIDA);

        assertTrue(
                "Esperava conter '" + MSG_VARIACAO_EXCLUIDA + "', mas veio: '" + feedback + "'",
                feedback.contains(MSG_VARIACAO_EXCLUIDA)
        );

        // E: o produto pai deve permanecer ATIVO
        assertTrue(
                "O produto de origem foi inativado indevidamente.",
                ProdutoDAO.produtoEstaAtivoPorSku(variacaoM.sku())
        );

        // E: somente a variação M deve ficar INATIVA
        assertFalse(
                "A variação selecionada permaneceu ativa.",
                ProdutoDAO.variacaoEstaAtivaPorSku(variacaoM.sku())
        );

        // E: a variação P deve continuar ATIVA
        assertTrue(
                "A variação não selecionada foi inativada indevidamente.",
                ProdutoDAO.variacaoEstaAtivaPorSku(variacaoP.sku())
        );

        // E: a variação M não deve mais aparecer na tela
        estoquePage.buscarPorSku(variacaoM.sku());

        assertFalse(
                "A variação inativada continua sendo exibida na tabela.",
                estoquePage.lerTabela().contains(variacaoM.sku())
        );

        // E: a variação P deve continuar disponível na tela
        estoquePage.buscarPorSku(variacaoP.sku());

        assertTrue(
                "A variação não selecionada sumiu da tabela.",
                estoquePage.lerTabela().contains(variacaoP.sku())
        );
    }

    @Test
    public void CT_EST_EXC_002_inativarUltimaVariacaoAtivaInativaProdutoDeOrigem() {

        CadastroProdutoPage cadastroPage = new CadastroProdutoPage(driver);
        ConsultarEstoquePage estoquePage = new ConsultarEstoquePage(driver);

        // ================================================================
        // Dado: um produto com uma única variação ativa.
        //
        // Complemento do CT-EST-EXC-001: lá o produto tinha duas variações e
        // inativar uma não podia derrubá-lo; aqui só existe uma, e inativá-la
        // deve derrubá-lo. O estado do produto é consequência do estado das
        // variações (RN-014), visto pelos dois lados.
        // ================================================================
        Produto produto = MassaProduto.valido();
        skusCriadosNoTeste.add(produto.sku());

        menuPage.irParaCadastroProduto();
        cadastroPage.cadastrar(produto);

        assertTrue(
                "Pré-condição falhou: o produto não foi cadastrado.",
                ProdutoDAO.aguardarProdutoPorSku(produto.sku())
        );

        assertTrue(
                "Pré-condição falhou: o produto deveria estar ativo.",
                ProdutoDAO.produtoEstaAtivoPorSku(produto.sku())
        );

        assertTrue(
                "Pré-condição falhou: a variação deveria estar ativa.",
                ProdutoDAO.variacaoEstaAtivaPorSku(produto.sku())
        );

        // ================================================================
        // Quando: excluir a única variação existente
        // ================================================================
        menuPage.irParaConsultarEstoque();
        estoquePage.buscarPorSku(produto.sku());

        assertTrue(
                "A variação não apareceu na tabela antes da exclusão.",
                estoquePage.lerTabela().contains(produto.sku())
        );

        estoquePage.clicarExcluir();
        estoquePage.confirmarExclusao();

        // ================================================================
        // Então: o sistema deve exibir a mensagem correta
        // ================================================================
        String feedback = estoquePage.lerFeedback(MSG_VARIACAO_EXCLUIDA);

        assertTrue(
                "Esperava conter '" + MSG_VARIACAO_EXCLUIDA + "', mas veio: '" + feedback + "'",
                feedback.contains(MSG_VARIACAO_EXCLUIDA)
        );

        // E: a variação deve ficar INATIVA
        assertFalse(
                "A variação selecionada permaneceu ativa.",
                ProdutoDAO.variacaoEstaAtivaPorSku(produto.sku())
        );

        // E: o produto de origem deve ficar INATIVO, por consequência
        assertFalse(
                "O produto de origem deveria ter sido inativado: não restou nenhuma variação ativa.",
                ProdutoDAO.produtoEstaAtivoPorSku(produto.sku())
        );

        // ----------------------------------------------------------------
        // E: a invariante da RN-014 deve continuar valendo em toda a base —
        // nenhuma variação ativa vinculada a produto inativo. Sem este assert
        // o defeito pode voltar sem que ninguém perceba, até alguém procurar
        // uma SKU que sumiu da tela.
        // ----------------------------------------------------------------
        assertEquals(
                "Existem variações ativas vinculadas a produtos inativos (estado proibido pela RN-014).",
                0,
                ProdutoDAO.contarVariacoesAtivasComProdutoInativo()
        );

        // E: a variação não deve mais aparecer na tela
        estoquePage.buscarPorSku(produto.sku());

        assertFalse(
                "A variação inativada continua sendo exibida na tabela.",
                estoquePage.lerTabela().contains(produto.sku())
        );
    }
}
