package api.controller;

import api.DAO.CotacaoDAO;
import api.model.Anexo;
import api.model.Cotacao;
import api.model.SessaoUsuario;
import api.model.Usuario;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class AprovacaoCotacaoController implements Initializable {

    @FXML private TextField searchPedido;
    @FXML private TextField searchFornecedor;
    @FXML private ComboBox<String> filtroStatus;

    @FXML private TableView<Cotacao> tabelaCotacoes;
    @FXML private TableColumn<Cotacao, Integer> colId;
    @FXML private TableColumn<Cotacao, String> colPedido;
    @FXML private TableColumn<Cotacao, String> colFornecedor;
    @FXML private TableColumn<Cotacao, String> colData;
    @FXML private TableColumn<Cotacao, String> colValor;
    @FXML private TableColumn<Cotacao, String> colStatus;
    @FXML private TableColumn<Cotacao, Void> colAcoes;

    @FXML private StackPane overlayDetalhes;
    @FXML private Label detalheTitulo;
    @FXML private Label detalheCod;
    @FXML private Label detalhePedido;
    @FXML private Label detalheStatus;
    @FXML private Label detalheFornecedor;
    @FXML private Label detalheData;
    @FXML private Label detalheValor;
    @FXML private Label detalheSolicitante;
    @FXML private Label detalheSetor;
    @FXML private Label detalheCentro;
    @FXML private Label detalheAnexo;
    @FXML private TextArea txtParecer;
    @FXML private Button btnAbrirAnexo;
    @FXML private Button btnNegar;
    @FXML private Button btnAprovar;

    private AnchorPane areaPrincipal;
    private ObservableList<Cotacao> todasCotacoes;
    private FilteredList<Cotacao> cotacoesFiltradas;
    private Cotacao cotacaoSelecionada;

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
                "Todos os status",
                "AGUARDANDO_APROVACAO",
                "APROVADO",
                "APROVADO_PARCIALMENTE",
                "NEGADO"
        ));
        filtroStatus.setValue("AGUARDANDO_APROVACAO");
    }

    private void configurarColunas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idCotacao"));
        colPedido.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNumPedido()));
        colFornecedor.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNomeFornecedor()));
        colData.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDataCriacaoFormatada()));
        colValor.setCellValueFactory(data ->
                new SimpleStringProperty(formatarMoeda(data.getValue().getValorTotal())));

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

                Label badge = new Label(formatarStatus(item));
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
                badge.setAlignment(Pos.CENTER);
                badge.setPrefWidth(145);
                badge.setStyle(getBadgeStyle(item));
                setGraphic(badge);
                setText(null);
            }
        });

        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnAnalisar = new Button("Analisar");

            {
                btnAnalisar.setStyle(
                        "-fx-background-color: #dbeafe; -fx-text-fill: #2563eb; " +
                                "-fx-background-radius: 6; -fx-font-size: 12px; " +
                                "-fx-font-weight: bold; -fx-cursor: hand; " +
                                "-fx-border-color: transparent; -fx-padding: 5 10;");
                btnAnalisar.setOnMouseEntered(e -> btnAnalisar.setStyle(
                        "-fx-background-color: #2563eb; -fx-text-fill: white; " +
                                "-fx-background-radius: 6; -fx-font-size: 12px; " +
                                "-fx-font-weight: bold; -fx-cursor: hand; " +
                                "-fx-border-color: transparent; -fx-padding: 5 10;"));
                btnAnalisar.setOnMouseExited(e -> btnAnalisar.setStyle(
                        "-fx-background-color: #dbeafe; -fx-text-fill: #2563eb; " +
                                "-fx-background-radius: 6; -fx-font-size: 12px; " +
                                "-fx-font-weight: bold; -fx-cursor: hand; " +
                                "-fx-border-color: transparent; -fx-padding: 5 10;"));
                btnAnalisar.setOnAction(e -> {
                    int index = getIndex();
                    if (index >= 0 && index < getTableView().getItems().size()) {
                        exibirDetalhes(getTableView().getItems().get(index));
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                HBox box = new HBox(btnAnalisar);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });

        tabelaCotacoes.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Cotacao item, boolean empty) {
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

    private void configurarBusca() {
        searchPedido.textProperty().addListener((obs, a, n) -> aplicarFiltros());
        searchFornecedor.textProperty().addListener((obs, a, n) -> aplicarFiltros());
        filtroStatus.valueProperty().addListener((obs, a, n) -> aplicarFiltros());
    }

    private void configurarSelecao() {
        tabelaCotacoes.getSelectionModel().selectedItemProperty().addListener(
                (obs, antigo, novo) -> {
                    if (novo != null) exibirDetalhes(novo);
                });
    }

    private void carregarDados() {
        todasCotacoes = CotacaoDAO.listarTodos();
        cotacoesFiltradas = new FilteredList<>(todasCotacoes, c -> true);

        SortedList<Cotacao> ordenadas = new SortedList<>(cotacoesFiltradas);
        ordenadas.comparatorProperty().bind(tabelaCotacoes.comparatorProperty());

        colId.setSortType(TableColumn.SortType.ASCENDING);
        tabelaCotacoes.getSortOrder().setAll(colId);
        tabelaCotacoes.setItems(ordenadas);
        aplicarFiltros();
    }

    private void aplicarFiltros() {
        if (cotacoesFiltradas == null) return;

        String pedido = textoFiltro(searchPedido);
        String fornecedor = textoFiltro(searchFornecedor);
        String status = filtroStatus.getValue();

        cotacoesFiltradas.setPredicate(c -> {
            boolean matchPedido = pedido.isEmpty()
                    || texto(c.getNumPedido()).contains(pedido);
            boolean matchFornecedor = fornecedor.isEmpty()
                    || texto(c.getNomeFornecedor()).contains(fornecedor);
            boolean matchStatus = status == null
                    || status.equals("Todos os status")
                    || status.equals(c.getStatus());
            return matchPedido && matchFornecedor && matchStatus;
        });
    }

    private void exibirDetalhes(Cotacao cotacao) {
        cotacaoSelecionada = cotacao;

        detalheTitulo.setText("Cotação #" + cotacao.getIdCotacao() + " - Pedido " + cotacao.getNumPedido());
        detalheCod.setText(String.valueOf(cotacao.getIdCotacao()));
        detalhePedido.setText(cotacao.getNumPedido());
        detalheFornecedor.setText(cotacao.getNomeFornecedor());
        detalheData.setText(cotacao.getDataCriacaoFormatada());
        detalheValor.setText(formatarMoeda(cotacao.getValorTotal()));
        detalheStatus.setText(formatarStatus(cotacao.getStatus()));
        detalheStatus.setStyle(getBadgeStyle(cotacao.getStatus()) +
                " -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 12;");

        if (cotacao.getPedido() != null) {
            detalheSolicitante.setText(cotacao.getPedido().getNomeSolicitante());
            detalheSetor.setText(cotacao.getPedido().getNomeSetor());
            detalheCentro.setText(cotacao.getPedido().getNomeCentroCusto());
        } else {
            detalheSolicitante.setText("-");
            detalheSetor.setText("-");
            detalheCentro.setText("-");
        }

        Anexo anexo = cotacao.getAnexo();
        boolean temAnexo = anexo != null && anexo.getCaminhoArquivo() != null
                && !anexo.getCaminhoArquivo().isBlank();
        detalheAnexo.setText(temAnexo ? anexo.getNomeArquivo() : "Sem anexo vinculado");
        btnAbrirAnexo.setDisable(!temAnexo);

        txtParecer.setText(cotacao.getParecer() == null ? "" : cotacao.getParecer());

        boolean aguardando = "AGUARDANDO_APROVACAO".equals(cotacao.getStatus());
        btnAprovar.setDisable(!aguardando);
        btnNegar.setDisable(!aguardando);

        overlayDetalhes.setVisible(true);
        overlayDetalhes.setManaged(true);
    }

    @FXML
    private void fecharDetalhes() {
        overlayDetalhes.setVisible(false);
        overlayDetalhes.setManaged(false);
        tabelaCotacoes.getSelectionModel().clearSelection();
        cotacaoSelecionada = null;
    }

    @FXML
    private void abrirAnexo() {
        if (cotacaoSelecionada == null || cotacaoSelecionada.getAnexo() == null) {
            exibirAlerta(Alert.AlertType.INFORMATION, "Anexo", "Nenhum anexo vinculado a esta cotação.");
            return;
        }

        File arquivo = new File(cotacaoSelecionada.getAnexo().getCaminhoArquivo());
        if (!arquivo.exists()) {
            exibirAlerta(Alert.AlertType.WARNING, "Anexo", "Arquivo não encontrado: " + arquivo.getAbsolutePath());
            return;
        }

        if (!Desktop.isDesktopSupported()) {
            exibirAlerta(Alert.AlertType.WARNING, "Anexo", "Abertura de arquivos não suportada neste ambiente.");
            return;
        }

        try {
            Desktop.getDesktop().open(arquivo);
        } catch (IOException e) {
            exibirAlerta(Alert.AlertType.ERROR, "Anexo", "Não foi possível abrir o anexo: " + e.getMessage());
        }
    }

    @FXML
    private void onAprovarCotacao() {
        alterarStatus("APROVADO");
    }

    @FXML
    private void onNegarCotacao() {
        if (txtParecer.getText() == null || txtParecer.getText().trim().isEmpty()) {
            exibirAlerta(Alert.AlertType.WARNING, "Validação", "Informe um parecer para reprovar a cotação.");
            txtParecer.requestFocus();
            return;
        }
        alterarStatus("NEGADO");
    }

    private void alterarStatus(String novoStatus) {
        if (cotacaoSelecionada == null) return;

        Usuario usuarioLogado = SessaoUsuario.getInstancia().getUsuarioLogado();
        if (usuarioLogado == null) {
            exibirAlerta(Alert.AlertType.WARNING, "Sessão", "Nenhum usuário logado para registrar a decisão.");
            return;
        }

        String parecer = txtParecer.getText() == null ? "" : txtParecer.getText().trim();
        boolean atualizado = CotacaoDAO.atualizarStatus(
                cotacaoSelecionada.getIdCotacao(),
                usuarioLogado.getIdUsuario(),
                novoStatus,
                parecer
        );

        if (!atualizado) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível atualizar a cotação.");
            return;
        }

        cotacaoSelecionada.statusProperty().set(novoStatus);
        cotacaoSelecionada.parecerProperty().set(parecer);
        cotacaoSelecionada.aprovadorProperty().set(usuarioLogado);
        cotacaoSelecionada.dataAprovacaoProperty().set(LocalDateTime.now());

        tabelaCotacoes.refresh();
        aplicarFiltros();
        fecharDetalhes();
        exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Cotação " + formatarStatus(novoStatus).toLowerCase() + " com sucesso.");
    }

    @FXML
    private void onSearch() {
        aplicarFiltros();
    }

    @FXML
    private void onFiltroStatus() {
        aplicarFiltros();
    }

    private String textoFiltro(TextField field) {
        return field.getText() == null ? "" : field.getText().trim().toLowerCase();
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.toLowerCase();
    }

    private String formatarMoeda(double valor) {
        return String.format("R$ %.2f", valor).replace(".", ",");
    }

    private String formatarStatus(String status) {
        if (status == null) return "";
        return status.replace("_", " ");
    }

    private String getBadgeStyle(String status) {
        return switch (status) {
            case "AGUARDANDO_APROVACAO" ->
                    "-fx-background-color: #fef9c3; -fx-text-fill: #854d0e; -fx-background-radius: 6; -fx-padding: 4 8;";
            case "APROVADO" ->
                    "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-background-radius: 6; -fx-padding: 4 8;";
            case "APROVADO_PARCIALMENTE" ->
                    "-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1; -fx-background-radius: 6; -fx-padding: 4 8;";
            case "NEGADO" ->
                    "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 6; -fx-padding: 4 8;";
            default ->
                    "-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-background-radius: 6; -fx-padding: 4 8;";
        };
    }

    private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
