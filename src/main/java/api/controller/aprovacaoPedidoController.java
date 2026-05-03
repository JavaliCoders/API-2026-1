package api.controller;

import api.DAO.pedidoDAO;
import api.model.*;
import api.service.HistoricoService;
import api.service.NotificacaoService;
import api.util.PermissaoUtil;
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
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class aprovacaoPedidoController implements Initializable {

    // ── Cabeçalho ─────────────────────────────────────────────
    @FXML private Label labelSubtitulo;
    @FXML private Label badgeStatus;

    // ── Informações do pedido ─────────────────────────────────
    @FXML private Label fieldNumPedido;
    @FXML private Label fieldData;
    @FXML private Label fieldSolicitante;
    @FXML private Label fieldSetor;
    @FXML private Label fieldCentro;
    @FXML private Label fieldValor;

    // ── Tabela de itens ───────────────────────────────────────
    @FXML private TableView<PedidoProduto>            tabelaItens;
    @FXML private TableColumn<PedidoProduto, String>  colProduto;
    @FXML private TableColumn<PedidoProduto, String>  colUnidade;
    @FXML private TableColumn<PedidoProduto, String>  colQtdSolic;
    @FXML private TableColumn<PedidoProduto, String>  colVlrUnit;
    @FXML private TableColumn<PedidoProduto, String>  colVlrTotal;
    @FXML private Label labelTotal;

    // ── Card de aprovação (só DIRETOR + EM_APROVACAO) ─────────
    @FXML private VBox     cardAprovacao;
    @FXML private TextArea fieldParecer;
    @FXML private Label    erroParecer;

    // ── Card de resultado (todos, após decisão) ────────────────
    @FXML private VBox  cardResultado;
    @FXML private Label labelAprovador;
    @FXML private Label labelDataAprovacao;
    @FXML private Label labelParecer;

    private AnchorPane areaPrincipal;
    private Pedido     pedido;
    private ObservableList<PedidoProduto> itens;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final boolean isDiretor = PermissaoUtil.temPermissao("DIRETOR");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColunas();
    }

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;

        // ── Preenche cabeçalho ────────────────────────────────
        fieldNumPedido .setText(pedido.getNumPedido());
        fieldData      .setText(pedido.getDataAberturaFormatada());
        fieldSolicitante.setText(pedido.getNomeSolicitante());
        fieldSetor     .setText(pedido.getNomeSetor());
        fieldCentro    .setText(pedido.getNomeCentroCusto());
        fieldValor     .setText(
                String.format("R$ %.2f", pedido.getValorTotalEstimado()).replace(".", ","));

        labelSubtitulo.setText("Pedido aberto em " + pedido.getDataAberturaFormatada()
                + "  |  Solicitante: " + pedido.getNomeSolicitante());

        // Badge de status
        badgeStatus.setText(formatarStatus(pedido.getStatus()));
        badgeStatus.setStyle(estiloBadge(pedido.getStatus()));

        // ── Carrega itens ─────────────────────────────────────
        itens = pedidoDAO.listarItens(pedido.getIdPedido());
        tabelaItens.setItems(itens);

        double total = itens.stream()
                .mapToDouble(PedidoProduto::getValorTotal).sum();
        labelTotal.setText(String.format("R$ %.2f", total).replace(".", ","));

        // ── Exibe card correto conforme status e perfil ───────
        String status = pedido.getStatus();

        if (status.equals("EM_APROVACAO") && isDiretor) {
            // Diretor vê o formulário de aprovação
            cardAprovacao.setVisible(true);
            cardAprovacao.setManaged(true);
            cardResultado.setVisible(false);
            cardResultado.setManaged(false);

        } else if (status.equals("APROVADO") || status.equals("NEGADO")) {
            // Todos veem o resultado da decisão
            cardAprovacao.setVisible(false);
            cardAprovacao.setManaged(false);
            preencherResultado();
            cardResultado.setVisible(true);
            cardResultado.setManaged(true);

        } else {
            // EM_APROVACAO mas não é diretor — só visualiza
            cardAprovacao.setVisible(false);
            cardAprovacao.setManaged(false);
            cardResultado.setVisible(false);
            cardResultado.setManaged(false);
        }
    }

    private void preencherResultado() {
        if (pedido.getAprovador() != null) {
            labelAprovador.setText(pedido.getAprovador().getNome());
        } else {
            labelAprovador.setText("—");
        }

        if (pedido.getDataAprovacao() != null) {
            labelDataAprovacao.setText(pedido.getDataAprovacao().format(FMT));
        } else {
            labelDataAprovacao.setText("—");
        }

        String parecer = pedido.getParecer();
        labelParecer.setText(parecer != null && !parecer.isBlank() ? parecer : "Sem parecer informado.");
    }

    // ── Colunas da tabela de itens ────────────────────────────

    private void configurarColunas() {
        colProduto .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeProduto()));
        colUnidade .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnidadeProduto()));
        colQtdSolic.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getQtdSolicitada())));
        colVlrUnit .setCellValueFactory(d -> new SimpleStringProperty(
                String.format("R$ %.2f", d.getValue().getValorUnitario()).replace(".", ",")));
        colVlrTotal.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("R$ %.2f", d.getValue().getValorTotal()).replace(".", ",")));

        // Fonte maior igual ao padrão do sistema
        for (TableColumn<PedidoProduto, String> col :
                new TableColumn[]{colProduto, colUnidade, colQtdSolic, colVlrUnit, colVlrTotal}) {
            col.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); return; }
                    setText(item);
                    setFont(Font.font("Segoe UI", 13));
                    setStyle("-fx-text-fill: #0f172a;");
                }
            });
        }
        // Rebind após setCellFactory
        colProduto .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeProduto()));
        colUnidade .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnidadeProduto()));
        colQtdSolic.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getQtdSolicitada())));
        colVlrUnit .setCellValueFactory(d -> new SimpleStringProperty(
                String.format("R$ %.2f", d.getValue().getValorUnitario()).replace(".", ",")));
        colVlrTotal.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("R$ %.2f", d.getValue().getValorTotal()).replace(".", ",")));

        // Zebra striping
        tabelaItens.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(PedidoProduto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setStyle("-fx-background-color: white;");
                else setStyle(getIndex() % 2 == 0
                        ? "-fx-background-color: white;"
                        : "-fx-background-color: #fafafa;");
            }
        });
    }

    // ── Aprovar ───────────────────────────────────────────────

    @FXML
    private void onAprovar() {
        erroParecer.setText("");
        String parecer = fieldParecer.getText().trim();

        int idAprovador = SessaoUsuario.getInstancia().getIdUsuarioLogado();
        String nomeAprovador = SessaoUsuario.getInstancia().getNomeUsuarioLogado();

        boolean ok = pedidoDAO.aprovar(pedido.getIdPedido(), idAprovador, parecer);

        if (ok) {
            // Histórico
            HistoricoService.registrar("Pedido", "Aprovação", pedido.getIdPedido(),
                    "Pedido " + pedido.getNumPedido() + " aprovado por " + nomeAprovador
                            + (parecer.isBlank() ? "" : ". Parecer: " + parecer));

            // Notificação usando o serviço existente
            NotificacaoService.notificarPedidoAprovado(
                    pedido.getIdPedido(),
                    pedido.getNumPedido(),
                    pedido.getValorTotalEstimado(),
                    nomeAprovador,
                    pedido.getNomeSolicitante(),
                    pedido.getSolicitante().getIdUsuario()
            );

            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Sucesso");
            alerta.setHeaderText(null);
            alerta.setContentText("Pedido " + pedido.getNumPedido() + " aprovado com sucesso!");
            alerta.showAndWait();
            voltarParaPedidos();

        } else {
            new Alert(Alert.AlertType.ERROR, "Erro ao aprovar pedido.").showAndWait();
        }
    }

    // ── Negar ─────────────────────────────────────────────────

    @FXML
    private void onNegar() {
        erroParecer.setText("");
        String parecer = fieldParecer.getText().trim();

        if (parecer.isBlank()) {
            erroParecer.setText("O parecer é obrigatório para negar o pedido.");
            fieldParecer.requestFocus();
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Negar Pedido");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Confirma a negação do pedido " + pedido.getNumPedido() + "?");

        confirmacao.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                int idAprovador  = SessaoUsuario.getInstancia().getIdUsuarioLogado();
                String nomeAprov = SessaoUsuario.getInstancia().getNomeUsuarioLogado();

                boolean ok = pedidoDAO.negar(pedido.getIdPedido(), idAprovador, parecer);

                if (ok) {
                    HistoricoService.registrar("Pedido", "Negação", pedido.getIdPedido(),
                            "Pedido " + pedido.getNumPedido() + " negado por " + nomeAprov
                                    + ". Motivo: " + parecer);

                    Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                    alerta.setTitle("Pedido Negado");
                    alerta.setHeaderText(null);
                    alerta.setContentText("Pedido " + pedido.getNumPedido() + " negado.");
                    alerta.showAndWait();
                    voltarParaPedidos();

                } else {
                    new Alert(Alert.AlertType.ERROR, "Erro ao negar pedido.").showAndWait();
                }
            }
        });
    }

    // ── Navegação ─────────────────────────────────────────────

    @FXML
    private void onVoltar() { voltarParaPedidos(); }

    private void voltarParaPedidos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/pedido.fxml"));
            Node tela = loader.load();
            pedidoController ctrl = loader.getController();
            ctrl.setAreaPrincipal(areaPrincipal);
            AnchorPane.setTopAnchor   (tela, 0.0);
            AnchorPane.setBottomAnchor(tela, 0.0);
            AnchorPane.setLeftAnchor  (tela, 0.0);
            AnchorPane.setRightAnchor (tela, 0.0);
            areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ── Utilitários ───────────────────────────────────────────

    private String formatarStatus(String s) {
        return switch (s) {
            case "EM_APROVACAO" -> "Em Aprovação";
            case "APROVADO"     -> "Aprovado";
            case "NEGADO"       -> "Negado";
            case "EM_COTACAO"   -> "Em Cotação";
            case "EM_COMPRA"    -> "Em Compra";
            case "FINALIZADO"   -> "Finalizado";
            default             -> s;
        };
    }

    private String estiloBadge(String s) {
        String base = "-fx-background-radius:8; -fx-padding:6 16; -fx-font-size:13px; -fx-font-weight:bold;";
        return base + switch (s) {
            case "EM_APROVACAO" -> "-fx-background-color:#fef9c3; -fx-text-fill:#854d0e;";
            case "APROVADO"     -> "-fx-background-color:#dcfce7; -fx-text-fill:#166534;";
            case "NEGADO"       -> "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b;";
            case "EM_COTACAO"   -> "-fx-background-color:#dbeafe; -fx-text-fill:#1e40af;";
            default             -> "-fx-background-color:#f3f4f6; -fx-text-fill:#6b7280;";
        };
    }
}