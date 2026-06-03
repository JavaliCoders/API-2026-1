package api.controller;

import api.DAO.notaFiscalDAO;
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

    @FXML private TableView<NfItem>           tabelaItens;
    @FXML private TableColumn<NfItem, String> colProduto;
    @FXML private TableColumn<NfItem, String> colUnidade;
    @FXML private TableColumn<NfItem, String> colQtdComprada;
    @FXML private TableColumn<NfItem, Void>   colQtdRecebida;
    @FXML private TableColumn<NfItem, Void>   colQtdRejeitada;
    @FXML private TableColumn<NfItem, Void>   colMotivo;

    @FXML private Label  labelErro;
    @FXML private Button btnConfirmar;
    @FXML private Button btnCancelar;

    private AnchorPane             areaPrincipal;
    private NotaFiscal             notaFiscal;
    private ObservableList<NfItem> itens;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        labelErro.setVisible(false);
        labelErro.setManaged(false);
        configurarColunas();
    }

    public void setAreaPrincipal(AnchorPane ap) { this.areaPrincipal = ap; }

    public void setNotaFiscal(NotaFiscal nf) {
        this.notaFiscal = nf;
        preencherCabecalho();
        carregarItens();
    }

    private void preencherCabecalho() {
        labelNumNota      .setText(notaFiscal.getNumeroNota());
        labelPedido       .setText(notaFiscal.getNumPedido());
        labelFornecedor   .setText(notaFiscal.getNomeFornecedor());
        labelValor        .setText(
                String.format("R$ %.2f", notaFiscal.getValorNf()).replace(".", ","));
        labelEmissao      .setText(notaFiscal.getDataEmissaoFormatada());
        labelRegistradoPor.setText(notaFiscal.getUsuarioRegistro().getNome());
    }

    private void carregarItens() {
        this.itens = notaFiscalDAO.listarItensParaConferencia(notaFiscal.getIdNota());
        tabelaItens.setItems(this.itens);
    }

    // ── Confirmar conferência ─────────────────────────────────
    @FXML private void onConfirmar() {
        btnConfirmar.requestFocus();

        if (itens == null || itens.isEmpty()) {
            mostrarErro("Nenhum item carregado para conferência."); return;
        }
        if (!validar()) return;

        boolean temDivergencia = itens.stream().anyMatch(NfItem::temDivergencia);

        // Se há divergência, pergunta se é DIVERGENTE (reconferir) ou RECUSADA (nova NF)
        if (temDivergencia) {
            Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
            dlg.setTitle("Divergência encontrada");
            dlg.setHeaderText("Foram encontradas divergências nesta nota.");
            dlg.setContentText(
                    "Escolha como proceder:\n\n" +
                            "• DIVERGENTE — Reabrir conferência desta mesma nota\n" +
                            "• RECUSADA   — Recusar esta nota e registrar uma nova\n" +
                            "• Cancelar   — Voltar sem salvar");

            ButtonType btnDivergente = new ButtonType("DIVERGENTE");
            ButtonType btnRecusada   = new ButtonType("RECUSADA");
            ButtonType btnCancelarDlg = new ButtonType("Cancelar",
                    ButtonBar.ButtonData.CANCEL_CLOSE);
            dlg.getButtonTypes().setAll(btnDivergente, btnRecusada, btnCancelarDlg);

            dlg.showAndWait().ifPresent(resp -> {
                if (resp == btnDivergente) processarDivergente();
                else if (resp == btnRecusada) processarRecusada();
                // se cancelar, não faz nada
            });
        } else {
            processarConferida();
        }
    }

    // ── CONFERIDA: entrada automática no estoque ──────────────
    private void processarConferida() {
        int idUsuario = SessaoUsuario.getInstancia().getIdUsuarioLogado();

        boolean okItens = notaFiscalDAO.inserirItensConferencia(
                notaFiscal.getIdNota(), itens);
        if (!okItens) { mostrarErro("Erro ao salvar itens."); return; }

        boolean okNota = notaFiscalDAO.conferir(
                notaFiscal.getIdNota(), idUsuario, "CONFERIDA");
        if (!okNota) { mostrarErro("Erro ao atualizar nota."); return; }

        // Entrada automática
        boolean okEntrada = notaFiscalDAO.darEntrada(
                notaFiscal.getIdNota(), idUsuario);
        if (!okEntrada) { mostrarErro("Erro ao dar entrada no estoque."); return; }

        HistoricoService.registrar("Nota fiscal", "Conferência",
                notaFiscal.getIdNota(),
                "Nota " + notaFiscal.getNumeroNota() + " CONFERIDA por "
                        + SessaoUsuario.getInstancia().getNomeUsuarioLogado()
                        + " — entrada automática realizada.");
        HistoricoService.registrar("Pedido", "Alteração",
                notaFiscal.getCompra().getPedido().getIdPedido(),
                "Entrada no estoque via nota " + notaFiscal.getNumeroNota()
                        + " por " + SessaoUsuario.getInstancia().getNomeUsuarioLogado());

        new Alert(Alert.AlertType.INFORMATION,
                "Conferência concluída! Entrada no estoque realizada automaticamente.")
                .showAndWait();
        voltarParaNotas();
    }

    // ── DIVERGENTE: salva conferência e reabre a mesma nota ───
    private void processarDivergente() {
        int idUsuario = SessaoUsuario.getInstancia().getIdUsuarioLogado();

        boolean okItens = notaFiscalDAO.inserirItensConferencia(
                notaFiscal.getIdNota(), itens);
        if (!okItens) { mostrarErro("Erro ao salvar itens."); return; }

        boolean okNota = notaFiscalDAO.conferir(
                notaFiscal.getIdNota(), idUsuario, "DIVERGENTE");
        if (!okNota) { mostrarErro("Erro ao atualizar nota."); return; }

        HistoricoService.registrar("Nota fiscal", "Conferência",
                notaFiscal.getIdNota(),
                "Nota " + notaFiscal.getNumeroNota() + " com DIVERGÊNCIAS por "
                        + SessaoUsuario.getInstancia().getNomeUsuarioLogado()
                        + " — aguardando nova conferência.");

        new Alert(Alert.AlertType.WARNING,
                "Divergências registradas. A nota poderá ser conferida novamente.")
                .showAndWait();
        voltarParaNotas();
    }

    // ── RECUSADA: recusa nota e reabilita compra para nova NF ─
    private void processarRecusada() {
        int idUsuario = SessaoUsuario.getInstancia().getIdUsuarioLogado();
        int idCompra  = notaFiscal.getCompra().getIdCompra();

        boolean okItens = notaFiscalDAO.inserirItensConferencia(
                notaFiscal.getIdNota(), itens);
        if (!okItens) { mostrarErro("Erro ao salvar itens."); return; }

        boolean okRecusa = notaFiscalDAO.recusar(
                notaFiscal.getIdNota(), idUsuario, idCompra);
        if (!okRecusa) { mostrarErro("Erro ao recusar nota."); return; }

        HistoricoService.registrar("Nota fiscal", "Cancelamento",
                notaFiscal.getIdNota(),
                "Nota " + notaFiscal.getNumeroNota() + " RECUSADA por "
                        + SessaoUsuario.getInstancia().getNomeUsuarioLogado()
                        + " — compra reabilitada para nova nota fiscal.");

        new Alert(Alert.AlertType.INFORMATION,
                "Nota recusada. A compra foi reabilitada para registro de nova nota fiscal.")
                .showAndWait();
        voltarParaNotas();
    }

    @FXML private void onCancelar() { voltarParaNotas(); }

    // ── Validação ─────────────────────────────────────────────
    private boolean validar() {
        for (NfItem item : itens) {
            // Nenhuma quantidade informada
            if (item.getQtdRecebida() == 0 && item.getQtdRejeitada() == 0) {
                mostrarErro("Preencha qtd recebida e/ou rejeitada para: "
                        + item.getNomeProduto());
                return false;
            }

            // Se recebeu menos do que foi comprado, a diferença deve ser rejeitada
            int diferenca = item.getQtdComprada() - item.getQtdRecebida();
            if (diferenca > 0) {
                int totalInformado = item.getQtdRecebida() + item.getQtdRejeitada();
                if (totalInformado < item.getQtdComprada()) {
                    mostrarErro("Para \"" + item.getNomeProduto()
                            + "\": a soma de recebido + rejeitado deve ser igual à "
                            + "quantidade comprada (" + item.getQtdComprada() + ").");
                    return false;
                }
            }

            // Se rejeitou algo, motivo é obrigatório
            if (item.getQtdRejeitada() > 0 && item.getMotivoDivergencia().isBlank()) {
                mostrarErro("Informe o motivo da divergência para: "
                        + item.getNomeProduto());
                return false;
            }

            // Não pode receber/rejeitar mais do que foi comprado
            if (item.getQtdRecebida() + item.getQtdRejeitada() > item.getQtdComprada()) {
                mostrarErro("Para \"" + item.getNomeProduto()
                        + "\": total informado excede a quantidade comprada ("
                        + item.getQtdComprada() + ").");
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

    // ── Colunas ───────────────────────────────────────────────
    private void configurarColunas() {
        colProduto    .setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNomeProduto()));
        colUnidade    .setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getUnidade()));
        colQtdComprada.setCellValueFactory(d ->
                new SimpleStringProperty(
                        String.valueOf(d.getValue().getQtdComprada())));

        colProduto    .setCellFactory(c -> celulaPadrao());
        colUnidade    .setCellFactory(c -> celulaPadrao());
        colQtdComprada.setCellFactory(c -> celulaPadrao());

        colProduto    .setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getNomeProduto()));
        colUnidade    .setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getUnidade()));
        colQtdComprada.setCellValueFactory(d ->
                new SimpleStringProperty(
                        String.valueOf(d.getValue().getQtdComprada())));

        // Qtd recebida — editável
        colQtdRecebida.setCellFactory(col -> new TableCell<>() {
            private final TextField tf = new TextField();
            {
                tf.setPrefWidth(70);
                tf.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1;" +
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
                    int val = tf.getText().isBlank() ? 0
                            : Integer.parseInt(tf.getText());
                    ((NfItem) getTableRow().getItem()).setQtdRecebida(val);
                } catch (NumberFormatException ignored) {}
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null
                        || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                NfItem item = (NfItem) getTableRow().getItem();
                tf.setText(item.getQtdRecebida() == 0 ? ""
                        : String.valueOf(item.getQtdRecebida()));
                HBox box = new HBox(tf); box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });

        // Qtd rejeitada — editável
        colQtdRejeitada.setCellFactory(col -> new TableCell<>() {
            private final TextField tf = new TextField();
            {
                tf.setPrefWidth(70);
                tf.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1;" +
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
                    int val = tf.getText().isBlank() ? 0
                            : Integer.parseInt(tf.getText());
                    ((NfItem) getTableRow().getItem()).setQtdRejeitada(val);
                } catch (NumberFormatException ignored) {}
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null
                        || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                NfItem item = (NfItem) getTableRow().getItem();
                tf.setText(item.getQtdRejeitada() == 0 ? ""
                        : String.valueOf(item.getQtdRejeitada()));
                HBox box = new HBox(tf); box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });

        // Motivo — editável
        colMotivo.setCellFactory(col -> new TableCell<>() {
            private final TextField tf = new TextField();
            {
                tf.setPrefWidth(200);
                tf.setPromptText("Informe se houver rejeição...");
                tf.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#cbd5e1;" +
                        "-fx-border-radius:6; -fx-background-radius:6; -fx-font-size:12px;");
                tf.focusedProperty().addListener((obs, o, focused) -> {
                    if (!focused && getTableRow() != null
                            && getTableRow().getItem() != null)
                        ((NfItem) getTableRow().getItem())
                                .setMotivoDivergencia(tf.getText());
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getTableRow() == null
                        || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                NfItem item = (NfItem) getTableRow().getItem();
                tf.setText(item.getMotivoDivergencia() != null
                        ? item.getMotivoDivergencia() : "");
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
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/notaFiscal.fxml"));
            Node tela = loader.load();
            notaFiscalController ctrl = loader.getController();
            ctrl.setAreaPrincipal(areaPrincipal);
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
}