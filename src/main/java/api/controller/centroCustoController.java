package api.controller;

import api.DAO.centroCustoDAO;
import api.model.CentroCusto;
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

public class centroCustoController implements Initializable {

    @FXML private TextField        searchCod;
    @FXML private TextField        searchNome;
    @FXML private ComboBox<String> filtroStatus;

    @FXML private TableView<CentroCusto>            tabelaCentroCusto;
    @FXML private TableColumn<CentroCusto, Integer> colId;
    @FXML private TableColumn<CentroCusto, String>  colNome;
    @FXML private TableColumn<CentroCusto, String>  colStatus;
    @FXML private TableColumn<CentroCusto, Void>    colAcoes;

    @FXML private StackPane overlayDetalhes;
    @FXML private Label     detalheNome;
    @FXML private Label     detalheCod;
    @FXML private Label     detalheStatus;

    private AnchorPane                  areaPrincipal;
    private ObservableList<CentroCusto> todosCentroCusto;
    private FilteredList<CentroCusto>   centrosCustoFiltrados;

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

    // ── Filtros ───────────────────────────────────────────────

    private void configurarFiltros() {
        filtroStatus.setItems(FXCollections.observableArrayList(
                "Todos os status", "ATIVO", "INATIVO"));
        filtroStatus.setValue("Todos os status");
    }

    // ── Colunas ───────────────────────────────────────────────

    private void configurarColunas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idCentroCusto"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("centroCusto"));

        // Badge de status
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
                        ? "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a;" +
                          "-fx-background-radius: 6; -fx-padding: 4 10;"
                        : "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;" +
                          "-fx-background-radius: 6; -fx-padding: 4 10;");
                setGraphic(badge);
                setText(null);
            }
        });

        // Botão editar
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("✏");
            {
                btnEditar.setStyle(
                        "-fx-background-color: #dbeafe; -fx-text-fill: #2563eb;" +
                                "-fx-background-radius: 6; -fx-font-size: 14px;" +
                                "-fx-cursor: hand; -fx-border-color: transparent; -fx-padding: 4 8;");
                btnEditar.setOnMouseEntered(e -> btnEditar.setStyle(
                        "-fx-background-color: #2563eb; -fx-text-fill: white;" +
                                "-fx-background-radius: 6; -fx-font-size: 14px;" +
                                "-fx-cursor: hand; -fx-border-color: transparent; -fx-padding: 4 8;"));
                btnEditar.setOnMouseExited(e -> btnEditar.setStyle(
                        "-fx-background-color: #dbeafe; -fx-text-fill: #2563eb;" +
                                "-fx-background-radius: 6; -fx-font-size: 14px;" +
                                "-fx-cursor: hand; -fx-border-color: transparent; -fx-padding: 4 8;"));
                btnEditar.setOnAction(e -> {
                    CentroCusto cc = getTableView().getItems().get(getIndex());
                    abrirEdicao(cc);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                HBox box = new HBox(btnEditar);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });

        // Zebra striping
        tabelaCentroCusto.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(CentroCusto item, boolean empty) {
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

    // ── Seleção → overlay ────────────────────────────────────

    private void configurarSelecao() {
        tabelaCentroCusto.getSelectionModel().selectedItemProperty().addListener(
                (obs, antigo, novo) -> {
                    if (novo != null) exibirDetalhes(novo);
                });
    }

    private void exibirDetalhes(CentroCusto cc) {
        detalheNome.setText(cc.getCentroCusto());
        detalheCod.setText(String.valueOf(cc.getIdCentroCusto()));

        boolean ativo = "ATIVO".equals(cc.getStatus());
        detalheStatus.setText(cc.getStatus());
        detalheStatus.setStyle(
                "-fx-font-size: 12px; -fx-font-weight: bold;" +
                        (ativo ? "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a;"
                                : "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;") +
                        "-fx-background-radius: 6; -fx-padding: 4 12;");

        overlayDetalhes.setVisible(true);
        overlayDetalhes.setManaged(true);
    }

    @FXML
    private void fecharDetalhes() {
        overlayDetalhes.setVisible(false);
        overlayDetalhes.setManaged(false);
        tabelaCentroCusto.getSelectionModel().clearSelection();
    }

    // ── Dados ─────────────────────────────────────────────────

    private void carregarDados() {
        todosCentroCusto      = centroCustoDAO.listarTodos();
        centrosCustoFiltrados = new FilteredList<>(todosCentroCusto, cc -> true);

        SortedList<CentroCusto> ordenados = new SortedList<>(centrosCustoFiltrados);
        ordenados.comparatorProperty().bind(tabelaCentroCusto.comparatorProperty());

        colId.setSortType(TableColumn.SortType.ASCENDING);
        tabelaCentroCusto.getSortOrder().add(colId);
        tabelaCentroCusto.setItems(ordenados);
    }

    // ── Busca ─────────────────────────────────────────────────

    private void configurarBusca() {
        searchCod.textProperty().addListener((obs, a, n)  -> aplicarFiltros());
        searchNome.textProperty().addListener((obs, a, n) -> aplicarFiltros());
        filtroStatus.valueProperty().addListener((obs, a, n) -> aplicarFiltros());
    }

    private void aplicarFiltros() {
        String cod    = searchCod.getText()  == null ? "" : searchCod.getText().toLowerCase();
        String nome   = searchNome.getText() == null ? "" : searchNome.getText().toLowerCase();
        String status = filtroStatus.getValue();

        centrosCustoFiltrados.setPredicate(cc -> {
            boolean matchCod    = cod.isEmpty()  || String.valueOf(cc.getIdCentroCusto()).contains(cod);
            boolean matchNome   = nome.isEmpty() || cc.getCentroCusto().toLowerCase().contains(nome);
            boolean matchStatus = status == null || status.equals("Todos os status")
                    || cc.getStatus().equals(status);
            return matchCod && matchNome && matchStatus;
        });
    }

    @FXML private void onSearch()       { aplicarFiltros(); }
    @FXML private void onFiltroStatus() { aplicarFiltros(); }

    // ── Navegação ─────────────────────────────────────────────

    private void abrirEdicao(CentroCusto centroCusto) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/cadastroCentroCusto.fxml"));
            Node tela = loader.load();

            cadastroCentroCustoController controller = loader.getController();
            controller.setCentrocustoEdicao(centroCusto);
            controller.setAreaPrincipal(areaPrincipal);

            AnchorPane.setTopAnchor   (tela, 0.0);
            AnchorPane.setBottomAnchor(tela, 0.0);
            AnchorPane.setLeftAnchor  (tela, 0.0);
            AnchorPane.setRightAnchor (tela, 0.0);

            areaPrincipal.getChildren().setAll(tela);

        } catch (IOException e) {
            System.err.println("Erro ao abrir edição: " + e.getMessage());
            e.printStackTrace();
        }
    }
}