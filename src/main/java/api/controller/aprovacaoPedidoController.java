package api.controller;

import api.DAO.pedidoDAO;
import api.model.*;
import api.service.HistoricoService;
import api.service.NotificacaoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class aprovacaoPedidoController implements Initializable {

    @FXML private Label     labelSubtitulo;
    @FXML private TextField fieldNumPedido;
    @FXML private TextField fieldSolicitante;
    @FXML private TextField fieldSetor;
    @FXML private TextField fieldCentro;

    @FXML private TableView<PedidoProduto>            tabelaItens;
    @FXML private TableColumn<PedidoProduto, String>  colProduto;
    @FXML private TableColumn<PedidoProduto, String>  colUnidade;
    @FXML private TableColumn<PedidoProduto, String>  colQtdSolic;
    @FXML private TableColumn<PedidoProduto, String>  colVlrUnit;
    @FXML private TableColumn<PedidoProduto, String>  colVlrTotal;
    @FXML private TableColumn<PedidoProduto, Integer> colQtdAprov;

    @FXML private Label    labelTotalAprovado;
    @FXML private TextArea fieldParecer;
    @FXML private Label    erroParecer;

    private AnchorPane areaPrincipal;
    private Pedido     pedido;
    private ObservableList<PedidoProduto> itens;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Tabela editável — coluna qtdAprovada é editável inline
        tabelaItens.setEditable(true);
        configurarColunas();
    }

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;

        fieldNumPedido  .setText(pedido.getNumPedido());
        fieldSolicitante.setText(pedido.getNomeSolicitante());
        fieldSetor      .setText(pedido.getNomeSetor());
        fieldCentro     .setText(pedido.getNomeCentroCusto());
        labelSubtitulo  .setText("Pedido aberto em " + pedido.getDataAberturaFormatada()
                + "  |  Solicitante: " + pedido.getNomeSolicitante());

        itens = pedidoDAO.listarItens(pedido.getIdPedido());

        // Inicializa qtdAprovada com o valor solicitado (padrão: aprovar tudo)
        for (PedidoProduto item : itens) {
            if (item.getQtdAprovada() == 0) {
                item.setQtdAprovada(item.getQtdSolicitada());
            }
        }

        tabelaItens.setItems(itens);
        atualizarTotal();
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

        // Coluna Qtd Aprovada — editável inline com campo numérico
        colQtdAprov.setCellValueFactory(d ->
                d.getValue().qtdAprovadaProperty().asObject());
        colQtdAprov.setCellFactory(col -> {
            TextFieldTableCell<PedidoProduto, Integer> cell =
                    new TextFieldTableCell<>(new IntegerStringConverter() {
                        @Override
                        public Integer fromString(String s) {
                            try {
                                int v = Integer.parseInt(s.trim());
                                return Math.max(0, v); // não permite negativo
                            } catch (NumberFormatException e) {
                                return 0;
                            }
                        }
                    });
            cell.setAlignment(Pos.CENTER);
            // Destaque visual: amarelo quando qtd < solicitada
            cell.itemProperty().addListener((obs, a, n) -> {
                if (n == null) return;
                PedidoProduto item = cell.getTableRow() != null
                        ? (PedidoProduto) cell.getTableRow().getItem() : null;
                if (item != null && n < item.getQtdSolicitada()) {
                    cell.setStyle("-fx-background-color: #fef9c3;");
                } else {
                    cell.setStyle("");
                }
            });
            return cell;
        });
        colQtdAprov.setEditable(true);
        colQtdAprov.setOnEditCommit(event -> {
            PedidoProduto item = event.getRowValue();
            int novaQtd = Math.min(event.getNewValue(), item.getQtdSolicitada());
            item.setQtdAprovada(novaQtd);
            tabelaItens.refresh();
            atualizarTotal();
        });
    }

    private void atualizarTotal() {
        if (itens == null) return;
        double total = itens.stream()
                .mapToDouble(i -> i.getQtdAprovada() * i.getValorUnitario())
                .sum();
        labelTotalAprovado.setText(String.format("R$ %.2f", total).replace(".", ","));
    }

    // ── Aprovar ───────────────────────────────────────────────
    @FXML
    private void onAprovar() {
        erroParecer.setText("");
        String parecer = fieldParecer.getText().trim();

        // Atualiza qtd_aprovada de cada item no banco
        boolean itensOk = pedidoDAO.atualizarQtdAprovada(pedido.getIdPedido(), itens);
        if (!itensOk) {
            new Alert(Alert.AlertType.ERROR, "Erro ao salvar quantidades aprovadas.").showAndWait();
            return;
        }

        int idAprov = SessaoUsuario.getInstancia().getIdUsuarioLogado();
        boolean ok = pedidoDAO.aprovar(pedido.getIdPedido(), idAprov, parecer);
        if (ok) {
            HistoricoService.registrar("Pedido", "Aprovação", pedido.getIdPedido(),
                    "Pedido " + pedido.getNumPedido() + " aprovado por "
                            + SessaoUsuario.getInstancia().getNomeUsuarioLogado()
                            + (parecer.isBlank() ? "" : ". Parecer: " + parecer));

            NotificacaoService.notificarPedidoAprovado (
                    pedido.getIdPedido(),
                    pedido.getNumPedido(),
                    Double.parseDouble(labelTotalAprovado.getText().replace("R$", "").replace(",", ".")),
                    SessaoUsuario.getInstancia().getNomeUsuarioLogado(),
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
                int idAprov = SessaoUsuario.getInstancia().getIdUsuarioLogado();
                boolean ok = pedidoDAO.negar(pedido.getIdPedido(), idAprov, parecer);
                if (ok) {
                    HistoricoService.registrar("Pedido", "Negação", pedido.getIdPedido(),
                            "Pedido " + pedido.getNumPedido() + " negado por "
                                    + SessaoUsuario.getInstancia().getNomeUsuarioLogado()
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

    @FXML
    private void onCancelar() { voltarParaPedidos(); }

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
}