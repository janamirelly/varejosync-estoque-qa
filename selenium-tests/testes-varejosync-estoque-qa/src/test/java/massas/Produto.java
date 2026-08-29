package massas;

/**
 * Um produto de teste. Só carrega dados: não conhece Selenium, tela nem banco.
 *
 * O formulário recebe o produto inteiro em vez de sete String soltas. Como
 * sku e tamanho são ambos String, uma inversão de argumentos compilaria sem
 * erro — passar um objeto único elimina essa classe de defeito.
 */
public record Produto(
        String nome,
        String cor,
        String tamanho,
        String sku,
        String preco,
        String quantidadeInicial,
        String estoqueMinimo
) {

    // Um record é imutável: para ter "o mesmo produto, só que sem nome",
    // devolve-se um produto novo com os demais campos copiados. É o que
    // permite a massa negativa partir sempre de um produto válido.

    /** Cópia deste produto trocando só o nome. */
    public Produto comNome(String novoNome) {
        return new Produto(
                novoNome, cor, tamanho, sku, preco, quantidadeInicial, estoqueMinimo
        );
    }

    /** Cópia deste produto trocando só o sku. */
    public Produto comSku(String novoSku) {
        return new Produto(
                nome, cor, tamanho, novoSku, preco, quantidadeInicial, estoqueMinimo
        );
    }
}
