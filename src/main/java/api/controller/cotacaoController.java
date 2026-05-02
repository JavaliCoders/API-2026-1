package api.controller;

import api.DAO.cotacaoDAO;
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
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class cotacaoController implements Initializable {

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

    @FXML private TextField        fieldBusca;
    @FXML private ComboBox<String> filtroStatus;
    @FXML private Label            labelTotal;

    @FXML private HBox  boxFiltradoPedido;
    @FXML private Label labelFiltradoPedido;
    @FXML private Button btnVoltar;

    @FXML private VBox  painelParecer;
    @FXML private Label labelParecer;

    private AnchorPane areaPrincipal;
    private Pedido     pedidoFiltro;

    private ObservableList<Cotacao> todasCotacoes;
    private FilteredList<Cotacao>   cotacoesFiltradas;

    private final boolean isDiretor    = PermissaoUtil.temPermissao("DIRETOR");
    private final boolean isFinanceiro = PermissaoUtil.temPermissao("FINANCEIRO");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarFiltros();
        configurarColunas();
        carregarCotacoes();

        tabelaCotacoes.getSelectionModel().selectedItemProperty()
                .addListener((obs, a, c) -> {
                    boolean temParecer = c != null && c.getParecer() != null
                            && !c.getParecer().isBlank();
                    painelParecer.setVisible(temParecer);
                    painelParecer.setManaged(temParecer);
                    if (temParecer) labelParecer.setText(c.getParecer());
                });
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
        todasCotacoes     = cotacaoDAO.listarTodas();
        cotacoesFiltradas = new FilteredList<>(todasCotacoes, c -> true);
        tabelaCotacoes.setItems(cotacoesFiltradas);
        reaplicarFiltro();
    }

    // ── Filtros ───────────────────────────────────────────────

    private void configurarFiltros() {
        filtroStatus.setItems(FXCollections.observableArrayList(
                "Todos os status", "AGUARDANDO_APROVACAO",
                "APROVADO", "APROVADO_PARCIALMENTE", "NEGADO"));
        filtroStatus.setValue("Todos os status");
        fieldBusca  .textProperty() .addListener((o, a, n) -> reaplicarFiltro());
        filtroStatus.valueProperty().addListener((o, a, n) -> reaplicarFiltro());
    }

    private void reaplicarFiltro() {
        if (cotacoesFiltradas == null) return;
        String busca  = fieldBusca.getText() == null ? "" : fieldBusca.getText().trim().toLowerCase();
        String status = filtroStatus.getValue();

        cotacoesFiltradas.setPredicate(c -> {
            if (pedidoFiltro != null && c.getIdPedido() != pedidoFiltro.getIdPedido())
                return false;
            boolean okB = busca.isEmpty()
                    || c.getNumPedido().toLowerCase().contains(busca)
                    || c.getNomeFornecedor().toLowerCase().contains(busca);
            boolean okS = status == null || status.equals("Todos os status")
                    || c.getStatus().equals(status);
            return okB && okS;
        });
        atualizarLabelTotal();
    }

    @FXML private void onLimparFiltro() {
        fieldBusca.clear();
        filtroStatus.setValue("Todos os status");
    }

    private void atualizarLabelTotal() {
        labelTotal.setText("Total: " + cotacoesFiltradas.size() + " cotação(ões)");
    }

    // ── Colunas ───────────────────────────────────────────────

    private void configurarColunas() {
        colNumPedido .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumPedido()));
        colFornecedor.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeFornecedor()));
        colValor     .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getValorFormatado()));
        colData      .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataCriacaoFormatada()));
        colAprovador .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeAprovador()));
        colDataAprov .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataAprovacaoFormatada()));

        // Fonte maior nas colunas de texto
        for (TableColumn<Cotacao, String> col : new TableColumn[]{
                colNumPedido, colFornecedor, colValor, colData, colAprovador, colDataAprov}) {
            col.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); return; }
                    setText(item);
                    setFont(Font.font("Segoe UI", 13));
                    setStyle("-fx-text-fill: #0f172a;");
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

        // Badge de status
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(formatarStatus(status));
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                badge.setAlignment(Pos.CENTER);
                badge.setPrefWidth(110);
                badge.setStyle(estiloBadge(status));
                HBox box = new HBox(badge);
                box.setAlignment(Pos.CENTER);
                setGraphic(box); setText(null);
            }
        });

        // Coluna Anexo — botão azul para abrir arquivo
        colAnexo.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("📎  Abrir");
            {
                btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                aplicarEstiloAnexo(false);
                btn.setOnMouseEntered(e -> aplicarEstiloAnexo(true));
                btn.setOnMouseExited (e -> aplicarEstiloAnexo(false));
                btn.setOnAction(e -> abrirAnexo(getTableView().getItems().get(getIndex())));
            }
            private void aplicarEstiloAnexo(boolean hover) {
                btn.setStyle(hover
                        ? "-fx-background-color:#2563eb; -fx-text-fill:white; -fx-background-radius:6; -fx-border-color:transparent; -fx-padding:5 12; -fx-cursor:hand;"
                        : "-fx-background-color:#dbeafe; -fx-text-fill:#1e40af; -fx-background-radius:6; -fx-border-color:transparent; -fx-padding:5 12; -fx-cursor:hand;");
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
                btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                aplicarEstiloNova(false);
                btn.setOnMouseEntered(e -> aplicarEstiloNova(true));
                btn.setOnMouseExited (e -> aplicarEstiloNova(false));
                btn.setOnAction(e -> {
                    Cotacao c = getTableView().getItems().get(getIndex());
                    navegarParaNovaCotacao(c.getIdPedido());
                });
            }
            private void aplicarEstiloNova(boolean hover) {
                btn.setStyle(hover
                        ? "-fx-background-color:#16a34a; -fx-text-fill:white; -fx-background-radius:6; -fx-border-color:transparent; -fx-padding:5 12; -fx-cursor:hand;"
                        : "-fx-background-color:#dcfce7; -fx-text-fill:#166534; -fx-background-radius:6; -fx-border-color:transparent; -fx-padding:5 12; -fx-cursor:hand;");
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setGraphic(null); return; }
                HBox box = new HBox(btn); box.setAlignment(Pos.CENTER); setGraphic(box);
            }
        });

        // Coluna Ações — só DIRETOR, só AGUARDANDO
        colAcoes.setVisible(isDiretor);
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("⚙  Ações  ▾");
            {
                btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                aplicarEstiloAcoes(false);
                btn.setOnMouseEntered(e -> aplicarEstiloAcoes(true));
                btn.setOnMouseExited (e -> aplicarEstiloAcoes(false));
                btn.setOnAction(e -> {
                    Cotacao c = getTableView().getItems().get(getIndex());
                    mostrarMenuAcoes(btn, c);
                });
            }
            private void aplicarEstiloAcoes(boolean hover) {
                btn.setStyle(hover
                        ? "-fx-background-color:#1e40af; -fx-text-fill:white; -fx-background-radius:6; -fx-border-color:transparent; -fx-padding:5 12; -fx-cursor:hand;"
                        : "-fx-background-color:#eff6ff; -fx-text-fill:#1e40af; -fx-background-radius:6; -fx-border-color:#bfdbfe; -fx-border-width:1; -fx-padding:5 12; -fx-cursor:hand;");
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setGraphic(null); return; }
                Cotacao c = (Cotacao) getTableRow().getItem();
                boolean mostrar = isDiretor && c.getStatus().equals("AGUARDANDO_APROVACAO");
                if (mostrar) {
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
                if (empty || item == null) setStyle("-fx-background-color: white;");
                else setStyle(getIndex() % 2 == 0
                        ? "-fx-background-color: white;"
                        : "-fx-background-color: #fafafa;");
            }
        });
    }

    private void mostrarMenuAcoes(Button btn, Cotacao cotacao) {
        ContextMenu menu = new ContextMenu();

        MenuItem mAprovar = new MenuItem("✅  Aprovar cotação");
        mAprovar.setStyle("-fx-text-fill: #166534; -fx-font-size: 13px;");
        mAprovar.setOnAction(e -> onAprovarCotacao(cotacao));

        MenuItem mNegar = new MenuItem("❌  Negar cotação");
        mNegar.setStyle("-fx-text-fill: #991b1b; -fx-font-size: 13px;");
        mNegar.setOnAction(e -> onNegarCotacao(cotacao));

        MenuItem mAnexo = new MenuItem("📎  Ver anexo");
        mAnexo.setStyle("-fx-text-fill: #1e40af; -fx-font-size: 13px;");
        mAnexo.setOnAction(e -> abrirAnexo(cotacao));
        mAnexo.setDisable(!cotacao.temAnexo());

        menu.getItems().addAll(mAprovar, mNegar, new SeparatorMenuItem(), mAnexo);
        menu.show(btn, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    // ── Ações do Diretor ──────────────────────────────────────

    private void onAprovarCotacao(Cotacao cotacao) {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Aprovar Cotação");
        dlg.setHeaderText("Fornecedor: " + cotacao.getNomeFornecedor()
                + "\nPedido: " + cotacao.getNumPedido()
                + "\nValor: " + cotacao.getValorFormatado());
        dlg.setContentText("Parecer (opcional):");
        dlg.showAndWait().ifPresent(parecer -> {
            int idAprov = SessaoUsuario.getInstancia().getIdUsuarioLogado();
            if (cotacaoDAO.aprovar(cotacao.getIdCotacao(), idAprov, parecer)) {
                HistoricoService.registrar("Cotação", "Aprovação", cotacao.getIdCotacao(),
                        "Cotação do pedido " + cotacao.getNumPedido()
                                + " aprovada por " + SessaoUsuario.getInstancia().getNomeUsuarioLogado());
                sucesso("Cotação aprovada com sucesso!");
                recarregarMantenendoFiltro();
            } else erro("Erro ao aprovar cotação.");
        });
    }

    private void onNegarCotacao(Cotacao cotacao) {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Negar Cotação");
        dlg.setHeaderText("Fornecedor: " + cotacao.getNomeFornecedor()
                + "\nPedido: " + cotacao.getNumPedido()
                + "\nValor: " + cotacao.getValorFormatado());
        dlg.setContentText("Motivo da negação (obrigatório):");
        dlg.showAndWait().ifPresent(parecer -> {
            if (parecer.isBlank()) { erro("Informe o motivo."); return; }
            int idAprov = SessaoUsuario.getInstancia().getIdUsuarioLogado();
            if (cotacaoDAO.negar(cotacao.getIdCotacao(), idAprov, parecer)) {
                HistoricoService.registrar("Cotação", "Negação", cotacao.getIdCotacao(),
                        "Cotação do pedido " + cotacao.getNumPedido()
                                + " negada por " + SessaoUsuario.getInstancia().getNomeUsuarioLogado()
                                + ". Motivo: " + parecer);
                sucesso("Cotação negada.");
                recarregarMantenendoFiltro();
            } else erro("Erro ao negar cotação.");
        });
    }

    private void recarregarMantenendoFiltro() {
        carregarCotacoes();
        if (pedidoFiltro != null) filtrarPorPedido(pedidoFiltro);
    }

    // ── Nova cotação para o mesmo pedido ─────────────────────

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
        AnchorPane.setTopAnchor   (tela, 0.0);
        AnchorPane.setBottomAnchor(tela, 0.0);
        AnchorPane.setLeftAnchor  (tela, 0.0);
        AnchorPane.setRightAnchor (tela, 0.0);
    }

    private String formatarStatus(String s) {
        return switch (s) {
            case "AGUARDANDO_APROVACAO"  -> "Aguardando";
            case "APROVADO"              -> "Aprovado";
            case "APROVADO_PARCIALMENTE" -> "Aprovado Parcial";
            case "NEGADO"                -> "Negado";
            default                      -> s;
        };
    }

    private String estiloBadge(String s) {
        String base = "-fx-background-radius:6; -fx-padding:4 10; -fx-font-size:11px; -fx-font-weight:bold;";
        return base + switch (s) {
            case "AGUARDANDO_APROVACAO"  -> "-fx-background-color:#fef9c3; -fx-text-fill:#854d0e;";
            case "APROVADO"              -> "-fx-background-color:#dcfce7; -fx-text-fill:#166534;";
            case "APROVADO_PARCIALMENTE" -> "-fx-background-color:#d1fae5; -fx-text-fill:#065f46;";
            case "NEGADO"                -> "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b;";
            default                      -> "-fx-background-color:#f3f4f6; -fx-text-fill:#374151;";
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