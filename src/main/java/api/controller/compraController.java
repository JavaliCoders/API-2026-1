package api.controller;

import api.DAO.compraDAO;
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

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class compraController implements Initializable {

    // ── Tabela ────────────────────────────────────────────────
    @FXML private TableView<Compra>           tabelaCompras;
    @FXML private TableColumn<Compra, String> colNum;
    @FXML private TableColumn<Compra, String> colFornecedor;
    @FXML private TableColumn<Compra, String> colValor;
    @FXML private TableColumn<Compra, String> colData;
    @FXML private TableColumn<Compra, String> colDataPrevista;
    @FXML private TableColumn<Compra, String> colComprador;
    @FXML private TableColumn<Compra, String> colStatus;
    @FXML private TableColumn<Compra, Void>   colNovaCompra;
    @FXML private TableColumn<Compra, Void>   colAcoes;

    // ── Filtros ───────────────────────────────────────────────
    @FXML private TextField        fieldBusca;
    @FXML private ComboBox<String> filtroStatus;
    @FXML private DatePicker       filtroDataInicio;
    @FXML private DatePicker       filtroDataFim;

    // ── Banner filtro por pedido ──────────────────────────────
    @FXML private HBox   boxFiltradoPedido;
    @FXML private Label  labelFiltradoPedido;
    @FXML private Button btnVoltar;

    // ── Overlay detalhes ──────────────────────────────────────
    @FXML private StackPane overlayDetalhes;
    @FXML private Label     detalheNumPedido;
    @FXML private Label     detalheFornecedor;
    @FXML private Label     detalheValor;
    @FXML private Label     detalheComprador;
    @FXML private Label     detalheDataCompra;
    @FXML private Label     detalheDataPrevista;
    @FXML private Label     detalheStatus;

    @FXML private TableView<CompraItem>           tabelaItensDetalhe;
    @FXML private TableColumn<CompraItem, String> colItemProduto;
    @FXML private TableColumn<CompraItem, String> colItemUnidade;
    @FXML private TableColumn<CompraItem, String> colItemQtd;
    @FXML private TableColumn<CompraItem, String> colItemValorUni;
    @FXML private TableColumn<CompraItem, String> colItemTotal;

    private AnchorPane areaPrincipal;
    private Pedido     pedidoFiltro;

    private ObservableList<Compra> todasCompras;
    private FilteredList<Compra>   comprasFiltradas;

    private final boolean isFinanceiro = PermissaoUtil.temPermissao("FINANCEIRO");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarFiltros();
        configurarColunas();
        carregarCompras();
        configurarTabelaItens();

        tabelaCompras.getSelectionModel().selectedItemProperty()
                .addListener((obs, a, c) -> { if (c != null) abrirOverlay(c); });
    }

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }

    public void filtrarPorPedido(Pedido pedido) {
        this.pedidoFiltro = pedido;
        btnVoltar.setVisible(true);
        btnVoltar.setManaged(true);
        btnVoltar.setOnAction(e -> onVoltar());
        boxFiltradoPedido.setVisible(true);
        boxFiltradoPedido.setManaged(true);
        labelFiltradoPedido.setText("Compras do pedido " + pedido.getNumPedido()
                + "  —  " + pedido.getNomeSolicitante());
        reaplicarFiltro();
    }

    // ── Dados ─────────────────────────────────────────────────

    private void carregarCompras() {
        todasCompras     = compraDAO.listarTodas();
        comprasFiltradas = new FilteredList<>(todasCompras, c -> true);
        tabelaCompras.setItems(comprasFiltradas);
        reaplicarFiltro();
    }

    private void configurarTabelaItens() {
        colItemProduto .setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNomeProduto()));
        colItemUnidade .setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getUnidade()));
        colItemQtd     .setCellValueFactory(d ->
                new SimpleStringProperty(
                        String.valueOf((int) d.getValue().getQtdComprada())));
        colItemValorUni.setCellValueFactory(d ->
                new SimpleStringProperty(formatarMoeda(d.getValue().getValorUni())));
        colItemTotal   .setCellValueFactory(d ->
                new SimpleStringProperty(formatarMoeda(d.getValue().getValorTotal())));

        for (TableColumn<CompraItem, String> col : new TableColumn[]{
                colItemProduto, colItemUnidade, colItemQtd,
                colItemValorUni, colItemTotal}) {
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
    }

    // ── Filtros ───────────────────────────────────────────────

    private void configurarFiltros() {
        filtroStatus.setItems(FXCollections.observableArrayList(
                "Todos os status", "REALIZADA", "CANCELADA"));
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
        if (comprasFiltradas == null) return;
        String    busca  = fieldBusca.getText() == null ? "" : fieldBusca.getText().trim().toLowerCase();
        String    status = filtroStatus.getValue();
        LocalDate di     = filtroDataInicio.getValue();
        LocalDate df     = filtroDataFim   .getValue();

        comprasFiltradas.setPredicate(c -> {
            if (pedidoFiltro != null && c.getPedido().getIdPedido() != pedidoFiltro.getIdPedido())
                return false;
            boolean okB = busca.isEmpty()
                    || c.getNumPedido()      .toLowerCase().contains(busca)
                    || c.getNomeFornecedor() .toLowerCase().contains(busca);
            boolean okS = status == null || status.equals("Todos os status")
                    || c.getStatus().equals(status);
            LocalDate dataCompra = c.getData().toLocalDate();
            boolean okDi = di == null || !dataCompra.isBefore(di);
            boolean okDf = df == null || !dataCompra.isAfter(df);
            return okB && okS && okDi && okDf;
        });
    }

    // ── Overlay de detalhes ───────────────────────────────────

    private void abrirOverlay(Compra c) {
        detalheNumPedido  .setText(c.getNumPedido());
        detalheFornecedor .setText(c.getNomeFornecedor());
        detalheValor      .setText(formatarMoeda(c.getValorTotal()));
        detalheComprador  .setText(c.getNomeComprador());
        detalheDataCompra .setText(c.getDataFormatada());
        detalheDataPrevista.setText(c.getDataPrevistaFormatada());
        detalheStatus     .setText(c.getStatus());
        detalheStatus     .setStyle(estiloBadge(c.getStatus()));

        overlayDetalhes.setVisible(true);
        overlayDetalhes.setManaged(true);
        ObservableList<CompraItem> itens =
                compraDAO.listarItensPorCompra(c.getIdCompra());
        tabelaItensDetalhe.setItems(itens);
    }

    @FXML private void fecharDetalhes() {
        overlayDetalhes.setVisible(false);
        overlayDetalhes.setManaged(false);
        tabelaCompras.getSelectionModel().clearSelection();
    }

    // ── Colunas ───────────────────────────────────────────────

    private void configurarColunas() {
        colNum        .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumPedido()));
        colFornecedor .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeFornecedor()));
        colValor      .setCellValueFactory(d -> new SimpleStringProperty(formatarMoeda(d.getValue().getValorTotal())));
        colData       .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataFormatada()));
        colDataPrevista.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataPrevistaFormatada()));
        colComprador  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeComprador()));

        for (TableColumn<Compra, String> col : new TableColumn[]{
                colNum, colFornecedor, colValor, colData, colDataPrevista, colComprador}) {
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
        colNum        .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumPedido()));
        colFornecedor .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeFornecedor()));
        colValor      .setCellValueFactory(d -> new SimpleStringProperty(formatarMoeda(d.getValue().getValorTotal())));
        colData       .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataFormatada()));
        colDataPrevista.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataPrevistaFormatada()));
        colComprador  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeComprador()));

        // Badge status
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(status);
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                badge.setAlignment(Pos.CENTER);
                badge.setPrefWidth(100);
                badge.setStyle(estiloBadge(status));
                HBox box = new HBox(badge); box.setAlignment(Pos.CENTER);
                setGraphic(box); setText(null);
            }
        });

        // Coluna +Compra — verde, só FINANCEIRO, só compras de pedidos com itens pendentes
        colNovaCompra.setVisible(isFinanceiro);
        colNovaCompra.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("➕  Compra");
            {
                btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                estilo(false);
                btn.setOnMouseEntered(e -> estilo(true));
                btn.setOnMouseExited (e -> estilo(false));
                btn.setOnAction(e -> {
                    Compra c = getTableView().getItems().get(getIndex());
                    navegarParaNovaCompra(c.getPedido().getIdPedido());
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
                Compra c = (Compra) getTableRow().getItem();
                // Só exibe se o pedido ainda tiver itens pendentes de compra
                if (isFinanceiro && compraDAO.pedidoTemItensPendentes(c.getPedido().getIdPedido())) {
                    HBox box = new HBox(btn); box.setAlignment(Pos.CENTER); setGraphic(box);
                } else { setGraphic(null); }
            }
        });

        // Coluna Ações — cancelar, só FINANCEIRO, só REALIZADA
        colAcoes.setVisible(isFinanceiro);
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Cancelar");
            {
                btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                estilo(false);
                btn.setOnMouseEntered(e -> estilo(true));
                btn.setOnMouseExited (e -> estilo(false));
                btn.setOnAction(e -> {
                    Compra c = getTableView().getItems().get(getIndex());
                    onCancelarCompra(c);
                });
            }
            private void estilo(boolean hover) {
                btn.setStyle(hover
                        ? "-fx-background-color:#fca5a5; -fx-text-fill:#991b1b; -fx-background-radius:6; -fx-border-color:transparent; -fx-padding:7 14; -fx-cursor:hand; -fx-font-size:13px;"
                        : "-fx-background-color:#fee2e2; -fx-text-fill:#dc2626; -fx-background-radius:6; -fx-border-color:transparent; -fx-padding:7 14; -fx-cursor:hand; -fx-font-size:13px;");
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setGraphic(null); return; }
                Compra c = (Compra) getTableRow().getItem();
                if ("REALIZADA".equals(c.getStatus())) {
                    HBox box = new HBox(btn); box.setAlignment(Pos.CENTER); setGraphic(box);
                } else { setGraphic(null); }
            }
        });

        // Zebra striping
        tabelaCompras.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Compra item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setStyle("-fx-background-color:white;");
                else setStyle(getIndex() % 2 == 0
                        ? "-fx-background-color:white;"
                        : "-fx-background-color:#fafafa;");
            }
        });
    }

    // ── Nova compra ───────────────────────────────────────────

    private void navegarParaNovaCompra(int idPedido) {
        ObservableList<Pedido> pedidos = api.DAO.pedidoDAO.listarTodos();
        Pedido pedidoCompleto = pedidos.stream()
                .filter(p -> p.getIdPedido() == idPedido)
                .findFirst().orElse(null);
        if (pedidoCompleto == null) { erro("Pedido não encontrado."); return; }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/cadastroCompra.fxml"));
            Node tela = loader.load();
            cadastroCompraController ctrl = loader.getController();
            ctrl.setAreaPrincipal(areaPrincipal);
            ctrl.setPedidoSelecionado(pedidoCompleto);
            anchorar(tela);
            areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ── Cancelar ─────────────────────────────────────────────

    private void onCancelarCompra(Compra compra) {
        Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
        dlg.setTitle("Cancelar Compra");
        dlg.setHeaderText(null);
        dlg.setContentText("Deseja cancelar a compra do pedido " + compra.getNumPedido() + "?\n"
                + "As quantidades compradas serão revertidas.");
        dlg.showAndWait().ifPresent(b -> {
            if (b == ButtonType.OK) {
                boolean ok = compraDAO.cancelar(compra.getIdCompra());
                if (ok) {
                    HistoricoService.registrar("Compra", "Cancelamento", compra.getIdCompra(),
                            "Compra cancelada — pedido " + compra.getNumPedido()
                                    + " por " + SessaoUsuario.getInstancia().getNomeUsuarioLogado());
                    new Alert(Alert.AlertType.INFORMATION, "Compra cancelada com sucesso.").showAndWait();
                    carregarCompras();
                } else {
                    erro("Erro ao cancelar a compra.");
                }
            }
        });
    }

    // ── Botão Voltar ──────────────────────────────────────────

    private void onVoltar() {
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

    private String estiloBadge(String s) {
        String base = "-fx-background-radius:6; -fx-padding:4 10; -fx-font-size:12px; -fx-font-weight:bold;";
        return base + switch (s) {
            case "REALIZADA" -> "-fx-background-color:#dcfce7; -fx-text-fill:#166534;";
            case "CANCELADA" -> "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b;";
            default          -> "-fx-background-color:#f3f4f6; -fx-text-fill:#374151;";
        };
    }

    private String formatarMoeda(double valor) {
        return String.format("R$ %.2f", valor).replace(".", ",");
    }

    private void erro(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erro"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}