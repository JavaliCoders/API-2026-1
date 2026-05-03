package api.controller;

import api.DAO.produtoDAO;
import api.model.Produto;
import api.util.PermissaoUtil;
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
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class estoqueController implements Initializable {

    // ── Filtros ───────────────────────────────────────────────
    @FXML private TextField searchCod;
    @FXML private TextField searchDescricao;
    @FXML private ComboBox<String> filtroStatus;

    // ── Tabela e colunas ──────────────────────────────────────
    @FXML private TableView<Produto>            tabelaProdutos;
    @FXML private TableColumn<Produto, Integer> colId;
    @FXML private TableColumn<Produto, String>  colProduto;
    @FXML private TableColumn<Produto, String>  colDescricao;
    @FXML private TableColumn<Produto, String>  colUnidade;
    @FXML private TableColumn<Produto, Integer> colNivelMinimo;
    @FXML private TableColumn<Produto, Double>  colValor;
    @FXML private TableColumn<Produto, Integer> colSaldo;
    @FXML private TableColumn<Produto, String>  colStatus;
    @FXML private TableColumn<Produto, Void>    colAcoes;

    // ── Painel de detalhes ────────────────────────────────────
    @FXML private StackPane overlayDetalhes;
    @FXML private Label detalheNome;
    @FXML private Label detalheCod;
    @FXML private Label detalheDescricao;
    @FXML private Label detalheUnidade;
    @FXML private Label detalheNivelMinimo;
    @FXML private Label detalheValor;
    @FXML private Label detalheSaldo;
    @FXML private Label detalheStatus;

    // ── Área principal ────────────────────────────────────────
    private AnchorPane areaPrincipal;

    // ── Dados ─────────────────────────────────────────────────
    private ObservableList<Produto> todosProdutos;
    private FilteredList<Produto>   produtosFiltrados;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarFiltros();
        configurarColunas();
        carregarDados();
        configurarBusca();
        configurarSelecao(); // ← listener de seleção da tabela
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
        colId.setCellValueFactory(new PropertyValueFactory<>("idProduto"));
        colProduto.setCellValueFactory(new PropertyValueFactory<>("produto"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colUnidade.setCellValueFactory(new PropertyValueFactory<>("unidadeMedida"));
        colNivelMinimo.setCellValueFactory(new PropertyValueFactory<>("nivelMinimo"));
        colSaldo.setCellValueFactory(new PropertyValueFactory<>("saldo"));

        // Valor formatado como moeda
        colValor.setCellValueFactory(new PropertyValueFactory<>("valorEstimado"));
        colValor.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(String.format("R$ %.2f", item).replace(".", ","));
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

        // Badge de Saldo
        colSaldo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Produto p = getTableView().getItems().get(getIndex());
                boolean baixo = item < p.getNivelMinimo();
                Label badge = new Label(String.valueOf(item));
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                badge.setAlignment(Pos.CENTER);
                badge.setPrefWidth(60);
                badge.setStyle(baixo
                        ? "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 6; -fx-padding: 4 8;"
                        : "-fx-background-color: #f3f4f6; -fx-text-fill: #374151; -fx-background-radius: 6; -fx-padding: 4 8;");
                setGraphic(badge);
                setText(null);
            }
        });

        // Coluna de Ações
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
                    Produto produto = getTableView().getItems().get(getIndex());
                    abrirEdicao(produto);
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
        tabelaProdutos.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Produto item, boolean empty) {
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

    // ── Seleção da tabela ─────────────────────────────────────

    private void configurarSelecao() {
        tabelaProdutos.getSelectionModel().selectedItemProperty().addListener(
                (obs, antigo, novo) -> {
                    if (novo != null) {
                        exibirDetalhes(novo);
                    }
                });
    }

    private void exibirDetalhes(Produto p) {
        detalheNome.setText(p.getProduto());
        detalheCod.setText(String.valueOf(p.getIdProduto()));
        detalheDescricao.setText(p.getDescricao());
        detalheUnidade.setText(p.getUnidadeMedida());
        detalheNivelMinimo.setText(String.valueOf(p.getNivelMinimo()));
        detalheValor.setText(String.format("R$ %.2f", p.getValorEstimado())
                .replace(".", ","));
        detalheSaldo.setText(String.valueOf(p.getSaldo()));

        if (p.getStatus().equals("ATIVO")) {
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

        if (p.getSaldo() < p.getNivelMinimo()) {
            detalheSaldo.setStyle(
                    "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");
            detalheNivelMinimo.setStyle(
                    "-fx-font-size: 14px; -fx-text-fill: #dc2626;");
        } else {
            detalheSaldo.setStyle(
                    "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
            detalheNivelMinimo.setStyle(
                    "-fx-font-size: 14px; -fx-text-fill: #0f172a;");
        }

        overlayDetalhes.setVisible(true);
        overlayDetalhes.setManaged(true);
    }

    @FXML
    private void fecharDetalhes() {
        overlayDetalhes.setVisible(false);
        overlayDetalhes.setManaged(false);
        // Limpa a seleção da tabela ao fechar
        tabelaProdutos.getSelectionModel().clearSelection();
    }

    // ── Edição ────────────────────────────────────────────────

    private void abrirEdicao(Produto produto) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/cadastroProduto.fxml"));
            Node tela = loader.load();

            cadastroProdutoController controller = loader.getController();
            controller.setProdutoEdicao(produto);
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

    // ── Dados ─────────────────────────────────────────────────

    private void carregarDados() {
        todosProdutos     = produtoDAO.listarTodos();
        produtosFiltrados = new FilteredList<>(todosProdutos, p -> true);

        SortedList<Produto> produtosOrdenados = new SortedList<>(produtosFiltrados);
        produtosOrdenados.comparatorProperty().bind(tabelaProdutos.comparatorProperty());

        colId.setSortType(TableColumn.SortType.ASCENDING);
        tabelaProdutos.getSortOrder().add(colId);
        tabelaProdutos.setItems(produtosOrdenados);
    }

    // ── Busca e filtros ───────────────────────────────────────

    private void configurarBusca() {
        searchCod.textProperty().addListener((obs, a, n)       -> aplicarFiltros());
        searchDescricao.textProperty().addListener((obs, a, n) -> aplicarFiltros());
        filtroStatus.valueProperty().addListener((obs, a, n)   -> aplicarFiltros());
    }

    private void aplicarFiltros() {
        String cod       = searchCod.getText() == null       ? "" : searchCod.getText().toLowerCase();
        String descricao = searchDescricao.getText() == null  ? "" : searchDescricao.getText().toLowerCase();
        String status    = filtroStatus.getValue();

        produtosFiltrados.setPredicate(p -> {
            boolean matchCod      = cod.isEmpty()
                    || String.valueOf(p.getIdProduto()).contains(cod);
            boolean matchDescricao = descricao.isEmpty()
                    || p.getDescricao().toLowerCase().contains(descricao)
                    || p.getProduto().toLowerCase().contains(descricao);
            boolean matchStatus   = status == null
                    || status.equals("Todos os status")
                    || p.getStatus().equals(status);
            return matchCod && matchDescricao && matchStatus;
        });
    }

    @FXML private void onSearch()       { aplicarFiltros(); }
    @FXML private void onFiltroStatus() { aplicarFiltros(); }
}