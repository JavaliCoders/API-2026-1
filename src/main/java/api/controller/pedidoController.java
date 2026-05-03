package api.controller;

import api.DAO.pedidoDAO;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class pedidoController implements Initializable {

    // ── Tabela principal ──────────────────────────────────────
    @FXML private TableView<Pedido>           tabelaPedidos;
    @FXML private TableColumn<Pedido, String> colNum;
    @FXML private TableColumn<Pedido, String> colData;
    @FXML private TableColumn<Pedido, String> colSolicitante;
    @FXML private TableColumn<Pedido, String> colSetor;
    @FXML private TableColumn<Pedido, String> colCentro;
    @FXML private TableColumn<Pedido, String> colValor;
    @FXML private TableColumn<Pedido, String> colStatus;
    @FXML private TableColumn<Pedido, Void>   colEditar;
    @FXML private TableColumn<Pedido, Void>   colAcoes;

    // ── Filtros ───────────────────────────────────────────────
    @FXML private TextField        searchNum;
    @FXML private TextField        searchSolicitante;
    @FXML private ComboBox<String> filtroStatus;
    @FXML private DatePicker       filtroDataInicio;
    @FXML private DatePicker       filtroDataFim;

    // ── Overlay ───────────────────────────────────────────────
    @FXML private StackPane overlayDetalhes;
    @FXML private Label     detalheNum;
    @FXML private Label     detalheData;
    @FXML private Label     detalheStatus;
    @FXML private Label     detalheSolicitante;
    @FXML private Label     detalheValor;
    @FXML private Label     detalheSetor;
    @FXML private Label     detalheCentro;

    @FXML private TableView<PedidoProduto>            tabelaItensPedido;
    @FXML private TableColumn<PedidoProduto, String>  colItemProduto;
    @FXML private TableColumn<PedidoProduto, String>  colItemUnidade;
    @FXML private TableColumn<PedidoProduto, String>  colItemQtdSolic;
    @FXML private TableColumn<PedidoProduto, String>  colItemQtdAprov;
    @FXML private TableColumn<PedidoProduto, String>  colItemValorUnit;
    @FXML private TableColumn<PedidoProduto, String>  colItemValorTotal;

    @FXML private VBox  secaoAprovacao;
    @FXML private Label detalheAprovador;
    @FXML private Label detalheDataAprovacao;
    @FXML private Label detalheParecer;

    private AnchorPane areaPrincipal;
    private ObservableList<Pedido> todosPedidos;
    private FilteredList<Pedido>   pedidosFiltrados;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final boolean isDiretor     = PermissaoUtil.temPermissao("DIRETOR");
    private final boolean isFinanceiro  = PermissaoUtil.temPermissao("FINANCEIRO");
    private final boolean podeGerenciar = isDiretor || isFinanceiro;
    private final int     idUsuarioLogado = SessaoUsuario.getInstancia().getIdUsuarioLogado();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarFiltros();
        configurarColunas();
        configurarOverlayItens();
        carregarPedidos();

        tabelaPedidos.getSelectionModel().selectedItemProperty()
                .addListener((obs, a, p) -> { if (p != null) abrirOverlay(p); });
    }

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }

    // ── Dados ─────────────────────────────────────────────────

    private void carregarPedidos() {
        todosPedidos     = pedidoDAO.listarTodos();
        pedidosFiltrados = new FilteredList<>(todosPedidos, p -> true);
        tabelaPedidos.setItems(pedidosFiltrados);
    }

    // ── Filtros ───────────────────────────────────────────────

    private void configurarFiltros() {
        filtroStatus.setItems(FXCollections.observableArrayList(
                "Todos os status", "EM_APROVACAO", "APROVADO",
                "APROVADO_PARCIALMENTE", "NEGADO",
                "EM_COTACAO", "EM_COMPRA", "FINALIZADO"));
        filtroStatus.setValue("Todos os status");

        // Listeners
        searchNum.textProperty()         .addListener((o, a, n) -> aplicarFiltro());
        searchSolicitante.textProperty() .addListener((o, a, n) -> aplicarFiltro());
        filtroStatus.valueProperty()     .addListener((o, a, n) -> aplicarFiltro());
        filtroDataInicio.valueProperty() .addListener((o, a, n) -> aplicarFiltro());
        filtroDataFim.valueProperty()    .addListener((o, a, n) -> aplicarFiltro());
    }

    @FXML private void onSearch(KeyEvent e) { aplicarFiltro(); }
    @FXML private void onFiltroStatus()     { aplicarFiltro(); }

    @FXML private void onLimparFiltros() {
        searchNum.clear();
        searchSolicitante.clear();
        filtroStatus.setValue("Todos os status");
        filtroDataInicio.setValue(null);
        filtroDataFim.setValue(null);
    }

    private void aplicarFiltro() {
        String num   = searchNum.getText() == null         ? "" : searchNum.getText().trim().toLowerCase();
        String soli  = searchSolicitante.getText() == null ? "" : searchSolicitante.getText().trim().toLowerCase();
        String stat  = filtroStatus.getValue();
        LocalDate di = filtroDataInicio.getValue();
        LocalDate df = filtroDataFim.getValue();

        pedidosFiltrados.setPredicate(p -> {
            boolean okNum  = num.isEmpty()  || p.getNumPedido().toLowerCase().contains(num);
            boolean okSoli = soli.isEmpty() || p.getNomeSolicitante().toLowerCase().contains(soli);
            boolean okStat = stat == null   || stat.equals("Todos os status") || p.getStatus().equals(stat);

            LocalDate dataAb = p.getDataAbertura().toLocalDate();
            boolean okDi = di == null || !dataAb.isBefore(di);
            boolean okDf = df == null || !dataAb.isAfter(df);

            return okNum && okSoli && okStat && okDi && okDf;
        });
    }

    // ── Overlay ───────────────────────────────────────────────

    private void abrirOverlay(Pedido p) {
        detalheNum        .setText(p.getNumPedido());
        detalheData       .setText(p.getDataAberturaFormatada());
        detalheSolicitante.setText(p.getNomeSolicitante());
        detalheValor      .setText(String.format("R$ %.2f", p.getValorTotalEstimado()).replace(".", ","));
        detalheSetor      .setText(p.getNomeSetor());
        detalheCentro     .setText(p.getNomeCentroCusto());
        detalheStatus     .setText(formatarStatus(p.getStatus()));
        detalheStatus     .setStyle(estiloBadge(p.getStatus()));

        // Itens com qtd solicitada E aprovada
        tabelaItensPedido.setItems(pedidoDAO.listarItens(p.getIdPedido()));

        // Seção de aprovação — visível para todos quando há decisão
        boolean temDecisao = p.getStatus().equals("APROVADO")
                || p.getStatus().equals("NEGADO")
                || p.getStatus().equals("APROVADO_PARCIALMENTE");

        if (temDecisao) {
            detalheAprovador.setText(p.getAprovador() != null ? p.getAprovador().getNome() : "—");
            detalheAprovador.setStyle(
                    (p.getStatus().equals("NEGADO")
                            ? "-fx-font-size:14px; -fx-text-fill:#991b1b; -fx-font-weight:bold;"
                            : "-fx-font-size:14px; -fx-text-fill:#166534; -fx-font-weight:bold;"));
            detalheDataAprovacao.setText(
                    p.getDataAprovacao() != null ? p.getDataAprovacao().format(FMT) : "—");
            detalheParecer.setText(
                    p.getParecer() != null && !p.getParecer().isBlank()
                            ? p.getParecer() : "Sem parecer.");
            secaoAprovacao.setVisible(true);
            secaoAprovacao.setManaged(true);
        } else {
            secaoAprovacao.setVisible(false);
            secaoAprovacao.setManaged(false);
        }

        overlayDetalhes.setVisible(true);
        overlayDetalhes.setManaged(true);
    }

    @FXML private void fecharDetalhes() {
        overlayDetalhes.setVisible(false);
        overlayDetalhes.setManaged(false);
        tabelaPedidos.getSelectionModel().clearSelection();
    }

    private void configurarOverlayItens() {
        colItemProduto  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeProduto()));
        colItemUnidade  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnidadeProduto()));
        colItemQtdSolic .setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getQtdSolicitada())));

        // Qtd aprovada — destaca em vermelho se diferente da solicitada
        colItemQtdAprov.setCellValueFactory(d -> {
            PedidoProduto item = d.getValue();
            int aprov = item.getQtdAprovada();
            return new SimpleStringProperty(aprov == 0 ? "—" : String.valueOf(aprov));
        });
        colItemQtdAprov.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || getTableRow() == null
                        || getTableRow().getItem() == null) {
                    setText(null); setStyle(""); return;
                }
                PedidoProduto pp = (PedidoProduto) getTableRow().getItem();
                setText(item);
                setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                if (pp.getQtdAprovada() < pp.getQtdSolicitada()) {
                    setStyle("-fx-text-fill: #dc2626;");
                } else {
                    setStyle("-fx-text-fill: #166534;");
                }
            }
        });

        colItemValorUnit .setCellValueFactory(d -> new SimpleStringProperty(
                String.format("R$ %.2f", d.getValue().getValorUnitario()).replace(".", ",")));
        colItemValorTotal.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("R$ %.2f", d.getValue().getValorTotal()).replace(".", ",")));

        // Zebra striping na tabela interna
        tabelaItensPedido.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(PedidoProduto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setStyle("-fx-background-color: white;");
                else setStyle(getIndex() % 2 == 0 ? "-fx-background-color: white;"
                        : "-fx-background-color: #fafafa;");
            }
        });
    }

    // ── Colunas tabela principal ──────────────────────────────

    private void configurarColunas() {
        colNum        .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumPedido()));
        colData       .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataAberturaFormatada()));
        colSolicitante.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeSolicitante()));
        colSetor      .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeSetor()));
        colCentro     .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeCentroCusto()));
        colValor      .setCellValueFactory(d -> new SimpleStringProperty(
                String.format("R$ %.2f", d.getValue().getValorTotalEstimado()).replace(".", ",")));

        // Fonte 13px cor #0f172a — igual ao estoque
        for (TableColumn<Pedido, String> col : new TableColumn[]{
                colNum, colData, colSolicitante, colSetor, colCentro, colValor}) {
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
        colData       .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataAberturaFormatada()));
        colSolicitante.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeSolicitante()));
        colSetor      .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeSetor()));
        colCentro     .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeCentroCusto()));
        colValor      .setCellValueFactory(d -> new SimpleStringProperty(
                String.format("R$ %.2f", d.getValue().getValorTotalEstimado()).replace(".", ",")));

        // Badge status — igual ao estoque
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(formatarStatus(status));
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                badge.setAlignment(Pos.CENTER);
                badge.setPrefWidth(115);
                badge.setStyle(estiloBadge(status));
                HBox box = new HBox(badge); box.setAlignment(Pos.CENTER);
                setGraphic(box); setText(null);
            }
        });

        // Coluna Editar
        colEditar.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("✏️  Editar");
            {
                btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                estilo(false);
                btn.setOnMouseEntered(e -> estilo(true));
                btn.setOnMouseExited (e -> estilo(false));
                btn.setOnAction(e -> {
                    Pedido p = getTableView().getItems().get(getIndex());
                    fecharDetalhes(); navegarParaEditar(p);
                });
            }
            private void estilo(boolean hover) {
                btn.setStyle(hover
                        ? "-fx-background-color:#2563eb; -fx-text-fill:white; -fx-background-radius:6; -fx-border-color:transparent; -fx-padding:6 14; -fx-cursor:hand;"
                        : "-fx-background-color:#dbeafe; -fx-text-fill:#1e40af; -fx-background-radius:6; -fx-border-color:transparent; -fx-padding:6 14; -fx-cursor:hand;");
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setGraphic(null); return; }
                Pedido p = (Pedido) getTableRow().getItem();
                if (p.getStatus().equals("EM_APROVACAO")
                        && p.getSolicitante().getIdUsuario() == idUsuarioLogado) {
                    HBox box = new HBox(btn); box.setAlignment(Pos.CENTER); setGraphic(box);
                } else { setGraphic(null); }
            }
        });

        // Coluna Ações
        colAcoes.setVisible(podeGerenciar);
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("⚙  Ações  ▾");
            {
                btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                estilo(false);
                btn.setOnMouseEntered(e -> estilo(true));
                btn.setOnMouseExited (e -> estilo(false));
                btn.setOnAction(e -> {
                    Pedido p = getTableView().getItems().get(getIndex());
                    fecharDetalhes(); mostrarMenuAcoes(btn, p);
                });
            }
            private void estilo(boolean hover) {
                btn.setStyle(hover
                        ? "-fx-background-color:#1e40af; -fx-text-fill:white; -fx-background-radius:6; -fx-border-color:transparent; -fx-padding:6 14; -fx-cursor:hand;"
                        : "-fx-background-color:#eff6ff; -fx-text-fill:#1e40af; -fx-background-radius:6; -fx-border-color:#bfdbfe; -fx-border-width:1; -fx-padding:6 14; -fx-cursor:hand;");
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setGraphic(null); return; }
                Pedido p = (Pedido) getTableRow().getItem();
                if (temAlgumaAcao(p)) {
                    HBox box = new HBox(btn); box.setAlignment(Pos.CENTER); setGraphic(box);
                } else { setGraphic(null); }
            }
        });

        // Zebra striping — igual ao estoque
        tabelaPedidos.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Pedido item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setStyle("-fx-background-color: white;");
                else setStyle(getIndex() % 2 == 0
                        ? "-fx-background-color: white;"
                        : "-fx-background-color: #fafafa;");
            }
        });
    }

    private boolean temAlgumaAcao(Pedido p) {
        String s = p.getStatus();
        if (isDiretor    && (s.equals("EM_APROVACAO") || s.equals("NEGADO")))
            return true;
        if (isFinanceiro && (s.equals("APROVADO") || s.equals("APROVADO_PARCIALMENTE")
                || s.equals("EM_APROVACAO") || s.equals("NEGADO")))
            return true;
        return false;
    }

    private void mostrarMenuAcoes(Button btn, Pedido pedido) {
        ContextMenu menu = new ContextMenu();
        String status = pedido.getStatus();

        if (isDiretor && status.equals("EM_APROVACAO")) {
            MenuItem m = new MenuItem("✅   Aprovar pedido");
            m.setStyle("-fx-text-fill:#166534; -fx-font-size:14px; -fx-padding:4 8;");
            m.setOnAction(e -> navegarParaAprovacao(pedido));
            menu.getItems().add(m);
        }
        // ← Agora inclui APROVADO_PARCIALMENTE
        if (isFinanceiro && (status.equals("APROVADO") || status.equals("APROVADO_PARCIALMENTE"))) {
            MenuItem m = new MenuItem("📄   Fazer cotação");
            m.setStyle("-fx-text-fill:#1e40af; -fx-font-size:14px; -fx-padding:4 8;");
            m.setOnAction(e -> navegarParaCadastroCotacao(pedido));
            menu.getItems().add(m);
        }
        if (podeGerenciar && (status.equals("EM_APROVACAO") || status.equals("NEGADO"))) {
            if (!menu.getItems().isEmpty()) menu.getItems().add(new SeparatorMenuItem());
            MenuItem m = new MenuItem("🚫   Cancelar pedido");
            m.setStyle("-fx-text-fill:#dc2626; -fx-font-size:14px; -fx-padding:4 8;");
            m.setOnAction(e -> onCancelarPedido(pedido));
            menu.getItems().add(m);
        }
        if (!menu.getItems().isEmpty())
            menu.show(btn, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    // ── Handlers ─────────────────────────────────────────────

    private void onCancelarPedido(Pedido pedido) {
        Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
        dlg.setTitle("Cancelar Pedido"); dlg.setHeaderText(null);
        dlg.setContentText("Cancelar o pedido " + pedido.getNumPedido()
                + "?\nEle será removido da lista. Ação irreversível.");
        dlg.showAndWait().ifPresent(b -> {
            if (b == ButtonType.OK) {
                if (pedidoDAO.cancelar(pedido.getIdPedido())) {
                    HistoricoService.registrar("Pedido", "Cancelamento", pedido.getIdPedido(),
                            "Pedido " + pedido.getNumPedido() + " cancelado por "
                                    + SessaoUsuario.getInstancia().getNomeUsuarioLogado());
                    carregarPedidos();
                } else new Alert(Alert.AlertType.ERROR, "Erro ao cancelar.").showAndWait();
            }
        });
    }

    private void navegarParaEditar(Pedido p) {
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource("/view/editarPedido.fxml"));
            Node tela = l.load();
            editarPedidoController c = l.getController();
            c.setAreaPrincipal(areaPrincipal); c.setPedidoEdicao(p);
            anchorar(tela); areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void navegarParaAprovacao(Pedido p) {
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource("/view/aprovacaoPedido.fxml"));
            Node tela = l.load();
            aprovacaoPedidoController c = l.getController();
            c.setAreaPrincipal(areaPrincipal); c.setPedido(p);
            anchorar(tela); areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void navegarParaCadastroCotacao(Pedido p) {
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource("/view/cadastroCotacao.fxml"));
            Node tela = l.load();
            cadastroCotacaoController c = l.getController();
            c.setAreaPrincipal(areaPrincipal); c.setPedido(p);
            anchorar(tela); areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void anchorar(Node tela) {
        AnchorPane.setTopAnchor(tela, 0.0); AnchorPane.setBottomAnchor(tela, 0.0);
        AnchorPane.setLeftAnchor(tela, 0.0); AnchorPane.setRightAnchor(tela, 0.0);
    }

    private String formatarStatus(String s) {
        return switch (s) {
            case "EM_APROVACAO"          -> "Em Aprovação";
            case "APROVADO"              -> "Aprovado";
            case "APROVADO_PARCIALMENTE" -> "Aprov. Parcial";
            case "NEGADO"                -> "Negado";
            case "EM_COTACAO"            -> "Em Cotação";
            case "EM_COMPRA"             -> "Em Compra";
            case "FINALIZADO"            -> "Finalizado";
            default                      -> s;
        };
    }

    private String estiloBadge(String s) {
        String base = "-fx-background-radius:6; -fx-padding:4 10; -fx-font-size:11px; -fx-font-weight:bold;";
        return base + switch (s) {
            case "EM_APROVACAO"          -> "-fx-background-color:#fef9c3; -fx-text-fill:#854d0e;";
            case "APROVADO"              -> "-fx-background-color:#dcfce7; -fx-text-fill:#166534;";
            case "APROVADO_PARCIALMENTE" -> "-fx-background-color:#d1fae5; -fx-text-fill:#065f46;";
            case "NEGADO"                -> "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b;";
            case "EM_COTACAO"            -> "-fx-background-color:#dbeafe; -fx-text-fill:#1e40af;";
            case "EM_COMPRA"             -> "-fx-background-color:#ede9fe; -fx-text-fill:#5b21b6;";
            case "FINALIZADO"            -> "-fx-background-color:#d1fae5; -fx-text-fill:#065f46;";
            default                      -> "-fx-background-color:#f3f4f6; -fx-text-fill:#6b7280;";
        };
    }
}