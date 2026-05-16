package api.controller;

import api.DAO.notaFiscalDAO;
import api.model.*;
import api.service.HistoricoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
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
import java.util.ResourceBundle;

public class entradaEstoqueController implements Initializable {

    // ── Info da nota ──────────────────────────────────────────
    @FXML private Label labelNumNota;
    @FXML private Label labelPedido;
    @FXML private Label labelFornecedor;
    @FXML private Label labelValor;
    @FXML private Label labelEmissao;
    @FXML private Label labelConferidoPor;

    // ── Tabela de itens ───────────────────────────────────────
    @FXML private TableView<NfItem>             tabelaItens;
    @FXML private TableColumn<NfItem, String>   colProduto;
    @FXML private TableColumn<NfItem, String>   colUnidade;
    @FXML private TableColumn<NfItem, String>   colQtdRecebida;
    @FXML private TableColumn<NfItem, String>   colQtdRejeitada;
    @FXML private TableColumn<NfItem, String>   colMotivo;
    @FXML private TableColumn<NfItem, String>   colStatus;

    @FXML private Button btnDarEntrada;
    @FXML private Button btnCancelar;

    private AnchorPane areaPrincipal;
    private NotaFiscal notaFiscal;
    private ObservableList<NfItem> itens;

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }

    public void setNotaFiscal(NotaFiscal nf) {
        this.notaFiscal = nf;
        preencherCabecalho();
        carregarItens();
        configurarColunas();
    }

    // ── Cabeçalho ─────────────────────────────────────────────
    private void preencherCabecalho() {
        labelNumNota    .setText(notaFiscal.getNumeroNota());
        labelPedido     .setText(notaFiscal.getNumPedido());
        labelFornecedor .setText(notaFiscal.getNomeFornecedor());
        labelValor      .setText(String.format("R$ %.2f", notaFiscal.getValorNf()).replace(".", ","));
        labelEmissao    .setText(notaFiscal.getDataEmissaoFormatada());
        if (notaFiscal.getUsuarioConferencia() != null)
            labelConferidoPor.setText(notaFiscal.getUsuarioConferencia().getNome());
        else
            labelConferidoPor.setText("—");
    }

    // ── Itens ─────────────────────────────────────────────────
    private void carregarItens() {
        itens = notaFiscalDAO.listarItensConferidos(notaFiscal.getIdNota());
        tabelaItens.setItems(itens);
    }

    // ── Colunas ───────────────────────────────────────────────
    private void configurarColunas() {
        colProduto    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeProduto()));
        colUnidade    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnidade()));
        colQtdRecebida.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQtdRecebida())));
        colQtdRejeitada.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getQtdRejeitada() > 0 ? String.valueOf(d.getValue().getQtdRejeitada()) : "—"));
        colMotivo     .setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getMotivoDivergencia().isBlank() ? "—" : d.getValue().getMotivoDivergencia()));

        // Estilo padrão
        for (TableColumn<NfItem, String> col : new TableColumn[]{
                colProduto, colUnidade, colQtdRecebida, colQtdRejeitada, colMotivo}) {
            col.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); return; }
                    setText(item); setFont(Font.font("Segoe UI", 13));
                    setStyle("-fx-text-fill:#0f172a;");
                }
            });
        }
        colProduto    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeProduto()));
        colUnidade    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnidade()));
        colQtdRecebida.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQtdRecebida())));
        colQtdRejeitada.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getQtdRejeitada() > 0 ? String.valueOf(d.getValue().getQtdRejeitada()) : "—"));
        colMotivo     .setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getMotivoDivergencia().isBlank() ? "—" : d.getValue().getMotivoDivergencia()));

        // Badge status por item
        colStatus.setCellValueFactory(d -> {
            boolean div = d.getValue().getQtdRejeitada() > 0;
            return new SimpleStringProperty(div ? "DIVERGENTE" : "OK");
        });
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(status);
                badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                badge.setAlignment(Pos.CENTER);
                badge.setPrefWidth(90);
                badge.setStyle("-fx-background-radius:6; -fx-padding:4 10; -fx-font-size:11px; -fx-font-weight:bold;"
                        + (status.equals("OK")
                        ? "-fx-background-color:#dcfce7; -fx-text-fill:#166534;"
                        : "-fx-background-color:#fef3c7; -fx-text-fill:#92400e;"));
                HBox box = new HBox(badge); box.setAlignment(Pos.CENTER);
                setGraphic(box); setText(null);
            }
        });

        tabelaItens.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(NfItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setStyle("-fx-background-color:white;");
                else if (item.temDivergencia()) setStyle("-fx-background-color:#fffbeb;");
                else setStyle(getIndex() % 2 == 0 ? "-fx-background-color:white;" : "-fx-background-color:#fafafa;");
            }
        });
    }

    // ── Dar entrada ───────────────────────────────────────────
    @FXML private void onDarEntrada() {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Entrada");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Confirmar a entrada dos produtos no estoque?\n"
                + "Os saldos serão atualizados e as movimentações registradas.");
        confirmacao.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                int idUsuario = SessaoUsuario.getInstancia().getIdUsuarioLogado();
                boolean ok = notaFiscalDAO.darEntrada(notaFiscal.getIdNota(), idUsuario);
                if (!ok) {
                    new Alert(Alert.AlertType.ERROR, "Erro ao dar entrada no estoque.").showAndWait();
                    return;
                }

                HistoricoService.registrar("Nota fiscal", "Entrada", notaFiscal.getIdNota(),
                        "Entrada no estoque — Nota " + notaFiscal.getNumeroNota()
                                + " — pedido " + notaFiscal.getNumPedido()
                                + " por " + SessaoUsuario.getInstancia().getNomeUsuarioLogado());

                new Alert(Alert.AlertType.INFORMATION,
                        "Entrada realizada com sucesso! Estoque atualizado.").showAndWait();
                voltarParaNotas();
            }
        });
    }

    @FXML private void onCancelar() {
        voltarParaNotas();
    }

    // ── Navegação ─────────────────────────────────────────────
    private void voltarParaNotas() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/notaFiscal.fxml"));
            Node tela = loader.load();
            notaFiscalController ctrl = loader.getController();
            ctrl.setAreaPrincipal(areaPrincipal);
            anchorar(tela); areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void anchorar(Node tela) {
        AnchorPane.setTopAnchor(tela, 0.0); AnchorPane.setBottomAnchor(tela, 0.0);
        AnchorPane.setLeftAnchor(tela, 0.0); AnchorPane.setRightAnchor(tela, 0.0);
    }
}