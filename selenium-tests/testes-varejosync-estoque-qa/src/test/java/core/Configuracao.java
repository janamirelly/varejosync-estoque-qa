package core;

/**
 * Tudo que muda entre a máquina local e um servidor mora aqui, de modo que a
 * pergunta "o que preciso ajustar para rodar em outro lugar?" tenha um único
 * arquivo de resposta.
 *
 * A precedência é sempre a mesma: -Dnome=valor na linha de comando vence a
 * variável de ambiente, que vence o padrão escrito aqui. Rodando local sem
 * configurar nada, cai no padrão.
 */
public class Configuracao {

    private static final String CAMINHO_BANCO_PADRAO =
            "C:/varejosync-estoque-qa/backend/db/estoque_qa_lab.db";

    /**
     * Onde está o arquivo SQLite da aplicação.
     * Em CI: mvn test -Dbanco.caminho=&lt;caminho do .db gerado pelo seed&gt;
     */
    public static String caminhoDoBanco() {
        String configurado = valorExterno("banco.caminho", "BANCO_CAMINHO");

        return configurado != null ? configurado : CAMINHO_BANCO_PADRAO;
    }

    /**
     * Se o Chrome deve abrir sem janela.
     *
     * Não precisa ser configurado no GitHub Actions: ele define CI=true
     * sozinho. Localmente CI não existe, então o padrão é com janela.
     * Para forçar: mvn test -Dheadless=true
     */
    public static boolean rodarHeadless() {
        String configurado = valorExterno("headless", "HEADLESS");

        if (configurado != null) {
            return Boolean.parseBoolean(configurado);
        }

        return Boolean.parseBoolean(System.getenv("CI"));
    }

    /** Lê uma configuração externa; null quando não definida em lugar nenhum. */
    private static String valorExterno(String propriedade, String variavelDeAmbiente) {
        String valor = System.getProperty(propriedade);

        if (valor == null || valor.isBlank()) {
            valor = System.getenv(variavelDeAmbiente);
        }

        return (valor == null || valor.isBlank()) ? null : valor;
    }
}
