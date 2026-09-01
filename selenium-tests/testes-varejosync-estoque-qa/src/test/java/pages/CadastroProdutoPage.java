package pages;

import massas.Produto;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Tela de Cadastro de Produto: sabe onde cada campo está e como preenchê-lo.
 * Não sabe o que é "certo" — nenhum assert mora aqui.
 *
 * A tela tem dois modos, cadastro e edição, e é a mesma tela nos dois. Por
 * isso é uma Page só.
 */
public class
CadastroProdutoPage extends BasePage {

    private static final By INPUT_NOME        = By.id("produtoNome");
    private static final By INPUT_COR         = By.id("produtoCor");
    private static final By INPUT_TAMANHO     = By.id("produtoTamanho");
    private static final By INPUT_SKU         = By.id("produtoSku");
    private static final By INPUT_PRECO       = By.id("produtoPreco");
    private static final By INPUT_QTD         = By.id("produtoQuantidade");
    private static final By INPUT_ESTOQUE_MIN = By.id("produtoEstoqueMinimo");

    private static final By BOTAO_CADASTRAR   = By.id("btnCadastrarProduto");

    /**
     * Também presente na MenuPage: existem duas formas de chegar nesta tela —
     * o menu lateral e o botão Editar da consulta. Trade-off assumido.
     */
    private static final By PG_CADASTRO_ATIVA =
            By.cssSelector("#page-produtos.active");

    public CadastroProdutoPage(WebDriver driver) {
        super(driver);
    }

    // ------------------------------------------------------------------
    // Passos
    // ------------------------------------------------------------------

    /** Recebe o produto inteiro: um argumento, sem ordem para inverter. */
    public void preencherFormulario(Produto produto) {
        digitar(INPUT_NOME,        produto.nome());
        digitar(INPUT_COR,         produto.cor());
        digitar(INPUT_TAMANHO,     produto.tamanho());
        digitar(INPUT_SKU,         produto.sku());
        digitar(INPUT_PRECO,       produto.preco());
        digitar(INPUT_QTD,         produto.quantidadeInicial());
        digitar(INPUT_ESTOQUE_MIN, produto.estoqueMinimo());
    }

    public void clicarCadastrar() {
        clicar(BOTAO_CADASTRAR);
    }

    // ------------------------------------------------------------------
    // Ação de negócio
    // ------------------------------------------------------------------

    /**
     * Preenche e envia o formulário numa chamada.
     *
     * Os passos separados (preencherFormulario + clicarCadastrar) são usados
     * quando o cadastro é o que está sendo testado, porque aí eles são o
     * "Quando" do cenário. Este método é para quando cadastrar é apenas
     * pré-condição de outro teste.
     */
    public void cadastrar(Produto produto) {
        preencherFormulario(produto);
        clicarCadastrar();
    }

    // ------------------------------------------------------------------
    // Modo edição
    // ------------------------------------------------------------------

    /**
     * Espera esta tela ficar ativa e devolve se conseguiu.
     *
     * Quem clica em Editar é a ConsultarEstoquePage, mas quem sabe dizer se a
     * tela de cadastro carregou é esta Page. Devolve boolean, e não exceção,
     * para que a falha apareça com a mensagem do assert do teste.
     */
    public boolean estaAtiva() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(PG_CADASTRO_ATIVA));
            return true;
        } catch (TimeoutException erro) {
            return false;
        }
    }

    public void alterarEstoqueMinimo(String novoEstoqueMinimo) {
        digitar(INPUT_ESTOQUE_MIN, novoEstoqueMinimo);
    }

    /**
     * Em modo edição a aplicação reusa o botão do cadastro: mesmo id, rótulo
     * diferente. O método tem nome próprio para o teste ler como o usuário vê
     * a tela; apontar para o mesmo elemento é intencional.
     */
    public void clicarSalvarAlteracoes() {
        clicar(BOTAO_CADASTRAR);
    }
}
