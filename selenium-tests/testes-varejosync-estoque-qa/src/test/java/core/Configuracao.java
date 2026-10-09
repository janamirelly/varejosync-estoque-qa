package core;

/**
 * Configurações que mudam entre a máquina local e um servidor: caminho do
 * banco e modo headless.
 *
 * Ordem de leitura: -Dnome=valor na linha de comando, depois variável de
 * ambiente, depois o padrão definido aqui.
 */
public class Configuracao {

    private static final String CAMINHO_BANCO_PADRAO =
            "C:/varejosync-estoque-qa/backend/db/estoque_qa_lab.db";

    /**
     * Caminho do arquivo SQLite da aplicação.
     * Em CI: mvn test -Dbanco.caminho=&lt;caminho do .db gerado pelo seed&gt;
     */
    public static String caminhoDoBanco() {
        String configurado = valorExterno("banco.caminho", "BANCO_CAMINHO");

        return configurado != null ? configurado : CAMINHO_BANCO_PADRAO;
    }

    /**
     * Se o Chrome abre sem janela. Sem configuração, segue a variável CI
     * (o GitHub Actions define CI=true). Na máquina local, abre com janela.
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
