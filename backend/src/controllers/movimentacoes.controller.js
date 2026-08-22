const { db } = require("../db/database");

function get(sql, params = []) {
  return new Promise((resolve, reject) => {
    db.get(sql, params, (err, row) => {
      if (err) return reject(err);
      resolve(row);
    });
  });
}

function all(sql, params = []) {
  return new Promise((resolve, reject) => {
    db.all(sql, params, (err, rows) => {
      if (err) return reject(err);
      resolve(rows);
    });
  });
}

function run(sql, params = []) {
  return new Promise((resolve, reject) => {
    db.run(sql, params, function (err) {
      if (err) return reject(err);
      resolve(this);
    });
  });
}

async function listarMovimentacoes(req, res) {
  try {
    const movimentacoes = await all(`
      SELECT
        id_movimentacao,
        criado_em,
        tipo,
        quantidade,
        observacao,
        id_variacao,
        sku,
        cor,
        tamanho,
        id_produto,
        produto
      FROM vw_movimentacao_detalhada
      ORDER BY criado_em DESC, id_movimentacao DESC
      LIMIT 100
    `);

    return res.json(movimentacoes);
  } catch (error) {
    console.error("[MOVIMENTACOES] erro ao listar:", error.message);

    return res.status(500).json({
      erro: "Erro ao listar movimentações.",
    });
  }
}

async function registrarMovimentacao(req, res) {
  try {
    const idVariacao = Number(req.body.id_variacao);
    const tipo = String(req.body.tipo || "")
      .trim()
      .toUpperCase();
    const quantidade = Number(req.body.quantidade);
    const observacao = String(
      req.body.observacao ?? req.body.motivo ?? "",
    ).trim();

    if (!Number.isInteger(idVariacao) || idVariacao <= 0) {
      return res.status(400).json({
        erro: "id_variacao inválido.",
      });
    }

    if (!["ENTRADA", "SAIDA", "AJUSTE"].includes(tipo)) {
      return res.status(400).json({
        erro: "Tipo de movimentação inválido. Use ENTRADA, SAIDA ou AJUSTE.",
      });
    }

    if (!Number.isInteger(quantidade) || quantidade <= 0) {
      return res.status(400).json({
        erro: "Quantidade deve ser um número inteiro maior que zero.",
      });
    }

    const estoqueAtual = await get(
      `
        SELECT
          e.id_estoque,
          e.id_variacao,
          e.quantidade,
          e.estoque_min
        FROM estoque e
        WHERE e.id_variacao = ?
      `,
      [idVariacao],
    );

    if (!estoqueAtual) {
      return res.status(404).json({
        erro: "Estoque da variação não encontrado.",
      });
    }

    let novaQuantidade = estoqueAtual.quantidade;

    if (tipo === "ENTRADA") {
      novaQuantidade = estoqueAtual.quantidade + quantidade;
    }

    if (tipo === "SAIDA") {
      if (quantidade > estoqueAtual.quantidade) {
        return res.status(400).json({
          erro: "Saldo insuficiente para saída de estoque.",
          quantidade_atual: estoqueAtual.quantidade,
          quantidade_solicitada: quantidade,
        });
      }

      novaQuantidade = estoqueAtual.quantidade - quantidade;
    }

    if (tipo === "AJUSTE") {
      novaQuantidade = quantidade;
    }

    await run("BEGIN");

    try {
      await run(
        `
          UPDATE estoque
          SET
            quantidade = ?,
            atualizado_em = datetime('now','localtime')
          WHERE id_variacao = ?
        `,
        [novaQuantidade, idVariacao],
      );

      const result = await run(
        `
          INSERT INTO movimentacao_estoque (
            id_variacao,
            tipo,
            quantidade,
            observacao
          )
          VALUES (?, ?, ?, ?)
        `,
        [idVariacao, tipo, quantidade, observacao || null],
      );

      await run(
        `
          INSERT INTO auditoria (
            acao,
            recurso,
            detalhes
          )
          VALUES (?, ?, ?)
        `,
        [
          "MOVIMENTACAO_ESTOQUE_REGISTRADA",
          "movimentacao_estoque",
          JSON.stringify({
            id_movimentacao: result.lastID,
            id_variacao: idVariacao,
            tipo,
            quantidade,
            quantidade_anterior: estoqueAtual.quantidade,
            quantidade_atual: novaQuantidade,
          }),
        ],
      );

      await run("COMMIT");

      const movimentacaoCriada = await get(
        `
    SELECT
      id_movimentacao,
      criado_em,
      tipo,
      quantidade,
      observacao,
      id_variacao,
      sku,
      cor,
      tamanho,
      id_produto,
      produto
    FROM vw_movimentacao_detalhada
    WHERE id_movimentacao = ?
  `,
        [result.lastID],
      );

      return res.status(201).json({
        message: "Movimentação registrada com sucesso.",

        produto: {
          id_produto: movimentacaoCriada.id_produto,
          id_variacao: movimentacaoCriada.id_variacao,
          nome: movimentacaoCriada.produto,
          sku: movimentacaoCriada.sku,
          variacao: `${movimentacaoCriada.cor} / ${movimentacaoCriada.tamanho}`,
        },

        movimentacao: {
          id_movimentacao: movimentacaoCriada.id_movimentacao,
          tipo: movimentacaoCriada.tipo,
          quantidade: Number(movimentacaoCriada.quantidade),
          motivo: movimentacaoCriada.observacao,
          data: movimentacaoCriada.criado_em,
        },

        estoque: {
          anterior: Number(estoqueAtual.quantidade),
          atual: Number(novaQuantidade),
        },
      });
    } catch (transactionError) {
      await run("ROLLBACK");
      throw transactionError;
    }
  } catch (error) {
    console.error("[MOVIMENTACOES] erro ao registrar:", error.message);

    return res.status(500).json({
      erro: "Erro ao registrar movimentação de estoque.",
    });
  }
}

module.exports = {
  listarMovimentacoes,
  registrarMovimentacao,
};
