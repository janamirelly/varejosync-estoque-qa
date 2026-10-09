package massas;

import net.datafaker.Faker;

/**
 * Massas de produto usadas nos testes. Cada chamada gera um SKU novo.
 */
public class MassaProduto {

    private static final Faker faker = new Faker();

    // Cenário positivo

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

    // Cenários negativos: partem de valido() e trocam só um campo, para
    // saber qual campo causou a recusa.

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

    // Valores usados nos cenários de edição

    /**
     * Novo estoque mínimo para os cenários de edição. Diferente do valor de
     * valido() de propósito: se fosse igual, o teste de edição passaria mesmo
     * que a alteração não salvasse.
     */
    public static String novoEstoqueMinimo() {
        return "12";
    }

    // Cenários com variações

    /**
     * Duas variações (P e M) do mesmo produto: mesmo nome e cor, tamanho e
     * SKU diferentes. O sufixo com timestamp cria um produto novo a cada
     * execução.
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
