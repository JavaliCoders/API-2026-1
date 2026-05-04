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

    @FXML private Label     labelSubtitulo;
    @FXML private Label     badgeStatus;
    @FXML private Label     fieldNumPedido;
    @FXML private Label     fieldData;
    @FXML private Label     fieldSolicitante;
    @FXML private Label     fieldSetor;
    @FXML private Label     fieldCentro;
    @FXML private Label     fieldValor;

    @FXML private TableView<PedidoProduto>            tabelaItens;
    @FXML private TableColumn<PedidoProduto, String>  colProduto;
    @FXML private TableColumn<PedidoProduto, String>  colUnidade;
    @FXML private TableColumn<PedidoProduto, String>  colQtdSolic;
    @FXML private TableColumn<PedidoProduto, String>  colQtdAprov;   // ← nova coluna
    @FXML private TableColumn<PedidoProduto, String>  colVlrUnit;
    @FXML private TableColumn<PedidoProduto, String>  colVlrTotal;
    @FXML private Label labelTotal;

    @FXML private VBox     cardAprovacao;
    @FXML private TextArea fieldParecer;
    @FXML private Label    erroParecer;

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

    public void setAreaPrincipal(AnchorPane a) { this.areaPrincipal = a; }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;

        fieldNumPedido  .setText(pedido.getNumPedido());
        fieldData       .setText(pedido.getDataAberturaFormatada());
        fieldSolicitante.setText(pedido.getNomeSolicitante());
        fieldSetor      .setText(pedido.getNomeSetor());
        fieldCentro     .setText(pedido.getNomeCentroCusto());
        fieldValor      .setText(String.format("R$ %.2f",
                pedido.getValorTotalEstimado()).replace(".", ","));
        labelSubtitulo  .setText("Pedido aberto em " + pedido.getDataAberturaFormatada()
                + "  |  Solicitante: " + pedido.getNomeSolicitante());
        badgeStatus.setText(formatarStatus(pedido.getStatus()));
        badgeStatus.setStyle(estiloBadge(pedido.getStatus()));

        itens = pedidoDAO.listarItens(pedido.getIdPedido());
        tabelaItens.setItems(itens);

        double total = itens.stream().mapToDouble(PedidoProduto::getValorTotal).sum();
        labelTotal.setText(String.format("R$ %.2f", total).replace(".", ","));

        String status = pedido.getStatus();

        if (status.equals("EM_APROVACAO") && isDiretor) {
            cardAprovacao.setVisible(true);  cardAprovacao.setManaged(true);
            cardResultado.setVisible(false); cardResultado.setManaged(false);
        } else if (status.equals("APROVADO") || status.equals("NEGADO")
                || status.equals("APROVADO_PARCIALMENTE")) {
            cardAprovacao.setVisible(false); cardAprovacao.setManaged(false);
            preencherResultado();
            cardResultado.setVisible(true);  cardResultado.setManaged(true);
        } else {
            cardAprovacao.setVisible(false); cardAprovacao.setManaged(false);
            cardResultado.setVisible(false); cardResultado.setManaged(false);
        }
    }

    private void preencherResultado() {
        labelAprovador    .setText(pedido.getAprovador() != null ? pedido.getAprovador().getNome() : "—");
        labelDataAprovacao.setText(pedido.getDataAprovacao() != null
                ? pedido.getDataAprovacao().format(FMT) : "—");
        String parecer = pedido.getParecer();
        labelParecer.setText(parecer != null && !parecer.isBlank() ? parecer : "Sem parecer.");

        if (pedido.getStatus().equals("NEGADO")) {
            labelAprovador.setStyle("-fx-font-size:14px; -fx-text-fill:#991b1b; -fx-font-weight:bold;");
        } else {
            labelAprovador.setStyle("-fx-font-size:14px; -fx-text-fill:#166534; -fx-font-weight:bold;");
        }
    }

    private void configurarColunas() {
        colProduto .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNomeProduto()));
        colUnidade .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnidadeProduto()));
        colQtdSolic.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getQtdSolicitada())));
        colVlrUnit .setCellValueFactory(d -> new SimpleStringProperty(
                String.format("R$ %.2f", d.getValue().getValorUnitario()).replace(".", ",")));
        colVlrTotal.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("R$ %.2f", d.getValue().getValorTotal()).replace(".", ",")));

        // Colunas texto padrão
        for (TableColumn<PedidoProduto, String> col :
                new TableColumn[]{colProduto, colUnidade, colQtdSolic, colVlrUnit, colVlrTotal}) {
            col.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); return; }
                    setText(item); setFont(Font.font("Segoe UI", 13));
                    setStyle("-fx-text-fill:#0f172a;");
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

        // Coluna qtd aprovada — editável via Spinner, destaca diferença em vermelho
        colQtdAprov.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getQtdAprovada())));

        colQtdAprov.setCellFactory(col -> new TableCell<>() {
            private final Spinner<Integer> spinner = new Spinner<>();

            {
                spinner.setEditable(true);
                spinner.setPrefWidth(85);

                spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        PedidoProduto pp = (PedidoProduto) getTableRow().getItem();
                        pp.setQtdAprovada(newVal);
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                PedidoProduto pp = (PedidoProduto) getTableRow().getItem();

                int valorInicial = (pp.getQtdAprovada() > 0)
                        ? pp.getQtdAprovada()
                        : pp.getQtdSolicitada(); // 🔥 AQUI ESTÁ A CORREÇÃO

                SpinnerValueFactory.IntegerSpinnerValueFactory factory =
                        new SpinnerValueFactory.IntegerSpinnerValueFactory(
                                0,
                                pp.getQtdSolicitada(),
                                valorInicial
                        );

                spinner.setValueFactory(factory);

                // 🔥 Garante que o objeto também receba esse valor
                pp.setQtdAprovada(valorInicial);

                // Destaque visual
                if (pp.getQtdAprovada() < pp.getQtdSolicitada()) {
                    spinner.setStyle("-fx-font-size:13px; -fx-border-color:#dc2626; -fx-border-radius:4;");
                } else {
                    spinner.setStyle("-fx-font-size:13px;");
                }

                setGraphic(spinner);
            }
        });

        tabelaItens.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(PedidoProduto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setStyle("-fx-background-color:white;");
                else setStyle(getIndex() % 2 == 0 ? "-fx-background-color:white;"
                        : "-fx-background-color:#fafafa;");
            }
        });
    }

    // ── Aprovar ───────────────────────────────────────────────
    @FXML private void onAprovar() {
        erroParecer.setText("");
        String parecer = fieldParecer.getText().trim();

        // Parecer obrigatório
        if (parecer.isBlank()) {
            erroParecer.setText("O parecer é obrigatório.");
            fieldParecer.requestFocus();
            return;
        }

        int    idAprov = SessaoUsuario.getInstancia().getIdUsuarioLogado();
        String nomeA   = SessaoUsuario.getInstancia().getNomeUsuarioLogado();

        // 🔥 GARANTE QUE NUNCA VAI SALVAR NULL
        for (PedidoProduto i : itens) {
            if (i.getQtdAprovada() == 0) {
                i.setQtdAprovada(i.getQtdSolicitada());
            }
        }

        boolean todosZero      = itens.stream().allMatch(i -> i.getQtdAprovada() == 0);
        boolean algumDiferente = itens.stream()
                .anyMatch(i -> i.getQtdAprovada() != i.getQtdSolicitada());

        // Todos com qtd zero → negar automaticamente
        if (todosZero) {
            Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
            conf.setTitle("Negar Pedido");
            conf.setHeaderText(null);
            conf.setContentText("Todos os itens têm quantidade aprovada = 0.\n"
                    + "O pedido será NEGADO automaticamente. Confirmar?");
            conf.showAndWait().ifPresent(b -> {
                if (b == ButtonType.OK) negarAutomaticamente(idAprov, nomeA, parecer);
            });
            return;
        }

        String novoStatus = algumDiferente ? "APROVADO_PARCIALMENTE" : "APROVADO";

        boolean itensOk = pedidoDAO.atualizarQtdAprovada(pedido.getIdPedido(), itens);
        if (!itensOk) {
            new Alert(Alert.AlertType.ERROR, "Erro ao salvar quantidades aprovadas.").showAndWait();
            return;
        }

        boolean ok = novoStatus.equals("APROVADO")
                ? pedidoDAO.aprovar(pedido.getIdPedido(), idAprov, parecer)
                : pedidoDAO.aprovarParcialmente(pedido.getIdPedido(), idAprov, parecer);

        if (ok) {
            HistoricoService.registrar("Pedido", "Aprovação", pedido.getIdPedido(),
                    "Pedido " + pedido.getNumPedido() + " " + novoStatus
                            + " por " + nomeA + ". Parecer: " + parecer);

            if (novoStatus.equals("APROVADO")) {
                NotificacaoService.notificarPedidoAprovado(
                        pedido.getIdPedido(), pedido.getNumPedido(),
                        pedido.getValorTotalEstimado(), nomeA,
                        pedido.getNomeSolicitante(),
                        pedido.getSolicitante().getIdUsuario());
            } else {
                NotificacaoService.notificarPedidoAprovadoParcialmente(
                        pedido.getIdPedido(), pedido.getNumPedido(),
                        pedido.getValorTotalEstimado(), nomeA,
                        pedido.getNomeSolicitante(),
                        pedido.getSolicitante().getIdUsuario());
            }

            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Sucesso"); a.setHeaderText(null);
            a.setContentText("Pedido " + pedido.getNumPedido() + " "
                    + (novoStatus.equals("APROVADO") ? "aprovado" : "aprovado parcialmente") + "!");
            a.showAndWait();
            voltarParaPedidos();
        }
    }

    private void negarAutomaticamente(int idAprov, String nomeA, String parecer) {
            boolean ok = pedidoDAO.negar(pedido.getIdPedido(), idAprov, parecer);
            if (ok) {
                HistoricoService.registrar("Pedido", "Negação", pedido.getIdPedido(),
                        "Pedido " + pedido.getNumPedido() + " negado automaticamente (qtd=0) por "
                                + nomeA + ". Motivo: " + parecer);
                NotificacaoService.notificarPedidoNegado(
                        pedido.getIdPedido(), pedido.getNumPedido(),
                        nomeA, pedido.getSolicitante().getIdUsuario());
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Negado"); a.setHeaderText(null);
                a.setContentText("Pedido " + pedido.getNumPedido() + " negado.");
                a.showAndWait();
                voltarParaPedidos();
            } else new Alert(Alert.AlertType.ERROR, "Erro ao negar pedido.").showAndWait();
    }


    // ── Negar ─────────────────────────────────────────────────
    @FXML private void onNegar() {
        erroParecer.setText("");
        String parecer = fieldParecer.getText().trim();

        if (parecer.isBlank()) {
            erroParecer.setText("O parecer é obrigatório para negar.");
            fieldParecer.requestFocus();
            return;
        }

        Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
        conf.setTitle("Negar Pedido"); conf.setHeaderText(null);
        conf.setContentText("Confirma a negação do pedido " + pedido.getNumPedido() + "?");
        conf.showAndWait().ifPresent(b -> {
            if (b == ButtonType.OK) {
                int idAprov = SessaoUsuario.getInstancia().getIdUsuarioLogado();
                boolean ok  = pedidoDAO.negar(pedido.getIdPedido(), idAprov, parecer);
                if (ok) {
                    HistoricoService.registrar("Pedido", "Negação", pedido.getIdPedido(),
                            "Pedido " + pedido.getNumPedido() + " negado por "
                                    + SessaoUsuario.getInstancia().getNomeUsuarioLogado()
                                    + ". Motivo: " + parecer);

                    NotificacaoService.notificarPedidoNegado(
                            pedido.getIdPedido(), pedido.getNumPedido(),
                            SessaoUsuario.getInstancia().getNomeUsuarioLogado(),
                            pedido.getSolicitante().getIdUsuario());

                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle("Negado"); a.setHeaderText(null);
                    a.setContentText("Pedido " + pedido.getNumPedido() + " negado.");
                    a.showAndWait(); voltarParaPedidos();
                } else new Alert(Alert.AlertType.ERROR, "Erro ao negar.").showAndWait();
            }
        });
    }

    @FXML private void onVoltar() { voltarParaPedidos(); }

    private void voltarParaPedidos() {
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource("/view/pedido.fxml"));
            Node tela = l.load();
            pedidoController c = l.getController();
            c.setAreaPrincipal(areaPrincipal);
            AnchorPane.setTopAnchor(tela,0.0); AnchorPane.setBottomAnchor(tela,0.0);
            AnchorPane.setLeftAnchor(tela,0.0); AnchorPane.setRightAnchor(tela,0.0);
            areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private String formatarStatus(String s) {
        return switch (s) {
            case "EM_APROVACAO"          -> "Em Aprovação";
            case "APROVADO"              -> "Aprovado";
            case "APROVADO_PARCIALMENTE" -> "Aprovado Parcial";
            case "NEGADO"                -> "Negado";
            default                      -> s;
        };
    }

    private String estiloBadge(String s) {
        String b = "-fx-background-radius:8; -fx-padding:6 16; -fx-font-size:13px; -fx-font-weight:bold;";
        return b + switch (s) {
            case "EM_APROVACAO"          -> "-fx-background-color:#fef9c3; -fx-text-fill:#854d0e;";
            case "APROVADO"              -> "-fx-background-color:#dcfce7; -fx-text-fill:#166534;";
            case "APROVADO_PARCIALMENTE" -> "-fx-background-color:#d1fae5; -fx-text-fill:#065f46;";
            case "NEGADO"                -> "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b;";
            default                      -> "-fx-background-color:#f3f4f6; -fx-text-fill:#6b7280;";
        };
    }
}