package massas;

/**
 * Duas variações do mesmo produto, criadas juntas.
 *
 * O que faz o sistema tratá-las como um único produto é compartilharem nome
 * e cor. Pedir "uma variação P" e depois "uma variação M" em chamadas
 * separadas geraria nomes diferentes, e o teste de vínculo perderia o sentido.
 */
public record ParDeVariacoes(Produto tamanhoP, Produto tamanhoM) {}
