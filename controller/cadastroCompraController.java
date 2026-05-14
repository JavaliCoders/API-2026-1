package api.controller;

import api.DAO.compraDAO;
import api.DAO.fornecedorDAO;
import api.DAO.pedidoDAO;
import api.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class cadastroCompraController implements Initializable {

    @FXML private ComboBox<Pedido> fieldPedido;
    @FXML private ComboBox<Fornecedor> fieldFornecedor;
    @FXML private TextField fieldComprador;
    @FXML private TextField fieldDataCompra;
    @FXML private DatePicker fieldDataPrevista;

    @FXML private Label labelSolicitante;
    @FXML private Label labelSetor;
    @FXML private Label labelCentro;
    @FXML private Label labelValorEstimado;
    @FXML private Label labelStatusPedido;

    @FXML private TableView<CompraItem> tabelaItens;
    @FXML private TableColumn<CompraItem, String> colProduto;
    @FXML private TableColumn<CompraItem, String> colUnidade;
    @FXML private TableColumn<CompraItem, String> colQtdSolicitada;
    @FXML private TableColumn<CompraItem, String> colQtdCompra;
    @FXML private TableColumn<CompraItem, String> colValorUnit;
    @FXML private TableColumn<CompraItem, String> colValorTotal;

    @FXML private Label labelTotal;
    @FXML private Label erroPedido;
    @FXML private Label erroFornecedor;
    @FXML private Label erroItens;

    private AnchorPane areaPrincipal;
    private Pedido pedidoSelecionado;
    private LocalDateTime dataCompra;
    private ObservableList<Pedido> pedidosDisponiveis;
    private final ObservableList<CompraItem> itensCompra =
            FXCollections.observableArrayList();

    private static final DateTimeFormatter DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        carregarCombos();
        configurarCombos();
        configurarColunas();
        preencherDadosAutomaticos();
    }

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }

    public void setPedidoSelecionado(Pedido pedido) {
        if (pedido == null) return;

        Pedido pedidoCombo = pedidosDisponiveis.stream()
                .filter(p -> p.getIdPedido() == pedido.getIdPedido())
                .findFirst()
                .orElse(pedido);

        fieldPedido.setValue(pedidoCombo);
    }

    private void carregarCombos() {
        pedidosDisponiveis = pedidoDAO.listarTodos()
                .filtered(p -> "APROVADO".equals(p.getStatus()));
        fieldPedido.setItems(pedidosDisponiveis);

        ObservableList<Fornecedor> fornecedoresAtivos = fornecedorDAO.listarTodos()
                .filtered(f -> "ATIVO".equals(f.getStatus()));
        fieldFornecedor.setItems(fornecedoresAtivos);
    }

    private void configurarCombos() {
        fieldPedido.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Pedido item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : textoPedido(item));
            }
        });
        fieldPedido.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Pedido item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : textoPedido(item));
            }
        });
        fieldPedido.valueProperty().addListener((obs, antigo, novo) -> selecionarPedido(novo));

        fieldFornecedor.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Fornecedor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : textoFornecedor(item));
            }
        });
        fieldFornecedor.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Fornecedor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : textoFornecedor(item));
            }
        });
    }

    private void preencherDadosAutomaticos() {
        dataCompra = LocalDateTime.now();
        fieldDataCompra.setText(dataCompra.format(DATA_HORA));

        Usuario usuarioLogado = SessaoUsuario.getInstancia().getUsuarioLogado();
        fieldComprador.setText(usuarioLogado != null ? usuarioLogado.getNome() : "");
    }

    private void configurarColunas() {
        colProduto.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNomeProduto()));
        colUnidade.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getUnidade()));
        colQtdSolicitada.setCellValueFactory(data ->
                new SimpleStringProperty(formatarQuantidade(
                        data.getValue().getPedidoProduto().getQtdSolicitada())));
        colQtdCompra.setCellValueFactory(data ->
                new SimpleStringProperty(formatarQuantidade(data.getValue().getQtdComprada())));
        colValorUnit.setCellValueFactory(data ->
                new SimpleStringProperty(formatarMoeda(data.getValue().getValorUni())));
        colValorTotal.setCellValueFactory(data ->
                new SimpleStringProperty(formatarMoeda(data.getValue().getValorTotal())));

        colQtdCompra.setCellFactory(TextFieldTableCell.forTableColumn());
        colQtdCompra.setOnEditCommit(event -> {
            double valor = parseDecimal(event.getNewValue());
            if (valor <= 0) {
                erroItens.setText("A quantidade comprada deve ser maior que zero.");
                tabelaItens.refresh();
                return;
            }
            event.getRowValue().setQtdComprada(valor);
            erroItens.setText("");
            tabelaItens.refresh();
            atualizarTotal();
        });

        colValorUnit.setCellFactory(TextFieldTableCell.forTableColumn());
        colValorUnit.setOnEditCommit(event -> {
            double valor = parseDecimal(event.getNewValue());
            if (valor <= 0) {
                erroItens.setText("O valor unitario deve ser maior que zero.");
                tabelaItens.refresh();
                return;
            }
            event.getRowValue().setValorUni(valor);
            erroItens.setText("");
            tabelaItens.refresh();
            atualizarTotal();
        });

        tabelaItens.setEditable(true);
        tabelaItens.setItems(itensCompra);
        tabelaItens.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(CompraItem item, boolean empty) {
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

    private void selecionarPedido(Pedido pedido) {
        pedidoSelecionado = pedido;
        itensCompra.clear();

        if (pedido == null) {
            limparResumoPedido();
            atualizarTotal();
            return;
        }

        labelSolicitante.setText(pedido.getNomeSolicitante());
        labelSetor.setText(pedido.getNomeSetor());
        labelCentro.setText(pedido.getNomeCentroCusto());
        labelValorEstimado.setText(formatarMoeda(pedido.getValorTotalEstimado()));
        labelStatusPedido.setText(pedido.getStatus().replace("_", " "));
        labelStatusPedido.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        labelStatusPedido.setAlignment(Pos.CENTER);
        labelStatusPedido.setStyle(
                "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; " +
                "-fx-background-radius: 6; -fx-padding: 4 10;");

        ObservableList<PedidoProduto> itensPedido = pedidoDAO.listarItens(pedido.getIdPedido());
        for (PedidoProduto item : itensPedido) {
            double qtdCompra = item.getQtdAprovada() > 0
                    ? item.getQtdAprovada()
                    : item.getQtdSolicitada();
            itensCompra.add(new CompraItem(item, item.getValorUnitario(), qtdCompra));
        }

        erroPedido.setText("");
        erroItens.setText("");
        atualizarTotal();
    }

    @FXML
    private void onSalvar() {
        limparErros();

        if (!validarFormulario()) return;

        Fornecedor fornecedor = fieldFornecedor.getValue();
        double total = calcularTotal();

        if (fornecedor.getPedidoMinimo() > 0 && total < fornecedor.getPedidoMinimo()) {
            erroFornecedor.setText("Total abaixo do pedido minimo do fornecedor.");
            return;
        }

        Usuario comprador = SessaoUsuario.getInstancia().getUsuarioLogado();
        LocalDate dataPrevistaSelecionada = fieldDataPrevista.getValue();
        LocalDateTime dataPrevista = dataPrevistaSelecionada == null
                ? null
                : dataPrevistaSelecionada.atStartOfDay();

        Compra compra = new Compra(
                pedidoSelecionado,
                fornecedor,
                dataCompra,
                comprador,
                total,
                dataPrevista
        );

        int idCompra = compraDAO.registrar(compra, itensCompra);
        if (idCompra == -1) {
            new Alert(Alert.AlertType.ERROR,
                    "Erro ao registrar compra. Verifique a conexao com o banco.")
                    .showAndWait();
            return;
        }

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Sucesso");
        alerta.setHeaderText(null);
        alerta.setContentText("Compra registrada com sucesso para o pedido "
                + pedidoSelecionado.getNumPedido() + ".");
        alerta.showAndWait();
        voltarParaCompras();
    }

    @FXML
    private void onCancelar() {
        voltarParaCompras();
    }

    private boolean validarFormulario() {
        boolean valido = true;

        if (pedidoSelecionado == null) {
            erroPedido.setText("Selecione um pedido aprovado.");
            valido = false;
        }

        if (fieldFornecedor.getValue() == null) {
            erroFornecedor.setText("Selecione um fornecedor.");
            valido = false;
        }

        Usuario comprador = SessaoUsuario.getInstancia().getUsuarioLogado();
        if (comprador == null) {
            erroFornecedor.setText("Usuario logado nao encontrado.");
            valido = false;
        }

        if (itensCompra.isEmpty()) {
            erroItens.setText("O pedido selecionado nao possui itens.");
            valido = false;
        }

        for (CompraItem item : itensCompra) {
            if (item.getQtdComprada() <= 0 || item.getValorUni() <= 0) {
                erroItens.setText("Revise quantidade e valor unitario dos itens.");
                valido = false;
                break;
            }
        }

        return valido;
    }

    private void voltarParaCompras() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/compra.fxml"));
            Node tela = loader.load();

            compraController controller = loader.getController();
            controller.setAreaPrincipal(areaPrincipal);

            AnchorPane.setTopAnchor(tela, 0.0);
            AnchorPane.setBottomAnchor(tela, 0.0);
            AnchorPane.setLeftAnchor(tela, 0.0);
            AnchorPane.setRightAnchor(tela, 0.0);

            areaPrincipal.getChildren().setAll(tela);
        } catch (IOException e) {
            System.err.println("Erro ao voltar para compras: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void atualizarTotal() {
        labelTotal.setText(formatarMoeda(calcularTotal()));
    }

    private double calcularTotal() {
        return itensCompra.stream()
                .mapToDouble(CompraItem::getValorTotal)
                .sum();
    }

    private void limparResumoPedido() {
        labelSolicitante.setText("-");
        labelSetor.setText("-");
        labelCentro.setText("-");
        labelValorEstimado.setText("R$ 0,00");
        labelStatusPedido.setText("-");
        labelStatusPedido.setStyle("-fx-text-fill: #64748b;");
    }

    private void limparErros() {
        erroPedido.setText("");
        erroFornecedor.setText("");
        erroItens.setText("");
    }

    private String textoPedido(Pedido pedido) {
        return pedido.getNumPedido() + " - " + pedido.getNomeSolicitante();
    }

    private String textoFornecedor(Fornecedor fornecedor) {
        return String.format("COD-%03d - %s", fornecedor.getIdFornecedor(), fornecedor.getNome());
    }

    private String formatarMoeda(double valor) {
        return String.format("R$ %.2f", valor).replace(".", ",");
    }

    private String formatarQuantidade(double valor) {
        if (valor == Math.rint(valor)) {
            return String.format("%.0f", valor);
        }
        return String.format("%.2f", valor).replace(".", ",");
    }

    private double parseDecimal(String texto) {
        if (texto == null || texto.isBlank()) return -1;

        String limpo = texto.replace("R$", "")
                .replaceAll("[^0-9,.-]", "")
                .trim();

        if (limpo.contains(",")) {
            limpo = limpo.replace(".", "").replace(",", ".");
        }

        try {
            return Double.parseDouble(limpo);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
