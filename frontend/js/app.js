const API_BASE_URL = "http://localhost:3001";

const motivosPorTipo = {
  ENTRADA: [
    { value: "REPOSICAO", label: "Reposição de estoque" },
    { value: "COMPRA", label: "Compra" },
    { value: "DEVOLUCAO", label: "Devolução" },
  ],
  SAIDA: [
    { value: "VENDA", label: "Venda" },
    { value: "PERDA", label: "Perda" },
    { value: "RETIRADA_OPERACIONAL", label: "Retirada operacional" },
  ],
  AJUSTE: [
    { value: "CORRECAO", label: "Correção de saldo" },
    { value: "INVENTARIO", label: "Inventário" },
  ],
};

let produtos = [];
let movimentacoes = [];
let dashboardDados = null;
let produtoSelecionado = null;
let produtoEmEdicao = null;
let movimentacaoConcluida = false;
let filtrosAlertasInicializados = false;

const conteudoTopbarPorPagina = {
  dashboard: {
    titulo: "Dashboard de Estoque",
    subtitulo: "Visão geral operacional do módulo",
  },
  estoque: {
    titulo: "Consultar Estoque",
    subtitulo: "Consulta de produtos, SKUs e saldos disponíveis",
  },
  produtos: {
    titulo: "Cadastrar Produto",
    subtitulo: "Cadastro de produtos, variações e estoque inicial",
  },
  movimentacoes: {
    titulo: "Movimentações",
    subtitulo: "Registro de entradas, saídas e ajustes de estoque",
  },
  historico: {
    titulo: "Histórico",
    subtitulo: "Consulta de movimentações registradas no estoque",
  },
  alertas: {
    titulo: "Alertas",
    subtitulo: "Acompanhamento de itens em atenção, críticos e esgotados",
  },
};

document.addEventListener("DOMContentLoaded", async () => {
  inicializarNavegacao();
  inicializarBuscaEstoque();
  inicializarMovimentacao();
  inicializarModalResultado();
  inicializarFiltrosHistorico();
  inicializarAtualizacaoHistorico();
  inicializarCadastroProduto();
  inicializarDatePickers();

  atualizarTopbarPagina("dashboard");

  await carregarDadosIniciais();
});

/* =========================
   Inicialização
========================= */

async function carregarDadosIniciais() {
  try {
    atualizarStatusApi("verificando");

    const [estoqueApi, movimentacoesApi] = await Promise.all([
      buscarEstoqueApi(),
      buscarMovimentacoesApi(),
    ]);

    produtos = estoqueApi.map(mapearProdutoApi);
    movimentacoes = movimentacoesApi.map(mapearMovimentacaoApi);

    try {
      dashboardDados = await buscarDashboardApi();
    } catch (erroDashboard) {
      console.error("[FRONT] erro ao carregar dashboard:", erroDashboard);

      dashboardDados = null;
    }

    preencherFiltrosAlertas();
    iniciarFiltrosAlertas();
    renderizarTudo();

    atualizarStatusApi("online");
  } catch (erro) {
    console.error("[FRONT] erro ao carregar dados iniciais:", erro);

    produtos = [];
    movimentacoes = [];
    dashboardDados = null;

    renderizarTudo();
    atualizarStatusApi("offline");

    exibirFeedback(
      "Não foi possível carregar os dados da API. Verifique se o backend está rodando.",
      "error",
    );
  }
}

function inicializarDatePickers() {
  const botoesCalendario = document.querySelectorAll(".date-picker-button");

  botoesCalendario.forEach((botao) => {
    botao.addEventListener("click", () => {
      const targetId = botao.dataset.dateTarget;
      const input = document.getElementById(targetId);

      if (!input) return;

      if (typeof input.showPicker === "function") {
        input.showPicker();
        return;
      }

      input.focus();
      input.click();
    });
  });
}

function renderizarTudo() {
  renderizarDashboard();
  renderizarTabelaEstoque(produtos);
  renderizarHistorico();
  renderizarAlertas();
}

/* =========================
   API
========================= */

async function buscarEstoqueApi() {
  const resposta = await fetch(`${API_BASE_URL}/estoque`);
  const dados = await resposta.json();

  if (!resposta.ok) {
    throw new Error(dados.message || "Erro ao buscar estoque.");
  }

  return dados;
}

async function buscarMovimentacoesApi() {
  const resposta = await fetch(`${API_BASE_URL}/estoque/movimentacoes`);
  const dados = await resposta.json();

  if (!resposta.ok) {
    throw new Error(dados.message || "Erro ao buscar movimentações.");
  }

  return dados;
}

async function buscarDashboardApi() {
  const resposta = await fetch(`${API_BASE_URL}/dashboard`);
  const dados = await resposta.json();

  if (!resposta.ok) {
    throw new Error(dados.erro || dados.message || "Erro ao buscar dashboard.");
  }

  return dados;
}

async function registrarMovimentacaoApi(payload) {
  const resposta = await fetch(`${API_BASE_URL}/estoque/movimentacoes`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  const dados = await resposta.json();

  if (!resposta.ok) {
    throw new Error(
      dados.erro || dados.message || "Erro ao registrar movimentação.",
    );
  }

  return dados;
}

async function cadastrarProdutoApi(payload) {
  const resposta = await fetch(`${API_BASE_URL}/produtos`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  const dados = await resposta.json();

  if (!resposta.ok) {
    throw new Error(dados.message || "Erro ao cadastrar produto.");
  }

  return dados;
}

async function editarProdutoApi(idVariacao, payload) {
  const resposta = await fetch(`${API_BASE_URL}/produtos/${idVariacao}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  const dados = await resposta.json();

  if (!resposta.ok) {
    throw new Error(dados.message || "Erro ao editar produto.");
  }

  return dados;
}

async function deletarProdutoApi(idVariacao) {
  const resposta = await fetch(`${API_BASE_URL}/produtos/${idVariacao}`, {
    method: "DELETE",
  });

  const dados = await resposta.json();

  if (!resposta.ok) {
    throw new Error(dados.message || "Erro ao excluir produto.");
  }

  return dados;
}

/* =========================
   Mapeamento API → Front
========================= */

function mapearProdutoApi(item) {
  return {
    id: item.id_produto,
    idVariacao: item.id_variacao,
    nome: item.produto,
    sku: item.sku,
    variacao: `${item.cor} / ${item.tamanho}`,
    cor: item.cor,
    tamanho: item.tamanho,
    preco: Number(item.preco),
    quantidade: Number(item.quantidade),
    estoqueMinimo: Number(item.estoque_min),
    statusApi: item.status,
  };
}

function mapearMovimentacaoApi(item) {
  return {
    produto: item.produto,
    sku: item.sku,
    tipo: item.tipo,
    quantidade: Number(item.quantidade),
    motivo: formatarMotivo(item.observacao),
    data: formatarDataHoraParaTela(item.criado_em),
  };
}

/* =========================
   Navegação
========================= */

function inicializarNavegacao() {
  const botoesMenu = document.querySelectorAll(".menu-button");

  botoesMenu.forEach((botao) => {
    botao.addEventListener("click", () => {
      navegarParaPagina(botao.dataset.page);
    });
  });
}

function navegarParaPagina(pagina) {
  const botoesMenu = document.querySelectorAll(".menu-button");
  const secoes = document.querySelectorAll(".page-section");

  botoesMenu.forEach((botao) => {
    botao.classList.toggle("active", botao.dataset.page === pagina);
  });

  secoes.forEach((secao) => {
    secao.classList.remove("active");
  });

  const secaoAtiva = document.getElementById(`page-${pagina}`);

  if (secaoAtiva) {
    secaoAtiva.classList.add("active");
  }

  atualizarTopbarPagina(pagina);
}

function atualizarTopbarPagina(pagina) {
  const topbarPageTitle = document.getElementById("topbarPageTitle");
  const topbarPageSubtitle = document.getElementById("topbarPageSubtitle");
  const conteudo = conteudoTopbarPorPagina[pagina];

  if (!topbarPageTitle || !topbarPageSubtitle || !conteudo) return;

  topbarPageTitle.textContent = conteudo.titulo;
  topbarPageSubtitle.textContent = conteudo.subtitulo;
}

function atualizarStatusApi(status) {
  const apiStatus = document.getElementById("apiStatus");

  if (!apiStatus) return;

  apiStatus.classList.remove("status-online", "status-out", "status-warning");

  if (status === "online") {
    apiStatus.textContent = "API: online";
    apiStatus.classList.add("status-online");
    return;
  }

  if (status === "offline") {
    apiStatus.textContent = "API: offline";
    apiStatus.classList.add("status-out");
    return;
  }

  apiStatus.textContent = "API: verificando...";
  apiStatus.classList.add("status-warning");
}

/* =========================
   Renderizações
========================= */

function obterStatusProduto(produto) {
  const status = String(produto.statusApi || "").toUpperCase();

  const statusPorTipo = {
    ESGOTADO: {
      texto: "Esgotado",
      classe: "status-out",
    },
    CRITICO: {
      texto: "Crítico",
      classe: "status-critical",
    },
    ATENCAO: {
      texto: "Atenção",
      classe: "status-warning",
    },
    DISPONIVEL: {
      texto: "Disponível",
      classe: "status-ok",
    },
  };

  return (
    statusPorTipo[status] || {
      texto: "Indefinido",
      classe: "status-warning",
    }
  );
}

function renderizarDashboard() {
  const totalProdutos = document.getElementById("totalProdutos");
  const totalCriticos = document.getElementById("totalCriticos");
  const totalEsgotados = document.getElementById("totalEsgotados");
  const totalMovimentacoesHoje = document.getElementById(
    "totalMovimentacoesHoje",
  );
  const totalEntradasHoje = document.getElementById("totalEntradasHoje");
  const totalSaidasHoje = document.getElementById("totalSaidasHoje");
  const totalAjustesHoje = document.getElementById("totalAjustesHoje");
  const dashboardMovimentacoes = document.getElementById(
    "dashboardMovimentacoes",
  );
  const btnVerHistorico = document.getElementById("btnVerHistorico");

  const cards = dashboardDados?.cards;
  const movimentacoesHoje = dashboardDados?.movimentacoes_hoje;

  if (totalProdutos) {
    totalProdutos.textContent = cards?.total_variacoes ?? "—";
  }

  if (totalCriticos) {
    totalCriticos.textContent = cards?.itens_criticos ?? "—";
  }

  if (totalEsgotados) {
    totalEsgotados.textContent = cards?.itens_esgotados ?? "—";
  }

  if (totalMovimentacoesHoje) {
    totalMovimentacoesHoje.textContent = movimentacoesHoje?.total ?? "—";
  }

  if (totalEntradasHoje) {
    totalEntradasHoje.textContent = movimentacoesHoje?.entradas ?? "—";
  }

  if (totalSaidasHoje) {
    totalSaidasHoje.textContent = movimentacoesHoje?.saidas ?? "—";
  }

  if (totalAjustesHoje) {
    totalAjustesHoje.textContent = movimentacoesHoje?.ajustes ?? "—";
  }

  if (btnVerHistorico) {
    btnVerHistorico.onclick = () => {
      navegarParaPagina("historico");
    };
  }

  if (!dashboardMovimentacoes) return;

  const ultimasMovimentacoes = Array.isArray(
    dashboardDados?.ultimas_movimentacoes,
  )
    ? dashboardDados.ultimas_movimentacoes.map(mapearMovimentacaoApi)
    : movimentacoes.slice(0, 5);

  if (ultimasMovimentacoes.length === 0) {
    dashboardMovimentacoes.innerHTML = `
      <tr>
        <td colspan="5" class="empty-row">Nenhuma movimentação carregada.</td>
      </tr>
    `;
    return;
  }

  dashboardMovimentacoes.innerHTML = ultimasMovimentacoes
    .map(
      (movimentacao) => `
        <tr>
          <td>
            <div class="table-product">
              <strong>${movimentacao.produto}</strong>
            </div>
          </td>

          <td>
            <span class="sku-text">${movimentacao.sku}</span>
          </td>

          <td>
            <span class="badge ${movimentacao.tipo.toLowerCase()}">
              ${formatarTipoMovimentacao(movimentacao.tipo)}
            </span>
          </td>

          <td>${movimentacao.quantidade}</td>

          <td>${movimentacao.data}</td>
        </tr>
      `,
    )
    .join("");
}

function renderizarTabelaEstoque(listaProdutos) {
  const estoqueTabela = document.getElementById("estoqueTabela");

  if (!estoqueTabela) return;

  if (listaProdutos.length === 0) {
    estoqueTabela.innerHTML = `
      <tr>
        <td colspan="7" class="empty-row">Nenhum produto encontrado.</td>
      </tr>
    `;
    return;
  }

  estoqueTabela.innerHTML = listaProdutos
    .map((produto) => {
      const status = obterStatusProduto(produto);

      return `
        <tr>
          <td>${produto.nome}</td>
          <td>${produto.sku}</td>
          <td>${produto.variacao}</td>
          <td>${produto.quantidade}</td>
          <td>${produto.estoqueMinimo}</td>
          <td>
            <span class="stock-status ${status.classe}">
              ${status.texto}
            </span>
          </td>
           <td>
  <div class="table-actions">
    <button
      type="button"
      class="button-secondary btn-editar-produto"
      data-testid="btn-editar-produto"
      data-id-variacao="${produto.idVariacao}"
    >
      Editar
    </button>

    <button
      type="button"
      class="button-danger-outline btn-excluir-produto"
      data-testid="btn-excluir-produto"
      data-id-variacao="${produto.idVariacao}"
      data-produto-nome="${produto.nome}"
      data-produto-sku="${produto.sku}"
    >
      Excluir
    </button>
  </div>
</td>
        </tr>
      `;
    })
    .join("");
  inicializarBotoesEdicaoProduto();
  inicializarBotoesExclusaoProduto();
}

function inicializarBotoesEdicaoProduto() {
  const botoesEditar = document.querySelectorAll(".btn-editar-produto");

  botoesEditar.forEach((botao) => {
    botao.addEventListener("click", () => {
      const idVariacao = Number(botao.dataset.idVariacao);

      const produto = produtos.find((item) => item.idVariacao === idVariacao);

      if (!produto) {
        exibirFeedback("Produto não encontrado para edição.", "error");
        return;
      }

      preencherFormularioEdicaoProduto(produto);
    });
  });
}

function inicializarBotoesExclusaoProduto() {
  const botoesExcluir = document.querySelectorAll(".btn-excluir-produto");

  botoesExcluir.forEach((botao) => {
    botao.addEventListener("click", async () => {
      const idVariacao = Number(botao.dataset.idVariacao);
      const nomeProduto = botao.dataset.produtoNome || "produto";
      const skuProduto = botao.dataset.produtoSku || "";

      const confirmouExclusao = window.confirm(
        `Deseja excluir a variação "${skuProduto}" do produto "${nomeProduto}"?`,
      );

      if (!confirmouExclusao) return;

      await excluirProduto(idVariacao);
    });
  });
}

function preencherFormularioEdicaoProduto(produto) {
  produtoEmEdicao = produto;

  document.getElementById("produtoNome").value = produto.nome;
  document.getElementById("produtoCor").value = produto.cor;
  document.getElementById("produtoTamanho").value = produto.tamanho;
  document.getElementById("produtoSku").value = produto.sku;
  document.getElementById("produtoPreco").value = produto.preco || "";
  document.getElementById("produtoQuantidade").value = produto.quantidade;
  document.getElementById("produtoEstoqueMinimo").value = produto.estoqueMinimo;

  atualizarModoFormularioProduto("edicao");

  limparFeedback();

  navegarParaPagina("produtos");
}

function renderizarHistorico() {
  const historicoTabela = document.getElementById("historicoTabela");

  if (!historicoTabela) return;

  const movimentacoesFiltradas = filtrarMovimentacoesHistorico();

  if (movimentacoesFiltradas.length === 0) {
    historicoTabela.innerHTML = `
      <tr>
        <td colspan="6" class="empty-row">
          Nenhuma movimentação encontrada para os filtros aplicados.
        </td>
      </tr>
    `;
    return;
  }

  historicoTabela.innerHTML = movimentacoesFiltradas
    .map(
      (movimentacao) => `
        <tr>
          <td>${movimentacao.produto}</td>

          <td>
            <span class="sku-text">${movimentacao.sku}</span>
          </td>

          <td>
            <span class="badge ${movimentacao.tipo.toLowerCase()}">
              ${formatarTipoMovimentacao(movimentacao.tipo)}
            </span>
          </td>

          <td>${movimentacao.quantidade}</td>
          <td>${movimentacao.motivo}</td>
          <td>${movimentacao.data}</td>
        </tr>
      `,
    )
    .join("");
}

function filtrarMovimentacoesHistorico() {
  const busca = normalizarTexto(
    document.getElementById("historicoBusca")?.value || "",
  );

  const tipo = document.getElementById("historicoTipo")?.value || "";
  const dataInicial =
    document.getElementById("historicoDataInicial")?.value || "";
  const dataFinal = document.getElementById("historicoDataFinal")?.value || "";

  return movimentacoes.filter((movimentacao) => {
    const correspondeBusca =
      !busca ||
      normalizarTexto(movimentacao.produto).includes(busca) ||
      normalizarTexto(movimentacao.sku).includes(busca);

    const correspondeTipo = !tipo || movimentacao.tipo === tipo;

    const dataMovimentacao = obterDataISOParaFiltro(movimentacao.data);

    const correspondeDataInicial =
      !dataInicial || (dataMovimentacao && dataMovimentacao >= dataInicial);

    const correspondeDataFinal =
      !dataFinal || (dataMovimentacao && dataMovimentacao <= dataFinal);

    return (
      correspondeBusca &&
      correspondeTipo &&
      correspondeDataInicial &&
      correspondeDataFinal
    );
  });
}

function obterDataISOParaFiltro(dataFormatada) {
  if (!dataFormatada) return null;

  // Formato esperado na tela: dd/mm/aaaa hh:mm
  if (dataFormatada.includes("/")) {
    const [data] = dataFormatada.split(" ");
    const [dia, mes, ano] = data.split("/");

    if (!dia || !mes || !ano) return null;

    return `${ano}-${mes}-${dia}`;
  }

  // Fallback para formato técnico: aaaa-mm-dd
  if (dataFormatada.includes("-")) {
    return dataFormatada.slice(0, 10);
  }

  return null;
}

function inicializarFiltrosHistorico() {
  const historicoBusca = document.getElementById("historicoBusca");
  const historicoTipo = document.getElementById("historicoTipo");
  const historicoDataInicial = document.getElementById("historicoDataInicial");
  const historicoDataFinal = document.getElementById("historicoDataFinal");
  const btnLimparFiltros = document.getElementById("btnLimparFiltrosHistorico");

  const camposFiltro = [
    historicoBusca,
    historicoTipo,
    historicoDataInicial,
    historicoDataFinal,
  ];

  camposFiltro.forEach((campo) => {
    if (!campo) return;

    campo.addEventListener("input", renderizarHistorico);
    campo.addEventListener("change", renderizarHistorico);
  });

  if (btnLimparFiltros) {
    btnLimparFiltros.addEventListener("click", () => {
      if (historicoBusca) historicoBusca.value = "";
      if (historicoTipo) historicoTipo.value = "";
      if (historicoDataInicial) historicoDataInicial.value = "";
      if (historicoDataFinal) historicoDataFinal.value = "";

      renderizarHistorico();
    });
  }
}

function inicializarAtualizacaoHistorico() {
  const btnAtualizarHistorico = document.getElementById(
    "btnAtualizarHistorico",
  );

  if (!btnAtualizarHistorico) return;

  btnAtualizarHistorico.addEventListener("click", async () => {
    try {
      const movimentacoesApi = await buscarMovimentacoesApi();

      movimentacoes = movimentacoesApi.map(mapearMovimentacaoApi);

      renderizarHistorico();

      exibirFeedback("Histórico atualizado com sucesso.", "success");
    } catch (erro) {
      console.error("[FRONT] erro ao atualizar histórico:", erro);

      exibirFeedback("Erro ao atualizar histórico.", "error");
    }
  });
}

function normalizarTexto(valor) {
  return String(valor || "")
    .trim()
    .toLowerCase()
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

function obterStatusFiltroProduto(produto) {
  return String(produto.statusApi || "").toUpperCase();
}
function obterProdutosComAlerta() {
  return produtos.filter((produto) => {
    const status = obterStatusFiltroProduto(produto);
    return ["ATENCAO", "CRITICO", "ESGOTADO"].includes(status);
  });
}

function preencherFiltrosAlertas() {
  const corFiltro = document.getElementById("alertaCorFiltro");
  const tamanhoFiltro = document.getElementById("alertaTamanhoFiltro");

  if (!corFiltro || !tamanhoFiltro) return;

  const produtosComAlerta = obterProdutosComAlerta();

  const cores = [
    ...new Set(
      produtosComAlerta
        .map((produto) => produto.cor)
        .filter(Boolean)
        .sort((a, b) => a.localeCompare(b)),
    ),
  ];

  const tamanhos = [
    ...new Set(
      produtosComAlerta
        .map((produto) => produto.tamanho)
        .filter(Boolean)
        .sort((a, b) =>
          String(a).localeCompare(String(b), "pt-BR", { numeric: true }),
        ),
    ),
  ];

  corFiltro.innerHTML = `
    <option value="TODAS">Todas</option>
    ${cores
      .map(
        (cor) => `
          <option value="${cor}">${cor}</option>
        `,
      )
      .join("")}
  `;

  tamanhoFiltro.innerHTML = `
    <option value="TODOS">Todos</option>
    ${tamanhos
      .map(
        (tamanho) => `
          <option value="${tamanho}">${tamanho}</option>
        `,
      )
      .join("")}
  `;
}

function filtrarAlertas(produtosComAlerta) {
  const busca = normalizarTexto(document.getElementById("alertaBusca")?.value);
  const statusSelecionado =
    document.getElementById("alertaStatusFiltro")?.value || "TODOS";
  const corSelecionada =
    document.getElementById("alertaCorFiltro")?.value || "TODAS";
  const tamanhoSelecionado =
    document.getElementById("alertaTamanhoFiltro")?.value || "TODOS";

  return produtosComAlerta.filter((produto) => {
    const statusProduto = obterStatusFiltroProduto(produto);

    const textoBusca = normalizarTexto(
      [
        produto.nome,
        produto.sku,
        produto.cor,
        produto.tamanho,
        produto.variacao,
      ].join(" "),
    );

    const atendeBusca = !busca || textoBusca.includes(busca);

    const atendeStatus =
      statusSelecionado === "TODOS" || statusProduto === statusSelecionado;

    const atendeCor =
      corSelecionada === "TODAS" || produto.cor === corSelecionada;

    const atendeTamanho =
      tamanhoSelecionado === "TODOS" ||
      String(produto.tamanho) === String(tamanhoSelecionado);

    return atendeBusca && atendeStatus && atendeCor && atendeTamanho;
  });
}

function renderizarAlertas() {
  const alertasLista = document.getElementById("alertasLista");
  const alertasStatus = document.getElementById("alertasStatus");

  if (!alertasLista) return;

  const produtosComAlerta = obterProdutosComAlerta();
  const produtosFiltrados = filtrarAlertas(produtosComAlerta);

  if (alertasStatus) {
    alertasStatus.textContent =
      produtosFiltrados.length > 0
        ? `${produtosFiltrados.length} alerta(s) encontrado(s)`
        : "Nenhum alerta encontrado";
  }

  if (produtosComAlerta.length === 0) {
    alertasLista.innerHTML = `
      <div class="empty-state">Nenhuma variação em situação de alerta.</div>
    `;
    return;
  }

  if (produtosFiltrados.length === 0) {
    alertasLista.innerHTML = `
      <div class="empty-state">
        Nenhum alerta encontrado para os filtros selecionados.
      </div>
    `;
    return;
  }

  alertasLista.innerHTML = produtosFiltrados
    .map((produto) => {
      const status = obterStatusProduto(produto);

      return `
        <article class="alert-card">
          <strong>${produto.nome}</strong>
          <span>${produto.sku} • ${produto.variacao}</span>
          <p>
            Estoque atual: ${produto.quantidade} |
            Estoque mínimo: ${produto.estoqueMinimo}
          </p>
          <span class="stock-status ${status.classe}">
            ${status.texto}
          </span>
        </article>
      `;
    })
    .join("");
}

function iniciarFiltrosAlertas() {
  if (filtrosAlertasInicializados) return;

  const busca = document.getElementById("alertaBusca");
  const status = document.getElementById("alertaStatusFiltro");
  const cor = document.getElementById("alertaCorFiltro");
  const tamanho = document.getElementById("alertaTamanhoFiltro");
  const limpar = document.getElementById("limparFiltrosAlertas");

  if (!busca || !status || !cor || !tamanho || !limpar) return;

  busca.addEventListener("input", renderizarAlertas);
  status.addEventListener("change", renderizarAlertas);
  cor.addEventListener("change", renderizarAlertas);
  tamanho.addEventListener("change", renderizarAlertas);

  limpar.addEventListener("click", () => {
    busca.value = "";
    status.value = "TODOS";
    cor.value = "TODAS";
    tamanho.value = "TODOS";

    renderizarAlertas();
  });

  filtrosAlertasInicializados = true;
}

/* =========================
   Consulta de estoque
========================= */

function inicializarBuscaEstoque() {
  const inputBusca = document.getElementById("estoqueBusca");
  const btnBuscar = document.getElementById("btnBuscarEstoque");

  if (!inputBusca || !btnBuscar) return;

  btnBuscar.addEventListener("click", () => {
    buscarProdutosEstoque(inputBusca.value);
  });

  inputBusca.addEventListener("input", () => {
    buscarProdutosEstoque(inputBusca.value);
  });
}

function buscarProdutosEstoque(termo) {
  const termoNormalizado = normalizarTexto(termo);

  const resultado = produtos.filter((produto) => {
    return (
      normalizarTexto(produto.nome).includes(termoNormalizado) ||
      normalizarTexto(produto.sku).includes(termoNormalizado) ||
      normalizarTexto(produto.variacao).includes(termoNormalizado)
    );
  });

  renderizarTabelaEstoque(resultado);
}

/* =========================
   Cadastro de produto
========================= */
function atualizarModoFormularioProduto(modo) {
  const tituloFormulario = document.querySelector(".product-create-card h3");
  const subtituloFormulario = document.querySelector(
    ".product-create-card .table-header p",
  );
  const btnCadastrarProduto = document.getElementById("btnCadastrarProduto");

  if (modo === "edicao") {
    if (tituloFormulario) {
      tituloFormulario.textContent = "Editar produto";
    }

    if (subtituloFormulario) {
      subtituloFormulario.textContent =
        "Altere os dados do produto, da variação e do estoque.";
    }

    if (btnCadastrarProduto) {
      btnCadastrarProduto.textContent = "Salvar alterações";
    }

    return;
  }

  if (tituloFormulario) {
    tituloFormulario.textContent = "Novo produto";
  }

  if (subtituloFormulario) {
    subtituloFormulario.textContent =
      "Cadastre o produto, a variação inicial e o saldo de estoque.";
  }

  if (btnCadastrarProduto) {
    btnCadastrarProduto.textContent = "Cadastrar produto";
  }
}

function inicializarCadastroProduto() {
  const btnCadastrarProduto = document.getElementById("btnCadastrarProduto");
  const btnLimparProduto = document.getElementById("btnLimparProduto");
  const produtoSku = document.getElementById("produtoSku");
  const produtoCor = document.getElementById("produtoCor");
  const produtoTamanho = document.getElementById("produtoTamanho");

  if (produtoSku) {
    produtoSku.addEventListener("input", () => {
      produtoSku.value = produtoSku.value.toUpperCase();
    });
  }

  if (produtoCor) {
    produtoCor.addEventListener("input", () => {
      produtoCor.value = produtoCor.value.toUpperCase();
    });
  }

  if (produtoTamanho) {
    produtoTamanho.addEventListener("input", () => {
      produtoTamanho.value = produtoTamanho.value.toUpperCase();
    });
  }

  if (btnCadastrarProduto) {
    btnCadastrarProduto.addEventListener("click", salvarProduto);
  }

  if (btnLimparProduto) {
    btnLimparProduto.addEventListener("click", limparFormularioProduto);
  }
}

async function salvarProduto() {
  if (produtoEmEdicao) {
    await editarProduto();
    return;
  }

  await cadastrarProduto();
}

async function cadastrarProduto() {
  limparFeedback();

  const validacao = validarFormularioProduto();

  if (!validacao.valido) {
    exibirFeedback(validacao.mensagem, "warning");
    return;
  }

  const payload = montarPayloadProduto();

  setBotaoCadastrarProdutoCarregando(true);

  try {
    const resposta = await cadastrarProdutoApi(payload);

    limparFormularioProduto();

    await carregarDadosIniciais();

    navegarParaPagina("estoque");

    exibirFeedback(
      resposta.message || "Produto cadastrado com sucesso.",
      "success",
    );
  } catch (erro) {
    exibirFeedback(
      erro.message || "Não foi possível cadastrar o produto.",
      "error",
    );
  } finally {
    setBotaoCadastrarProdutoCarregando(false);
  }
}

async function editarProduto() {
  limparFeedback();

  const validacao = validarFormularioProduto();

  if (!validacao.valido) {
    exibirFeedback(validacao.mensagem, "warning");
    return;
  }

  const payload = montarPayloadProduto();

  setBotaoCadastrarProdutoCarregando(true);

  try {
    const resposta = await editarProdutoApi(
      produtoEmEdicao.idVariacao,
      payload,
    );

    produtoEmEdicao = null;

    limparFormularioProduto();
    atualizarModoFormularioProduto("cadastro");

    await carregarDadosIniciais();

    navegarParaPagina("estoque");

    exibirFeedback(
      resposta.message || "Alteração salva com sucesso.",
      "success",
    );
  } catch (erro) {
    exibirFeedback(
      erro.message || "Não foi possível atualizar o produto.",
      "error",
    );
  } finally {
    setBotaoCadastrarProdutoCarregando(false);
  }
}

async function excluirProduto(idVariacao) {
  limparFeedback();

  if (!Number.isInteger(idVariacao) || idVariacao <= 0) {
    exibirFeedback("Produto inválido para exclusão.", "error");
    return;
  }

  try {
    const resposta = await deletarProdutoApi(idVariacao);

    if (
      produtoEmEdicao &&
      Number(produtoEmEdicao.idVariacao) === Number(idVariacao)
    ) {
      produtoEmEdicao = null;
      limparFormularioProduto();
      atualizarModoFormularioProduto("cadastro");
    }

    await carregarDadosIniciais();

    navegarParaPagina("estoque");

    exibirFeedback(
      resposta.message || "Produto excluído com sucesso.",
      "success",
    );
  } catch (erro) {
    exibirFeedback(
      erro.message || "Não foi possível excluir o produto.",
      "error",
    );
  }
}

function validarFormularioProduto() {
  const nome = document.getElementById("produtoNome")?.value.trim() || "";
  const cor = document.getElementById("produtoCor")?.value.trim() || "";
  const tamanho = document.getElementById("produtoTamanho")?.value.trim() || "";
  const sku = document.getElementById("produtoSku")?.value.trim() || "";
  const precoCampo =
    document.getElementById("produtoPreco")?.value.trim() || "";

  const preco = precoCampo === "" ? null : Number(precoCampo);
  const quantidade = Number(
    document.getElementById("produtoQuantidade")?.value || 0,
  );
  const estoqueMinimoCampo =
    document.getElementById("produtoEstoqueMinimo")?.value.trim() || "";

  const estoqueMinimo =
    estoqueMinimoCampo === "" ? null : Number(estoqueMinimoCampo);

  if (!nomeProdutoValido(nome)) {
    return {
      valido: false,
      mensagem: "Informe um nome de produto válido.",
    };
  }

  if (!corVariacaoValida(cor)) {
    return {
      valido: false,
      mensagem: "Informe uma cor válida para a variação.",
    };
  }

  if (!tamanho) {
    return {
      valido: false,
      mensagem: "Informe o tamanho da variação.",
    };
  }

  if (!sku) {
    return {
      valido: false,
      mensagem: "Informe um sku válido para a variação.",
    };
  }

  const precoValido = /^\d+(\.\d{1,2})?$/.test(precoCampo);

  if (preco === null || !precoValido || Number.isNaN(preco) || preco <= 0) {
    return {
      valido: false,
      mensagem: "Informe um preço válido para a variação.",
    };
  }

  if (estoqueMinimo === null) {
    return {
      valido: false,
      mensagem: "Informe o estoque mínimo.",
    };
  }

  if (!Number.isInteger(estoqueMinimo) || estoqueMinimo < 10) {
    return {
      valido: false,
      mensagem:
        "O estoque mínimo deve ser um número inteiro maior ou igual a 10.",
    };
  }

  return {
    valido: true,
  };
}

function montarPayloadProduto() {
  const nome = document.getElementById("produtoNome")?.value.trim() || "";
  const cor = document.getElementById("produtoCor")?.value.trim() || "";
  const tamanho = document.getElementById("produtoTamanho")?.value.trim() || "";
  const sku = document.getElementById("produtoSku")?.value.trim() || "";
  const precoCampo =
    document.getElementById("produtoPreco")?.value.trim() || "";

  const preco = precoCampo === "" ? null : Number(precoCampo);
  const quantidade = Number(
    document.getElementById("produtoQuantidade")?.value || 0,
  );
  const estoqueMinimoCampo =
    document.getElementById("produtoEstoqueMinimo")?.value.trim() || "";

  const estoqueMinimo =
    estoqueMinimoCampo === "" ? null : Number(estoqueMinimoCampo);

  return {
    nome,
    cor,
    tamanho,
    sku,
    preco,
    quantidade,
    estoque_min: estoqueMinimo,
  };
}

function limparFormularioProduto() {
  const campos = [
    "produtoNome",
    "produtoCor",
    "produtoTamanho",
    "produtoSku",
    "produtoPreco",
    "produtoQuantidade",
    "produtoEstoqueMinimo",
  ];

  campos.forEach((id) => {
    const campo = document.getElementById(id);

    if (!campo) return;

    if (id === "produtoQuantidade") {
      campo.value = "0";
      return;
    }

    if (id === "produtoEstoqueMinimo") {
      campo.value = "10";
      return;
    }

    campo.value = "";
  });
  atualizarModoFormularioProduto("cadastro");
  limparFeedback();
}

function setBotaoCadastrarProdutoCarregando(carregando) {
  const btnCadastrarProduto = document.getElementById("btnCadastrarProduto");

  if (!btnCadastrarProduto) return;

  btnCadastrarProduto.disabled = carregando;

  if (carregando) {
    btnCadastrarProduto.textContent = produtoEmEdicao
      ? "Salvando..."
      : "Cadastrando...";
    return;
  }

  btnCadastrarProduto.textContent = produtoEmEdicao
    ? "Salvar alterações"
    : "Cadastrar produto";
}

/* =========================
   Movimentação
========================= */

function inicializarMovimentacao() {
  const btnBuscarProdutoMov = document.getElementById("btnBuscarProdutoMov");
  const movBuscaProduto = document.getElementById("movBuscaProduto");
  const tipoMovimentacao = document.getElementById("tipoMovimentacao");
  const quantidadeMovimentacao = document.getElementById(
    "quantidadeMovimentacao",
  );
  const motivoMovimentacao = document.getElementById("motivoMovimentacao");
  const btnLimpar = document.getElementById("btnLimparMovimentacao");
  const btnConfirmar = document.getElementById("btnConfirmarMovimentacao");

  atualizarMotivosPorTipo("");
  atualizarLabelQuantidadePorTipo("");

  if (btnBuscarProdutoMov && movBuscaProduto) {
    btnBuscarProdutoMov.addEventListener("click", () => {
      selecionarProdutoParaMovimentacao(movBuscaProduto.value);
    });

    movBuscaProduto.addEventListener("keydown", (evento) => {
      if (evento.key === "Enter") {
        selecionarProdutoParaMovimentacao(movBuscaProduto.value);
      }
    });
  }

  if (tipoMovimentacao) {
    tipoMovimentacao.addEventListener("change", () => {
      atualizarMotivosPorTipo(tipoMovimentacao.value);
      atualizarLabelQuantidadePorTipo(tipoMovimentacao.value);
      atualizarResumoMovimentacao();
      limparFeedback();
    });
  }

  [quantidadeMovimentacao, motivoMovimentacao].forEach((campo) => {
    if (!campo) return;

    campo.addEventListener("input", () => {
      atualizarResumoMovimentacao();
      limparFeedback();
    });

    campo.addEventListener("change", () => {
      atualizarResumoMovimentacao();
      limparFeedback();
    });
  });

  if (btnLimpar) {
    btnLimpar.addEventListener("click", limparMovimentacao);
  }

  if (btnConfirmar) {
    btnConfirmar.addEventListener("click", confirmarMovimentacao);
  }
}

function atualizarMotivosPorTipo(tipo) {
  const motivoMovimentacao = document.getElementById("motivoMovimentacao");

  if (!motivoMovimentacao) return;

  if (!tipo) {
    motivoMovimentacao.innerHTML = `
      <option value="">Selecione o tipo primeiro...</option>
    `;
    motivoMovimentacao.disabled = true;
    return;
  }

  const motivos = motivosPorTipo[tipo] || [];

  motivoMovimentacao.disabled = false;

  motivoMovimentacao.innerHTML = `
    <option value="">Selecione...</option>
    ${motivos
      .map(
        (motivo) => `
          <option value="${motivo.value}">${motivo.label}</option>
        `,
      )
      .join("")}
  `;
}

function selecionarProdutoParaMovimentacao(termo) {
  const termoNormalizado = normalizarTexto(termo);

  limparFeedback();

  if (!termoNormalizado) {
    produtoSelecionado = null;
    renderizarProdutoSelecionado();
    atualizarResumoMovimentacao();

    exibirFeedback(
      "Informe nome, código ou SKU para buscar um produto.",
      "warning",
    );
    return;
  }

  const produtoEncontrado = encontrarProduto(termoNormalizado);

  if (!produtoEncontrado) {
    produtoSelecionado = null;
    renderizarProdutoSelecionado();
    atualizarResumoMovimentacao();

    exibirFeedback("Produto não encontrado.", "warning");
    return;
  }

  produtoSelecionado = produtoEncontrado;

  renderizarProdutoSelecionado();
  atualizarResumoMovimentacao();
}

function encontrarProduto(termoNormalizado) {
  return (
    produtos.find(
      (produto) => normalizarTexto(produto.sku) === termoNormalizado,
    ) ||
    produtos.find((produto) =>
      normalizarTexto(produto.sku).includes(termoNormalizado),
    ) ||
    produtos.find((produto) =>
      normalizarTexto(produto.nome).includes(termoNormalizado),
    ) ||
    produtos.find((produto) =>
      normalizarTexto(produto.variacao).includes(termoNormalizado),
    )
  );
}

function renderizarProdutoSelecionado() {
  const produtoSelecionadoBox = document.getElementById("produtoSelecionado");

  if (!produtoSelecionadoBox) return;

  if (!produtoSelecionado) {
    produtoSelecionadoBox.innerHTML = `
      <span>Nenhum produto selecionado.</span>
    `;
    return;
  }

  const status = obterStatusProduto(produtoSelecionado);

  produtoSelecionadoBox.innerHTML = `
    <strong>${produtoSelecionado.nome}</strong>
    <span>${produtoSelecionado.sku} • ${produtoSelecionado.variacao}</span>
    <span>
      Estoque atual: ${produtoSelecionado.quantidade} |
      Mínimo: ${produtoSelecionado.estoqueMinimo}
    </span>
    <span class="stock-status ${status.classe}">
      ${status.texto}
    </span>
  `;
}

function atualizarResumoMovimentacao() {
  const resumoProduto = document.getElementById("resumoProduto");
  const resumoTipo = document.getElementById("resumoTipo");
  const resumoQuantidade = document.getElementById("resumoQuantidade");
  const resumoMotivo = document.getElementById("resumoMotivo");
  const resumoNovoEstoque = document.getElementById("resumoNovoEstoque");

  const tipo = document.getElementById("tipoMovimentacao")?.value || "";
  const quantidade = Number(
    document.getElementById("quantidadeMovimentacao")?.value || 0,
  );
  const motivo = document.getElementById("motivoMovimentacao")?.value || "";

  if (resumoProduto) {
    resumoProduto.textContent = produtoSelecionado
      ? `${produtoSelecionado.nome} - ${produtoSelecionado.variacao}`
      : "—";
  }

  if (resumoTipo) {
    resumoTipo.textContent = tipo ? formatarTipoMovimentacao(tipo) : "—";
  }

  if (resumoQuantidade) {
    resumoQuantidade.textContent = quantidade;
  }

  if (resumoMotivo) {
    resumoMotivo.textContent = motivo ? formatarMotivo(motivo) : "—";
  }

  if (resumoNovoEstoque) {
    resumoNovoEstoque.textContent = calcularNovoEstoquePrevisto(
      tipo,
      quantidade,
    );
  }
}

function calcularNovoEstoquePrevisto(tipo, quantidade) {
  if (!produtoSelecionado || !tipo || quantidade <= 0) {
    return "—";
  }

  const estoqueAtual = Number(produtoSelecionado.quantidade);

  if (tipo === "ENTRADA") {
    return estoqueAtual + quantidade;
  }

  if (tipo === "SAIDA") {
    return estoqueAtual - quantidade;
  }

  if (tipo === "AJUSTE") {
    return quantidade;
  }

  return "—";
}

function atualizarLabelQuantidadePorTipo(tipo) {
  const quantidadeLabel = document.getElementById(
    "quantidadeMovimentacaoLabel",
  );
  const resumoQuantidadeLabel = document.getElementById(
    "resumoQuantidadeLabel",
  );

  let label = "Quantidade";
  let resumoLabel = "Quantidade:";

  if (tipo === "ENTRADA") {
    label = "Quantidade a adicionar";
    resumoLabel = "Quantidade a adicionar:";
  }

  if (tipo === "SAIDA") {
    label = "Quantidade a retirar";
    resumoLabel = "Quantidade a retirar:";
  }

  if (tipo === "AJUSTE") {
    label = "Novo saldo do estoque";
    resumoLabel = "Novo saldo informado:";
  }

  if (quantidadeLabel) {
    quantidadeLabel.textContent = label;
  }

  if (resumoQuantidadeLabel) {
    resumoQuantidadeLabel.textContent = resumoLabel;
  }
}

function inicializarModalResultado() {
  const btnFechar = document.getElementById("btnFecharModalResultado");
  const modal = document.getElementById("modalResultado");

  if (btnFechar) {
    btnFechar.addEventListener("click", fecharModalResultado);
  }

  if (modal) {
    modal.addEventListener("click", (evento) => {
      if (evento.target === modal) {
        fecharModalResultado();
      }
    });
  }
}

function abrirModalResultado({ titulo, mensagem, detalhes = [] }) {
  const modal = document.getElementById("modalResultado");
  const modalTitulo = document.getElementById("modalResultadoTitulo");
  const modalMensagem = document.getElementById("modalResultadoMensagem");
  const modalDetalhes = document.getElementById("modalResultadoDetalhes");

  if (!modal || !modalTitulo || !modalMensagem || !modalDetalhes) return;

  modalTitulo.textContent = titulo;
  modalMensagem.textContent = mensagem;

  modalDetalhes.innerHTML = detalhes
    .map(
      (item) => `
        <p>
          <span>${item.label}</span>
          <strong>${item.valor}</strong>
        </p>
      `,
    )
    .join("");

  modal.classList.add("show");
  modal.setAttribute("aria-hidden", "false");
}

function fecharModalResultado() {
  const modal = document.getElementById("modalResultado");

  if (!modal) return;

  modal.classList.remove("show");
  modal.setAttribute("aria-hidden", "true");

  limparMovimentacao();
  movimentacaoConcluida = false;
  setBotaoConfirmarCarregando(false);
}

async function confirmarMovimentacao() {
  limparFeedback();

  if (movimentacaoConcluida) {
    exibirFeedback(
      "Esta movimentação já foi registrada. Clique em Limpar para iniciar uma nova operação.",
      "warning",
    );
    return;
  }

  const validacao = validarFormularioMovimentacao();

  if (!validacao.valido) {
    exibirFeedback(validacao.mensagem, "warning");
    return;
  }

  const payload = montarPayloadMovimentacao();

  setBotaoConfirmarCarregando(true);

  try {
    const resposta = await registrarMovimentacaoApi(payload);

    try {
      await sincronizarDadosAposMovimentacao(resposta);
    } catch (erroSincronizacao) {
      console.error(
        "[FRONT] movimentação registrada, mas houve erro ao sincronizar a tela:",
        erroSincronizacao,
      );

      atualizarProdutoAposResposta(resposta);
      registrarMovimentacaoNoHistorico(resposta);

      renderizarTudo();
      renderizarProdutoSelecionado();
    }

    atualizarResumoComEstoqueAtual(resposta.estoque.atual);

    movimentacaoConcluida = true;

    abrirModalResultado({
      titulo: "Movimentação registrada com sucesso",
      mensagem: resposta.message,
      detalhes: [
        {
          label: "Produto",
          valor: `${resposta.produto.nome} - ${resposta.produto.variacao}`,
        },
        {
          label: "Tipo",
          valor: formatarTipoMovimentacao(resposta.movimentacao.tipo),
        },
        {
          label: "Quantidade",
          valor: resposta.movimentacao.quantidade,
        },
        {
          label: "Motivo",
          valor:
            resposta.movimentacao.motivo_descricao ||
            formatarMotivo(resposta.movimentacao.motivo),
        },
        {
          label: "Estoque anterior",
          valor: resposta.estoque.anterior,
        },
        {
          label: "Estoque atualizado",
          valor: resposta.estoque.atual,
        },
      ],
    });

    const btnConfirmar = document.getElementById("btnConfirmarMovimentacao");

    if (btnConfirmar) {
      btnConfirmar.disabled = true;
      btnConfirmar.textContent = "Movimentação registrada";
    }
  } catch (erro) {
    abrirModalResultado({
      titulo: "Movimentação não realizada",
      mensagem:
        erro.message || "Não foi possível registrar a movimentação de estoque.",
      detalhes: [
        {
          label: "Status",
          valor: "Erro",
        },
      ],
    });

    setBotaoConfirmarCarregando(false);
  }
}

function validarFormularioMovimentacao() {
  const tipo = document.getElementById("tipoMovimentacao")?.value || "";
  const quantidade = Number(
    document.getElementById("quantidadeMovimentacao")?.value || 0,
  );
  const motivo = document.getElementById("motivoMovimentacao")?.value || "";

  if (!produtoSelecionado) {
    return {
      valido: false,
      mensagem: "Selecione um produto antes de confirmar a movimentação.",
    };
  }

  if (!tipo) {
    return {
      valido: false,
      mensagem: "Selecione o tipo de movimentação.",
    };
  }

  if (!quantidade || quantidade <= 0) {
    return {
      valido: false,
      mensagem: "Informe uma quantidade maior que zero.",
    };
  }

  if (!motivo) {
    return {
      valido: false,
      mensagem: "Selecione o motivo da movimentação.",
    };
  }

  return {
    valido: true,
  };
}

function montarPayloadMovimentacao() {
  return {
    id_variacao: produtoSelecionado.idVariacao,
    tipo: document.getElementById("tipoMovimentacao")?.value || "",
    quantidade: Number(
      document.getElementById("quantidadeMovimentacao")?.value || 0,
    ),
    motivo: document.getElementById("motivoMovimentacao")?.value || "",
  };
}

function atualizarProdutoAposResposta(resposta) {
  const produto = produtos.find(
    (item) => item.idVariacao === resposta.produto.id_variacao,
  );

  if (!produto) return;

  produto.quantidade = resposta.estoque.atual;

  if (
    produtoSelecionado &&
    produtoSelecionado.idVariacao === produto.idVariacao
  ) {
    produtoSelecionado = produto;
  }
}

function registrarMovimentacaoNoHistorico(resposta) {
  movimentacoes.unshift({
    produto: resposta.produto.nome,
    sku: resposta.produto.sku,
    tipo: resposta.movimentacao.tipo,
    quantidade: resposta.movimentacao.quantidade,
    motivo:
      resposta.movimentacao.motivo_descricao ||
      formatarMotivo(resposta.movimentacao.motivo),
    data: formatarDataHoraParaTela(resposta.movimentacao.data),
  });
}

async function sincronizarDadosAposMovimentacao(resposta) {
  const idVariacaoSelecionada = produtoSelecionado?.idVariacao;

  const [estoqueResultado, movimentacoesResultado, dashboardResultado] =
    await Promise.allSettled([
      buscarEstoqueApi(),
      buscarMovimentacoesApi(),
      buscarDashboardApi(),
    ]);

  if (estoqueResultado.status === "fulfilled") {
    produtos = estoqueResultado.value.map(mapearProdutoApi);
    preencherFiltrosAlertas();
  } else {
    console.error(
      "[FRONT] erro ao sincronizar estoque após movimentação:",
      estoqueResultado.reason,
    );

    atualizarProdutoAposResposta(resposta);
  }

  if (movimentacoesResultado.status === "fulfilled") {
    movimentacoes = movimentacoesResultado.value.map(mapearMovimentacaoApi);
  } else {
    console.error(
      "[FRONT] erro ao sincronizar movimentações:",
      movimentacoesResultado.reason,
    );

    registrarMovimentacaoNoHistorico(resposta);
  }

  if (dashboardResultado.status === "fulfilled") {
    dashboardDados = dashboardResultado.value;
  } else {
    console.error(
      "[FRONT] erro ao sincronizar dashboard:",
      dashboardResultado.reason,
    );

    dashboardDados = null;
  }

  if (idVariacaoSelecionada) {
    const produtoAtualizado = produtos.find(
      (produto) => produto.idVariacao === idVariacaoSelecionada,
    );

    if (produtoAtualizado) {
      produtoSelecionado = produtoAtualizado;
    }
  }

  renderizarTudo();
  renderizarProdutoSelecionado();
}

function atualizarResumoComEstoqueAtual(estoqueAtual) {
  const resumoNovoEstoque = document.getElementById("resumoNovoEstoque");

  if (!resumoNovoEstoque) return;

  resumoNovoEstoque.textContent = estoqueAtual;
}

function setBotaoConfirmarCarregando(carregando) {
  const btnConfirmar = document.getElementById("btnConfirmarMovimentacao");

  if (!btnConfirmar) return;

  btnConfirmar.disabled = carregando;
  btnConfirmar.textContent = carregando
    ? "Registrando..."
    : "Confirmar movimentação";
}

function limparMovimentacao() {
  produtoSelecionado = null;

  const campos = [
    "movBuscaProduto",
    "tipoMovimentacao",
    "quantidadeMovimentacao",
    "motivoMovimentacao",
  ];

  campos.forEach((id) => {
    const campo = document.getElementById(id);

    if (campo) {
      campo.value = id === "quantidadeMovimentacao" ? "0" : "";
    }
  });

  atualizarMotivosPorTipo("");
  atualizarLabelQuantidadePorTipo("");
  renderizarProdutoSelecionado();
  atualizarResumoMovimentacao();
  limparFeedback();

  movimentacaoConcluida = false;
  setBotaoConfirmarCarregando(false);
}

/* =========================
   Feedback
========================= */

function exibirFeedback(mensagem, tipo = "success") {
  const feedbackMessage = document.getElementById("feedbackMessage");
  const feedbackText = document.getElementById("feedbackText");

  if (!feedbackMessage || !feedbackText) return;

  feedbackMessage.className = `feedback-message show ${tipo}`;
  feedbackText.textContent = mensagem;

  window.clearTimeout(exibirFeedback.timeoutId);

  exibirFeedback.timeoutId = window.setTimeout(() => {
    limparFeedback();
  }, 5000);
}

function limparFeedback() {
  const feedbackMessage = document.getElementById("feedbackMessage");
  const feedbackText = document.getElementById("feedbackText");

  if (!feedbackMessage || !feedbackText) return;

  feedbackMessage.className = "feedback-message";
  feedbackText.textContent = "";

  window.clearTimeout(exibirFeedback.timeoutId);
}

/* =========================
   Formatadores
========================= */

function formatarTipoMovimentacao(tipo) {
  const tipos = {
    ENTRADA: "Entrada",
    SAIDA: "Saída",
    AJUSTE: "Ajuste",
  };

  return tipos[tipo] || tipo;
}

function formatarMotivo(motivo) {
  const motivos = {
    REPOSICAO: "Reposição de estoque",
    COMPRA: "Compra",
    DEVOLUCAO: "Devolução",
    VENDA: "Venda",
    PERDA: "Perda",
    RETIRADA_OPERACIONAL: "Retirada operacional",
    CORRECAO: "Correção de saldo",
    INVENTARIO: "Inventário",
  };

  return motivos[motivo] || motivo || "—";
}

function formatarDataHoraParaTela(dataHora) {
  if (!dataHora) return "—";

  if (dataHora.includes("T")) {
    const data = new Date(dataHora);

    if (Number.isNaN(data.getTime())) {
      return dataHora;
    }

    return data.toLocaleString("pt-BR", {
      dateStyle: "short",
      timeStyle: "short",
    });
  }

  const [data, hora] = dataHora.split(" ");

  if (!data || !hora) return dataHora;

  const [ano, mes, dia] = data.split("-");

  if (!ano || !mes || !dia) return dataHora;

  return `${dia}/${mes}/${ano} ${hora.slice(0, 5)}`;
}
