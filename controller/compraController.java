package api.controller;

import api.DAO.pedidoDAO;
import api.model.Pedido;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class compraController implements Initializable {

    @FXML private TextField searchNum;
    @FXML private TextField searchSolicitante;

    @FXML private TableView<Pedido> tabelaPedidos;
    @FXML private TableColumn<Pedido, String> colNum;
    @FXML private TableColumn<Pedido, String> colData;
    @FXML private TableColumn<Pedido, String> colSolicitante;
    @FXML private TableColumn<Pedido, String> colSetor;
    @FXML private TableColumn<Pedido, String> colCentro;
    @FXML private TableColumn<Pedido, String> colValor;
    @FXML private TableColumn<Pedido, String> colStatus;
    @FXML private TableColumn<Pedido, Void> colAcoes;

    private AnchorPane areaPrincipal;
    private ObservableList<Pedido> todosPedidos;
    private FilteredList<Pedido> pedidosFiltrados;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColunas();
        carregarDados();
        configurarBusca();
    }

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
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
                new SimpleStringProperty(formatarMoeda(data.getValue().getValorTotalEstimado())));

        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label badge = new Label(item.replace("_", " "));
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
                badge.setAlignment(Pos.CENTER);
                badge.setPrefWidth(100);
                badge.setStyle(getBadgeStyle(item));
                setGraphic(badge);
                setText(null);
            }
        });

        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnRegistrar = new Button("Registrar");

            {
                btnRegistrar.setStyle(estiloBotaoAcao(false));
                btnRegistrar.setOnMouseEntered(e -> btnRegistrar.setStyle(estiloBotaoAcao(true)));
                btnRegistrar.setOnMouseExited(e -> btnRegistrar.setStyle(estiloBotaoAcao(false)));
                btnRegistrar.setOnAction(e -> {
                    Pedido pedido = getTableView().getItems().get(getIndex());
                    abrirCadastro(pedido);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                HBox box = new HBox(btnRegistrar);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });

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
    }

    private void carregarDados() {
        todosPedidos = pedidoDAO.listarTodos()
                .filtered(p -> "APROVADO".equals(p.getStatus()));
        pedidosFiltrados = new FilteredList<>(todosPedidos, p -> true);

        SortedList<Pedido> ordenados = new SortedList<>(pedidosFiltrados);
        ordenados.comparatorProperty().bind(tabelaPedidos.comparatorProperty());
        tabelaPedidos.setItems(ordenados);
    }

    private void configurarBusca() {
        searchNum.textProperty().addListener((obs, a, n) -> aplicarFiltros());
        searchSolicitante.textProperty().addListener((obs, a, n) -> aplicarFiltros());
    }

    private void aplicarFiltros() {
        String numero = searchNum.getText() == null ? "" : searchNum.getText().toLowerCase();
        String solicitante = searchSolicitante.getText() == null
                ? "" : searchSolicitante.getText().toLowerCase();

        pedidosFiltrados.setPredicate(p -> {
            boolean matchNumero = numero.isEmpty()
                    || p.getNumPedido().toLowerCase().contains(numero);
            boolean matchSolicitante = solicitante.isEmpty()
                    || p.getNomeSolicitante().toLowerCase().contains(solicitante);
            return matchNumero && matchSolicitante;
        });
    }

    private void abrirCadastro(Pedido pedido) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/cadastroCompra.fxml"));
            Node tela = loader.load();

            cadastroCompraController controller = loader.getController();
            controller.setAreaPrincipal(areaPrincipal);
            controller.setPedidoSelecionado(pedido);

            AnchorPane.setTopAnchor(tela, 0.0);
            AnchorPane.setBottomAnchor(tela, 0.0);
            AnchorPane.setLeftAnchor(tela, 0.0);
            AnchorPane.setRightAnchor(tela, 0.0);

            areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) {
            System.err.println("Erro ao abrir cadastro de compra: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getBadgeStyle(String status) {
        return switch (status) {
            case "APROVADO" ->
                    "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-background-radius: 6; -fx-padding: 4 8;";
            case "EM_COMPRA" ->
                    "-fx-background-color: #ede9fe; -fx-text-fill: #6d28d9; -fx-background-radius: 6; -fx-padding: 4 8;";
            default ->
                    "-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-background-radius: 6; -fx-padding: 4 8;";
        };
    }

    private String estiloBotaoAcao(boolean hover) {
        if (hover) {
            return "-fx-background-color: #2563eb; -fx-text-fill: white; " +
                    "-fx-background-radius: 6; -fx-font-size: 12px; -fx-font-weight: bold; " +
                    "-fx-cursor: hand; -fx-border-color: transparent; -fx-padding: 5 10;";
        }
        return "-fx-background-color: #dbeafe; -fx-text-fill: #2563eb; " +
                "-fx-background-radius: 6; -fx-font-size: 12px; -fx-font-weight: bold; " +
                "-fx-cursor: hand; -fx-border-color: transparent; -fx-padding: 5 10;";
    }

    private String formatarMoeda(double valor) {
        return String.format("R$ %.2f", valor).replace(".", ",");
    }

    @FXML private void onSearch() {
        aplicarFiltros();
    }
}
