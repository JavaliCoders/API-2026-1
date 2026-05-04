package api.controller;

import api.DAO.notaFiscalDAO;
import api.controller.notaFiscalController;
import api.model.NfItem;
import api.model.NotaFiscal;
import api.model.SessaoUsuario;
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

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class conferenciaNotaFiscalController implements Initializable {

    @FXML private Label labelNumNota;
    @FXML private Label labelPedido;
    @FXML private Label labelFornecedor;
    @FXML private Label labelValor;
    @FXML private Label labelEmissao;
    @FXML private Label labelRegistradoPor;

    @FXML private TableView<NfItem> tabelaItens;
    @FXML private TableColumn<NfItem, String> colProduto;
    @FXML private TableColumn<NfItem, String> colUnidade;
    @FXML private TableColumn<NfItem, String> colQtdComprada;
    @FXML
    private TableColumn<NfItem, Void>   colQtdRecebida;
    @FXML private TableColumn<NfItem, Void>   colQtdRejeitada;
    @FXML private TableColumn<NfItem, Void>   colMotivo;

    @FXML private Label  labelErro;
    @FXML private Button btnConfirmar;
    @FXML private Button btnCancelar;

    private AnchorPane areaPrincipal;
    private NotaFiscal notaFiscal;
    private ObservableList<NfItem> itens; // ← campo da classe, não variável local

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        labelErro.setVisible(false);
        labelErro.setManaged(false);
        configurarColunas(); // ← DEVE ser chamado aqui
    }

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }

    public void setNotaFiscal(NotaFiscal nf) {
        this.notaFiscal = nf;
        preencherCabecalho(); // ← agora é chamado
        carregarItens();      // ← agora salva em this.itens
    }

    private void preencherCabecalho() {
        labelNumNota      .setText(notaFiscal.getNumeroNota());
        labelPedido       .setText(notaFiscal.getNumPedido());
        labelFornecedor   .setText(notaFiscal.getNomeFornecedor());
        labelValor        .setText(String.format("R$ %.2f", notaFiscal.getValorNf()).replace(".", ","));
        labelEmissao      .setText(notaFiscal.getDataEmissaoFormatada());
        labelRegistradoPor.setText(notaFiscal.getUsuarioRegistro().getNome());
    }

    private void carregarItens() {
        this.itens = notaFiscalDAO.listarItensParaConferencia(notaFiscal.getIdNota()); // ← this.itens
        tabelaItens.setItems(this.itens);
    }

    private void configurarColunas() {
        colProduto    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeProduto()));
        colUnidade    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnidade()));
        colQtdComprada.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQtdComprada())));

        colProduto    .setCellFactory(c -> celulaPadrao());
        colUnidade    .setCellFactory(c -> celulaPadrao());
        colQtdComprada.setCellFactory(c -> celulaPadrao());

        // Rebind após setCellFactory
        colProduto    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeProduto()));
        colUnidade    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnidade()));
        colQtdComprada.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQtdComprada())));

        // Qtd recebida — TextField editável
        colQtdRecebida.setCellFactory(col -> new TableCell<>() {
            private final TextField tf = new TextField();
            {
                tf.setPrefWidth(70);
                tf.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; " +
                        "-fx-border-radius:6; -fx-background-radius:6; -fx-font-size:13px;");
                tf.textProperty().addListener((obs, o, n) -> {
                    if (!n.matches("[0-9]*")) tf.setText(o);
                });
                tf.focusedProperty().addListener((obs, o, focused) -> {
                    if (!focused) commit();
                });
            }
            private void commit() {
                if (getTableRow() == null || getTableRow().getItem() == null) return;
                try {
                    int val = tf.getText().isBlank() ? 0 : Integer.parseInt(tf.getText());
                    ((NfItem) getTableRow().getItem()).setQtdRecebida(val);
                } catch (NumberFormatException ignored) {}
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                NfItem item = (NfItem) getTableRow().getItem();
                tf.setText(item.getQtdRecebida() == 0 ? "" : String.valueOf(item.getQtdRecebida()));
                HBox box = new HBox(tf); box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });

        // Qtd rejeitada — TextField editável
        colQtdRejeitada.setCellFactory(col -> new TableCell<>() {
            private final TextField tf = new TextField();
            {
                tf.setPrefWidth(70);
                tf.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; " +
                        "-fx-border-radius:6; -fx-background-radius:6; -fx-font-size:13px;");
                tf.textProperty().addListener((obs, o, n) -> {
                    if (!n.matches("[0-9]*")) tf.setText(o);
                });
                tf.focusedProperty().addListener((obs, o, focused) -> {
                    if (!focused) commit();
                });
            }
            private void commit() {
                if (getTableRow() == null || getTableRow().getItem() == null) return;
                try {
                    int val = tf.getText().isBlank() ? 0 : Integer.parseInt(tf.getText());
                    ((NfItem) getTableRow().getItem()).setQtdRejeitada(val);
                } catch (NumberFormatException ignored) {}
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                NfItem item = (NfItem) getTableRow().getItem();
                tf.setText(item.getQtdRejeitada() == 0 ? "" : String.valueOf(item.getQtdRejeitada()));
                HBox box = new HBox(tf); box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });

        // Motivo divergência — TextField editável
        colMotivo.setCellFactory(col -> new TableCell<>() {
            private final TextField tf = new TextField();
            {
                tf.setPrefWidth(200);
                tf.setPromptText("Informe se houver rejeição...");
                tf.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1; " +
                        "-fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px;");
                tf.focusedProperty().addListener((obs, o, focused) -> {
                    if (!focused && getTableRow() != null && getTableRow().getItem() != null)
                        ((NfItem) getTableRow().getItem()).setMotivoDivergencia(tf.getText());
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                NfItem item = (NfItem) getTableRow().getItem();
                tf.setText(item.getMotivoDivergencia() != null ? item.getMotivoDivergencia() : "");
                HBox box = new HBox(tf); box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        tabelaItens.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(NfItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setStyle("-fx-background-color:white;");
                else setStyle(getIndex() % 2 == 0
                        ? "-fx-background-color:white;"
                        : "-fx-background-color:#fafafa;");
            }
        });
    }

    @FXML private void onConfirmar() {
        btnConfirmar.requestFocus();

        if (itens == null || itens.isEmpty()) {
            mostrarErro("Nenhum item carregado para conferência.");
            return;
        }

        if (!validar()) return;

        boolean temDivergencia = itens.stream().anyMatch(NfItem::temDivergencia);
        String  novoStatus     = temDivergencia ? "RECUSADA" : "CONFERIDA";

        int idUsuario = SessaoUsuario.getInstancia().getIdUsuarioLogado();

        boolean okItens = notaFiscalDAO.inserirItensConferencia(notaFiscal.getIdNota(), itens);
        if (!okItens) { mostrarErro("Erro ao salvar itens da conferência."); return; }

        boolean okNota = notaFiscalDAO.conferir(notaFiscal.getIdNota(), idUsuario, novoStatus);
        if (!okNota) { mostrarErro("Erro ao atualizar a nota fiscal."); return; }

        // Dar entrada no estoque apenas se CONFERIDA (sem divergências)
        if (novoStatus.equals("CONFERIDA")) {
            notaFiscalDAO.darEntrada(notaFiscal.getIdNota(), idUsuario);
        }

        HistoricoService.registrar("Nota fiscal", "Conferência", notaFiscal.getIdNota(),
                "Nota " + notaFiscal.getNumeroNota() + " conferida por "
                        + SessaoUsuario.getInstancia().getNomeUsuarioLogado()
                        + " — resultado: " + novoStatus);

        String msg = temDivergencia
                ? "Conferência registrada com divergências. Status: RECUSADA."
                : "Conferência concluída sem divergências. Entrada no estoque realizada.";
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
        voltarParaNotas();
    }

    @FXML private void onCancelar() { voltarParaNotas(); }

    private boolean validar() {
        for (NfItem item : itens) {
            if (item.getQtdRecebida() == 0 && item.getQtdRejeitada() == 0) {
                mostrarErro("Preencha qtd recebida e/ou rejeitada para: " + item.getNomeProduto());
                return false;
            }
            if (item.getQtdRejeitada() > 0 && item.getMotivoDivergencia().isBlank()) {
                mostrarErro("Informe o motivo da divergência para: " + item.getNomeProduto());
                return false;
            }
        }
        labelErro.setVisible(false);
        labelErro.setManaged(false);
        return true;
    }

    private void mostrarErro(String msg) {
        labelErro.setText("⚠  " + msg);
        labelErro.setVisible(true);
        labelErro.setManaged(true);
    }

    private TableCell<NfItem, String> celulaPadrao() {
        return new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item);
                setFont(Font.font("Segoe UI", 13));
                setStyle("-fx-text-fill:#0f172a;");
            }
        };
    }

    private void voltarParaNotas() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/notaFiscal.fxml"));
            Node tela = loader.load();
            notaFiscalController ctrl = loader.getController();
            ctrl.setAreaPrincipal(areaPrincipal);
            anchorar(tela);
            areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void anchorar(Node tela) {
        AnchorPane.setTopAnchor(tela, 0.0); AnchorPane.setBottomAnchor(tela, 0.0);
        AnchorPane.setLeftAnchor(tela, 0.0); AnchorPane.setRightAnchor(tela, 0.0);
    }
}