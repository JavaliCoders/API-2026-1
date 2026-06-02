package api.controller;

import api.DAO.movimentacaoDAO;
import api.model.Movimentacao;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class movimentacaoController implements Initializable {

    // ── Tabela ────────────────────────────────────────────────
    @FXML private TableView<Movimentacao>           tabelaMovimentacoes;
    @FXML private TableColumn<Movimentacao, String> colData;
    @FXML private TableColumn<Movimentacao, String> colProduto;
    @FXML private TableColumn<Movimentacao, String> colTipo;
    @FXML private TableColumn<Movimentacao, String> colQuantidade;
    @FXML private TableColumn<Movimentacao, String> colUsuario;
    @FXML private TableColumn<Movimentacao, String> colPedido;
    @FXML private TableColumn<Movimentacao, String> colNota;
    @FXML private TableColumn<Movimentacao, String> colObservacao;

    // ── Filtros ───────────────────────────────────────────────
    @FXML private TextField        fieldBusca;
    @FXML private ComboBox<String> filtroTipo;
    @FXML private DatePicker       filtroDataInicio;
    @FXML private DatePicker       filtroDataFim;

    // ── Overlay ───────────────────────────────────────────────
    @FXML private StackPane overlayDetalhes;
    @FXML private Label     detalheProduto;
    @FXML private Label     detalheTipo;
    @FXML private Label     detalheData;
    @FXML private Label     detalheQuantidade;
    @FXML private Label     detalheUsuario;
    @FXML private Label     detalhePedido;
    @FXML private Label     detalheNota;
    @FXML private Label     detalheObservacao;

    // ── Botão nova manual ─────────────────────────────────────
    @FXML private Button btnNovaManual;

    private AnchorPane areaPrincipal;
    private ObservableList<Movimentacao> todasMovimentacoes;
    private FilteredList<Movimentacao>   movimentacoesFiltradas;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarFiltros();
        configurarColunas();
        carregarMovimentacoes();

        // Abre overlay ao clicar na linha
        tabelaMovimentacoes.getSelectionModel().selectedItemProperty()
                .addListener((obs, ant, novo) -> { if (novo != null) abrirOverlay(novo); });
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
        filtroDataFim   .setValue(null);
    }

    private void aplicarFiltro() {
        String    busca = fieldBusca.getText() == null ? "" : fieldBusca.getText().trim().toLowerCase();
        String    tipo  = filtroTipo.getValue();
        LocalDate di    = filtroDataInicio.getValue();
        LocalDate df    = filtroDataFim   .getValue();

        movimentacoesFiltradas.setPredicate(m -> {
            // Busca por produto, usuário ou pedido
            boolean okB = busca.isEmpty()
                    || m.getNomeProduto().toLowerCase().contains(busca)
                    || m.getNomeUsuario().toLowerCase().contains(busca)
                    || (m.getNumPedido() != null && m.getNumPedido().toLowerCase().contains(busca));
            boolean okT = tipo == null || tipo.equals("Todos")
                    || m.getTipo().equals(tipo);
            LocalDate dm = m.getData().toLocalDate();
            boolean okDi = di == null || !dm.isBefore(di);
            boolean okDf = df == null || !dm.isAfter(df);
            return okB && okT && okDi && okDf;
        });
    }

    // ── Overlay ───────────────────────────────────────────────

    private void abrirOverlay(Movimentacao m) {
        detalheProduto  .setText(m.getNomeProduto());
        detalheData     .setText(m.getDataFormatada());
        detalheQuantidade.setText(m.getQuantidade() + " unidade(s)");
        detalheUsuario  .setText(m.getNomeUsuario());
        detalhePedido   .setText(m.getNumPedido()   != null && !m.getNumPedido().isBlank()   ? m.getNumPedido()   : "—");
        detalheNota     .setText(m.getNumeroNota()  != null && !m.getNumeroNota().isBlank()  ? m.getNumeroNota()  : "—");
        detalheObservacao.setText(m.getObservacao() != null && !m.getObservacao().isBlank()  ? m.getObservacao()  : "—");

        // Badge de tipo
        detalheTipo.setText(m.getTipo().replace("_", " "));
        detalheTipo.setStyle(estiloBadgeTipo(m.getTipo()));

        overlayDetalhes.setVisible(true);
        overlayDetalhes.setManaged(true);
    }

    @FXML private void fecharDetalhes() {
        overlayDetalhes.setVisible(false);
        overlayDetalhes.setManaged(false);
        tabelaMovimentacoes.getSelectionModel().clearSelection();
    }

    // ── Nova movimentação manual ──────────────────────────────

    @FXML private void onNovaManual() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/cadastroMovimentacaoManual.fxml"));
            Node tela = loader.load();
            cadastroMovimentacaoManualController ctrl = loader.getController();
            ctrl.setAreaPrincipal(areaPrincipal);
            anchorar(tela);
            areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) {
            System.err.println("Erro ao abrir movimentação manual: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Colunas ───────────────────────────────────────────────

    private void configurarColunas() {
        colData      .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataFormatada()));
        colProduto   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeProduto()));
        colQuantidade.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantidade())));
        colUsuario   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeUsuario()));
        colPedido    .setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNumPedido() != null ? d.getValue().getNumPedido() : "—"));
        colNota      .setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNumeroNota() != null ? d.getValue().getNumeroNota() : "—"));
        colObservacao.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getObservacao() != null ? d.getValue().getObservacao() : "—"));

        // Texto simples
        for (TableColumn<Movimentacao, String> col : new TableColumn[]{
                colData, colProduto, colQuantidade, colUsuario, colPedido, colNota, colObservacao}) {
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
        colData      .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataFormatada()));
        colProduto   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeProduto()));
        colQuantidade.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantidade())));
        colUsuario   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeUsuario()));
        colPedido    .setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNumPedido() != null ? d.getValue().getNumPedido() : "—"));
        colNota      .setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNumeroNota() != null ? d.getValue().getNumeroNota() : "—"));
        colObservacao.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getObservacao() != null ? d.getValue().getObservacao() : "—"));

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
                badge.setStyle(estiloBadgeTipo(tipo));
                HBox box = new HBox(badge); box.setAlignment(Pos.CENTER);
                setGraphic(box); setText(null);
            }
        });
        colTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo()));

        // Zebra
        tabelaMovimentacoes.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Movimentacao item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setStyle("-fx-background-color:white;");
                else setStyle(getIndex() % 2 == 0
                        ? "-fx-background-color:white;"
                        : "-fx-background-color:#fafafa;");
            }
        });
    }

    // ── Estilos ───────────────────────────────────────────────

    private String estiloBadgeTipo(String tipo) {
        String base = "-fx-background-radius:6; -fx-padding:4 10; -fx-font-size:11px; -fx-font-weight:bold;";
        return base + switch (tipo) {
            case "ENTRADA"        -> "-fx-background-color:#dcfce7; -fx-text-fill:#166534;";
            case "SAÍDA"          -> "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b;";
            case "ENTRADA_MANUAL" -> "-fx-background-color:#dbeafe; -fx-text-fill:#1e40af;";
            case "SAIDA_MANUAL"   -> "-fx-background-color:#fef3c7; -fx-text-fill:#92400e;";
            default               -> "-fx-background-color:#f3f4f6; -fx-text-fill:#374151;";
        };
    }

    private void anchorar(Node tela) {
        AnchorPane.setTopAnchor   (tela, 0.0);
        AnchorPane.setBottomAnchor(tela, 0.0);
        AnchorPane.setLeftAnchor  (tela, 0.0);
        AnchorPane.setRightAnchor (tela, 0.0);
    }
}