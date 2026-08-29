package massas;

import net.datafaker.Faker;

/**
 * Fábrica de massa de teste. Cada método devolve um cenário pronto, de modo
 * que o teste não monte dados na mão: ler MassaProduto.semNome() já diz qual
 * cenário está em jogo.
 *
 * O Faker é o gerador e vive uma vez só; os dados não, para que cada chamada
 * produza um SKU novo e os testes sejam repetíveis.
 */
public class MassaProduto {

    private static final Faker faker = new Faker();

    // ------------------------------------------------------------------
    // Cenário positivo
    // ------------------------------------------------------------------

    /** Produto com todos os campos válidos. SKU único a cada chamada. */
    public static Produto valido() {
        return new Produto(
                "Camiseta" + faker.number().numberBetween(100, 999), // nome
                "Verde Oliva",                                       // cor
                "P",                                                 // tamanho
                "CAM" + System.currentTimeMillis() + "-VO-P",        // sku
                "69.90",                                             // preco
                "0",                                                 // quantidadeInicial
                "10"                                                 // estoqueMinimo
        );
    }

    // ------------------------------------------------------------------
    // Cenários negativos: todos partem de valido() e trocam UM campo.
    //
    // Massa negativa montada do zero não prova nada: se o cadastro for
    // recusado, não se sabe qual campo causou a recusa. Partindo do válido,
    // existe uma única diferença possível — a que está sendo testada.
    // ------------------------------------------------------------------

    /** Nome vazio. Todos os outros campos válidos. */
    public static Produto semNome() {
        return valido().comNome("");
    }

    /** Nome com 2 caracteres, abaixo do mínimo aceito. */
    public static Produto comNomeAbaixoMinimo() {
        return valido().comNome("CA");
    }

    /** SKU vazio. Todos os outros campos válidos. */
    public static Produto comSkuVazio() {
        return valido().comSku("");
    }

    // ------------------------------------------------------------------
    // Valores usados nos cenários de edição
    // ------------------------------------------------------------------

    /**
     * Novo estoque mínimo para os cenários de edição. Diferente do valor de
     * valido() de propósito: se fosse igual, o teste de edição passaria mesmo
     * que a alteração não salvasse.
     */
    public static String novoEstoqueMinimo() {
        return "12";
    }

    // ------------------------------------------------------------------
    // Cenários com variações
    // ------------------------------------------------------------------

    /**
     * Duas variações (P e M) do mesmo produto.
     *
     * A regra do sistema: nome e cor iguais significam o mesmo produto;
     * tamanho e sku diferentes significam variações distintas dele.
     *
     * O sufixo de timestamp garante que cada execução crie um produto novo —
     * sem ele o teste falharia por massa suja, não por defeito no sistema.
     */
    public static ParDeVariacoes duasVariacoesDoMesmoProduto() {
        String sufixo = String.valueOf(System.currentTimeMillis());

        String nome = "Blusa Canelada " + sufixo;
        String cor  = "PRETA";

        Produto tamanhoP = new Produto(
                nome, cor, "P", "BLU" + sufixo + "-PRETA-P", "69.90", "0", "10"
        );

        Produto tamanhoM = new Produto(
                nome, cor, "M", "BLU" + sufixo + "-PRETA-M", "69.90", "0", "10"
        );

        return new ParDeVariacoes(tamanhoP, tamanhoM);
    }
}
