package api.controller;

import api.DAO.historicoDAO;
import api.model.Historico;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class historicoSistemaController implements Initializable {

    // ── Filtros ───────────────────────────────────────────────
    @FXML private TextField        searchIdPedido;   // filtra entidadeId quando tipo=Pedido
    @FXML private TextField        searchUsuario;    // filtra nomeUsuario (texto livre)
    @FXML private DatePicker       filtroDataInicio;
    @FXML private DatePicker       filtroDataFim;
    @FXML private ComboBox<String> filtroEntidade;
    @FXML private ComboBox<String> filtroAcao;
    @FXML private ComboBox<String> filtroUsuario;
    @FXML private Label            labelTotal;

    // ── Tabela ────────────────────────────────────────────────
    @FXML private TableView<Historico>           tabelaHistorico;
    @FXML private TableColumn<Historico, String> colData;
    @FXML private TableColumn<Historico, String> colEntidade;
    @FXML private TableColumn<Historico, String> colAcao;
    @FXML private TableColumn<Historico, String> colId;       // "ID Pedido"
    @FXML private TableColumn<Historico, String> colUsuario;
    @FXML private TableColumn<Historico, String> colDescricao;

    // ── Overlay ───────────────────────────────────────────────
    @FXML private StackPane overlayDetalhes;
    @FXML private Label     detalheDescricaoTitulo;
    @FXML private Label     detalheAcaoBadge;
    @FXML private Label     detalheData;
    @FXML private Label     detalheEntidade;
    @FXML private Label     detalheId;
    @FXML private Label     detalheUsuario;
    @FXML private Label     detalheAcao;
    @FXML private Label     detalheDescricao;

    private AnchorPane areaPrincipal;
    private final ObservableList<String[]> usuariosCache = FXCollections.observableArrayList();

    // Lista completa mantida em memória para filtro local
    private ObservableList<Historico> todosRegistros = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarFiltros();
        configurarColunas();
        carregarDados();

        tabelaHistorico.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, selecionado) -> {
                    if (selecionado != null) abrirOverlay(selecionado);
                });
    }

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }

    // ── Filtros ───────────────────────────────────────────────

    private void configurarFiltros() {
        filtroEntidade.setItems(FXCollections.observableArrayList(
                "Todas", "Pedido", "Cotação", "Compra",
                "Nota fiscal", "Produto", "Fornecedor",
                "Usuário", "Centro de Custo", "Sistema"));
        filtroEntidade.setValue("Todas");

        filtroAcao.setItems(FXCollections.observableArrayList(
                "Todas", "Cadastro", "Alteração", "Cancelamento",
                "Aprovação", "Negação", "Conferência",
                "Entrada", "Saída"));
        filtroAcao.setValue("Todas");

        carregarUsuarios();

        filtroEntidade  .valueProperty().addListener((o, a, n) -> aplicarFiltro());
        filtroAcao      .valueProperty().addListener((o, a, n) -> aplicarFiltro());
        filtroUsuario   .valueProperty().addListener((o, a, n) -> aplicarFiltro());
        filtroDataInicio.valueProperty().addListener((o, a, n) -> aplicarFiltro());
        filtroDataFim   .valueProperty().addListener((o, a, n) -> aplicarFiltro());

        // Só números no campo de ID do pedido
        searchIdPedido.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("[0-9]*")) searchIdPedido.setText(o);
            else aplicarFiltro();
        });
    }

    private void carregarUsuarios() {
        usuariosCache.setAll(historicoDAO.listarUsuariosComHistorico());
        ObservableList<String> nomes = FXCollections.observableArrayList("Todos");
        for (String[] u : usuariosCache) nomes.add(u[1]);
        filtroUsuario.setItems(nomes);
        filtroUsuario.setValue("Todos");
    }

    @FXML private void onSearch(KeyEvent e) { aplicarFiltro(); }
    @FXML private void onFiltrar()          { aplicarFiltro(); }

    @FXML private void onLimparFiltros() {
        searchIdPedido  .clear();
        searchUsuario   .clear();
        filtroDataInicio.setValue(null);
        filtroDataFim   .setValue(null);
        filtroEntidade  .setValue("Todas");
        filtroAcao      .setValue("Todas");
        filtroUsuario   .setValue("Todos");
        aplicarFiltro();
    }

    // ── Dados ─────────────────────────────────────────────────

    private void carregarDados() {
        // Busca todos sem filtro do DAO, filtra em memória para combinar
        // os parâmetros do DAO com os campos de texto livre
        todosRegistros = historicoDAO.listarFiltrado(
                null, null, "Todas", "Todas", null, null);
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        LocalDate di = filtroDataInicio.getValue();
        LocalDate df = filtroDataFim.getValue();

        String entidade = filtroEntidade.getValue();
        String acao     = filtroAcao.getValue();

        // Combo usuário → id
        Integer idUsuarioCombo = null;
        String usuarioSel = filtroUsuario.getValue();
        if (usuarioSel != null && !usuarioSel.equals("Todos")) {
            for (String[] u : usuariosCache) {
                if (u[1].equals(usuarioSel)) {
                    idUsuarioCombo = Integer.parseInt(u[0]);
                    break;
                }
            }
        }

        // Texto livre — nome do usuário
        String buscaNome = searchUsuario.getText() == null
                ? "" : searchUsuario.getText().trim().toLowerCase();

        // ID do pedido digitado
        Integer idPedidoBusca = null;
        if (!searchIdPedido.getText().isBlank()) {
            try { idPedidoBusca = Integer.parseInt(searchIdPedido.getText().trim()); }
            catch (NumberFormatException ignored) {}
        }

        final Integer idUsuarioFinal  = idUsuarioCombo;
        final Integer idPedidoFinal   = idPedidoBusca;

        ObservableList<Historico> filtrada = todosRegistros.filtered(h -> {

            // Data início
            if (di != null && h.getData() != null &&
                    h.getData().toLocalDate().isBefore(di)) return false;

            // Data fim
            if (df != null && h.getData() != null &&
                    h.getData().toLocalDate().isAfter(df)) return false;

            // Entidade
            if (entidade != null && !entidade.equals("Todas") &&
                    !entidade.equalsIgnoreCase(h.getEntidadeTipo())) return false;

            // Ação
            if (acao != null && !acao.equals("Todas") &&
                    !acao.equalsIgnoreCase(h.getAcao())) return false;

            // Combo usuário (por id)
            if (idUsuarioFinal != null && h.getIdUsuario() != idUsuarioFinal) return false;

            // Texto livre — nome usuário
            if (!buscaNome.isEmpty() &&
                    (h.getNomeUsuario() == null ||
                            !h.getNomeUsuario().toLowerCase().contains(buscaNome))) return false;

            // ID do pedido — só filtra registros do tipo Pedido
            if (idPedidoFinal != null) {
                if (!"Pedido".equalsIgnoreCase(h.getEntidadeTipo())) return false;
                if (h.getEntidadeId() == null ||
                        !h.getEntidadeId().equals(idPedidoFinal)) return false;
            }

            return true;
        });

        tabelaHistorico.setItems(filtrada);
        labelTotal.setText(filtrada.size() + " registro(s)");
    }

    // ── Overlay ───────────────────────────────────────────────

    private void abrirOverlay(Historico h) {
        // Título: "Pedido #42" ou "Produto #7" etc.
        detalheDescricaoTitulo.setText(h.getEntidadeTipo() + " · " + idPedidoLabel(h));

        detalheData     .setText(h.getDataFormatada());
        detalheEntidade .setText(h.getEntidadeTipo());

        // Coluna/overlay "ID Pedido": mostra entidadeId só se for Pedido, senão "—"
        detalheId.setText(idPedidoLabel(h));

        detalheUsuario  .setText(h.getNomeUsuario());
        detalheAcao     .setText(h.getAcao());
        detalheDescricao.setText(h.getDescricao());

        detalheAcaoBadge.setText(h.getAcao());
        detalheAcaoBadge.setStyle(estiloAcaoBadgeOverlay(h.getAcao()));

        overlayDetalhes.setVisible(true);
        overlayDetalhes.setManaged(true);
    }

    @FXML private void fecharDetalhes() {
        overlayDetalhes.setVisible(false);
        overlayDetalhes.setManaged(false);
        tabelaHistorico.getSelectionModel().clearSelection();
    }

    /**
     * Retorna o entidadeId formatado quando o tipo é Pedido; caso contrário "—".
     * Isso garante que a coluna "ID Pedido" só exibe algo relevante para registros
     * de pedido, evitando confusão com IDs de outras entidades.
     */
    private String idPedidoLabel(Historico h) {
        if ("Pedido".equalsIgnoreCase(h.getEntidadeTipo()) && h.getEntidadeId() != null) {
            return String.valueOf(h.getEntidadeId());
        }
        return "—";
    }

    // ── Colunas ───────────────────────────────────────────────

    private void configurarColunas() {

        // Texto simples — data e usuário
        colData   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataFormatada()));
        colUsuario.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeUsuario()));

        for (TableColumn<Historico, String> col : new TableColumn[]{colData, colUsuario}) {
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
        colData   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDataFormatada()));
        colUsuario.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeUsuario()));

        // Coluna "ID Pedido" — exibe entidadeId só para registros de Pedido
        colId.setCellValueFactory(d -> new SimpleStringProperty(idPedidoLabel(d.getValue())));
        colId.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item);
                setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                setStyle("—".equals(item)
                        ? "-fx-text-fill:#cbd5e1;"   // cinza claro para "—"
                        : "-fx-text-fill:#2563eb;");  // azul para IDs reais
            }
        });
        colId.setCellValueFactory(d -> new SimpleStringProperty(idPedidoLabel(d.getValue())));

        // Badge — Entidade
        colEntidade.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEntidadeTipo()));
        colEntidade.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(item);
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                badge.setAlignment(Pos.CENTER);
                badge.setPrefWidth(105);
                badge.setStyle(estiloEntidade(item));
                HBox box = new HBox(badge); box.setAlignment(Pos.CENTER);
                setGraphic(box); setText(null);
            }
        });
        colEntidade.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEntidadeTipo()));

        // Badge — Ação
        colAcao.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAcao()));
        colAcao.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(item);
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                badge.setAlignment(Pos.CENTER);
                badge.setPrefWidth(90);
                badge.setStyle(estiloAcao(item));
                HBox box = new HBox(badge); box.setAlignment(Pos.CENTER);
                setGraphic(box); setText(null);
            }
        });
        colAcao.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAcao()));

        // Descrição com wrap
        colDescricao.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescricao()));
        colDescricao.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item);
                setFont(Font.font("Segoe UI", 13));
                setStyle("-fx-text-fill:#0f172a;");
                setWrapText(true);
            }
        });
        colDescricao.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescricao()));

        // Zebra
        tabelaHistorico.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Historico item, boolean empty) {
                super.updateItem(item, empty);
                setStyle(empty || item == null ? "-fx-background-color:white;"
                        : (getIndex() % 2 == 0 ? "-fx-background-color:white;"
                        : "-fx-background-color:#fafafa;"));
            }
        });
    }

    // ── Estilos ───────────────────────────────────────────────

    private String estiloEntidade(String e) {
        String b = "-fx-background-radius:6; -fx-padding:4 8; -fx-font-size:11px; -fx-font-weight:bold;";
        return b + switch (e) {
            case "Pedido"          -> "-fx-background-color:#fef9c3; -fx-text-fill:#854d0e;";
            case "Cotação"         -> "-fx-background-color:#dbeafe; -fx-text-fill:#1e40af;";
            case "Compra"          -> "-fx-background-color:#ede9fe; -fx-text-fill:#5b21b6;";
            case "Nota fiscal"     -> "-fx-background-color:#cffafe; -fx-text-fill:#0e7490;";
            case "Produto"         -> "-fx-background-color:#dcfce7; -fx-text-fill:#166534;";
            case "Fornecedor"      -> "-fx-background-color:#fce7f3; -fx-text-fill:#9d174d;";
            case "Usuário"         -> "-fx-background-color:#f3f4f6; -fx-text-fill:#374151;";
            case "Centro de Custo" -> "-fx-background-color:#ffedd5; -fx-text-fill:#c2410c;";
            case "Sistema"         -> "-fx-background-color:#1e293b; -fx-text-fill:white;";
            default                -> "-fx-background-color:#f3f4f6; -fx-text-fill:#6b7280;";
        };
    }

    private String estiloAcao(String a) {
        String b = "-fx-background-radius:6; -fx-padding:4 8; -fx-font-size:11px; -fx-font-weight:bold;";
        return b + switch (a) {
            case "Cadastro"     -> "-fx-background-color:#dcfce7; -fx-text-fill:#166534;";
            case "Alteração"    -> "-fx-background-color:#dbeafe; -fx-text-fill:#1e40af;";
            case "Cancelamento" -> "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b;";
            case "Aprovação"    -> "-fx-background-color:#d1fae5; -fx-text-fill:#065f46;";
            case "Negação"      -> "-fx-background-color:#fee2e2; -fx-text-fill:#7f1d1d;";
            case "Conferência"  -> "-fx-background-color:#fef9c3; -fx-text-fill:#854d0e;";
            case "Entrada"      -> "-fx-background-color:#cffafe; -fx-text-fill:#0e7490;";
            case "Saída"        -> "-fx-background-color:#ffedd5; -fx-text-fill:#c2410c;";
            default             -> "-fx-background-color:#f3f4f6; -fx-text-fill:#6b7280;";
        };
    }

    private String estiloAcaoBadgeOverlay(String a) {
        String base = "-fx-background-radius:6; -fx-padding:4 12; -fx-font-size:12px; -fx-font-weight:bold;";
        return base + switch (a) {
            case "Cadastro"     -> "-fx-background-color:rgba(220,252,231,0.9); -fx-text-fill:#166534;";
            case "Alteração"    -> "-fx-background-color:rgba(219,234,254,0.9); -fx-text-fill:#1e40af;";
            case "Cancelamento" -> "-fx-background-color:rgba(254,226,226,0.9); -fx-text-fill:#991b1b;";
            case "Aprovação"    -> "-fx-background-color:rgba(209,250,229,0.9); -fx-text-fill:#065f46;";
            case "Negação"      -> "-fx-background-color:rgba(254,226,226,0.9); -fx-text-fill:#7f1d1d;";
            case "Entrada"      -> "-fx-background-color:rgba(207,250,254,0.9); -fx-text-fill:#0e7490;";
            case "Saída"        -> "-fx-background-color:rgba(255,237,213,0.9); -fx-text-fill:#c2410c;";
            default             -> "-fx-background-color:rgba(243,244,246,0.9); -fx-text-fill:#374151;";
        };
    }
}