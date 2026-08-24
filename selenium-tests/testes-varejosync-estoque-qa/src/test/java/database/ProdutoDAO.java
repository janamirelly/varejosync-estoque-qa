package database;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;

public class ProdutoDAO {
    private static final String CAMINHO_BANCO =
            "C:/estoque-qa-lab/backend/db/estoque_qa_lab.db";

    private static final String URL_BANCO =
            "jdbc:sqlite:" + CAMINHO_BANCO;


    private static Connection conectar() {
        try {
            if (!Files.exists(Path.of(CAMINHO_BANCO))) {
                throw new RuntimeException(
                        "Arquivo do banco não encontrado no caminho: " + CAMINHO_BANCO
                );
            }

            return DriverManager.getConnection(URL_BANCO);

        } catch (Exception erro) {
            throw new RuntimeException
                    ("Erro ao conectar no banco SQLite.", erro);
        }
    }

    public static boolean testarConexao() {
        try (Connection conexao = conectar()) {
            return conexao != null;


        } catch (Exception erro) {
            throw new RuntimeException
                    ("Falha ao testar conexão com SQlite!", erro);
        }
    }

    public static boolean existeProdutoPorSku(String sku) {
        String sql = "SELECT COUNT(*) FROM variacao_produto WHERE sku = ?";
        try (Connection conexao = conectar();
             PreparedStatement statement = conexao.prepareStatement(sql)) {
            statement.setString(1, sku);
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    int quantidadeEncontrada = resultado.getInt(1);
                    return quantidadeEncontrada > 0;

                }

            }

        } catch (Exception erro) {
            throw new RuntimeException("Erro ao consultar produto por sku!", erro);
        }
        return false;
    }

    public static boolean existeProdutoComEstoqueMinimo(String sku, String novoEstoqueMinimo) {
        String sql = """
            SELECT COUNT(*)
            FROM variacao_produto vp
            INNER JOIN estoque e
                ON e.id_variacao = vp.id_variacao
            WHERE vp.sku = ?
              AND e.estoque_min = ?
            """;

        try (Connection conexao = conectar();
             PreparedStatement statement = conexao.prepareStatement(sql)) {

            statement.setString(1, sku);
            statement.setInt(2, Integer.parseInt(novoEstoqueMinimo));

            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getInt(1) > 0;
                }
            }

        } catch (Exception erro) {
            throw new RuntimeException
                    ("Erro ao consultar estoque minímo por SKU"
                            , erro);
        }

        return false;
    }


    public static boolean produtoEstaAtivoPorSku(String sku) {
        String sql = """
            SELECT p.ativo
            FROM produto p
            INNER JOIN variacao_produto vp
                    ON vp.id_produto = p.id_produto
            WHERE vp.sku = ?
            """;

        try (Connection conexao = conectar();
             PreparedStatement statement = conexao.prepareStatement(sql)) {

            statement.setString(1, sku);

            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getInt("ativo") == 1;
                }
            }

        } catch (Exception erro) {
            throw new RuntimeException(
                    "Erro ao consultar status ativo do produto por SKU.",
                    erro
            );
        }

        return false;
    }

    public static int obterIdProdutoPorSku(String sku) {
        String sql = """
            SELECT vp.id_produto
            FROM variacao_produto vp
            WHERE vp.sku = ?
            """;

        try (Connection conexao = conectar();
             PreparedStatement statement = conexao.prepareStatement(sql)) {

            statement.setString(1, sku);

            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getInt("id_produto");
                }
            }

        } catch (Exception erro) {
            throw new RuntimeException(
                    "Erro ao consultar id_produto por SKU.",
                    erro
            );
        }

        return -1;
    }

    public static boolean variacaoEstaAtivaPorSku(String sku) {
        String sql = """
        SELECT ativo
        FROM variacao_produto
        WHERE sku = ?
        """;

        try (Connection conexao = conectar();
             PreparedStatement statement = conexao.prepareStatement(sql)) {

            statement.setString(1, sku);

            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getInt("ativo") == 1;
                }
            }

        } catch (Exception erro) {
            throw new RuntimeException(
                    "Erro ao consultar status ativo da variação por SKU.",
                    erro
            );
        }

        return false;
    }

    public static int obterIdVariacaoPorSku(String sku) {
        String sql = """
        SELECT id_variacao
        FROM variacao_produto
        WHERE sku = ?
        """;

        try (Connection conexao = conectar();
             PreparedStatement statement = conexao.prepareStatement(sql)) {

            statement.setString(1, sku);

            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getInt("id_variacao");
                }
            }

        } catch (Exception erro) {
            throw new RuntimeException(
                    "Erro ao consultar id_variacao por SKU.",
                    erro
            );
        }

        return -1;
    }


    public static int obterQuantidadePorSku(String sku) {

        String sql = """
        SELECT e.quantidade
        FROM variacao_produto vp
        INNER JOIN estoque e
            ON e.id_variacao = vp.id_variacao
        WHERE vp.sku = ?
        """;

        try (Connection conexao = conectar();
             PreparedStatement statement = conexao.prepareStatement(sql)) {

            statement.setString(1, sku);

            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getInt("quantidade");
                }
            }

        } catch (Exception erro) {
            throw new RuntimeException(
                    "Erro ao consultar quantidade por SKU.",
                    erro
            );
        }

        throw new RuntimeException(
                "Quantidade não encontrada para o SKU: " + sku
        );
    }
    public static void removerDadosTestePorSkus(List<String> skus) {

        if (skus == null || skus.isEmpty()) {
            return;
        }

        try (Connection conexao = conectar()) {

            conexao.setAutoCommit(false);

            try {

                try (Statement statement = conexao.createStatement()) {
                    statement.execute("PRAGMA foreign_keys = ON");
                }

                for (String sku : skus) {

                    if (sku == null || sku.isBlank()) {
                        continue;
                    }

                    int idVariacao;
                    int idProduto;

                    String sqlBuscarIds = """
                    SELECT
                        vp.id_variacao,
                        vp.id_produto
                    FROM variacao_produto vp
                    WHERE vp.sku = ?
                    """;

                    try (PreparedStatement statement =
                                 conexao.prepareStatement(sqlBuscarIds)) {

                        statement.setString(1, sku);

                        try (ResultSet resultado = statement.executeQuery()) {

                            if (!resultado.next()) {
                                continue;
                            }

                            idVariacao =
                                    resultado.getInt("id_variacao");

                            idProduto =
                                    resultado.getInt("id_produto");
                        }
                    }


                    // 1. Remover movimentações vinculadas à variação
                    try (PreparedStatement statement =
                                 conexao.prepareStatement(
                                         """
                                         DELETE FROM movimentacao_estoque
                                         WHERE id_variacao = ?
                                         """
                                 )) {

                        statement.setInt(1, idVariacao);
                        statement.executeUpdate();
                    }


                    // 2. Remover o estoque da variação
                    try (PreparedStatement statement =
                                 conexao.prepareStatement(
                                         """
                                         DELETE FROM estoque
                                         WHERE id_variacao = ?
                                         """
                                 )) {

                        statement.setInt(1, idVariacao);
                        statement.executeUpdate();
                    }


                    // 3. Remover auditorias específicas da variação
                    try (PreparedStatement statement =
                                 conexao.prepareStatement(
                                         """
                                         DELETE FROM auditoria
                                         WHERE recurso = ?
                                         """
                                 )) {

                        statement.setString(
                                1,
                                "variacao:" + idVariacao
                        );

                        statement.executeUpdate();
                    }


                    // 4. Remover a variação
                    try (PreparedStatement statement =
                                 conexao.prepareStatement(
                                         """
                                         DELETE FROM variacao_produto
                                         WHERE id_variacao = ?
                                         """
                                 )) {

                        statement.setInt(1, idVariacao);
                        statement.executeUpdate();
                    }


                    // 5. Verificar se o produto ainda possui variações
                    int quantidadeVariacoes;

                    try (PreparedStatement statement =
                                 conexao.prepareStatement(
                                         """
                                         SELECT COUNT(*)
                                         FROM variacao_produto
                                         WHERE id_produto = ?
                                         """
                                 )) {

                        statement.setInt(1, idProduto);

                        try (ResultSet resultado =
                                     statement.executeQuery()) {

                            resultado.next();

                            quantidadeVariacoes =
                                    resultado.getInt(1);
                        }
                    }


                    // 6. Remover o produto somente se não houver
                    // nenhuma outra variação vinculada
                    if (quantidadeVariacoes == 0) {

                        try (PreparedStatement statement =
                                     conexao.prepareStatement(
                                             """
                                             DELETE FROM auditoria
                                             WHERE recurso = ?
                                             """
                                     )) {

                            statement.setString(
                                    1,
                                    "produto:" + idProduto
                            );

                            statement.executeUpdate();
                        }

                        try (PreparedStatement statement =
                                     conexao.prepareStatement(
                                             """
                                             DELETE FROM produto
                                             WHERE id_produto = ?
                                             """
                                     )) {

                            statement.setInt(1, idProduto);
                            statement.executeUpdate();
                        }
                    }
                }

                conexao.commit();

            } catch (Exception erro) {

                conexao.rollback();

                throw new RuntimeException(
                        "Erro ao remover massa de dados criada pelos testes.",
                        erro
                );
            }

        } catch (Exception erro) {

            if (erro instanceof RuntimeException) {
                throw (RuntimeException) erro;
            }

            throw new RuntimeException(
                    "Erro ao executar limpeza da massa de testes.",
                    erro
            );
        }
    }
}









