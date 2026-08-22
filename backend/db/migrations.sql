PRAGMA foreign_keys = ON;

-- =========================================================
-- VIEW: ESTOQUE DETALHADO
-- =========================================================

DROP VIEW IF EXISTS vw_estoque_detalhado;

CREATE VIEW vw_estoque_detalhado AS
SELECT
  p.id_produto,
  p.nome AS produto,
  p.descricao,
  p.ativo AS produto_ativo,

  vp.id_variacao,
  vp.cor,
  vp.tamanho,
  vp.sku,
  vp.preco,
  vp.ativo AS variacao_ativa,

  COALESCE(e.quantidade, 0) AS quantidade,
  COALESCE(e.estoque_min, 10) AS estoque_min,
  e.atualizado_em,

  CASE
    WHEN COALESCE(e.quantidade, 0) <= 0
      THEN 'ESGOTADO'
    WHEN COALESCE(e.quantidade, 0) <= CAST(COALESCE(e.estoque_min, 10) * 0.3 AS INTEGER)
      THEN 'CRITICO'
    WHEN COALESCE(e.quantidade, 0) <= COALESCE(e.estoque_min, 10)
      THEN 'ATENCAO'
    ELSE 'DISPONIVEL'
  END AS status
FROM variacao_produto vp
JOIN produto p
  ON p.id_produto = vp.id_produto
LEFT JOIN estoque e
  ON e.id_variacao = vp.id_variacao;

-- =========================================================
-- VIEW: ALERTAS DE ESTOQUE
-- =========================================================

DROP VIEW IF EXISTS vw_alertas_estoque;

CREATE VIEW vw_alertas_estoque AS
SELECT
  id_produto,
  produto,
  id_variacao,
  cor,
  tamanho,
  sku,
  quantidade,
  estoque_min,
  status
FROM vw_estoque_detalhado
WHERE produto_ativo = 1
  AND variacao_ativa = 1
  AND status IN ('ESGOTADO', 'CRITICO', 'ATENCAO')
ORDER BY
  CASE status
    WHEN 'ESGOTADO' THEN 1
    WHEN 'CRITICO' THEN 2
    WHEN 'ATENCAO' THEN 3
    ELSE 4
  END,
  produto,
  cor,
  tamanho;

-- =========================================================
-- VIEW: DASHBOARD ESTOQUE POR PRODUTO
-- =========================================================

DROP VIEW IF EXISTS vw_dashboard_estoque_por_produto;

CREATE VIEW vw_dashboard_estoque_por_produto AS
SELECT
  p.id_produto,
  p.nome AS produto,
  COUNT(vp.id_variacao) AS total_variacoes,
  COALESCE(SUM(e.quantidade), 0) AS quantidade_total
FROM produto p
JOIN variacao_produto vp
  ON vp.id_produto = p.id_produto
LEFT JOIN estoque e
  ON e.id_variacao = vp.id_variacao
WHERE p.ativo = 1
  AND vp.ativo = 1
GROUP BY
  p.id_produto,
  p.nome;

-- =========================================================
-- VIEW: MOVIMENTAÇÃO DETALHADA
-- =========================================================

DROP VIEW IF EXISTS vw_movimentacao_detalhada;

CREATE VIEW vw_movimentacao_detalhada AS
SELECT
  me.id_movimentacao,
  me.criado_em,
  me.tipo,
  me.quantidade,
  me.observacao,

  vp.id_variacao,
  vp.sku,
  vp.cor,
  vp.tamanho,

  p.id_produto,
  p.nome AS produto
FROM movimentacao_estoque me
JOIN variacao_produto vp
  ON vp.id_variacao = me.id_variacao
JOIN produto p
  ON p.id_produto = vp.id_produto
ORDER BY datetime(me.criado_em) DESC;