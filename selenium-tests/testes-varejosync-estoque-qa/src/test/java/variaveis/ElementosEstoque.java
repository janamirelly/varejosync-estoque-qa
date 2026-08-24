package variaveis;

import org.openqa.selenium.By;

//Página Dashboard
public class ElementosEstoque {
    public static final By PG_DASHBOARD_ATIVA =
            By.cssSelector("#page-dashboard.active");

    //Menu lateral
    public static final By BOTAO_CADASTRO =
            By.cssSelector(".menu-button[data-page=produtos]");

    public static final By BOTAO_CONSULTAR_ESTOQUE =
            By.cssSelector(".menu-button[data-page='estoque']");

    //Página de cadastro
    public static final  By PG_CADASTRO_PRODUTO_ATIVA =
            By.cssSelector("#page-produtos.active");

    // Página Consultar Estoque
    public static final By PG_CONSULTAR_ESTOQUE_ATIVA =
            By.cssSelector("#page-estoque.active");

    public static final By INPUT_BUSCA_ESTOQUE =
            By.id("estoqueBusca");

    public static final By BOTAO_BUSCAR_ESTOQUE =
            By.id("btnBuscarEstoque");

    public static final By TABELA_ESTOQUE =
            By.id("estoqueTabela");

    public static final By BOTAO_CADASTRAR =
            By.id("btnCadastrarProduto");


    // Edição de produto
    public static final By BOTAO_EDITAR_PRODUTO =
            By.cssSelector(".btn-editar-produto");

    public static final By BOTAO_SALVAR_ALTERACOES =
            By.id("btnCadastrarProduto");

    public static final By BOTAO_EXCLUIR_PRODUTO =
            By.cssSelector(".btn-excluir-produto");

    // Feedback geral da aplicação
    public static final By MENSAGEM_SUCESSO =
            By.id("feedbackMessage");

    public static final By TEXTO_FEEDBACK =
            By.id("feedbackText");

}
