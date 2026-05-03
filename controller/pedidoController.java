package api.controller;

import api.DAO.pedidoDAO;
import api.model.Pedido;
import api.model.PedidoProduto;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class pedidoController implements Initializable {

    // Filtros
    @FXML private TextField    searchNum;
    @FXML private TextField    searchSolicitante;
    @FXML private ComboBox<String> filtroStatus;

    // Tabela principal
    @FXML private TableView<Pedido>            tabelaPedidos;
    @FXML private TableColumn<Pedido, String>  colNum;
    @FXML private TableColumn<Pedido, String>  colData;
    @FXML private TableColumn<Pedido, String>  colSolicitante;
    @FXML private TableColumn<Pedido, String>  colSetor;
    @FXML private TableColumn<Pedido, String>  colCentro;
    @FXML private TableColumn<Pedido, String>  colValor;
    @FXML private TableColumn<Pedido, String>  colStatus;

    // Overlay de detalhes
    @FXML private StackPane overlayDetalhes;
    @FXML private Label     detalheNum;
    @FXML private Label     detalheData;
    @FXML private Label     detalheStatus;
    @FXML private Label     detalheSolicitante;
    @FXML private Label     detalheValor;
    @FXML private Label     detalheSetor;
    @FXML private Label     detalheCentro;

    // Tabela de itens no overlay
    @FXML private TableView<PedidoProduto>            tabelaItensPedido;
    @FXML private TableColumn<PedidoProduto, String>  colItemProduto;
    @FXML private TableColumn<PedidoProduto, String>  colItemUnidade;
    @FXML private TableColumn<PedidoProduto, Integer> colItemQtd;
    @FXML private TableColumn<PedidoProduto, String>  colItemValorUnit;
    @FXML private TableColumn<PedidoProduto, String>  colItemValorTotal;
    @FXML private TableColumn<Pedido, Void> colAcoes;

    private AnchorPane areaPrincipal;
    private ObservableList<Pedido> todosPedidos;
    private FilteredList<Pedido>   pedidosFiltrados;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarFiltros();
        configurarColunas();
        configurarColunasItens();
        carregarDados();
        configurarBusca();
        configurarSelecao();
    }

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }

    private void configurarFiltros() {
        filtroStatus.setItems(FXCollections.observableArrayList(
                "Todos os status", "EM_APROVACAO", "APROVADO", "NEGADO",
                "EM_COTACAO", "EM_COMPRA", "FINALIZADO", "CANCELADO"));
        filtroStatus.setValue("Todos os status");
    }
    private void abrirEdicao(Pedido pedido) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/editarPedido.fxml"));
            Node tela = loader.load();

            editarPedidoController controller = loader.getController();
            controller.setPedidoEdicao(pedido);
            controller.setAreaPrincipal(areaPrincipal);

            AnchorPane.setTopAnchor(tela, 0.0);
            AnchorPane.setBottomAnchor(tela, 0.0);
            AnchorPane.setLeftAnchor(tela, 0.0);
            AnchorPane.setRightAnchor(tela, 0.0);

            areaPrincipal.getChildren().setAll(tela);

        } catch (IOException e) {
            System.err.println("Erro ao abrir edição: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void configurarColunas() {
        colNum.setCellValueFactory(new PropertyValueFactory<>("numPedido"));
        colData.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDataAberturaFormatada()));
        colSolicitante.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNomeSolicitante()));
        colSetor.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNomeSetor()));
        colCentro.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNomeCentroCusto()));
        colValor.setCellValueFactory(data ->
                new SimpleStringProperty(
                        String.format("R$ %.2f", data.getValue().getValorTotalEstimado())
                                .replace(".", ",")));

        // Badge de Status
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item.replace("_", " "));
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
                badge.setAlignment(Pos.CENTER);
                badge.setPrefWidth(100);
                badge.setStyle(getBadgeStyle(item));
                setGraphic(badge);
                setText(null);
            }
        });

        // Zebra striping
        tabelaPedidos.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Pedido item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("-fx-background-color: white;");
                } else {
                    setStyle(getIndex() % 2 == 0
                            ? "-fx-background-color: white;"
                            : "-fx-background-color: #fafafa;");
                }
            }
        });
        // Botão de editar — só habilitado se status for EM_APROVACAO
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("✏");

            {
                btnEditar.setOnMouseEntered(e -> {
                    if (!btnEditar.isDisabled()) {
                        btnEditar.setStyle(
                                "-fx-background-color: #2563eb; -fx-text-fill: white; " +
                                        "-fx-background-radius: 6; -fx-font-size: 14px; " +
                                        "-fx-cursor: hand; -fx-border-color: transparent; -fx-padding: 4 8;");
                    }
                });
                btnEditar.setOnMouseExited(e -> {
                    if (!btnEditar.isDisabled()) {
                        btnEditar.setStyle(
                                "-fx-background-color: #dbeafe; -fx-text-fill: #2563eb; " +
                                        "-fx-background-radius: 6; -fx-font-size: 14px; " +
                                        "-fx-cursor: hand; -fx-border-color: transparent; -fx-padding: 4 8;");
                    }
                });
                btnEditar.setOnAction(e -> {
                    Pedido pedido = getTableView().getItems().get(getIndex());
                    abrirEdicao(pedido);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                Pedido pedido = getTableView().getItems().get(getIndex());
                boolean podeEditar = pedido.getStatus().equals("EM_APROVACAO");

                if (podeEditar) {
                    // Botão azul habilitado
                    btnEditar.setStyle(
                            "-fx-background-color: #dbeafe; -fx-text-fill: #2563eb; " +
                                    "-fx-background-radius: 6; -fx-font-size: 14px; " +
                                    "-fx-cursor: hand; -fx-border-color: transparent; -fx-padding: 4 8;");
                    btnEditar.setDisable(false);
                    btnEditar.setOpacity(1.0);
                } else {
                    // Botão cinza desabilitado
                    btnEditar.setStyle(
                            "-fx-background-color: #f1f5f9; -fx-text-fill: #cbd5e1; " +
                                    "-fx-background-radius: 6; -fx-font-size: 14px; " +
                                    "-fx-border-color: transparent; -fx-padding: 4 8;");
                    btnEditar.setDisable(true);
                    btnEditar.setOpacity(0.5);
                }

                HBox box = new HBox(btnEditar);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });

    }

    private void configurarColunasItens() {
        colItemProduto.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNomeProduto()));
        colItemUnidade.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getUnidadeProduto()));
        colItemQtd.setCellValueFactory(new PropertyValueFactory<>("qtdSolicitada"));
        colItemValorUnit.setCellValueFactory(data ->
                new SimpleStringProperty(
                        String.format("R$ %.2f", data.getValue().getValorUnitario())
                                .replace(".", ",")));
        colItemValorTotal.setCellValueFactory(data ->
                new SimpleStringProperty(
                        String.format("R$ %.2f", data.getValue().getValorTotal())
                                .replace(".", ",")));
    }

    private void configurarSelecao() {
        tabelaPedidos.getSelectionModel().selectedItemProperty().addListener(
                (obs, antigo, novo) -> {
                    if (novo != null) exibirDetalhes(novo);
                });
    }

    private void exibirDetalhes(Pedido p) {
        detalheNum.setText(p.getNumPedido());
        detalheData.setText(p.getDataAberturaFormatada());
        detalheSolicitante.setText(p.getNomeSolicitante());
        detalheSetor.setText(p.getNomeSetor());
        detalheCentro.setText(p.getNomeCentroCusto());
        detalheValor.setText(
                String.format("R$ %.2f", p.getValorTotalEstimado()).replace(".", ","));

        detalheStatus.setText(p.getStatus().replace("_", " "));
        detalheStatus.setStyle(getBadgeStyle(p.getStatus()) +
                " -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 12;");

        // Carrega os itens do pedido
        ObservableList<PedidoProduto> itens = pedidoDAO.listarItens(p.getIdPedido());
        tabelaItensPedido.setItems(itens);

        overlayDetalhes.setVisible(true);
        overlayDetalhes.setManaged(true);
    }

    @FXML
    private void fecharDetalhes() {
        overlayDetalhes.setVisible(false);
        overlayDetalhes.setManaged(false);
        tabelaPedidos.getSelectionModel().clearSelection();
    }

    private void carregarDados() {
        todosPedidos     = pedidoDAO.listarTodos();
        pedidosFiltrados = new FilteredList<>(todosPedidos, p -> true);

        SortedList<Pedido> ordenados = new SortedList<>(pedidosFiltrados);
        ordenados.comparatorProperty().bind(tabelaPedidos.comparatorProperty());
        tabelaPedidos.setItems(ordenados);
    }

    private void configurarBusca() {
        searchNum.textProperty().addListener((obs, a, n)         -> aplicarFiltros());
        searchSolicitante.textProperty().addListener((obs, a, n) -> aplicarFiltros());
        filtroStatus.valueProperty().addListener((obs, a, n)     -> aplicarFiltros());
    }

    private void aplicarFiltros() {
        String num         = searchNum.getText()         == null ? "" : searchNum.getText().toLowerCase();
        String solicitante = searchSolicitante.getText() == null ? "" : searchSolicitante.getText().toLowerCase();
        String status      = filtroStatus.getValue();

        pedidosFiltrados.setPredicate(p -> {
            boolean matchNum  = num.isEmpty() || p.getNumPedido().toLowerCase().contains(num);
            boolean matchSol  = solicitante.isEmpty()
                    || p.getNomeSolicitante().toLowerCase().contains(solicitante);
            boolean matchStat = status == null || status.equals("Todos os status")
                    || p.getStatus().equals(status);
            return matchNum && matchSol && matchStat;
        });
    }

    private String getBadgeStyle(String status) {
        return switch (status) {
            case "EM_APROVACAO" ->
                    "-fx-background-color: #fef9c3; -fx-text-fill: #854d0e; -fx-background-radius: 6; -fx-padding: 4 8;";
            case "APROVADO" ->
                    "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-background-radius: 6; -fx-padding: 4 8;";
            case "NEGADO", "CANCELADO" ->
                    "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 6; -fx-padding: 4 8;";
            case "EM_COTACAO" ->
                    "-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8; -fx-background-radius: 6; -fx-padding: 4 8;";
            case "EM_COMPRA" ->
                    "-fx-background-color: #ede9fe; -fx-text-fill: #6d28d9; -fx-background-radius: 6; -fx-padding: 4 8;";
            case "FINALIZADO" ->
                    "-fx-background-color: #f0fdf4; -fx-text-fill: #15803d; -fx-background-radius: 6; -fx-padding: 4 8;";
            default ->
                    "-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-background-radius: 6; -fx-padding: 4 8;";
        };
    }

    @FXML private void onSearch()       { aplicarFiltros(); }
    @FXML private void onFiltroStatus() { aplicarFiltros(); }
}