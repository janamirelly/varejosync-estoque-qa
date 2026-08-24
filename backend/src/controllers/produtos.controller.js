const { db } = require("../db/database");

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
    db.run(sql, params, function callback(err) {
      if (err) return reject(err);

      resolve({
        lastID: this.lastID,
        changes: this.changes,
      });
    });
  });
}

function normalizarTexto(valor) {
  return String(valor || "")
    .trim()
    .toUpperCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "");
}

function nomeProdutoValido(nome) {
  const nomeTratado = String(nome || "").trim();

  return (
    nomeTratado.length >= 3 &&
    nomeTratado.length <= 30 &&
    /[\p{L}]/u.test(nomeTratado) &&
    /^[\p{L}\p{N} \-/.()%]+$/u.test(nomeTratado)
  );
}

function corVariacaoValida(cor) {
  const corTratada = String(cor || "").trim();

  return (
    corTratada.length >= 3 &&
    /[\p{L}]/u.test(corTratada) &&
    /^[\p{L} \-/.]+$/u.test(corTratada)
  );
}

async function listarProdutos(req, res) {
  try {
    const produtos = await all(`
      SELECT
        p.id_produto,
        p.nome,
        p.descricao,
        p.ativo,
        p.criado_em,
        vp.id_variacao,
        vp.sku,
        vp.cor,
        vp.tamanho
      FROM produto p
      INNER JOIN variacao_produto vp
        ON vp.id_produto = p.id_produto
      WHERE p.ativo = 1
      ORDER BY p.criado_em DESC
    `);

    return res.json(produtos);
  } catch (error) {
    console.error("[PRODUTOS] erro ao listar:", error.message);

    return res.status(500).json({
      message: "Erro ao listar produtos.",
    });
  }
}

async function criarProduto(req, res) {
  const nome = String(req.body.nome || "").trim();
  const cor = String(req.body.cor || "").trim();
  const tamanho = String(req.body.tamanho || "").trim();
  const sku = String(req.body.sku || "")
    .trim()
    .toUpperCase();
  const precoRaw = req.body.preco;

  const precoTexto =
    precoRaw === undefined || precoRaw === null ? "" : String(precoRaw).trim();

  const preco = Number(precoTexto);
  const quantidade = Number(req.body.quantidade || 0);
  const estoqueMinRaw = req.body.estoque_min;

  const estoqueMin =
    estoqueMinRaw === undefined ||
    estoqueMinRaw === null ||
    estoqueMinRaw === ""
      ? null
      : Number(estoqueMinRaw);

  try {
    if (!nomeProdutoValido(nome)) {
      return res.status(400).json({
        message: "Informe um nome de produto válido.",
      });
    }

    if (!corVariacaoValida(cor)) {
      return res.status(400).json({
        message: "Informe uma cor válida para a variação.",
      });
    }

    if (!tamanho) {
      return res.status(400).json({
        message: "Tamanho da variação é obrigatório.",
      });
    }

    if (!sku) {
      return res.status(400).json({
        message: "SKU da variação é obrigatório.",
      });
    }

    const precoValido = /^\d+(\.\d{1,2})?$/.test(precoTexto);

    if (!precoTexto || !precoValido || Number.isNaN(preco) || preco <= 0) {
      return res.status(400).json({
        message: "Informe um preço válido para a variação.",
      });
    }

    if (estoqueMin === null) {
      return res.status(400).json({
        message: "Estoque mínimo é obrigatório.",
      });
    }

    if (!Number.isInteger(estoqueMin) || estoqueMin < 10) {
      return res.status(400).json({
        message:
          "Estoque mínimo deve ser um número inteiro maior ou igual a 10.",
      });
    }

    if (!Number.isInteger(estoqueMin) || estoqueMin < 0) {
      return res.status(400).json({
        message:
          "Estoque mínimo deve ser um número inteiro maior ou igual a zero.",
      });
    }

    const skuExistente = await all(
      `
        SELECT id_variacao
        FROM variacao_produto
        WHERE sku = ?
      `,
      [sku],
    );

    if (skuExistente.length > 0) {
      return res.status(409).json({
        message: "SKU já cadastrado para outra variação.",
      });
    }

    const corNormalizada = normalizarTexto(cor);
    const tamanhoNormalizado = normalizarTexto(tamanho);

    const produtosMesmoNome = await all(
      `
    SELECT
      p.id_produto,
      p.nome,
      vp.id_variacao,
      vp.cor_normalizada,
      vp.tamanho_normalizado
    FROM produto p
    INNER JOIN variacao_produto vp
      ON vp.id_produto = p.id_produto
  `,
    );

    const variacaoDuplicada = produtosMesmoNome.find((item) => {
      return (
        normalizarTexto(item.nome) === normalizarTexto(nome) &&
        item.cor_normalizada === corNormalizada &&
        item.tamanho_normalizado === tamanhoNormalizado
      );
    });

    if (variacaoDuplicada) {
      return res.status(409).json({
        message:
          "Já existe uma variação cadastrada para este produto com a mesma cor e tamanho.",
      });
    }

    const produtosExistentes = await all(`
            SELECT id_produto, nome
            FROM produto
            WHERE ativo = 1
          `);

    const produtoExistente = produtosExistentes.find(
      (item) => normalizarTexto(item.nome) === normalizarTexto(nome),
    );

    await run("BEGIN TRANSACTION");

    try {
      let idProduto;
      let produtoFoiCriado = false;

      if (produtoExistente) {
        idProduto = produtoExistente.id_produto;
      } else {
        const produtoCriado = await run(
          `
        INSERT INTO produto (
            nome,
            descricao,
            ativo
         )
        VALUES (?, NULL, 1)
         `,
          [nome],
        );

        idProduto = produtoCriado.lastID;
        produtoFoiCriado = true;
      }

      const variacaoCriada = await run(
        `
          INSERT INTO variacao_produto (
            id_produto,
            cor,
            tamanho,
            cor_normalizada,
            tamanho_normalizado,
            sku,
            preco,
            ativo
          )
          VALUES (?, ?, ?, ?, ?, ?, ?, 1)
        `,
        [
          idProduto,
          cor,
          tamanho,
          corNormalizada,
          tamanhoNormalizado,
          sku,
          preco,
        ],
      );

      await run(
        `
          INSERT INTO estoque (
            id_variacao,
            quantidade,
            estoque_min,
            atualizado_em
          )
          VALUES (?, ?, ?, datetime('now','localtime'))
        `,
        [variacaoCriada.lastID, quantidade, estoqueMin],
      );

      if (quantidade > 0) {
        await run(
          `
      INSERT INTO movimentacao_estoque (
        id_variacao,
        tipo,
        quantidade,
        observacao
      )
      VALUES (?, ?, ?, ?)
    `,
          [
            variacaoCriada.lastID,
            "ENTRADA",
            quantidade,
            "Entrada inicial no cadastro do produto",
          ],
        );
      }

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
          produtoFoiCriado ? "PRODUTO_CRIADO" : "VARIACAO_CRIADA",
          produtoFoiCriado
            ? `produto:${idProduto}`
            : `variacao:${variacaoCriada.lastID}`,
          JSON.stringify({
            id_produto: idProduto,
            id_variacao: variacaoCriada.lastID,
            nome,
            sku,
            quantidade,
            estoque_min: estoqueMin,
          }),
        ],
      );

      await run("COMMIT");

      return res.status(201).json({
        message: "Produto cadastrado com sucesso.",
        produto: {
          id_produto: idProduto,
          nome,
          ativo: 1,
        },
        variacao: {
          id_variacao: variacaoCriada.lastID,
          cor,
          tamanho,
          sku,
          preco,
          ativo: 1,
        },
        estoque: {
          quantidade,
          estoque_min: estoqueMin,
        },
      });
    } catch (error) {
      await run("ROLLBACK");
      throw error;
    }
  } catch (error) {
    console.error("[PRODUTOS] erro ao criar:", error.message);

    if (
      error.message.includes("UNIQUE constraint failed: variacao_produto.sku")
    ) {
      return res.status(409).json({
        message: "SKU já cadastrado para outra variação.",
      });
    }

    if (
      error.message.includes(
        "UNIQUE constraint failed: variacao_produto.id_produto",
      )
    ) {
      return res.status(409).json({
        message:
          "Já existe uma variação cadastrada para este produto com a mesma cor e tamanho..",
      });
    }

    return res.status(500).json({
      message: "Erro ao criar produto.",
    });
  }
}

async function editarProduto(req, res) {
  const idVariacao = Number(req.params.idVariacao);

  const nome = String(req.body.nome || "").trim();
  const cor = String(req.body.cor || "").trim();
  const tamanho = String(req.body.tamanho || "").trim();
  const sku = String(req.body.sku || "")
    .trim()
    .toUpperCase();

  const precoRaw = req.body.preco;

  const precoTexto =
    precoRaw === undefined || precoRaw === null ? "" : String(precoRaw).trim();

  const preco = Number(precoTexto);

  const quantidadeFoiInformada = Object.prototype.hasOwnProperty.call(
    req.body,
    "quantidade",
  );

  const quantidadeInformada = quantidadeFoiInformada
    ? Number(req.body.quantidade)
    : null;

  const estoqueMinRaw = req.body.estoque_min;

  const estoqueMin =
    estoqueMinRaw === undefined ||
    estoqueMinRaw === null ||
    estoqueMinRaw === ""
      ? null
      : Number(estoqueMinRaw);

  try {
    if (!Number.isInteger(idVariacao) || idVariacao <= 0) {
      return res.status(400).json({
        message: "ID da variação é inválido.",
      });
    }

    const produtoAtual = await all(
      `
        SELECT
          p.id_produto,
          p.nome,
          vp.id_variacao,
          vp.cor,
          vp.tamanho,
          vp.sku,
          vp.preco,
          e.quantidade,
          e.estoque_min
        FROM variacao_produto vp
        INNER JOIN produto p
          ON p.id_produto = vp.id_produto
        INNER JOIN estoque e
          ON e.id_variacao = vp.id_variacao
        WHERE vp.id_variacao = ?
      `,
      [idVariacao],
    );

    if (produtoAtual.length === 0) {
      return res.status(404).json({
        message: "Produto não encontrado para edição.",
      });
    }

    const produtoEncontrado = produtoAtual[0];

    if (!nomeProdutoValido(nome)) {
      return res.status(400).json({
        message: "Informe um nome de produto válido.",
      });
    }

    if (!corVariacaoValida(cor)) {
      return res.status(400).json({
        message: "Informe uma cor válida para a variação.",
      });
    }

    if (!tamanho) {
      return res.status(400).json({
        message: "Tamanho da variação é obrigatório.",
      });
    }

    if (!sku) {
      return res.status(400).json({
        message: "SKU da variação é obrigatório.",
      });
    }

    const precoValido = /^\d+(\.\d{1,2})?$/.test(precoTexto);

    if (!precoTexto || !precoValido || Number.isNaN(preco) || preco <= 0) {
      return res.status(400).json({
        message: "Informe um preço válido para a variação.",
      });
    }

    if (estoqueMin === null) {
      return res.status(400).json({
        message: "Estoque mínimo é obrigatório.",
      });
    }

    if (!Number.isInteger(estoqueMin) || estoqueMin < 10) {
      return res.status(400).json({
        message:
          "Estoque mínimo deve ser um número inteiro maior ou igual a 10.",
      });
    }

    const quantidadeAtual = Number(produtoEncontrado.quantidade);

    if (
      quantidadeFoiInformada &&
      (!Number.isInteger(quantidadeInformada) || quantidadeInformada < 0)
    ) {
      return res.status(400).json({
        message: "Quantidade de estoque inválida.",
      });
    }

    if (quantidadeFoiInformada && quantidadeInformada !== quantidadeAtual) {
      return res.status(409).json({
        message:
          "O saldo de estoque não pode ser alterado pela edição. Use a tela de Movimentações.",
      });
    }

    const skuExistente = await all(
      `
        SELECT id_variacao
        FROM variacao_produto
        WHERE sku = ?
          AND id_variacao <> ?
      `,
      [sku, idVariacao],
    );

    if (skuExistente.length > 0) {
      return res.status(409).json({
        message: "SKU já cadastrado para outra variação.",
      });
    }

    const corNormalizada = normalizarTexto(cor);
    const tamanhoNormalizado = normalizarTexto(tamanho);

    const variacaoDuplicada = await all(
      `
        SELECT
          p.id_produto,
          p.nome,
          vp.id_variacao,
          vp.cor_normalizada,
          vp.tamanho_normalizado
        FROM produto p
        INNER JOIN variacao_produto vp
          ON vp.id_produto = p.id_produto
        WHERE vp.id_variacao <> ?
      `,
      [idVariacao],
    );

    const existeMesmaVariacao = variacaoDuplicada.find((item) => {
      return (
        normalizarTexto(item.nome) === normalizarTexto(nome) &&
        item.cor_normalizada === corNormalizada &&
        item.tamanho_normalizado === tamanhoNormalizado
      );
    });

    if (existeMesmaVariacao) {
      return res.status(409).json({
        message:
          "Já existe uma variação cadastrada para este produto com a mesma cor e tamanho.",
      });
    }

    await run("BEGIN TRANSACTION");

    try {
      await run(
        `
          UPDATE produto
          SET nome = ?
          WHERE id_produto = ?
        `,
        [nome, produtoEncontrado.id_produto],
      );

      await run(
        `
          UPDATE variacao_produto
          SET
            cor = ?,
            tamanho = ?,
            cor_normalizada = ?,
            tamanho_normalizado = ?,
            sku = ?,
            preco = ?
          WHERE id_variacao = ?
        `,
        [
          cor,
          tamanho,
          corNormalizada,
          tamanhoNormalizado,
          sku,
          preco,
          idVariacao,
        ],
      );

      await run(
        `
       UPDATE estoque
       SET
      estoque_min = ?,
      atualizado_em = datetime('now','localtime')
    WHERE id_variacao = ?
  `,
        [estoqueMin, idVariacao],
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
          "PRODUTO_EDITADO",
          `variacao:${idVariacao}`,
          JSON.stringify({
            id_produto: produtoEncontrado.id_produto,
            id_variacao: idVariacao,
            antes: {
              nome: produtoEncontrado.nome,
              cor: produtoEncontrado.cor,
              tamanho: produtoEncontrado.tamanho,
              sku: produtoEncontrado.sku,
              preco: produtoEncontrado.preco,
              quantidade: produtoEncontrado.quantidade,
              estoque_min: produtoEncontrado.estoque_min,
            },
            depois: {
              nome,
              cor,
              tamanho,
              sku,
              preco,
              quantidade: quantidadeAtual,
              estoque_min: estoqueMin,
            },
          }),
        ],
      );

      await run("COMMIT");

      return res.json({
        message: "Alteração salva com sucesso.",
        produto: {
          id_produto: produtoEncontrado.id_produto,
          nome,
          ativo: 1,
        },
        variacao: {
          id_variacao: idVariacao,
          cor,
          tamanho,
          sku,
          preco,
          ativo: 1,
        },
        estoque: {
          quantidade: quantidadeAtual,
          estoque_min: estoqueMin,
        },
      });
    } catch (error) {
      await run("ROLLBACK");
      throw error;
    }
  } catch (error) {
    console.error("[PRODUTOS] erro ao editar:", error.message);

    if (
      error.message.includes("UNIQUE constraint failed: variacao_produto.sku")
    ) {
      return res.status(409).json({
        message: "SKU já cadastrado para outra variação.",
      });
    }

    return res.status(500).json({
      message: "Erro ao editar produto.",
    });
  }
}

async function deletarProduto(req, res) {
  const idVariacao = Number(req.params.idVariacao);

  try {
    if (!Number.isInteger(idVariacao) || idVariacao <= 0) {
      return res.status(400).json({
        message: "ID da variação é inválido.",
      });
    }

    const variacaoAtual = await all(
      `
        SELECT
          p.id_produto,
          p.nome,
          p.ativo AS produto_ativo,
          vp.id_variacao,
          vp.ativo AS variacao_ativa,
          vp.cor,
          vp.tamanho,
          vp.sku,
          e.quantidade,
          e.estoque_min
        FROM variacao_produto vp
        INNER JOIN produto p
          ON p.id_produto = vp.id_produto
        LEFT JOIN estoque e
          ON e.id_variacao = vp.id_variacao
        WHERE vp.id_variacao = ?
      `,
      [idVariacao],
    );

    if (variacaoAtual.length === 0) {
      return res.status(404).json({
        message: "Variação não encontrada para exclusão.",
      });
    }

    const variacaoEncontrada = variacaoAtual[0];

    if (Number(variacaoEncontrada.produto_ativo) === 0) {
      return res.status(409).json({
        message: "Produto de origem está inativo.",
      });
    }

    if (Number(variacaoEncontrada.variacao_ativa) === 0) {
      return res.status(409).json({
        message: "Variação já está excluída.",
      });
    }

    await run("BEGIN TRANSACTION");

    try {
      const resultadoAtualizacao = await run(
        `
          UPDATE variacao_produto
          SET ativo = 0
          WHERE id_variacao = ?
            AND ativo = 1
        `,
        [idVariacao],
      );

      if (resultadoAtualizacao.changes !== 1) {
        throw new Error("Nenhuma variação foi inativada.");
      }

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
          "VARIACAO_INATIVADA",
          `variacao:${idVariacao}`,
          JSON.stringify({
            id_produto: variacaoEncontrada.id_produto,
            id_variacao: idVariacao,
            nome: variacaoEncontrada.nome,
            cor: variacaoEncontrada.cor,
            tamanho: variacaoEncontrada.tamanho,
            sku: variacaoEncontrada.sku,
            quantidade: variacaoEncontrada.quantidade,
            estoque_min: variacaoEncontrada.estoque_min,
          }),
        ],
      );

      await run("COMMIT");

      return res.json({
        message: "Variação excluída com sucesso.",
        produto: {
          id_produto: variacaoEncontrada.id_produto,
          nome: variacaoEncontrada.nome,
          ativo: Number(variacaoEncontrada.produto_ativo),
        },
        variacao: {
          id_variacao: idVariacao,
          sku: variacaoEncontrada.sku,
          ativo: 0,
        },
      });
    } catch (error) {
      await run("ROLLBACK");
      throw error;
    }
  } catch (error) {
    console.error("[VARIACOES] erro ao excluir:", error);

    return res.status(500).json({
      message: "Erro ao excluir variação.",
    });
  }
}

async function excluirProdutosEmMassa(req, res) {
  const { idsVariacao } = req.body;

  try {
    if (!Array.isArray(idsVariacao) || idsVariacao.length === 0) {
      return res.status(400).json({
        message: "Informe uma lista de IDs de variação.",
      });
    }

    const idsValidos = [
      ...new Set(
        idsVariacao.map(Number).filter((id) => Number.isInteger(id) && id > 0),
      ),
    ];

    if (idsValidos.length === 0) {
      return res.status(400).json({
        message: "Nenhum ID de variação válido foi informado.",
      });
    }

    const placeholders = idsValidos.map(() => "?").join(",");

    const variacoesEncontradas = await all(
      `
        SELECT
          p.id_produto,
          p.nome,
          vp.id_variacao,
          vp.sku,
          vp.ativo AS variacao_ativa
        FROM variacao_produto vp
        INNER JOIN produto p
          ON p.id_produto = vp.id_produto
        WHERE vp.id_variacao IN (${placeholders})
          AND vp.ativo = 1
      `,
      idsValidos,
    );

    if (variacoesEncontradas.length === 0) {
      return res.status(404).json({
        message: "Nenhuma variação ativa encontrada para exclusão.",
      });
    }

    const idsProdutos = [
      ...new Set(variacoesEncontradas.map((item) => item.id_produto)),
    ];

    const placeholdersProdutos = idsProdutos.map(() => "?").join(",");

    await run("BEGIN TRANSACTION");

    try {
      const resultadoVariacoes = await run(
        `
          UPDATE variacao_produto
          SET ativo = 0
          WHERE id_variacao IN (${placeholders})
            AND ativo = 1
        `,
        idsValidos,
      );

      /*
       * Produto pai só fica inativo quando não possuir
       * nenhuma outra variação ativa.
       */
      await run(
        `
          UPDATE produto
          SET ativo = 0
          WHERE id_produto IN (${placeholdersProdutos})
            AND NOT EXISTS (
              SELECT 1
              FROM variacao_produto vp
              WHERE vp.id_produto = produto.id_produto
                AND vp.ativo = 1
            )
        `,
        idsProdutos,
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
          "VARIACOES_INATIVADAS_EM_MASSA",
          "variacao_produto",
          JSON.stringify({
            total: resultadoVariacoes.changes,
            variacoes: variacoesEncontradas,
          }),
        ],
      );

      await run("COMMIT");

      return res.json({
        message: "Variações excluídas com sucesso.",
        total: resultadoVariacoes.changes,
        variacoes: variacoesEncontradas,
      });
    } catch (error) {
      await run("ROLLBACK");
      throw error;
    }
  } catch (error) {
    console.error("[VARIACOES] erro ao excluir em massa:", error);

    return res.status(500).json({
      message: "Erro ao excluir variações em massa.",
    });
  }
}

module.exports = {
  listarProdutos,
  criarProduto,
  editarProduto,
  deletarProduto,
  excluirProdutosEmMassa,
};
