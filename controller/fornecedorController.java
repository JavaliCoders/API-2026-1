package api.controller;

import api.DAO.fornecedorDAO;
import api.model.Fornecedor;
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

public class fornecedorController implements Initializable {

    // Filtros
    @FXML private TextField searchCod;
    @FXML private TextField searchNome;
    @FXML private ComboBox<String> filtroStatus;

    // Tabela
    @FXML private TableView<Fornecedor>            tabelaFornecedores;
    @FXML private TableColumn<Fornecedor, Integer> colId;
    @FXML private TableColumn<Fornecedor, String>  colNome;
    @FXML private TableColumn<Fornecedor, String>  colCnpj;
    @FXML private TableColumn<Fornecedor, String>  colTipoPagamento;
    @FXML private TableColumn<Fornecedor, Double>  colPedidoMinimo;
    @FXML private TableColumn<Fornecedor, String>  colStatus;
    @FXML private TableColumn<Fornecedor, Void>    colAcoes;

    // Overlay de detalhes
    @FXML private StackPane overlayDetalhes;
    @FXML private Label     detalheNome;
    @FXML private Label     detalheCod;
    @FXML private Label     detalheCnpj;
    @FXML private Label     detalheTipoPagamento;
    @FXML private Label     detalhePedidoMinimo;
    @FXML private Label     detalheStatus;

    private AnchorPane areaPrincipal;
    private ObservableList<Fornecedor> todosFornecedores;
    private FilteredList<Fornecedor>   fornecedoresFiltrados;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarFiltros();
        configurarColunas();
        carregarDados();
        configurarBusca();
        configurarSelecao();
    }

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }

    private void configurarFiltros() {
        filtroStatus.setItems(FXCollections.observableArrayList(
                "Todos os status", "ATIVO", "INATIVO"));
        filtroStatus.setValue("Todos os status");
    }

    private void configurarColunas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idFornecedor"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCnpj.setCellValueFactory(new PropertyValueFactory<>("cnpj"));
        colTipoPagamento.setCellValueFactory(new PropertyValueFactory<>("tipoPagamento"));

        // Pedido mínimo formatado como moeda
        colPedidoMinimo.setCellValueFactory(new PropertyValueFactory<>("pedidoMinimo"));
        colPedidoMinimo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item == 0.0 ? "-"
                        : String.format("R$ %.2f", item).replace(".", ","));
            }
        });

        // Badge de Status
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                badge.setAlignment(Pos.CENTER);
                badge.setPrefWidth(70);
                badge.setStyle(item.equals("ATIVO")
                        ? "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-background-radius: 6; -fx-padding: 4 10;"
                        : "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 6; -fx-padding: 4 10;");
                setGraphic(badge);
                setText(null);
            }
        });

        // Badge de Tipo Pagamento
        colTipoPagamento.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                badge.setAlignment(Pos.CENTER);
                badge.setPrefWidth(110);
                badge.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8; " +
                        "-fx-background-radius: 6; -fx-padding: 4 10;");
                setGraphic(badge);
                setText(null);
            }
        });

        // Botão de editar
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("✏");

            {
                btnEditar.setStyle(
                        "-fx-background-color: #dbeafe; -fx-text-fill: #2563eb; " +
                                "-fx-background-radius: 6; -fx-font-size: 14px; " +
                                "-fx-cursor: hand; -fx-border-color: transparent; -fx-padding: 4 8;");

                btnEditar.setOnMouseEntered(e -> btnEditar.setStyle(
                        "-fx-background-color: #2563eb; -fx-text-fill: white; " +
                                "-fx-background-radius: 6; -fx-font-size: 14px; " +
                                "-fx-cursor: hand; -fx-border-color: transparent; -fx-padding: 4 8;"));

                btnEditar.setOnMouseExited(e -> btnEditar.setStyle(
                        "-fx-background-color: #dbeafe; -fx-text-fill: #2563eb; " +
                                "-fx-background-radius: 6; -fx-font-size: 14px; " +
                                "-fx-cursor: hand; -fx-border-color: transparent; -fx-padding: 4 8;"));

                btnEditar.setOnAction(e -> {
                    Fornecedor f = getTableView().getItems().get(getIndex());
                    abrirEdicao(f);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(btnEditar);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        // Zebra striping
        tabelaFornecedores.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Fornecedor item, boolean empty) {
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

    private void configurarSelecao() {
        tabelaFornecedores.getSelectionModel().selectedItemProperty().addListener(
                (obs, antigo, novo) -> {
                    if (novo != null) exibirDetalhes(novo);
                });
    }

    private void exibirDetalhes(Fornecedor f) {
        detalheNome.setText(f.getNome());
        detalheCod.setText(String.valueOf(f.getIdFornecedor()));
        detalheCnpj.setText(f.getCnpj());
        detalheTipoPagamento.setText(f.getTipoPagamento());
        detalhePedidoMinimo.setText(f.getPedidoMinimo() == 0.0 ? "Não definido"
                : String.format("R$ %.2f", f.getPedidoMinimo()).replace(".", ","));

        if (f.getStatus().equals("ATIVO")) {
            detalheStatus.setText("ATIVO");
            detalheStatus.setStyle(
                    "-fx-font-size: 12px; -fx-font-weight: bold; " +
                            "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; " +
                            "-fx-background-radius: 6; -fx-padding: 4 12;");
        } else {
            detalheStatus.setText("INATIVO");
            detalheStatus.setStyle(
                    "-fx-font-size: 12px; -fx-font-weight: bold; " +
                            "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; " +
                            "-fx-background-radius: 6; -fx-padding: 4 12;");
        }

        overlayDetalhes.setVisible(true);
        overlayDetalhes.setManaged(true);
    }

    @FXML
    private void fecharDetalhes() {
        overlayDetalhes.setVisible(false);
        overlayDetalhes.setManaged(false);
        tabelaFornecedores.getSelectionModel().clearSelection();
    }

    private void abrirEdicao(Fornecedor fornecedor) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/cadastroFornecedor.fxml"));
            Node tela = loader.load();

            cadastroFornecedorController controller = loader.getController();
            controller.setFornecedorEdicao(fornecedor);
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

    private void carregarDados() {
        todosFornecedores     = fornecedorDAO.listarTodos();
        fornecedoresFiltrados = new FilteredList<>(todosFornecedores, f -> true);

        SortedList<Fornecedor> ordenados = new SortedList<>(fornecedoresFiltrados);
        ordenados.comparatorProperty().bind(tabelaFornecedores.comparatorProperty());

        colId.setSortType(TableColumn.SortType.ASCENDING);
        tabelaFornecedores.getSortOrder().add(colId);
        tabelaFornecedores.setItems(ordenados);
    }

    private void configurarBusca() {
        searchCod.textProperty().addListener((obs, a, n)  -> aplicarFiltros());
        searchNome.textProperty().addListener((obs, a, n) -> aplicarFiltros());
        filtroStatus.valueProperty().addListener((obs, a, n) -> aplicarFiltros());
    }

    private void aplicarFiltros() {
        String cod    = searchCod.getText()  == null ? "" : searchCod.getText().toLowerCase();
        String nome   = searchNome.getText() == null ? "" : searchNome.getText().toLowerCase();
        String status = filtroStatus.getValue();

        fornecedoresFiltrados.setPredicate(f -> {
            boolean matchCod    = cod.isEmpty()  || String.valueOf(f.getIdFornecedor()).contains(cod);
            boolean matchNome   = nome.isEmpty() || f.getNome().toLowerCase().contains(nome);
            boolean matchStatus = status == null || status.equals("Todos os status")
                    || f.getStatus().equals(status);
            return matchCod && matchNome && matchStatus;
        });
    }

    @FXML private void onSearch()       { aplicarFiltros(); }
    @FXML private void onFiltroStatus() { aplicarFiltros(); }
}