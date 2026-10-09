package massas;

/**
 * Dados de um produto de teste: os sete campos do formulário de cadastro.
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

    // Cópias do produto com um campo trocado (usadas nas massas negativas).

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
