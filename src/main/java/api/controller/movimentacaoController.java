package api.controller;

import api.DAO.movimentacaoDAO;
import api.model.Movimentacao;
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
import java.time.LocalDate;
import java.util.ResourceBundle;

public class movimentacaoController implements Initializable {

    @FXML private TableView<Movimentacao>             tabelaMovimentacoes;
    @FXML private TableColumn<Movimentacao, String>   colData;
    @FXML private TableColumn<Movimentacao, String>   colProduto;
    @FXML private TableColumn<Movimentacao, String>   colTipo;
    @FXML private TableColumn<Movimentacao, String>   colQuantidade;
    @FXML private TableColumn<Movimentacao, String>   colUsuario;
    @FXML private TableColumn<Movimentacao, String>   colPedido;
    @FXML private TableColumn<Movimentacao, String>   colNota;
    @FXML private TableColumn<Movimentacao, String>   colObservacao;

    @FXML private ComboBox<String> filtroTipo;
    @FXML private DatePicker       filtroDataInicio;
    @FXML private DatePicker       filtroDataFim;
    @FXML private TextField        fieldBusca;

    private AnchorPane areaPrincipal;
    private ObservableList<Movimentacao> todasMovimentacoes;
    private FilteredList<Movimentacao>   movimentacoesFiltradas;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarFiltros();
        configurarColunas();
        carregarMovimentacoes();
    }

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }

    // ── Dados ─────────────────────────────────────────────────
    private void carregarMovimentacoes() {
        todasMovimentacoes     = movimentacaoDAO.listarTodas();
        movimentacoesFiltradas = new FilteredList<>(todasMovimentacoes, m -> true);
        tabelaMovimentacoes.setItems(movimentacoesFiltradas);
    }

    // ── Filtros ───────────────────────────────────────────────
    private void configurarFiltros() {
        filtroTipo.setItems(FXCollections.observableArrayList(
                "Todos", "ENTRADA", "SAÍDA", "ENTRADA_MANUAL", "SAIDA_MANUAL"));
        filtroTipo.setValue("Todos");

        fieldBusca      .textProperty() .addListener((o, a, n) -> aplicarFiltro());
        filtroTipo      .valueProperty().addListener((o, a, n) -> aplicarFiltro());
        filtroDataInicio.valueProperty().addListener((o, a, n) -> aplicarFiltro());
        filtroDataFim   .valueProperty().addListener((o, a, n) -> aplicarFiltro());
    }

    @FXML private void onLimparFiltros() {
        fieldBusca.clear();
        filtroTipo.setValue("Todos");
        filtroDataInicio.setValue(null);
        filtroDataFim.setValue(null);
    }

    private void aplicarFiltro() {
        String    busca = fieldBusca.getText() == null ? "" : fieldBusca.getText().trim().toLowerCase();
        String    tipo  = filtroTipo.getValue();
        LocalDate di    = filtroDataInicio.getValue();
        LocalDate df    = filtroDataFim.getValue();

        movimentacoesFiltradas.setPredicate(m -> {
            boolean okB = busca.isEmpty()
                    || m.getNomeProduto().toLowerCase().contains(busca)
                    || m.getNomeUsuario().toLowerCase().contains(busca)
                    || m.getNumPedido().toLowerCase().contains(busca);
            boolean okT = tipo == null || tipo.equals("Todos")
                    || m.getTipo().equals(tipo);
            LocalDate dm = m.getData().toLocalDate();
            boolean okDi = di == null || !dm.isBefore(di);
            boolean okDf = df == null || !dm.isAfter(df);
            return okB && okT && okDi && okDf;
        });
    }

    // ── Colunas ───────────────────────────────────────────────
    private void configurarColunas() {
        colData      .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataFormatada()));
        colProduto   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeProduto()));
        colQuantidade.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantidade())));
        colUsuario   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeUsuario()));
        colPedido    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumPedido()));
        colNota      .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumeroNota()));
        colObservacao.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getObservacao()));

        for (TableColumn<Movimentacao, String> col : new TableColumn[]{
                colData, colProduto, colQuantidade, colUsuario, colPedido, colNota, colObservacao}) {
            col.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); return; }
                    setText(item); setFont(Font.font("Segoe UI", 13));
                    setStyle("-fx-text-fill:#0f172a;");
                }
            });
        }
        // Rebind
        colData      .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataFormatada()));
        colProduto   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeProduto()));
        colQuantidade.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantidade())));
        colUsuario   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeUsuario()));
        colPedido    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumPedido()));
        colNota      .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumeroNota()));
        colObservacao.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getObservacao()));

        // Badge tipo
        colTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo()));
        colTipo.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String tipo, boolean empty) {
                super.updateItem(tipo, empty);
                if (empty || tipo == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(tipo.replace("_", " "));
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                badge.setAlignment(Pos.CENTER);
                badge.setPrefWidth(115);
                badge.setStyle("-fx-background-radius:6; -fx-padding:4 10; -fx-font-size:11px; -fx-font-weight:bold;"
                        + estiloCor(tipo));
                HBox box = new HBox(badge); box.setAlignment(Pos.CENTER);
                setGraphic(box); setText(null);
            }
        });

        tabelaMovimentacoes.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Movimentacao item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setStyle("-fx-background-color:white;");
                else setStyle(getIndex() % 2 == 0 ? "-fx-background-color:white;" : "-fx-background-color:#fafafa;");
            }
        });
    }

    private String estiloCor(String tipo) {
        return switch (tipo) {
            case "ENTRADA"        -> "-fx-background-color:#dcfce7; -fx-text-fill:#166534;";
            case "SAÍDA"          -> "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b;";
            case "ENTRADA_MANUAL" -> "-fx-background-color:#dbeafe; -fx-text-fill:#1e40af;";
            case "SAIDA_MANUAL"   -> "-fx-background-color:#fef3c7; -fx-text-fill:#92400e;";
            default               -> "-fx-background-color:#f3f4f6; -fx-text-fill:#374151;";
        };
    }
}