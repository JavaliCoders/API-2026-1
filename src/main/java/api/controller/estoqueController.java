package api.controller;

import api.DAO.produtoDAO;
import api.model.Produto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.URL;
import java.util.ResourceBundle;

public class estoqueController implements Initializable {

    // Buscas separadas
    @FXML private TextField searchCod;
    @FXML private TextField searchDescricao;

    // Filtro de status
    @FXML private ComboBox<String> filtroStatus;

    // Tabela e colunas
    @FXML private TableView<Produto>            tabelaProdutos;
    @FXML private TableColumn<Produto, Integer> colId;
    @FXML private TableColumn<Produto, String>  colProduto;
    @FXML private TableColumn<Produto, String>  colDescricao;
    @FXML private TableColumn<Produto, String>  colUnidade;
    @FXML private TableColumn<Produto, Integer> colNivelMinimo;
    @FXML private TableColumn<Produto, Double>  colValor;
    @FXML private TableColumn<Produto, Integer> colSaldo;
    @FXML private TableColumn<Produto, String>  colStatus;

    private ObservableList<Produto> todosProdutos;
    private FilteredList<Produto>   produtosFiltrados;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarFiltros();
        configurarColunas();
        carregarDados();
        configurarBusca();
    }

    private void configurarFiltros() {
        filtroStatus.setItems(FXCollections.observableArrayList(
                "Todos os status", "ATIVO", "INATIVO"));
        filtroStatus.setValue("Todos os status");
    }

    private void configurarColunas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idProduto"));
        colProduto.setCellValueFactory(new PropertyValueFactory<>("produto"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colUnidade.setCellValueFactory(new PropertyValueFactory<>("unidadeMedida"));
        colNivelMinimo.setCellValueFactory(new PropertyValueFactory<>("nivelMinimo"));
        colSaldo.setCellValueFactory(new PropertyValueFactory<>("saldo"));

        // Formata valor como moeda
        colValor.setCellValueFactory(new PropertyValueFactory<>("valorEstimado"));
        colValor.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(String.format("R$ %.2f", item).replace(".", ","));
            }
        });

        // Badge colorido para Status
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

        // Badge colorido para Saldo (vermelho se abaixo do nível mínimo)
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

    private void carregarDados() {
        todosProdutos     = produtoDAO.listarTodos();
        produtosFiltrados = new FilteredList<>(todosProdutos, p -> true);
        tabelaProdutos.setItems(produtosFiltrados);
    }

    private void configurarBusca() {
        searchCod.textProperty().addListener((obs, antigo, novo)       -> aplicarFiltros());
        searchDescricao.textProperty().addListener((obs, antigo, novo) -> aplicarFiltros());
        filtroStatus.valueProperty().addListener((obs, antigo, novo)   -> aplicarFiltros());
    }

    private void aplicarFiltros() {
        String cod      = searchCod.getText() == null      ? "" : searchCod.getText().toLowerCase();
        String descricao = searchDescricao.getText() == null ? "" : searchDescricao.getText().toLowerCase();
        String status   = filtroStatus.getValue();

        produtosFiltrados.setPredicate(p -> {
            // Filtra por COD (converte o int para string para comparar)
            boolean matchCod      = cod.isEmpty()
                    || String.valueOf(p.getIdProduto()).contains(cod);

            // Filtra por descrição
            boolean matchDescricao = descricao.isEmpty()
                    || p.getDescricao().toLowerCase().contains(descricao)
                    || p.getProduto().toLowerCase().contains(descricao);

            // Filtra por status
            boolean matchStatus   = status == null
                    || status.equals("Todos os status")
                    || p.getStatus().equals(status);

            return matchCod && matchDescricao && matchStatus;
        });
    }

    @FXML private void onSearch()       { aplicarFiltros(); }
    @FXML private void onFiltroStatus() { aplicarFiltros(); }
}