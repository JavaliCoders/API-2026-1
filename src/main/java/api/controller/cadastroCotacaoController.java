package api.controller;

import api.DAO.CotacaoDAO;
import api.DAO.pedidoDAO;
import api.DAO.fornecedorDAO;
import api.model.*;
import api.service.HistoricoService;
import api.service.NotificacaoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.ResourceBundle;

public class cadastroCotacaoController implements Initializable {

    // ── Cabeçalho ─────────────────────────────────────────────
    @FXML private Label     labelTitulo;
    @FXML private Label     labelSubtitulo;
    @FXML private TextField fieldNumPedido;
    @FXML private TextField fieldSolicitante;
    @FXML private TextField fieldValorPedido;

    // ── Fornecedor ────────────────────────────────────────────
    @FXML private ComboBox<Fornecedor> fieldFornecedor;
    @FXML private TextArea             fieldObservacoes;
    @FXML private Label                erroFornecedor;

    // ── Lista de produtos do pedido ───────────────────────────
    @FXML private ListView<PedidoProduto> listaProdutosPedido;
    @FXML private TextField               fieldQtd;
    @FXML private TextField               fieldValorUnit;
    @FXML private Label                   labelProdutoSelecionado;
    @FXML private HBox                    boxProdutoSelecionado;
    @FXML private Label                   erroProduto;

    // ── Tabela de itens adicionados ───────────────────────────
    @FXML private TableView<CotacaoItem>           tabelaItens;
    @FXML private TableColumn<CotacaoItem, String> colProduto;
    @FXML private TableColumn<CotacaoItem, String> colUnidade;
    @FXML private TableColumn<CotacaoItem, String> colQtd;
    @FXML private TableColumn<CotacaoItem, String> colVlrUnit;
    @FXML private TableColumn<CotacaoItem, String> colVlrTotal;
    @FXML private TableColumn<CotacaoItem, Void>   colRemover;
    @FXML private Label                            labelTotal;

    // ── Anexo ─────────────────────────────────────────────────
    @FXML private Label labelArquivoSelecionado;
    @FXML private Label labelNomeArquivo;
    @FXML private Label labelTamanhoArquivo;
    @FXML private HBox  boxPreviewAnexo;
    @FXML private Label erroAnexo;

    // ── Estado ────────────────────────────────────────────────
    private AnchorPane    areaPrincipal;
    private Pedido        pedido;
    private Cotacao       cotacaoEdicao = null;
    private File          arquivoSelecionado;
    private PedidoProduto produtoSelecionado;

    private ObservableList<PedidoProduto> itensDoPedido =
            FXCollections.observableArrayList();
    private final ObservableList<CotacaoItem> itensCotacao =
            FXCollections.observableArrayList();

    private static final String PASTA_ANEXOS = "anexos/cotacoes/";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColunaItens();
        tabelaItens.setItems(itensCotacao);

        fieldQtd.textProperty().addListener((obs, a, n) -> {
            if (!n.matches("\\d*")) fieldQtd.setText(a);
        });
        fieldValorUnit.textProperty().addListener((obs, a, n) -> {
            if (!n.matches("[\\d,\\.]*")) fieldValorUnit.setText(a);
        });

        // Clique na lista de produtos do pedido
        listaProdutosPedido.setOnMouseClicked(e -> {
            PedidoProduto pp = listaProdutosPedido.getSelectionModel()
                    .getSelectedItem();
            if (pp != null) selecionarProduto(pp);
        });

        // Renderização da lista: mostra produto + status (adicionado ou não)
        listaProdutosPedido.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(PedidoProduto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }

                boolean jaAdicionado = itensCotacao.stream()
                        .anyMatch(i -> i.getIdPedidoProduto() == item.getIdPedidoProduto());

                // Quantidade de referência: aprovada se houver, senão solicitada
                int qtdReferencia = item.getQtdAprovada() > 0
                        ? item.getQtdAprovada()
                        : item.getQtdSolicitada();
                String labelQtd = item.getQtdAprovada() > 0
                        ? "Qtd aprovada: " + item.getQtdAprovada()
                        : "Qtd solicitada: " + item.getQtdSolicitada() + " (sem aprovação)";

                HBox card = new HBox();
                card.setSpacing(0);

                javafx.scene.layout.VBox iconBar = new javafx.scene.layout.VBox();
                iconBar.setAlignment(javafx.geometry.Pos.CENTER);
                iconBar.setMinWidth(6);
                iconBar.setStyle("-fx-background-color: "
                        + (jaAdicionado ? "#d1d5db" : "#2563eb")
                        + "; -fx-background-radius: 8 0 0 8;");

                javafx.scene.layout.VBox content = new javafx.scene.layout.VBox();
                content.setSpacing(2);
                content.setStyle("-fx-padding: 10 14; -fx-background-color: "
                        + (jaAdicionado ? "#f9fafb" : "white")
                        + "; -fx-background-radius: 0 8 8 0;");
                HBox.setHgrow(content, javafx.scene.layout.Priority.ALWAYS);

                Label nomeLbl = new Label(
                        (jaAdicionado ? "✅  " : "📦  ") + item.getNomeProduto());
                nomeLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;"
                        + " -fx-text-fill: "
                        + (jaAdicionado ? "#9ca3af;" : "#0f172a;"));
                nomeLbl.setWrapText(true);

                HBox detalhes = new HBox();
                detalhes.setSpacing(16);

                Label unidLbl = new Label("Unidade: " + item.getUnidadeProduto());
                unidLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: "
                        + (jaAdicionado ? "#d1d5db;" : "#64748b;"));

                // ← usa qtdReferencia
                Label qtdLbl = new Label(labelQtd);
                qtdLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: "
                        + (jaAdicionado ? "#d1d5db;" : "#1d4ed8;"));

                detalhes.getChildren().addAll(unidLbl, qtdLbl);

                Label statusLbl = new Label(
                        jaAdicionado ? "já adicionado" : "clique para selecionar");
                statusLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;"
                        + " -fx-background-radius: 20; -fx-padding: 2 10;"
                        + " -fx-background-color: "
                        + (jaAdicionado ? "#e5e7eb;" : "#dbeafe;")
                        + " -fx-text-fill: "
                        + (jaAdicionado ? "#9ca3af;" : "#1d4ed8;"));

                HBox statusRow = new HBox(statusLbl);
                statusRow.setStyle("-fx-padding: 4 0 0 0;");

                content.getChildren().addAll(nomeLbl, detalhes, statusRow);
                card.getChildren().addAll(iconBar, content);

                setText(null);
                setGraphic(card);
                setStyle("-fx-background-color: transparent; -fx-padding: 4 8;");
                setDisable(jaAdicionado);
            }
        });

        // Atualiza a lista sempre que itensCotacao muda
        // (para refletir quais já foram adicionados)
        itensCotacao.addListener(
                (javafx.collections.ListChangeListener<CotacaoItem>) c ->
                        listaProdutosPedido.refresh());
    }

    // ── Injeção de contexto ───────────────────────────────────

    public void setAreaPrincipal(AnchorPane ap) { this.areaPrincipal = ap; }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
        preencherCabecalho();
        carregarFornecedores();
        carregarItensDoPedido();
    }

    public void setCotacaoEdicao(Cotacao c, Pedido pedidoCompleto) {
        this.cotacaoEdicao = c;
        this.pedido        = pedidoCompleto;

        labelTitulo   .setText("Editar Cotação");
        labelSubtitulo.setText("Editando cotação do pedido "
                + pedidoCompleto.getNumPedido());

        preencherCabecalho();
        carregarFornecedores();
        carregarItensDoPedido();

        // Pré-seleciona fornecedor
        fieldFornecedor.getItems().stream()
                .filter(f -> f.getIdFornecedor()
                        == c.getFornecedor().getIdFornecedor())
                .findFirst()
                .ifPresent(fieldFornecedor::setValue);

        // Carrega itens já cadastrados
        itensCotacao.setAll(CotacaoDAO.listarItens(c.getIdCotacao()));
        atualizarTotal();

        // Mostra anexo existente
        if (c.temAnexo()) {
            labelArquivoSelecionado.setText(c.getNomeAnexo());
            labelNomeArquivo       .setText(c.getNomeAnexo());
            labelTamanhoArquivo    .setText("arquivo existente");
            boxPreviewAnexo.setVisible(true);
            boxPreviewAnexo.setManaged(true);
        }
    }

    // ── Carregamentos ─────────────────────────────────────────

    private void preencherCabecalho() {
        fieldNumPedido  .setText(pedido.getNumPedido());
        fieldSolicitante.setText(pedido.getNomeSolicitante());
        fieldValorPedido.setText(
                String.format("R$ %.2f", pedido.getValorTotalEstimado())
                        .replace(".", ","));
        if (labelSubtitulo.getText().isBlank())
            labelSubtitulo.setText(
                    "Registrando cotação para o pedido " + pedido.getNumPedido());
    }

    private void carregarFornecedores() {
        ObservableList<Fornecedor> lista = fornecedorDAO.listarAtivos();
        fieldFornecedor.setItems(lista);
        fieldFornecedor.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Fornecedor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : item.getNome() + "  —  CNPJ: " + item.getCnpj());
            }
        });
        fieldFornecedor.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Fornecedor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNome());
            }
        });
    }

    private void carregarItensDoPedido() {
        itensDoPedido.setAll(pedidoDAO.listarItens(pedido.getIdPedido()));
        // Exibe todos imediatamente, sem precisar digitar nada
        listaProdutosPedido.setItems(itensDoPedido);
    }

    // ── Seleção de produto ────────────────────────────────────

    private void selecionarProduto(PedidoProduto pp) {
        boolean jaAdicionado = itensCotacao.stream()
                .anyMatch(i -> i.getIdPedidoProduto() == pp.getIdPedidoProduto());
        if (jaAdicionado) return;

        this.produtoSelecionado = pp;

        // ← mostra qtd aprovada se houver, senão solicitada
        int qtdReferencia = pp.getQtdAprovada() > 0
                ? pp.getQtdAprovada()
                : pp.getQtdSolicitada();
        String labelQtd = pp.getQtdAprovada() > 0
                ? "Qtd aprovada: " + pp.getQtdAprovada()
                : "Qtd solicitada: " + pp.getQtdSolicitada() + " (sem aprovação)";

        labelProdutoSelecionado.setText(
                pp.getNomeProduto()
                        + "  [" + pp.getUnidadeProduto() + "]"
                        + "  —  " + labelQtd);

        boxProdutoSelecionado.setVisible(true);
        boxProdutoSelecionado.setManaged(true);
        erroProduto.setText("");
        fieldQtd.requestFocus();
    }

    @FXML
    private void onLimparProduto() {
        produtoSelecionado = null;
        boxProdutoSelecionado.setVisible(false);
        boxProdutoSelecionado.setManaged(false);
        fieldQtd.clear();
        fieldValorUnit.clear();
        listaProdutosPedido.getSelectionModel().clearSelection();
    }

    @FXML
    private void onAdicionarProduto() {
        erroProduto.setText("");

        if (produtoSelecionado == null) {
            erroProduto.setText("Clique em um produto da lista acima.");
            return;
        }
        if (fieldQtd.getText().isBlank()) {
            erroProduto.setText("Informe a quantidade.");
            return;
        }
        if (fieldValorUnit.getText().isBlank()) {
            erroProduto.setText("Informe o valor unitário.");
            return;
        }

        int    qtd;
        double valorUnit;
        try {
            qtd       = Integer.parseInt(fieldQtd.getText().trim());
            valorUnit = Double.parseDouble(
                    fieldValorUnit.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            erroProduto.setText("Quantidade ou valor inválido.");
            return;
        }

        if (qtd <= 0) {
            erroProduto.setText("A quantidade deve ser maior que zero.");
            return;
        }
        if (valorUnit <= 0) {
            erroProduto.setText("O valor deve ser maior que zero.");
            return;
        }

        if (qtd <= 0) {
            erroProduto.setText("A quantidade deve ser maior que zero.");
            return;
        }

        int qtdMaxima = produtoSelecionado.getQtdAprovada() > 0
                ? produtoSelecionado.getQtdAprovada()
                : produtoSelecionado.getQtdSolicitada();
        if (qtd > qtdMaxima) {
            erroProduto.setText("Quantidade máxima permitida: " + qtdMaxima
                    + " (" + (produtoSelecionado.getQtdAprovada() > 0
                    ? "qtd aprovada" : "qtd solicitada") + ").");
            return;
        }

        itensCotacao.add(new CotacaoItem(
                produtoSelecionado.getIdPedidoProduto(),
                produtoSelecionado.getNomeProduto(),
                produtoSelecionado.getUnidadeProduto(),
                qtd,
                valorUnit
        ));

        atualizarTotal();
        onLimparProduto(); // Limpa seleção e campos
    }

    private void atualizarTotal() {
        double total = itensCotacao.stream()
                .mapToDouble(CotacaoItem::getValorTotal).sum();
        labelTotal.setText(String.format("R$ %.2f", total).replace(".", ","));
    }

    // ── Colunas da tabela de itens adicionados ────────────────

    private void configurarColunaItens() {
        colProduto .setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNomeProduto()));
        colUnidade .setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getUnidade()));
        colQtd     .setCellValueFactory(d ->
                new SimpleStringProperty(
                        String.valueOf(d.getValue().getQtdCotada())));
        colVlrUnit .setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getValorUnitFormatado()));
        colVlrTotal.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getValorTotalFormatado()));

        colRemover.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("🗑");
            {
                btn.setStyle(
                        "-fx-background-color:#fee2e2; -fx-text-fill:#dc2626;" +
                                "-fx-background-radius:6; -fx-border-color:transparent;" +
                                "-fx-cursor:hand; -fx-font-size:13px; -fx-padding:4 8;");
                btn.setOnMouseEntered(e -> btn.setStyle(
                        "-fx-background-color:#dc2626; -fx-text-fill:white;" +
                                "-fx-background-radius:6; -fx-border-color:transparent;" +
                                "-fx-cursor:hand; -fx-font-size:13px; -fx-padding:4 8;"));
                btn.setOnMouseExited(e -> btn.setStyle(
                        "-fx-background-color:#fee2e2; -fx-text-fill:#dc2626;" +
                                "-fx-background-radius:6; -fx-border-color:transparent;" +
                                "-fx-cursor:hand; -fx-font-size:13px; -fx-padding:4 8;"));
                btn.setOnAction(e -> {
                    itensCotacao.remove(
                            getTableView().getItems().get(getIndex()));
                    atualizarTotal();
                    // Ao remover, a lista de produtos atualiza automaticamente
                    // pelo listener que chama listaProdutosPedido.refresh()
                });
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                HBox box = new HBox(btn);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });
    }

    // ── Anexo ─────────────────────────────────────────────────

    @FXML
    private void onSelecionarArquivo() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Selecionar Arquivo da Cotação");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Documentos e Imagens",
                "*.pdf","*.PDF","*.png","*.PNG","*.jpg","*.JPG",
                "*.jpeg","*.JPEG","*.docx","*.xlsx","*.xls"));

        File arquivo = fc.showOpenDialog(areaPrincipal.getScene().getWindow());
        if (arquivo != null) {
            arquivoSelecionado = arquivo;
            erroAnexo.setText("");
            labelArquivoSelecionado.setText(arquivo.getName());
            labelNomeArquivo       .setText(arquivo.getName());
            labelTamanhoArquivo    .setText(formatarTamanho(arquivo.length()));
            boxPreviewAnexo.setVisible(true);
            boxPreviewAnexo.setManaged(true);
        }
    }

    @FXML
    private void onRemoverArquivo() {
        arquivoSelecionado = null;
        labelArquivoSelecionado.setText("Nenhum arquivo selecionado");
        boxPreviewAnexo.setVisible(false);
        boxPreviewAnexo.setManaged(false);
    }

    // ── Salvar ────────────────────────────────────────────────

    @FXML
    private void onSalvar() {
        limparErros();
        if (!validar()) return;

        double valorTotal = itensCotacao.stream()
                .mapToDouble(CotacaoItem::getValorTotal).sum();

        if (cotacaoEdicao == null) {
            String caminhoFinal;
            try {
                caminhoFinal = copiarArquivo(arquivoSelecionado);
            } catch (IOException e) {
                erroAnexo.setText("Erro ao salvar arquivo: " + e.getMessage());
                return;
            }

            int idCadastrador = SessaoUsuario.getInstancia().getIdUsuarioLogado();
            int idCotacao = CotacaoDAO.inserir(
                    valorTotal,
                    pedido.getIdPedido(),
                    fieldFornecedor.getValue().getIdFornecedor(),
                    idCadastrador,
                    arquivoSelecionado.getName(),
                    caminhoFinal,
                    itensCotacao
            );

            if (idCotacao == -1) {
                new Alert(Alert.AlertType.ERROR,
                        "Erro ao salvar cotação.").showAndWait();
                return;
            }

            CotacaoDAO.marcarPedidoEmCotacao(pedido.getIdPedido());

            StringBuilder descItens = new StringBuilder();
            for (CotacaoItem item : itensCotacao) {
                descItens.append(item.getNomeProduto())
                        .append(" (qtd: ").append(item.getQtdCotada())
                        .append(", unit: ").append(item.getValorUnitFormatado())
                        .append("); ");
            }
            HistoricoService.registrar("Cotação", "Cadastro", idCotacao,
                    "Cotação para " + pedido.getNumPedido()
                            + " — Fornecedor: " + fieldFornecedor.getValue().getNome()
                            + " — Total: R$ " + String.format("%.2f", valorTotal)
                            + " — Itens: " + descItens
                            + " — por " + SessaoUsuario.getInstancia().getNomeUsuarioLogado());

            NotificacaoService.notificarNovaCotacao(
                    pedido.getIdPedido(),
                    pedido.getNumPedido(),
                    fieldFornecedor.getValue().getNome(),
                    valorTotal,
                    SessaoUsuario.getInstancia().getNomeUsuarioLogado());

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Sucesso");
            ok.setHeaderText(null);
            ok.setContentText("Cotação registrada com sucesso! O pedido "
                    + pedido.getNumPedido() + " agora está Em Cotação.");
            ok.showAndWait();

        } else {
            String nomeArq    = null;
            String caminhoArq = null;
            if (arquivoSelecionado != null) {
                try {
                    caminhoArq = copiarArquivo(arquivoSelecionado);
                    nomeArq    = arquivoSelecionado.getName();
                } catch (IOException e) {
                    erroAnexo.setText("Erro ao salvar arquivo: " + e.getMessage());
                    return;
                }
            }

            boolean ok2 = CotacaoDAO.atualizar(
                    cotacaoEdicao.getIdCotacao(), valorTotal,
                    fieldFornecedor.getValue().getIdFornecedor(),
                    nomeArq, caminhoArq, itensCotacao);

            if (!ok2) {
                new Alert(Alert.AlertType.ERROR,
                        "Erro ao atualizar cotação.").showAndWait();
                return;
            }

            HistoricoService.registrar("Cotação", "Alteração",
                    cotacaoEdicao.getIdCotacao(),
                    "Cotação " + cotacaoEdicao.getIdCotacao()
                            + " editada por "
                            + SessaoUsuario.getInstancia().getNomeUsuarioLogado()
                            + " — novo valor: R$ "
                            + String.format("%.2f", valorTotal));

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Sucesso");
            ok.setHeaderText(null);
            ok.setContentText("Cotação atualizada com sucesso!");
            ok.showAndWait();
        }

        navegarParaCotacoes();
    }

    private boolean validar() {
        boolean ok = true;
        if (fieldFornecedor.getValue() == null) {
            erroFornecedor.setText("Selecione o fornecedor.");
            ok = false;
        }
        if (itensCotacao.isEmpty()) {
            erroProduto.setText("Adicione ao menos um produto à cotação.");
            ok = false;
        }
        if (cotacaoEdicao == null && arquivoSelecionado == null) {
            erroAnexo.setText("Selecione o arquivo de cotação.");
            ok = false;
        }
        return ok;
    }

    private void limparErros() {
        erroFornecedor.setText("");
        erroProduto   .setText("");
        erroAnexo     .setText("");
    }

    // ── Navegação ─────────────────────────────────────────────

    @FXML private void onCancelar() { navegarParaCotacoes(); }

    private void navegarParaCotacoes() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/cotacao.fxml"));
            Node tela = loader.load();
            CotacaoController ctrl = loader.getController();
            ctrl.setAreaPrincipal(areaPrincipal);
            // ← sem filtrarPorPedido: volta para a listagem completa
            anchorar(tela);
            areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Utilitários ───────────────────────────────────────────

    private String copiarArquivo(File origem) throws IOException {
        Path pasta = Paths.get(PASTA_ANEXOS);
        Files.createDirectories(pasta);
        String nomeUnico = System.currentTimeMillis() + "_" + origem.getName();
        Path destino = pasta.resolve(nomeUnico);
        Files.copy(origem.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
        return destino.toString();
    }

    private void anchorar(Node tela) {
        AnchorPane.setTopAnchor   (tela, 0.0);
        AnchorPane.setBottomAnchor(tela, 0.0);
        AnchorPane.setLeftAnchor  (tela, 0.0);
        AnchorPane.setRightAnchor (tela, 0.0);
    }

    private String formatarTamanho(long bytes) {
        if (bytes < 1024)        return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}