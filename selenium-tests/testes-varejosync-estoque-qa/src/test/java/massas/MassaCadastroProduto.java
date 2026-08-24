package massas;

import net.datafaker.Faker;

public class MassaCadastroProduto {
    private static final Faker faker = new Faker();

    public static String nomeProdutoValido() {
        return "Camiseta" + faker.number().numberBetween(100, 999);
    }

    public static String nomeVazio(){
        return "";
    }

    public static String nomeAbaixoMinimo(){
        return "CA";
    }

    public static String corValida() {
        return "Verde Oliva";
    }

    public static String tamanhoValido() {
        return "P";
    }

    public static String skuValido() {
        return "CAM" + System.currentTimeMillis() + "-VO-P";
    }

    public static String skuFormatoInvalido() {
        return "1";
    }

    public static String skuVazio() {
        return "";
    }


    public static String skuComEspaco() {
        return "CAM AZUL M";
    }

    public static String precoValido() {
        return "69.90";
    }

    public static String precoFormatoInvalido() {
        return "0.00";
    }

    public static String quantidadeInicialValida() {
        return "0";
    }

    public static String estoqueMinimoValido() {
        return "10";
    }

    public static String estoqueMinimoNegativo() {
        return "-1";
    }

    public static String estoqueMinimoDecimal() {
        return "10.5";
    }

    public static String estoqueMinimoTexto() {
        return "abc";
    }

    public static String estoqueMinimoVazio() {
        return "";
    }

    public static String novoEstoqueMinimoValidoEdicao() {
        return "12";
    }
}
