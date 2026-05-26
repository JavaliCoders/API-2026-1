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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class notaFiscalController implements Initializable {

    // ── Tabela principal ──────────────────────────────────────
    @FXML private TableView<NotaFiscal>           tabelaNotas;
    @FXML private TableColumn<NotaFiscal, String> colNumero;
    @FXML private TableColumn<NotaFiscal, String> colPedido;
    @FXML private TableColumn<NotaFiscal, String> colFornecedor;
    @FXML private TableColumn<NotaFiscal, String> colEmissao;
    @FXML private TableColumn<NotaFiscal, String> colRegistro;
    @FXML private TableColumn<NotaFiscal, String> colValor;
    @FXML private TableColumn<NotaFiscal, String> colStatus;
    @FXML private TableColumn<NotaFiscal, Void>   colAcoes;

    // ── Filtros ───────────────────────────────────────────────
    @FXML private TextField        fieldBusca;
    @FXML private ComboBox<String> filtroStatus;
    @FXML private DatePicker       filtroDataInicio;
    @FXML private DatePicker       filtroDataFim;

    // ── Overlay de detalhes ───────────────────────────────────
    @FXML private StackPane overlayDetalhes;
    @FXML private Label     detalheNumNota;
    @FXML private Label     detalheStatus;
    @FXML private Label     detalhePedido;
    @FXML private Label     detalheFornecedor;
    @FXML private Label     detalheValor;
    @FXML private Label     detalheEmissao;
    @FXML private Label     detalheRegistro;
    @FXML private Label     detalheRegistradoPor;

    // Seção conferência
    @FXML private VBox  secaoConferencia;
    @FXML private Label detalheConferidoPor;
    @FXML private Label detalheDataConferencia;
    @FXML private Label detalheResultado;

    // Seção itens conferidos
    @FXML private VBox                        secaoItens;
    @FXML private TableView<NfItem>           tabelaItensDetalhe;
    @FXML private TableColumn<NfItem, String> colItemProduto;
    @FXML private TableColumn<NfItem, String> colItemUnidade;
    @FXML private TableColumn<NfItem, String> colItemQtdComprada;
    @FXML private TableColumn<NfItem, String> colItemQtdRecebida;
    @FXML private TableColumn<NfItem, String> colItemQtdRejeitada;
    @FXML private TableColumn<NfItem, String> colItemMotivo;

    private AnchorPane areaPrincipal;
    private ObservableList<NotaFiscal> todasNotas;
    private FilteredList<NotaFiscal>   notasFiltradas;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarFiltros();
        configurarColunas();
        configurarTabelaItensDetalhe();
        carregarNotas();

        tabelaNotas.getSelectionModel().selectedItemProperty()
                .addListener((obs, a, nf) -> { if (nf != null) abrirOverlay(nf); });
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
        String    busca  = fieldBusca.getText() == null ? ""
                : fieldBusca.getText().trim().toLowerCase();
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

    // ── Overlay de detalhes ───────────────────────────────────

    private void abrirOverlay(NotaFiscal nf) {
        detalheNumNota.setText(nf.getNumeroNota());
        detalheStatus .setText(formatarStatus(nf.getStatus()));
        detalheStatus .setStyle(estiloBadge(nf.getStatus()));

        detalhePedido    .setText(nf.getNumPedido());
        detalheFornecedor.setText(nf.getNomeFornecedor());
        detalheValor     .setText(
                String.format("R$ %.2f", nf.getValorNf()).replace(".", ","));

        detalheEmissao      .setText(nf.getDataEmissaoFormatada());
        detalheRegistro     .setText(nf.getDataRegistroFormatada());
        detalheRegistradoPor.setText(
                nf.getUsuarioRegistro() != null
                        ? nf.getUsuarioRegistro().getNome() : "—");

        boolean conferida = nf.getUsuarioConferencia() != null;
        if (conferida) {
            detalheConferidoPor.setText(nf.getUsuarioConferencia().getNome());
            detalheDataConferencia.setText(
                    nf.getDataConferencia() != null
                            ? nf.getDataConferencia().format(
                            java.time.format.DateTimeFormatter
                                    .ofPattern("dd/MM/yyyy HH:mm"))
                            : "—");

            boolean recusada = nf.getStatus().equals("RECUSADA");
            detalheResultado.setText(recusada
                    ? "RECUSADA — divergências encontradas"
                    : "CONFERIDA — sem divergências");
            detalheResultado.setStyle(
                    "-fx-font-size:13px; -fx-font-weight:bold;"
                            + "-fx-background-radius:6; -fx-padding:4 12;"
                            + (recusada
                            ? "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b;"
                            : "-fx-background-color:#dcfce7; -fx-text-fill:#166534;"));

            secaoConferencia.setVisible(true);
            secaoConferencia.setManaged(true);
        } else {
            secaoConferencia.setVisible(false);
            secaoConferencia.setManaged(false);
        }

        if (conferida) {
            ObservableList<NfItem> itens =
                    notaFiscalDAO.listarItensConferidos(nf.getIdNota());
            if (!itens.isEmpty()) {
                tabelaItensDetalhe.setItems(itens);
                secaoItens.setVisible(true);
                secaoItens.setManaged(true);
            } else {
                secaoItens.setVisible(false);
                secaoItens.setManaged(false);
            }
        } else {
            secaoItens.setVisible(false);
            secaoItens.setManaged(false);
        }

        overlayDetalhes.setVisible(true);
        overlayDetalhes.setManaged(true);
    }

    @FXML private void fecharDetalhes() {
        overlayDetalhes.setVisible(false);
        overlayDetalhes.setManaged(false);
        tabelaNotas.getSelectionModel().clearSelection();
    }

    // ── Tabela de itens no overlay ────────────────────────────

    private void configurarTabelaItensDetalhe() {
        colItemProduto    .setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNomeProduto()));
        colItemUnidade    .setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getUnidade()));
        colItemQtdComprada.setCellValueFactory(d ->
                new SimpleStringProperty(
                        String.valueOf(d.getValue().getQtdComprada())));
        colItemQtdRecebida.setCellValueFactory(d ->
                new SimpleStringProperty(
                        String.valueOf(d.getValue().getQtdRecebida())));
        colItemQtdRejeitada.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getQtdRejeitada() > 0
                        ? String.valueOf(d.getValue().getQtdRejeitada()) : "—"));
        colItemMotivo.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getMotivoDivergencia().isBlank()
                                ? "—" : d.getValue().getMotivoDivergencia()));

        // Qtd rejeitada colorida
        colItemQtdRejeitada.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                setStyle(item.equals("—")
                        ? "-fx-text-fill:#166534;"
                        : "-fx-text-fill:#dc2626;");
            }
        });
        colItemQtdRejeitada.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getQtdRejeitada() > 0
                        ? String.valueOf(d.getValue().getQtdRejeitada()) : "—"));

        tabelaItensDetalhe.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(NfItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setStyle("-fx-background-color:white;");
                else if (item.temDivergencia())
                    setStyle("-fx-background-color:#fff5f5;");
                else setStyle(getIndex() % 2 == 0
                            ? "-fx-background-color:white;"
                            : "-fx-background-color:#fafafa;");
            }
        });
    }

    // ── Colunas da tabela principal ───────────────────────────

    private void configurarColunas() {
        colNumero    .setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNumeroNota()));
        colPedido    .setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNumPedido()));
        colFornecedor.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNomeFornecedor()));
        colEmissao   .setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDataEmissaoFormatada()));
        colRegistro  .setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDataRegistroFormatada()));
        colValor     .setCellValueFactory(d ->
                new SimpleStringProperty(
                        String.format("R$ %.2f", d.getValue().getValorNf())
                                .replace(".", ",")));

        for (TableColumn<NotaFiscal, String> col : new TableColumn[]{
                colNumero, colPedido, colFornecedor,
                colEmissao, colRegistro, colValor}) {
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
        // Rebind
        colNumero    .setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNumeroNota()));
        colPedido    .setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNumPedido()));
        colFornecedor.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNomeFornecedor()));
        colEmissao   .setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDataEmissaoFormatada()));
        colRegistro  .setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDataRegistroFormatada()));
        colValor     .setCellValueFactory(d ->
                new SimpleStringProperty(
                        String.format("R$ %.2f", d.getValue().getValorNf())
                                .replace(".", ",")));

        // Badge status
        colStatus.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null); setText(null); return;
                }
                Label badge = new Label(formatarStatus(status));
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                badge.setAlignment(Pos.CENTER);
                badge.setPrefWidth(105);
                badge.setStyle(estiloBadge(status));
                HBox box = new HBox(badge);
                box.setAlignment(Pos.CENTER);
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
                    fecharDetalhes();
                    mostrarMenuAcoes(btn, nf);
                });
            }
            private void estiloAcoes(boolean h) {
                btn.setStyle(h
                        ? "-fx-background-color:#1e40af; -fx-text-fill:white;" +
                        "-fx-background-radius:6; -fx-border-color:transparent;" +
                        "-fx-padding:6 14; -fx-cursor:hand;"
                        : "-fx-background-color:#eff6ff; -fx-text-fill:#1e40af;" +
                        "-fx-background-radius:6; -fx-border-color:#bfdbfe;" +
                        "-fx-border-width:1; -fx-padding:6 14; -fx-cursor:hand;");
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null
                        || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                NotaFiscal nf = (NotaFiscal) getTableRow().getItem();
                if (temAlgumaAcao(nf)) {
                    HBox box = new HBox(btn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                } else {
                    setGraphic(null);
                }
            }
        });

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
            case "REGISTRADA"  -> true;  // pode conferir
            case "DIVERGENTE"  -> true;  // pode reconferir
            default            -> false;
        };
    }

    private void mostrarMenuAcoes(Button btn, NotaFiscal nf) {
        ContextMenu menu = new ContextMenu();

        // Conferir (REGISTRADA) ou Reconferir (DIVERGENTE)
        if (nf.getStatus().equals("REGISTRADA")) {
            MenuItem mConferir = new MenuItem("🔍   Conferir nota fiscal");
            mConferir.setStyle("-fx-text-fill:#1e40af; -fx-font-size:14px; -fx-padding:4 8;");
            mConferir.setOnAction(e -> navegarParaConferencia(nf));
            menu.getItems().add(mConferir);
        }

        if (nf.getStatus().equals("DIVERGENTE")) {
            MenuItem mReconferir = new MenuItem("🔄   Reconferir nota fiscal");
            mReconferir.setStyle(
                    "-fx-text-fill:#92400e; -fx-font-size:14px; -fx-padding:4 8;");
            mReconferir.setOnAction(e -> reabrirEConferir(nf));
            menu.getItems().add(mReconferir);
        }

        if (!menu.getItems().isEmpty())
            menu.show(btn, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    // Reabrir conferência de nota DIVERGENTE
    private void reabrirEConferir(NotaFiscal nf) {
        Alert confirma = new Alert(Alert.AlertType.CONFIRMATION,
                "Reabrir a conferência da nota " + nf.getNumeroNota() + "?\n" +
                        "Os itens anteriores serão apagados e a conferência reiniciada.",
                ButtonType.OK, ButtonType.CANCEL);
        confirma.setHeaderText(null);
        confirma.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                boolean ok = notaFiscalDAO.reabrirConferencia(nf.getIdNota());
                if (ok) {
                    carregarNotas(); // recarrega lista com status REGISTRADA
                    navegarParaConferencia(nf); // abre conferência
                } else {
                    new Alert(Alert.AlertType.ERROR,
                            "Erro ao reabrir conferência.").showAndWait();
                }
            }
        });
    }

    // ── Navegação ─────────────────────────────────────────────

    @FXML private void onNovaNotaFiscal() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/registroNotaFiscal.fxml"));
            Node tela = loader.load();
            registroNotaFiscalController ctrl = loader.getController();
            ctrl.setAreaPrincipal(areaPrincipal);
            anchorar(tela);
            areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void navegarParaConferencia(NotaFiscal nf) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/conferenciaNotaFiscal.fxml"));
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
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/entradaEstoque.fxml"));
            Node tela = loader.load();
            entradaEstoqueController ctrl = loader.getController();
            ctrl.setAreaPrincipal(areaPrincipal);
            ctrl.setNotaFiscal(nf);
            anchorar(tela);
            areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void anchorar(Node tela) {
        AnchorPane.setTopAnchor   (tela, 0.0);
        AnchorPane.setBottomAnchor(tela, 0.0);
        AnchorPane.setLeftAnchor  (tela, 0.0);
        AnchorPane.setRightAnchor (tela, 0.0);
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
        String b = "-fx-background-radius:6; -fx-padding:4 10;" +
                "-fx-font-size:11px; -fx-font-weight:bold;";
        return b + switch (s) {
            case "REGISTRADA" ->
                    "-fx-background-color:#fef9c3; -fx-text-fill:#854d0e;";
            case "CONFERIDA"  ->
                    "-fx-background-color:#dcfce7; -fx-text-fill:#166534;";
            case "DIVERGENTE" ->
                    "-fx-background-color:#fef3c7; -fx-text-fill:#92400e;";
            case "RECUSADA"   ->
                    "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b;";
            default           ->
                    "-fx-background-color:#f3f4f6; -fx-text-fill:#374151;";
        };
    }
}