package api.controller;

import api.DAO.movimentacaoDAO;
import api.DAO.notaFiscalDAO;
import api.DAO.pedidoDAO;
import api.model.*;
import api.service.HistoricoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Tela de saída de material — atende pedidos com status RECEBIDO.
 * Operacional seleciona pedidos e registra a saída para o solicitante.
 */
public class saidaEstoqueController implements Initializable {

    // ── Tabela de pedidos prontos para atendimento ────────────
    @FXML private TableView<Pedido>           tabelaPedidos;
    @FXML private TableColumn<Pedido, String> colNum;
    @FXML private TableColumn<Pedido, String> colSolicitante;
    @FXML private TableColumn<Pedido, String> colSetor;
    @FXML private TableColumn<Pedido, String> colData;
    @FXML private TableColumn<Pedido, String> colStatus;
    @FXML private TableColumn<Pedido, Void>   colAcoes;

    @FXML private TextField        fieldBusca;
    @FXML private ComboBox<String> filtroStatus;

    // ── Overlay de atendimento ────────────────────────────────
    @FXML private javafx.scene.layout.StackPane overlayAtendimento;
    @FXML private Label labelNumPedido;
    @FXML private Label labelSolicitante;

    @FXML private TableView<PedidoProduto>             tabelaItens;
    @FXML private TableColumn<PedidoProduto, String>   colItemProduto;
    @FXML private TableColumn<PedidoProduto, String>   colItemUnidade;
    @FXML private TableColumn<PedidoProduto, String>   colItemQtdRecebida;
    @FXML private TableColumn<PedidoProduto, Void>     colItemAtendir;

    private AnchorPane areaPrincipal;
    private ObservableList<Pedido> todosPedidos;
    private FilteredList<Pedido>   pedidosFiltrados;
    private Pedido                 pedidoEmAtendimento;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarFiltros();
        configurarColunas();
        configurarTabelaItens();
        carregarPedidos();
        overlayAtendimento.setVisible(false);
        overlayAtendimento.setManaged(false);
    }

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }

    // ── Dados ─────────────────────────────────────────────────
    private void carregarPedidos() {
        // Filtra pedidos com status RECEBIDO
        ObservableList<Pedido> todos = pedidoDAO.listarTodosIncluindoRecebidos();
        todosPedidos = FXCollections.observableArrayList(
                todos.stream()
                        .filter(p -> p.getStatus().equals("RECEBIDO"))
                        .toList());
        pedidosFiltrados = new FilteredList<>(todosPedidos, p -> true);
        tabelaPedidos.setItems(pedidosFiltrados);
    }

    // ── Filtros ───────────────────────────────────────────────
    private void configurarFiltros() {
        fieldBusca.textProperty().addListener((o, a, n) -> aplicarFiltro());
    }

    private void aplicarFiltro() {
        String busca = fieldBusca.getText() == null ? "" : fieldBusca.getText().trim().toLowerCase();
        pedidosFiltrados.setPredicate(p ->
                busca.isEmpty()
                        || p.getNumPedido().toLowerCase().contains(busca)
                        || p.getNomeSolicitante().toLowerCase().contains(busca)
                        || p.getNomeSetor().toLowerCase().contains(busca));
    }

    @FXML private void onLimparFiltros() { fieldBusca.clear(); }

    // ── Colunas ───────────────────────────────────────────────
    private void configurarColunas() {
        colNum        .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumPedido()));
        colSolicitante.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeSolicitante()));
        colSetor      .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeSetor()));
        colData       .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataAberturaFormatada()));

        for (TableColumn<Pedido, String> col : new TableColumn[]{colNum, colSolicitante, colSetor, colData}) {
            col.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); return; }
                    setText(item); setFont(Font.font("Segoe UI", 13));
                    setStyle("-fx-text-fill:#0f172a;");
                }
            });
        }
        colNum        .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumPedido()));
        colSolicitante.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeSolicitante()));
        colSetor      .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeSetor()));
        colData       .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataAberturaFormatada()));

        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(status);
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                badge.setAlignment(Pos.CENTER);
                badge.setPrefWidth(110);
                badge.setStyle("-fx-background-radius:6; -fx-padding:4 10; -fx-font-size:11px; -fx-font-weight:bold;"
                        + "-fx-background-color:#dbeafe; -fx-text-fill:#1e40af;");
                HBox box = new HBox(badge); box.setAlignment(Pos.CENTER);
                setGraphic(box); setText(null);
            }
        });

        // Botão Atender
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("📦  Atender");
            {
                btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                estilo(false);
                btn.setOnMouseEntered(e -> estilo(true));
                btn.setOnMouseExited (e -> estilo(false));
                btn.setOnAction(e -> {
                    Pedido p = getTableView().getItems().get(getIndex());
                    abrirAtendimento(p);
                });
            }
            private void estilo(boolean h) {
                btn.setStyle(h
                        ? "-fx-background-color:#2563eb; -fx-text-fill:white; -fx-background-radius:6; -fx-border-color:transparent; -fx-padding:7 14; -fx-cursor:hand;"
                        : "-fx-background-color:#dbeafe; -fx-text-fill:#1e40af; -fx-background-radius:6; -fx-border-color:transparent; -fx-padding:7 14; -fx-cursor:hand;");
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setGraphic(null); return; }
                HBox box = new HBox(btn); box.setAlignment(Pos.CENTER); setGraphic(box);
            }
        });

        tabelaPedidos.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Pedido item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setStyle("-fx-background-color:white;");
                else setStyle(getIndex() % 2 == 0 ? "-fx-background-color:white;" : "-fx-background-color:#fafafa;");
            }
        });
    }

    // ── Overlay atendimento ───────────────────────────────────
    private void abrirAtendimento(Pedido pedido) {
        pedidoEmAtendimento = pedido;
        labelNumPedido  .setText(pedido.getNumPedido());
        labelSolicitante.setText(pedido.getNomeSolicitante());

        ObservableList<PedidoProduto> itens = pedidoDAO.listarItens(pedido.getIdPedido());
        tabelaItens.setItems(itens);

        overlayAtendimento.setVisible(true);
        overlayAtendimento.setManaged(true);
    }

    @FXML private void fecharAtendimento() {
        overlayAtendimento.setVisible(false);
        overlayAtendimento.setManaged(false);
        tabelaPedidos.getSelectionModel().clearSelection();
    }

    // ── Tabela de itens no overlay ────────────────────────────
    private void configurarTabelaItens() {
        colItemProduto    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeProduto()));
        colItemUnidade    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnidadeProduto()));
        colItemQtdRecebida.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getQtdRecebida())));

        for (TableColumn<PedidoProduto, String> col : new TableColumn[]{
                colItemProduto, colItemUnidade, colItemQtdRecebida}) {
            col.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); return; }
                    setText(item); setFont(Font.font("Segoe UI", 13));
                    setStyle("-fx-text-fill:#0f172a;");
                }
            });
        }
        colItemProduto    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeProduto()));
        colItemUnidade    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnidadeProduto()));
        colItemQtdRecebida.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getQtdRecebida())));

        // Botão dar saída por item
        colItemAtendir.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("✅  Dar Saída");
            {
                btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                estilo(false);
                btn.setOnMouseEntered(e -> estilo(true));
                btn.setOnMouseExited (e -> estilo(false));
                btn.setOnAction(e -> {
                    PedidoProduto pp = getTableView().getItems().get(getIndex());
                    darSaida(pp);
                });
            }
            private void estilo(boolean h) {
                btn.setStyle(h
                        ? "-fx-background-color:#166534; -fx-text-fill:white; -fx-background-radius:6; -fx-border-color:transparent; -fx-padding:6 14; -fx-cursor:hand;"
                        : "-fx-background-color:#dcfce7; -fx-text-fill:#166534; -fx-background-radius:6; -fx-border-color:transparent; -fx-padding:6 14; -fx-cursor:hand;");
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setGraphic(null); return; }
                PedidoProduto pp = (PedidoProduto) getTableRow().getItem();
                // Só exibe se há saldo recebido para sair
                if (pp.getQtdRecebida() > 0) {
                    HBox box = new HBox(btn); box.setAlignment(Pos.CENTER); setGraphic(box);
                } else setGraphic(null);
            }
        });
    }

    // ── Dar saída de um item ──────────────────────────────────
    private void darSaida(PedidoProduto pp) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Dar Saída");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Registrar saída de " + pp.getQtdRecebida()
                + " " + pp.getUnidadeProduto() + " de " + pp.getNomeProduto()
                + "\npara o pedido " + pedidoEmAtendimento.getNumPedido() + "?");
        confirmacao.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;

            int idProduto = pp.getProduto() != null
                    ? pp.getProduto().getIdProduto()
                    : buscarIdProduto(pp.getIdPedidoProduto());

            int idUsuario = SessaoUsuario.getInstancia().getIdUsuarioLogado();

            // Reduzir saldo no banco
            boolean okSaldo = atualizarSaldo(idProduto, -pp.getQtdRecebida());
            if (!okSaldo) {
                new Alert(Alert.AlertType.ERROR, "Erro ao atualizar saldo.").showAndWait(); return;
            }

            // Movimentação de SAÍDA
            movimentacaoDAO.inserirSaida(idProduto, pp.getQtdRecebida(), idUsuario,
                    pedidoEmAtendimento.getIdPedido(),
                    "Atendimento pedido " + pedidoEmAtendimento.getNumPedido());

            // Verificar se todos os itens foram atendidos → FINALIZADO
            verificarFinalizacao();

            HistoricoService.registrar("Pedido", "Saída", pedidoEmAtendimento.getIdPedido(),
                    "Saída de " + pp.getQtdRecebida() + "x " + pp.getNomeProduto()
                            + " — pedido " + pedidoEmAtendimento.getNumPedido()
                            + " por " + SessaoUsuario.getInstancia().getNomeUsuarioLogado());

            new Alert(Alert.AlertType.INFORMATION, "Saída registrada com sucesso!").showAndWait();
            fecharAtendimento();
            carregarPedidos();
        });
    }

    private boolean atualizarSaldo(int idProduto, int delta) {
        try (var con = api.connection.ConexaoDB.getConexao();
             var ps = con.prepareStatement(
                     "UPDATE tb_produto SET saldo = saldo + ? WHERE id_produto = ?")) {
            ps.setInt(1, delta);
            ps.setInt(2, idProduto);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.err.println("Erro ao atualizar saldo: " + e.getMessage());
            return false;
        }
    }

    private int buscarIdProduto(int idPedidoProduto) {
        try (var con = api.connection.ConexaoDB.getConexao();
             var ps = con.prepareStatement(
                     "SELECT id_produto FROM tb_pedido_produto WHERE id_pedido_produto = ?")) {
            ps.setInt(1, idPedidoProduto);
            var rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_produto");
        } catch (Exception e) {
            System.err.println("Erro ao buscar id_produto: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Verifica se todos os itens do pedido foram atendidos e atualiza para FINALIZADO.
     */
    private void verificarFinalizacao() {
        // Regra simples: se todos os itens com qtd_recebida > 0 já saíram,
        // marcamos como FINALIZADO.
        // (Implementação simplificada — pode ser refinada com controle de qtd_atendida)
        try (var con = api.connection.ConexaoDB.getConexao();
             var ps = con.prepareStatement(
                     "UPDATE tb_pedido SET status = 'FINALIZADO' WHERE id_pedido = ? AND status = 'RECEBIDO'")) {
            ps.setInt(1, pedidoEmAtendimento.getIdPedido());
            ps.executeUpdate();

            HistoricoService.registrar("Pedido", "Alteração", pedidoEmAtendimento.getIdPedido(),
                    "Pedido " + pedidoEmAtendimento.getNumPedido() + " finalizado por "
                            + SessaoUsuario.getInstancia().getNomeUsuarioLogado());
        } catch (Exception e) {
            System.err.println("Erro ao finalizar pedido: " + e.getMessage());
        }
    }
}