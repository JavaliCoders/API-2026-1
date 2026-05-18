package api.controller;

import api.DAO.CotacaoDAO;
import api.model.*;
import api.service.HistoricoService;
import api.util.PermissaoUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class CotacaoController implements Initializable {

    // ── Tabela ────────────────────────────────────────────────
    @FXML private TableView<Cotacao>           tabelaCotacoes;
    @FXML private TableColumn<Cotacao, String> colNumPedido;
    @FXML private TableColumn<Cotacao, String> colFornecedor;
    @FXML private TableColumn<Cotacao, String> colValor;
    @FXML private TableColumn<Cotacao, String> colData;
    @FXML private TableColumn<Cotacao, String> colStatus;
    @FXML private TableColumn<Cotacao, String> colAprovador;
    @FXML private TableColumn<Cotacao, String> colDataAprov;
    @FXML private TableColumn<Cotacao, Void>   colAnexo;
    @FXML private TableColumn<Cotacao, Void>   colNovaCotacao;
    @FXML private TableColumn<Cotacao, Void>   colAcoes;

    // ── Filtros ───────────────────────────────────────────────
    @FXML private TextField  fieldBusca;
    @FXML private ComboBox<String> filtroStatus;
    @FXML private DatePicker filtroDataInicio;
    @FXML private DatePicker filtroDataFim;

    // ── Banner filtro por pedido ──────────────────────────────
    @FXML private HBox  boxFiltradoPedido;
    @FXML private Label labelFiltradoPedido;
    @FXML private Button btnVoltar;

    // ── Overlay detalhes ──────────────────────────────────────
    @FXML private StackPane overlayDetalhes;
    @FXML private Label     detalheNumPedido;
    @FXML private Label     detalheFornecedor;
    @FXML private Label     detalheValor;
    @FXML private Label     detalheDataCriacao;
    @FXML private Label     detalheStatus;
    @FXML private Label     detalheAprovador;
    @FXML private Label     detalheDataAprovacao;
    @FXML private Label     detalheParecer;
    @FXML private VBox      secaoAprovacaoCotacao;

    private AnchorPane areaPrincipal;
    private Pedido     pedidoFiltro;

    private ObservableList<Cotacao> todasCotacoes;
    private FilteredList<Cotacao>   cotacoesFiltradas;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final boolean isDiretor    = PermissaoUtil.temPermissao("DIRETOR");
    private final boolean isFinanceiro = PermissaoUtil.temPermissao("FINANCEIRO");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarFiltros();
        configurarColunas();
        carregarCotacoes();

        tabelaCotacoes.getSelectionModel().selectedItemProperty()
                .addListener((obs, a, c) -> { if (c != null) abrirOverlay(c); });
    }



    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }

    public void filtrarPorPedido(Pedido pedido) {
        this.pedidoFiltro = pedido;
        btnVoltar.setVisible(true);
        btnVoltar.setManaged(true);
        boxFiltradoPedido.setVisible(true);
        boxFiltradoPedido.setManaged(true);
        labelFiltradoPedido.setText("Cotações do pedido " + pedido.getNumPedido()
                + "  —  " + pedido.getNomeSolicitante());
        reaplicarFiltro();
    }

    // ── Dados ─────────────────────────────────────────────────

    private void carregarCotacoes() {
        todasCotacoes     = CotacaoDAO.listarTodas();
        cotacoesFiltradas = new FilteredList<>(todasCotacoes, c -> true);
        tabelaCotacoes.setItems(cotacoesFiltradas);
        reaplicarFiltro();
    }

    // ── Filtros ───────────────────────────────────────────────

    private void configurarFiltros() {
        // Sem APROVADO_PARCIALMENTE conforme solicitado
        filtroStatus.setItems(FXCollections.observableArrayList(
                "Todos os status", "AGUARDANDO_APROVACAO", "APROVADO", "NEGADO"));
        filtroStatus.setValue("Todos os status");

        fieldBusca      .textProperty() .addListener((o, a, n) -> reaplicarFiltro());
        filtroStatus    .valueProperty().addListener((o, a, n) -> reaplicarFiltro());
        filtroDataInicio.valueProperty().addListener((o, a, n) -> reaplicarFiltro());
        filtroDataFim   .valueProperty().addListener((o, a, n) -> reaplicarFiltro());
    }

    @FXML private void onLimparFiltro() {
        fieldBusca.clear();
        filtroStatus.setValue("Todos os status");
        filtroDataInicio.setValue(null);
        filtroDataFim   .setValue(null);
    }

    private void reaplicarFiltro() {
        if (cotacoesFiltradas == null) return;
        String    busca  = fieldBusca.getText() == null ? "" : fieldBusca.getText().trim().toLowerCase();
        String    status = filtroStatus.getValue();
        LocalDate di     = filtroDataInicio.getValue();
        LocalDate df     = filtroDataFim   .getValue();

        cotacoesFiltradas.setPredicate(c -> {
            if (pedidoFiltro != null && c.getIdPedido() != pedidoFiltro.getIdPedido())
                return false;
            boolean okB = busca.isEmpty()
                    || c.getNumPedido()   .toLowerCase().contains(busca)
                    || c.getNomeFornecedor().toLowerCase().contains(busca);
            boolean okS = status == null || status.equals("Todos os status")
                    || c.getStatus().equals(status);
            LocalDate dataCot = c.getDataCriacao().toLocalDate();
            boolean okDi = di == null || !dataCot.isBefore(di);
            boolean okDf = df == null || !dataCot.isAfter(df);
            return okB && okS && okDi && okDf;
        });
    }

    // ── Overlay de detalhes ───────────────────────────────────

    private void abrirOverlay(Cotacao c) {
        detalheNumPedido  .setText(c.getNumPedido());
        detalheFornecedor .setText(c.getNomeFornecedor());
        detalheValor      .setText(c.getValorFormatado());
        detalheDataCriacao.setText(c.getDataCriacaoFormatada());
        detalheStatus     .setText(formatarStatus(c.getStatus()));
        detalheStatus     .setStyle(estiloBadge(c.getStatus()));

        boolean temDecisao = c.getStatus().equals("APROVADO") || c.getStatus().equals("NEGADO");
        if (temDecisao) {
            detalheAprovador.setText(c.getNomeAprovador());
            detalheAprovador.setStyle(c.getStatus().equals("NEGADO")
                    ? "-fx-font-size:14px; -fx-text-fill:#991b1b; -fx-font-weight:bold;"
                    : "-fx-font-size:14px; -fx-text-fill:#166534; -fx-font-weight:bold;");
            detalheDataAprovacao.setText(c.getDataAprovacaoFormatada());
            String parecer = c.getParecer();
            detalheParecer.setText(parecer != null && !parecer.isBlank() ? parecer : "Sem parecer.");
            secaoAprovacaoCotacao.setVisible(true);
            secaoAprovacaoCotacao.setManaged(true);
        } else {
            secaoAprovacaoCotacao.setVisible(false);
            secaoAprovacaoCotacao.setManaged(false);
        }

        overlayDetalhes.setVisible(true);
        overlayDetalhes.setManaged(true);
    }

    @FXML private void fecharDetalhes() {
        overlayDetalhes.setVisible(false);
        overlayDetalhes.setManaged(false);
        tabelaCotacoes.getSelectionModel().clearSelection();
    }

    // ── Colunas ───────────────────────────────────────────────

    private void configurarColunas() {
        colNumPedido .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumPedido()));
        colFornecedor.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeFornecedor()));
        colValor     .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getValorFormatado()));
        colData      .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataCriacaoFormatada()));
        colAprovador .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeAprovador()));
        colDataAprov .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataAprovacaoFormatada()));

        for (TableColumn<Cotacao, String> col : new TableColumn[]{
                colNumPedido, colFornecedor, colValor, colData, colAprovador, colDataAprov}) {
            col.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); return; }
                    setText(item);
                    setFont(Font.font("Segoe UI", 13));
                    setStyle("-fx-text-fill:#0f172a;");
                }
            });
        }
        // Rebind após setCellFactory
        colNumPedido .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumPedido()));
        colFornecedor.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeFornecedor()));
        colValor     .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getValorFormatado()));
        colData      .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataCriacaoFormatada()));
        colAprovador .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeAprovador()));
        colDataAprov .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataAprovacaoFormatada()));

        // Badge status
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(formatarStatus(status));
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                badge.setAlignment(Pos.CENTER);
                badge.setPrefWidth(130);
                badge.setStyle(estiloBadge(status));
                HBox box = new HBox(badge); box.setAlignment(Pos.CENTER);
                setGraphic(box); setText(null);
            }
        });

        // Coluna Anexo
        colAnexo.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("📎  Abrir");
            {
                btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                estilo(false);
                btn.setOnMouseEntered(e -> estilo(true));
                btn.setOnMouseExited (e -> estilo(false));
                btn.setOnAction(e -> abrirAnexo(getTableView().getItems().get(getIndex())));
            }
            private void estilo(boolean hover) {
                btn.setStyle(hover
                        ? "-fx-background-color:#2563eb; -fx-text-fill:white; -fx-background-radius:6; -fx-border-color:transparent; -fx-padding:7 14; -fx-cursor:hand; -fx-font-size:13px;"
                        : "-fx-background-color:#dbeafe; -fx-text-fill:#1e40af; -fx-background-radius:6; -fx-border-color:transparent; -fx-padding:7 14; -fx-cursor:hand; -fx-font-size:13px;");
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setGraphic(null); return; }
                Cotacao c = (Cotacao) getTableRow().getItem();
                if (c.temAnexo()) {
                    HBox box = new HBox(btn); box.setAlignment(Pos.CENTER); setGraphic(box);
                } else {
                    Label dash = new Label("—");
                    dash.setFont(Font.font("Segoe UI", 13));
                    dash.setStyle("-fx-text-fill:#9ca3af;");
                    HBox box = new HBox(dash); box.setAlignment(Pos.CENTER); setGraphic(box);
                }
            }
        });

        // Coluna Nova Cotação — verde, só FINANCEIRO
        colNovaCotacao.setVisible(isFinanceiro);
        colNovaCotacao.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("➕  Nova Cotação");
            {
                btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                estilo(false);
                btn.setOnMouseEntered(e -> estilo(true));
                btn.setOnMouseExited (e -> estilo(false));
                btn.setOnAction(e -> {
                    Cotacao c = getTableView().getItems().get(getIndex());
                    navegarParaNovaCotacao(c.getIdPedido());
                });
            }
            private void estilo(boolean hover) {
                btn.setStyle(hover
                        ? "-fx-background-color:#16a34a; -fx-text-fill:white; -fx-background-radius:6; -fx-border-color:transparent; -fx-padding:7 14; -fx-cursor:hand; -fx-font-size:13px;"
                        : "-fx-background-color:#dcfce7; -fx-text-fill:#166534; -fx-background-radius:6; -fx-border-color:transparent; -fx-padding:7 14; -fx-cursor:hand; -fx-font-size:13px;");
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setGraphic(null); return; }
                HBox box = new HBox(btn); box.setAlignment(Pos.CENTER); setGraphic(box);
            }
        });

        // Coluna Ações — só DIRETOR, só AGUARDANDO
       // colAcoes.setVisible(isDiretor);
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("⚙  Ações  ▾");
            {
                btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                estilo(false);
                btn.setOnMouseEntered(e -> estilo(true));
                btn.setOnMouseExited (e -> estilo(false));
                btn.setOnAction(e -> {
                    Cotacao c = getTableView().getItems().get(getIndex());
                    mostrarMenuAcoes(btn, c);
                });
            }
            private void estilo(boolean hover) {
                btn.setStyle(hover
                        ? "-fx-background-color:#1e40af; -fx-text-fill:white; -fx-background-radius:6; -fx-border-color:transparent; -fx-border-width:1; -fx-padding:7 16; -fx-cursor:hand; -fx-font-size:13px;"
                        : "-fx-background-color:#eff6ff; -fx-text-fill:#1e40af; -fx-background-radius:6; -fx-border-color:#bfdbfe; -fx-border-width:1; -fx-padding:7 16; -fx-cursor:hand; -fx-font-size:13px;");
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setGraphic(null); return; }
                Cotacao c = (Cotacao) getTableRow().getItem();
                int idLogado = SessaoUsuario.getInstancia().getIdUsuarioLogado();
                boolean podeAprovar = isDiretor && c.getStatus().equals("AGUARDANDO_APROVACAO");
                boolean podeEditar  = c.getIdCadastrador() == idLogado
                        && c.getStatus().equals("AGUARDANDO_APROVACAO");
                if (podeAprovar || podeEditar) {
                    HBox box = new HBox(btn); box.setAlignment(Pos.CENTER); setGraphic(box);
                } else {
                    setGraphic(null);
                }
            }
        });

        // Zebra striping
        tabelaCotacoes.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Cotacao item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setStyle("-fx-background-color:white;");
                else setStyle(getIndex() % 2 == 0
                        ? "-fx-background-color:white;"
                        : "-fx-background-color:#fafafa;");
            }
        });
    }

    private void mostrarMenuAcoes(Button btn, Cotacao cotacao) {
        ContextMenu menu = new ContextMenu();
        int idLogado = SessaoUsuario.getInstancia().getIdUsuarioLogado();

        if (isDiretor && cotacao.getStatus().equals("AGUARDANDO_APROVACAO")) {
            MenuItem mAprovar = new MenuItem("✅   Aprovar cotação");
            mAprovar.setStyle("-fx-text-fill:#166534; -fx-font-size:15px; -fx-padding:6 10;");
            mAprovar.setOnAction(e -> onAprovarCotacao(cotacao));

            MenuItem mNegar = new MenuItem("❌   Negar cotação");
            mNegar.setStyle("-fx-text-fill:#991b1b; -fx-font-size:15px; -fx-padding:6 10;");
            mNegar.setOnAction(e -> onNegarCotacao(cotacao));

            menu.getItems().addAll(mAprovar, mNegar);
        }

        if (cotacao.getIdCadastrador() == idLogado
                && cotacao.getStatus().equals("AGUARDANDO_APROVACAO")) {
            MenuItem mEditar = new MenuItem("✏   Editar cotação");
            mEditar.setStyle("-fx-text-fill:#1e40af; -fx-font-size:15px; -fx-padding:6 10;");
            mEditar.setOnAction(e -> abrirEdicaoCotacao(cotacao));
            menu.getItems().add(mEditar);
        }

        MenuItem mAnexo = new MenuItem("📎   Ver anexo");
        mAnexo.setStyle("-fx-text-fill:#374151; -fx-font-size:15px; -fx-padding:6 10;");
        mAnexo.setOnAction(e -> abrirAnexo(cotacao));
        mAnexo.setDisable(!cotacao.temAnexo());

        if (!menu.getItems().isEmpty()) menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(mAnexo);
        menu.show(btn, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private void abrirEdicaoCotacao(Cotacao cotacao) {
        ObservableList<Pedido> pedidos = api.DAO.pedidoDAO.listarTodos();
        Pedido pedidoCompleto = pedidos.stream()
                .filter(p -> p.getIdPedido() == cotacao.getIdPedido())
                .findFirst().orElse(null);
        if (pedidoCompleto == null) { erro("Pedido não encontrado."); return; }
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/cadastroCotacao.fxml"));
            Node tela = loader.load();
            cadastroCotacaoController ctrl = loader.getController();
            ctrl.setAreaPrincipal(areaPrincipal);
            ctrl.setCotacaoEdicao(cotacao, pedidoCompleto);
            anchorar(tela);
            areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ── Ações do Diretor ──────────────────────────────────────

    private void onAprovarCotacao(Cotacao cotacao) {
        Dialog<String> dlg = criarDialogoParecer(
                "Aprovar Cotação",
                "Fornecedor: " + cotacao.getNomeFornecedor()
                        + "\nPedido: " + cotacao.getNumPedido()
                        + "\nValor: " + cotacao.getValorFormatado(),
                false);
        dlg.showAndWait().ifPresent(parecer -> {
            int idAprov = SessaoUsuario.getInstancia().getIdUsuarioLogado();
            if (CotacaoDAO.aprovar(cotacao.getIdCotacao(), idAprov, parecer)) {

                // ✅ ADICIONE ISTO: marca o pedido como EM_COTACAO ao aprovar cotação
                CotacaoDAO.marcarPedidoEmCotacao(cotacao.getIdPedido());

                HistoricoService.registrar("Cotação", "Aprovação", cotacao.getIdCotacao(),
                        "Cotação do pedido " + cotacao.getNumPedido()
                                + " aprovada por " + SessaoUsuario.getInstancia().getNomeUsuarioLogado()
                                + ". Parecer: " + parecer);
                sucesso("Cotação aprovada com sucesso!");
                recarregarMantenendoFiltro();
            } else erro("Erro ao aprovar cotação.");
        });
    }

    private void onNegarCotacao(Cotacao cotacao) {
        Dialog<String> dlg = criarDialogoParecer(
                "Negar Cotação",
                "Fornecedor: " + cotacao.getNomeFornecedor()
                        + "\nPedido: " + cotacao.getNumPedido()
                        + "\nValor: " + cotacao.getValorFormatado(),
                true);
        dlg.showAndWait().ifPresent(parecer -> {
            int idAprov = SessaoUsuario.getInstancia().getIdUsuarioLogado();
            if (CotacaoDAO.negar(cotacao.getIdCotacao(), idAprov, parecer)) {
                HistoricoService.registrar("Cotação", "Negação", cotacao.getIdCotacao(),
                        "Cotação do pedido " + cotacao.getNumPedido()
                                + " negada por " + SessaoUsuario.getInstancia().getNomeUsuarioLogado()
                                + ". Motivo: " + parecer);
                sucesso("Cotação negada.");
                recarregarMantenendoFiltro();
            } else erro("Erro ao negar cotação.");
        });
    }

    /**
     * Cria um diálogo customizado com TextArea para parecer obrigatório.
     * @param isNegacao true = título vermelho / label "Motivo"; false = verde / label "Parecer"
     */
    private Dialog<String> criarDialogoParecer(String titulo, String cabecalho, boolean isNegacao) {
        Dialog<String> dlg = new Dialog<>();
        dlg.setTitle(titulo);
        dlg.setHeaderText(cabecalho);

        ButtonType btnConfirmar = new ButtonType(
                isNegacao ? "Confirmar Negação" : "Confirmar Aprovação",
                ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar  = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dlg.getDialogPane().getButtonTypes().addAll(btnConfirmar, btnCancelar);

        TextArea areaParecer = new TextArea();
        areaParecer.setPromptText(isNegacao
                ? "Informe o motivo da negação (obrigatório)..."
                : "Informe o parecer da aprovação (obrigatório)...");
        areaParecer.setPrefRowCount(5);
        areaParecer.setWrapText(true);
        areaParecer.setStyle("-fx-font-size:14px;");

        Label lblErro = new Label();
        lblErro.setStyle("-fx-text-fill:#dc2626; -fx-font-size:13px;");

        VBox content = new VBox(10,
                new Label(isNegacao ? "Motivo da negação:" : "Parecer:"),
                areaParecer, lblErro);
        content.setStyle("-fx-padding:10;");
        content.setPrefWidth(480);

        // Estilo do label do campo
        ((Label) content.getChildren().get(0)).setStyle(
                "-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:"
                        + (isNegacao ? "#991b1b;" : "#166534;"));

        dlg.getDialogPane().setContent(content);

        // Bloqueia OK se parecer vazio
        javafx.scene.control.ButtonBar.ButtonData data = btnConfirmar.getButtonData();
        Node btnOk = dlg.getDialogPane().lookupButton(btnConfirmar);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            if (areaParecer.getText().trim().isBlank()) {
                lblErro.setText("O parecer é obrigatório.");
                ev.consume();
            }
        });

        dlg.setResultConverter(bt ->
                bt == btnConfirmar ? areaParecer.getText().trim() : null);
        return dlg;
    }

    private void recarregarMantenendoFiltro() {
        carregarCotacoes();
        if (pedidoFiltro != null) filtrarPorPedido(pedidoFiltro);
    }

    // ── Nova cotação ──────────────────────────────────────────

    private void navegarParaNovaCotacao(int idPedido) {
        ObservableList<api.model.Pedido> pedidos = api.DAO.pedidoDAO.listarTodos();
        api.model.Pedido pedidoCompleto = pedidos.stream()
                .filter(p -> p.getIdPedido() == idPedido)
                .findFirst().orElse(null);
        if (pedidoCompleto == null) { erro("Pedido não encontrado."); return; }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/cadastroCotacao.fxml"));
            Node tela = loader.load();
            cadastroCotacaoController ctrl = loader.getController();
            ctrl.setAreaPrincipal(areaPrincipal);
            ctrl.setPedido(pedidoCompleto);
            anchorar(tela);
            areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ── Abrir anexo ───────────────────────────────────────────

    private void abrirAnexo(Cotacao cotacao) {
        if (!cotacao.temAnexo()) { erro("Esta cotação não possui anexo."); return; }
        File arquivo = new File(cotacao.getCaminhoAnexo());
        if (!arquivo.exists()) { erro("Arquivo não encontrado:\n" + cotacao.getCaminhoAnexo()); return; }
        try { Desktop.getDesktop().open(arquivo); }
        catch (IOException e) { erro("Não foi possível abrir o arquivo:\n" + e.getMessage()); }
    }

    // ── Botão Voltar ──────────────────────────────────────────

    @FXML private void onVoltar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/pedido.fxml"));
            Node tela = loader.load();
            pedidoController ctrl = loader.getController();
            ctrl.setAreaPrincipal(areaPrincipal);
            anchorar(tela);
            areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ── Utilitários ───────────────────────────────────────────

    private void anchorar(Node tela) {
        AnchorPane.setTopAnchor   (tela, 0.0); AnchorPane.setBottomAnchor(tela, 0.0);
        AnchorPane.setLeftAnchor  (tela, 0.0); AnchorPane.setRightAnchor (tela, 0.0);
    }

    private String formatarStatus(String s) {
        return switch (s) {
            case "AGUARDANDO_APROVACAO" -> "Aguardando";
            case "APROVADO"             -> "Aprovado";
            case "NEGADO"               -> "Negado";
            default                     -> s;
        };
    }

    private String estiloBadge(String s) {
        String base = "-fx-background-radius:6; -fx-padding:4 10; -fx-font-size:12px; -fx-font-weight:bold;";
        return base + switch (s) {
            case "AGUARDANDO_APROVACAO" -> "-fx-background-color:#fef9c3; -fx-text-fill:#854d0e;";
            case "APROVADO"             -> "-fx-background-color:#dcfce7; -fx-text-fill:#166534;";
            case "NEGADO"               -> "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b;";
            default                     -> "-fx-background-color:#f3f4f6; -fx-text-fill:#374151;";
        };
    }

    private void sucesso(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Sucesso"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void erro(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erro"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}