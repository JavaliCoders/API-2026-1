package api.controller;

import api.DAO.notaFiscalDAO;
import api.model.*;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class notaFiscalController implements Initializable {

    @FXML private TableView<NotaFiscal>           tabelaNotas;
    @FXML private TableColumn<NotaFiscal, String> colNumero;
    @FXML private TableColumn<NotaFiscal, String> colPedido;
    @FXML private TableColumn<NotaFiscal, String> colFornecedor;
    @FXML private TableColumn<NotaFiscal, String> colEmissao;
    @FXML private TableColumn<NotaFiscal, String> colRegistro;
    @FXML private TableColumn<NotaFiscal, String> colValor;
    @FXML private TableColumn<NotaFiscal, String> colStatus;
    @FXML private TableColumn<NotaFiscal, Void>   colAcoes;

    @FXML private TextField        fieldBusca;
    @FXML private ComboBox<String> filtroStatus;
    @FXML private DatePicker       filtroDataInicio;
    @FXML private DatePicker       filtroDataFim;

    private AnchorPane areaPrincipal;
    private ObservableList<NotaFiscal> todasNotas;
    private FilteredList<NotaFiscal>   notasFiltradas;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarFiltros();
        configurarColunas();
        carregarNotas();
    }

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }

    // ── Dados ─────────────────────────────────────────────────

    private void carregarNotas() {
        todasNotas     = notaFiscalDAO.listarTodas();
        notasFiltradas = new FilteredList<>(todasNotas, n -> true);
        tabelaNotas.setItems(notasFiltradas);
    }

    // ── Filtros ───────────────────────────────────────────────

    private void configurarFiltros() {
        filtroStatus.setItems(FXCollections.observableArrayList(
                "Todos os status", "REGISTRADA", "CONFERIDA", "DIVERGENTE", "RECUSADA"));
        filtroStatus.setValue("Todos os status");
        fieldBusca      .textProperty() .addListener((o, a, n) -> aplicarFiltro());
        filtroStatus    .valueProperty().addListener((o, a, n) -> aplicarFiltro());
        filtroDataInicio.valueProperty().addListener((o, a, n) -> aplicarFiltro());
        filtroDataFim   .valueProperty().addListener((o, a, n) -> aplicarFiltro());
    }

    @FXML private void onLimparFiltros() {
        fieldBusca.clear();
        filtroStatus.setValue("Todos os status");
        filtroDataInicio.setValue(null);
        filtroDataFim.setValue(null);
    }

    private void aplicarFiltro() {
        String    busca  = fieldBusca.getText() == null ? "" : fieldBusca.getText().trim().toLowerCase();
        String    status = filtroStatus.getValue();
        LocalDate di     = filtroDataInicio.getValue();
        LocalDate df     = filtroDataFim.getValue();
        notasFiltradas.setPredicate(n -> {
            boolean okB = busca.isEmpty()
                    || n.getNumeroNota().toLowerCase().contains(busca)
                    || n.getNumPedido().toLowerCase().contains(busca)
                    || n.getNomeFornecedor().toLowerCase().contains(busca);
            boolean okS = status == null || status.equals("Todos os status")
                    || n.getStatus().equals(status);
            LocalDate dr = n.getDataRegistro().toLocalDate();
            boolean okDi = di == null || !dr.isBefore(di);
            boolean okDf = df == null || !dr.isAfter(df);
            return okB && okS && okDi && okDf;
        });
    }

    // ── Colunas ───────────────────────────────────────────────

    private void configurarColunas() {
        colNumero    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumeroNota()));
        colPedido    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumPedido()));
        colFornecedor.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeFornecedor()));
        colEmissao   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataEmissaoFormatada()));
        colRegistro  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataRegistroFormatada()));
        colValor     .setCellValueFactory(d -> new SimpleStringProperty(
                String.format("R$ %.2f", d.getValue().getValorNf()).replace(".", ",")));

        for (TableColumn<NotaFiscal, String> col : new TableColumn[]{
                colNumero, colPedido, colFornecedor, colEmissao, colRegistro, colValor}) {
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
        colNumero    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumeroNota()));
        colPedido    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumPedido()));
        colFornecedor.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeFornecedor()));
        colEmissao   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataEmissaoFormatada()));
        colRegistro  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataRegistroFormatada()));
        colValor     .setCellValueFactory(d -> new SimpleStringProperty(
                String.format("R$ %.2f", d.getValue().getValorNf()).replace(".", ",")));

        // Badge status
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(formatarStatus(status));
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                badge.setAlignment(Pos.CENTER);
                badge.setPrefWidth(105);
                badge.setStyle(estiloBadge(status));
                HBox box = new HBox(badge); box.setAlignment(Pos.CENTER);
                setGraphic(box); setText(null);
            }
        });

        // Coluna Ações
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("⚙  Ações  ▾");
            {
                btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                estiloAcoes(false);
                btn.setOnMouseEntered(e -> estiloAcoes(true));
                btn.setOnMouseExited (e -> estiloAcoes(false));
                btn.setOnAction(e -> {
                    NotaFiscal nf = getTableView().getItems().get(getIndex());
                    mostrarMenuAcoes(btn, nf);
                });
            }
            private void estiloAcoes(boolean h) {
                btn.setStyle(h
                        ? "-fx-background-color:#1e40af; -fx-text-fill:white; -fx-background-radius:6; -fx-border-color:transparent; -fx-padding:6 14; -fx-cursor:hand;"
                        : "-fx-background-color:#eff6ff; -fx-text-fill:#1e40af; -fx-background-radius:6; -fx-border-color:#bfdbfe; -fx-border-width:1; -fx-padding:6 14; -fx-cursor:hand;");
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                NotaFiscal nf = (NotaFiscal) getTableRow().getItem();
                if (temAlgumaAcao(nf)) {
                    HBox box = new HBox(btn); box.setAlignment(Pos.CENTER); setGraphic(box);
                } else {
                    setGraphic(null);
                }
            }
        });

        // Zebra striping
        tabelaNotas.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(NotaFiscal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setStyle("-fx-background-color:white;");
                else setStyle(getIndex() % 2 == 0
                        ? "-fx-background-color:white;"
                        : "-fx-background-color:#fafafa;");
            }
        });
    }

    // ── Menu de ações ─────────────────────────────────────────

    private boolean temAlgumaAcao(NotaFiscal nf) {
        return switch (nf.getStatus()) {
            case "REGISTRADA"            -> true;
            // Dar entrada apenas se a entrada ainda NÃO está completa
            case "CONFERIDA", "DIVERGENTE" -> !notaFiscalDAO.entradaCompleta(nf.getIdNota());
            default -> false;
        };
    }

    private void mostrarMenuAcoes(Button btn, NotaFiscal nf) {
        ContextMenu menu = new ContextMenu();

        // Conferir nota (REGISTRADA)
        if (nf.getStatus().equals("REGISTRADA")) {
            MenuItem mConferir = new MenuItem("🔍   Conferir nota fiscal");
            mConferir.setStyle("-fx-text-fill:#1e40af; -fx-font-size:14px; -fx-padding:4 8;");
            mConferir.setOnAction(e -> navegarParaConferencia(nf));
            menu.getItems().add(mConferir);
        }

        // Dar entrada — só se não estiver completa
        if ((nf.getStatus().equals("CONFERIDA") || nf.getStatus().equals("DIVERGENTE"))
                && !notaFiscalDAO.entradaCompleta(nf.getIdNota())) {
            MenuItem mEntrada = new MenuItem("📦   Dar entrada no estoque");
            mEntrada.setStyle("-fx-text-fill:#166534; -fx-font-size:14px; -fx-padding:4 8;");
            mEntrada.setOnAction(e -> navegarParaEntrada(nf));
            menu.getItems().add(mEntrada);
        }

        if (!menu.getItems().isEmpty())
            menu.show(btn, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    // ── Navegação ─────────────────────────────────────────────

    @FXML private void onNovaNotaFiscal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/registroNotaFiscal.fxml"));
            Node tela = loader.load();
            registroNotaFiscalController ctrl = loader.getController();
            ctrl.setAreaPrincipal(areaPrincipal);
            anchorar(tela);
            areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void navegarParaConferencia(NotaFiscal nf) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/conferenciaNotaFiscal.fxml"));
            Node tela = loader.load();
            conferenciaNotaFiscalController ctrl = loader.getController();
            ctrl.setAreaPrincipal(areaPrincipal);
            ctrl.setNotaFiscal(nf);
            anchorar(tela);
            areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void navegarParaEntrada(NotaFiscal nf) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/entradaEstoque.fxml"));
            Node tela = loader.load();
            entradaEstoqueController ctrl = loader.getController();
            ctrl.setAreaPrincipal(areaPrincipal);
            ctrl.setNotaFiscal(nf);
            anchorar(tela);
            areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void anchorar(Node tela) {
        AnchorPane.setTopAnchor   (tela, 0.0); AnchorPane.setBottomAnchor(tela, 0.0);
        AnchorPane.setLeftAnchor  (tela, 0.0); AnchorPane.setRightAnchor (tela, 0.0);
    }

    // ── Utilitários ───────────────────────────────────────────

    private String formatarStatus(String s) {
        return switch (s) {
            case "REGISTRADA" -> "Registrada";
            case "CONFERIDA"  -> "Conferida";
            case "DIVERGENTE" -> "Divergente";
            case "RECUSADA"   -> "Recusada";
            default           -> s;
        };
    }

    private String estiloBadge(String s) {
        String b = "-fx-background-radius:6; -fx-padding:4 10; -fx-font-size:11px; -fx-font-weight:bold;";
        return b + switch (s) {
            case "REGISTRADA" -> "-fx-background-color:#fef9c3; -fx-text-fill:#854d0e;";
            case "CONFERIDA"  -> "-fx-background-color:#dcfce7; -fx-text-fill:#166534;";
            case "DIVERGENTE" -> "-fx-background-color:#fef3c7; -fx-text-fill:#92400e;";
            case "RECUSADA"   -> "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b;";
            default           -> "-fx-background-color:#f3f4f6; -fx-text-fill:#374151;";
        };
    }
}