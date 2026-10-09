package pages;

import massas.Produto;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Tela de cadastro de produto. A mesma tela é usada para editar.
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

    // O mesmo locator está na MenuPage: dá para chegar nesta tela pelo menu
    // ou pelo botão Editar da consulta.
    private static final By PG_CADASTRO_ATIVA =
            By.cssSelector("#page-produtos.active");

    public CadastroProdutoPage(WebDriver driver) {
        super(driver);
    }

    /** Preenche os sete campos do formulário. */
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

    /**
     * Preenche e envia o formulário. Usado quando o cadastro é só
     * pré-condição de outro teste.
     */
    public void cadastrar(Produto produto) {
        preencherFormulario(produto);
        clicarCadastrar();
    }

    // Modo edição

    /**
     * Espera a tela de cadastro ficar ativa. Devolve false se ela não abrir,
     * e o assert do teste mostra a falha.
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
     * No modo edição, o botão de cadastrar vira "Salvar alterações" (mesmo id,
     * outro texto).
     */
    public void clicarSalvarAlteracoes() {
        clicar(BOTAO_CADASTRAR);
    }
}
