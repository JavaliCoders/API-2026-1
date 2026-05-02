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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.net.URL;
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

    // ── Overlay de detalhes ───────────────────────────────────
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
    @FXML private TableColumn<PedidoProduto, Integer> colItemQtd;
    @FXML private TableColumn<PedidoProduto, String>  colItemValorUnit;
    @FXML private TableColumn<PedidoProduto, String>  colItemValorTotal;

    private AnchorPane areaPrincipal;
    private ObservableList<Pedido> todosPedidos;
    private FilteredList<Pedido>   pedidosFiltrados;

    private final boolean isDiretor     = PermissaoUtil.temPermissao("DIRETOR");
    private final boolean isFinanceiro  = PermissaoUtil.temPermissao("FINANCEIRO");
    private final boolean podeGerenciar = isDiretor || isFinanceiro;
    private final int     idUsuarioLogado = SessaoUsuario.getInstancia().getIdUsuarioLogado();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarFiltroStatus();
        configurarColunas();
        configurarOverlayItens();
        carregarPedidos();

        tabelaPedidos.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                Pedido p = tabelaPedidos.getSelectionModel().getSelectedItem();
                if (p != null) abrirOverlay(p);
            }
        });
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

    @FXML private void onSearch(KeyEvent event) { aplicarFiltro(); }
    @FXML private void onFiltroStatus()         { aplicarFiltro(); }

    private void aplicarFiltro() {
        String num  = searchNum.getText() == null        ? "" : searchNum.getText().trim().toLowerCase();
        String soli = searchSolicitante.getText() == null ? "" : searchSolicitante.getText().trim().toLowerCase();
        String stat = filtroStatus.getValue();

        pedidosFiltrados.setPredicate(p -> {
            boolean okNum  = num.isEmpty()  || p.getNumPedido().toLowerCase().contains(num);
            boolean okSoli = soli.isEmpty() || p.getNomeSolicitante().toLowerCase().contains(soli);
            boolean okStat = stat == null   || stat.equals("Todos os status") || p.getStatus().equals(stat);
            return okNum && okSoli && okStat;
        });
    }

    private void configurarFiltroStatus() {
        filtroStatus.setItems(FXCollections.observableArrayList(
                "Todos os status", "EM_APROVACAO", "APROVADO", "NEGADO",
                "EM_COTACAO", "EM_COMPRA", "FINALIZADO"));
        filtroStatus.setValue("Todos os status");
    }

    // ── Overlay ───────────────────────────────────────────────

    private void abrirOverlay(Pedido p) {
        detalheNum        .setText(p.getNumPedido());
        detalheData       .setText(p.getDataAberturaFormatada());
        detalheSolicitante.setText(p.getNomeSolicitante());
        detalheValor      .setText(String.format("R$ %.2f", p.getValorTotalEstimado()).replace(".", ","));
        detalheSetor      .setText(p.getNomeSetor());
        detalheCentro     .setText(p.getNomeCentroCusto());
        detalheStatus.setText(formatarStatus(p.getStatus()));
        detalheStatus.setStyle(estiloBadge(p.getStatus()));
        tabelaItensPedido.setItems(pedidoDAO.listarItens(p.getIdPedido()));
        overlayDetalhes.setVisible(true);
        overlayDetalhes.setManaged(true);
    }

    @FXML private void fecharDetalhes() {
        overlayDetalhes.setVisible(false);
        overlayDetalhes.setManaged(false);
        tabelaPedidos.getSelectionModel().clearSelection();
    }

    private void configurarOverlayItens() {
        colItemProduto   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeProduto()));
        colItemUnidade   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnidadeProduto()));
        colItemQtd       .setCellValueFactory(d -> d.getValue().qtdSolicitadaProperty().asObject());
        colItemValorUnit .setCellValueFactory(d -> new SimpleStringProperty(
                String.format("R$ %.2f", d.getValue().getValorUnitario()).replace(".", ",")));
        colItemValorTotal.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("R$ %.2f", d.getValue().getValorTotal()).replace(".", ",")));
    }

    // ── Colunas ───────────────────────────────────────────────

    private void configurarColunas() {
        // Fonte padrão para todas as células de texto
        colNum        .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumPedido()));
        colData       .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataAberturaFormatada()));
        colSolicitante.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeSolicitante()));
        colSetor      .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeSetor()));
        colCentro     .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeCentroCusto()));
        colValor      .setCellValueFactory(d -> new SimpleStringProperty(
                String.format("R$ %.2f", d.getValue().getValorTotalEstimado()).replace(".", ",")));

        // Aumenta fonte das colunas de texto
        for (TableColumn<Pedido, String> col : new TableColumn[]{
                colNum, colData, colSolicitante, colSetor, colCentro, colValor}) {
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
        // Rebind após setCellFactory (factory sobrescreve o value factory visual, precisa manter)
        colNum        .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumPedido()));
        colData       .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataAberturaFormatada()));
        colSolicitante.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeSolicitante()));
        colSetor      .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeSetor()));
        colCentro     .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeCentroCusto()));
        colValor      .setCellValueFactory(d -> new SimpleStringProperty(
                String.format("R$ %.2f", d.getValue().getValorTotalEstimado()).replace(".", ",")));

        // Badge de status colorido
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(formatarStatus(status));
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                badge.setAlignment(Pos.CENTER);
                badge.setPrefWidth(100);
                badge.setStyle(estiloBadge(status));
                HBox box = new HBox(badge);
                box.setAlignment(Pos.CENTER);
                setGraphic(box); setText(null);
            }
        });

        // Coluna Editar — só aparece para o criador do pedido enquanto EM_APROVACAO
        colEditar.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("✏️  Editar");
            {
                btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                aplicarEstiloEditar(false);
                btn.setOnMouseEntered(e -> aplicarEstiloEditar(true));
                btn.setOnMouseExited (e -> aplicarEstiloEditar(false));
                btn.setOnAction(e -> {
                    Pedido p = getTableView().getItems().get(getIndex());
                    fecharDetalhes();
                    navegarParaEditar(p);
                });
            }
            private void aplicarEstiloEditar(boolean hover) {
                btn.setStyle(hover
                        ? "-fx-background-color:#2563eb; -fx-text-fill:white; -fx-background-radius:6; -fx-border-color:transparent; -fx-padding:5 12; -fx-cursor:hand;"
                        : "-fx-background-color:#dbeafe; -fx-text-fill:#1e40af; -fx-background-radius:6; -fx-border-color:transparent; -fx-padding:5 12; -fx-cursor:hand;");
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                Pedido p = (Pedido) getTableRow().getItem();
                // Só o criador do pedido pode editar, e só enquanto EM_APROVACAO
                boolean podeEditar = p.getStatus().equals("EM_APROVACAO")
                        && p.getSolicitante().getIdUsuario() == idUsuarioLogado;
                if (podeEditar) {
                    HBox box = new HBox(btn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                } else {
                    setGraphic(null);
                }
            }
        });

        // Coluna Ações — menu contextual por perfil
        colAcoes.setVisible(podeGerenciar);
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("⚙  Ações  ▾");
            {
                btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                aplicarEstiloAcoes(false);
                btn.setOnMouseEntered(e -> aplicarEstiloAcoes(true));
                btn.setOnMouseExited (e -> aplicarEstiloAcoes(false));
                btn.setOnAction(e -> {
                    Pedido p = getTableView().getItems().get(getIndex());
                    fecharDetalhes();
                    mostrarMenuAcoes(btn, p);
                });
            }
            private void aplicarEstiloAcoes(boolean hover) {
                btn.setStyle(hover
                        ? "-fx-background-color:#1e40af; -fx-text-fill:white; -fx-background-radius:6; -fx-border-color:transparent; -fx-padding:5 12; -fx-cursor:hand;"
                        : "-fx-background-color:#eff6ff; -fx-text-fill:#1e40af; -fx-background-radius:6; -fx-border-color:#bfdbfe; -fx-border-width:1; -fx-padding:5 12; -fx-cursor:hand;");
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                Pedido p = (Pedido) getTableRow().getItem();
                if (temAlgumaAcao(p)) {
                    HBox box = new HBox(btn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                } else {
                    setGraphic(null);
                }
            }
        });

        // Zebra striping igual ao estoque
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

    // ── Menu de ações ─────────────────────────────────────────

    private boolean temAlgumaAcao(Pedido p) {
        String s = p.getStatus();
        if (isDiretor    && (s.equals("EM_APROVACAO") || s.equals("NEGADO")))                      return true;
        if (isFinanceiro && (s.equals("APROVADO") || s.equals("EM_APROVACAO") || s.equals("NEGADO"))) return true;
        return false;
    }

    private void mostrarMenuAcoes(Button btn, Pedido pedido) {
        ContextMenu menu = new ContextMenu();
        String status = pedido.getStatus();

        // ── DIRETOR: aprovar pedido EM_APROVACAO ──
        if (isDiretor && status.equals("EM_APROVACAO")) {
            MenuItem mAprovar = new MenuItem("✅  Aprovar pedido");
            mAprovar.setStyle("-fx-text-fill: #166534; -fx-font-size: 13px;");
            mAprovar.setOnAction(e -> navegarParaAprovacao(pedido));
            menu.getItems().add(mAprovar);
        }

        // ── FINANCEIRO: fazer cotação em pedido APROVADO ──
        if (isFinanceiro && status.equals("APROVADO")) {
            MenuItem mCotacao = new MenuItem("📄  Fazer cotação");
            mCotacao.setStyle("-fx-text-fill: #1e40af; -fx-font-size: 13px;");
            mCotacao.setOnAction(e -> navegarParaCadastroCotacao(pedido));
            menu.getItems().add(mCotacao);
        }

        // ── DIRETOR ou FINANCEIRO: cancelar pedido EM_APROVACAO ou NEGADO ──
        if (podeGerenciar && (status.equals("EM_APROVACAO") || status.equals("NEGADO"))) {
            if (!menu.getItems().isEmpty())
                menu.getItems().add(new SeparatorMenuItem());
            MenuItem mCancelar = new MenuItem("🚫  Cancelar pedido");
            mCancelar.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 13px;");
            mCancelar.setOnAction(e -> onCancelarPedido(pedido));
            menu.getItems().add(mCancelar);
        }

        if (!menu.getItems().isEmpty())
            menu.show(btn, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    // ── Handlers ─────────────────────────────────────────────

    private void onCancelarPedido(Pedido pedido) {
        Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
        dlg.setTitle("Cancelar Pedido");
        dlg.setHeaderText(null);
        dlg.setContentText("Cancelar o pedido " + pedido.getNumPedido()
                + "?\nEle será removido da lista. Esta ação não pode ser desfeita.");
        dlg.showAndWait().ifPresent(b -> {
            if (b == ButtonType.OK) {
                if (pedidoDAO.cancelar(pedido.getIdPedido())) {
                    HistoricoService.registrar("Pedido", "Cancelamento",
                            pedido.getIdPedido(),
                            "Pedido " + pedido.getNumPedido() + " cancelado por "
                                    + SessaoUsuario.getInstancia().getNomeUsuarioLogado());
                    carregarPedidos();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Erro ao cancelar pedido.").showAndWait();
                }
            }
        });
    }

    private void navegarParaEditar(Pedido pedido) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/editarPedido.fxml"));
            Node tela = loader.load();
            editarPedidoController ctrl = loader.getController();
            ctrl.setAreaPrincipal(areaPrincipal);
            ctrl.setPedidoEdicao(pedido);
            anchorar(tela);
            areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void navegarParaAprovacao(Pedido pedido) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/aprovacaoPedido.fxml"));
            Node tela = loader.load();
            aprovacaoPedidoController ctrl = loader.getController();
            ctrl.setAreaPrincipal(areaPrincipal);
            ctrl.setPedido(pedido);
            anchorar(tela);
            areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void navegarParaCadastroCotacao(Pedido pedido) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/cadastroCotacao.fxml"));
            Node tela = loader.load();
            cadastroCotacaoController ctrl = loader.getController();
            ctrl.setAreaPrincipal(areaPrincipal);
            ctrl.setPedido(pedido);
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
            case "EM_APROVACAO" -> "Em Aprovação";
            case "APROVADO"     -> "Aprovado";
            case "NEGADO"       -> "Negado";
            case "EM_COTACAO"   -> "Em Cotação";
            case "EM_COMPRA"    -> "Em Compra";
            case "FINALIZADO"   -> "Finalizado";
            default             -> s;
        };
    }

    private String estiloBadge(String s) {
        String base = "-fx-background-radius:6; -fx-padding:4 10; -fx-font-size:11px; -fx-font-weight:bold;";
        return base + switch (s) {
            case "EM_APROVACAO" -> "-fx-background-color:#fef9c3; -fx-text-fill:#854d0e;";
            case "APROVADO"     -> "-fx-background-color:#dcfce7; -fx-text-fill:#166534;";
            case "NEGADO"       -> "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b;";
            case "EM_COTACAO"   -> "-fx-background-color:#dbeafe; -fx-text-fill:#1e40af;";
            case "EM_COMPRA"    -> "-fx-background-color:#ede9fe; -fx-text-fill:#5b21b6;";
            case "FINALIZADO"   -> "-fx-background-color:#d1fae5; -fx-text-fill:#065f46;";
            default             -> "-fx-background-color:#f3f4f6; -fx-text-fill:#6b7280;";
        };
    }
}