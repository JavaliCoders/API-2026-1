package api.controller;

import api.DAO.centroCustoDAO;
import api.DAO.pedidoDAO;
import api.DAO.produtoDAO;
import api.DAO.SetorDAO;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class editarPedidoController implements Initializable {

    @FXML private TextField              fieldNumPedido;
    @FXML private TextField              fieldData;
    @FXML private TextField              fieldSolicitante;
    @FXML private ComboBox<Setor>        fieldSetor;
    @FXML private ComboBox<CentroCusto>  fieldCentroCusto;

    @FXML private TextField              searchProduto;
    @FXML private ListView<Produto>      listaProdutos;
    @FXML private HBox                   boxProdutoSelecionado;
    @FXML private Label                  labelProdutoSelecionado;
    @FXML private TextField              fieldQtd;
    @FXML private TextField              fieldValorUnit;

    @FXML private TableView<PedidoProduto>            tabelaItens;
    @FXML private TableColumn<PedidoProduto, String>  colProduto;
    @FXML private TableColumn<PedidoProduto, String>  colUnidade;
    @FXML private TableColumn<PedidoProduto, Integer> colQtd;
    @FXML private TableColumn<PedidoProduto, String>  colVlrUnit;
    @FXML private TableColumn<PedidoProduto, String>  colVlrTotal;
    @FXML private TableColumn<PedidoProduto, Void>    colRemover;

    @FXML private Label labelTotal;
    @FXML private Label erroSetor;
    @FXML private Label erroCentro;
    @FXML private Label erroProduto;

    private AnchorPane areaPrincipal;
    private Pedido pedidoEdicao;
    private Produto produtoSelecionado = null;
    private ObservableList<Produto> todosProdutos;
    private final ObservableList<PedidoProduto> itensPedido =
            FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fieldSetor.setItems(SetorDAO.listarTodos());
        fieldCentroCusto.setItems(centroCustoDAO.listarTodos());

        todosProdutos = produtoDAO.listarTodos()
                .filtered(p -> p.getStatus().equals("ATIVO"));

        configurarColunas();
        configurarListaProdutos();

        fieldQtd.textProperty().addListener((obs, a, n) -> {
            if (!n.matches("\\d*")) fieldQtd.setText(a);
        });
    }

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }

    public void setPedidoEdicao(Pedido pedido) {
        this.pedidoEdicao = pedido;

        // Preenche campos fixos
        fieldNumPedido.setText(pedido.getNumPedido());
        fieldData.setText(pedido.getDataAbertura()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        fieldSolicitante.setText(pedido.getNomeSolicitante());

        // Seleciona setor e centro de custo correspondentes
        fieldSetor.getItems().stream()
                .filter(s -> s.getIdSetor() == pedido.getSetor().getIdSetor())
                .findFirst()
                .ifPresent(fieldSetor::setValue);

        fieldCentroCusto.getItems().stream()
                .filter(c -> c.getIdCentroCusto() ==
                        pedido.getCentroCusto().getIdCentroCusto())
                .findFirst()
                .ifPresent(fieldCentroCusto::setValue);

        // Carrega os itens existentes do pedido
        ObservableList<PedidoProduto> itensExistentes =
                pedidoDAO.listarItens(pedido.getIdPedido());
        itensPedido.setAll(itensExistentes);
        atualizarTotal();
    }

    private void configurarListaProdutos() {
        listaProdutos.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Produto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("COD-%03d  —  %s  (%s)  —  R$ %.2f",
                            item.getIdProduto(),
                            item.getProduto(),
                            item.getUnidadeMedida(),
                            item.getValorEstimado()).replace(".", ","));
                }
            }
        });

        listaProdutos.setOnMouseClicked(e -> {
            Produto selecionado = listaProdutos.getSelectionModel().getSelectedItem();
            if (selecionado != null) selecionarProduto(selecionado);
        });
    }

    @FXML
    private void onBuscarProduto() {
        String texto = searchProduto.getText().trim().toLowerCase();
        if (texto.isEmpty()) {
            listaProdutos.setVisible(false);
            listaProdutos.setManaged(false);
            return;
        }

        ObservableList<Produto> resultado = todosProdutos.filtered(p ->
                p.getProduto().toLowerCase().contains(texto) ||
                        String.valueOf(p.getIdProduto()).contains(texto));

        if (resultado.isEmpty()) {
            listaProdutos.setVisible(false);
            listaProdutos.setManaged(false);
        } else {
            listaProdutos.setItems(resultado);
            listaProdutos.setVisible(true);
            listaProdutos.setManaged(true);
        }
    }

    private void selecionarProduto(Produto produto) {
        produtoSelecionado = produto;
        listaProdutos.setVisible(false);
        listaProdutos.setManaged(false);
        labelProdutoSelecionado.setText(
                String.format("COD-%03d  —  %s  (%s)  —  R$ %.2f",
                        produto.getIdProduto(),
                        produto.getProduto(),
                        produto.getUnidadeMedida(),
                        produto.getValorEstimado()).replace(".", ","));
        boxProdutoSelecionado.setVisible(true);
        boxProdutoSelecionado.setManaged(true);
        fieldValorUnit.setText(
                String.format("R$ %.2f", produto.getValorEstimado())
                        .replace(".", ","));
        fieldQtd.requestFocus();
        erroProduto.setText("");
    }

    @FXML
    private void onLimparProduto() {
        produtoSelecionado = null;
        searchProduto.clear();
        fieldValorUnit.clear();
        boxProdutoSelecionado.setVisible(false);
        boxProdutoSelecionado.setManaged(false);
        listaProdutos.setVisible(false);
        listaProdutos.setManaged(false);
    }

    @FXML
    private void onAdicionarProduto() {
        erroProduto.setText("");

        if (produtoSelecionado == null) {
            erroProduto.setText("Selecione um produto na busca.");
            return;
        }

        String qtdTexto = fieldQtd.getText().trim();
        if (qtdTexto.isBlank()) {
            erroProduto.setText("Informe a quantidade.");
            return;
        }

        int qtd = Integer.parseInt(qtdTexto);
        if (qtd <= 0) {
            erroProduto.setText("A quantidade deve ser maior que zero.");
            return;
        }

        boolean jaExiste = false;
        for (PedidoProduto item : itensPedido) {
            if (item.getProduto().getIdProduto() == produtoSelecionado.getIdProduto()) {
                item.setQtdSolicitada(item.getQtdSolicitada() + qtd);
                tabelaItens.refresh();
                jaExiste = true;
                break;
            }
        }

        if (!jaExiste) {
            itensPedido.add(new PedidoProduto(produtoSelecionado, qtd));
        }

        atualizarTotal();
        onLimparProduto();
        fieldQtd.clear();
    }

    private void atualizarTotal() {
        double total = itensPedido.stream()
                .mapToDouble(PedidoProduto::getValorTotal)
                .sum();
        labelTotal.setText(String.format("R$ %.2f", total).replace(".", ","));
    }

    private void configurarColunas() {
        colProduto.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNomeProduto()));
        colUnidade.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getUnidadeProduto()));
        colQtd.setCellValueFactory(new PropertyValueFactory<>("qtdSolicitada"));
        colVlrUnit.setCellValueFactory(data ->
                new SimpleStringProperty(
                        String.format("R$ %.2f", data.getValue().getValorUnitario())
                                .replace(".", ",")));
        colVlrTotal.setCellValueFactory(data ->
                new SimpleStringProperty(
                        String.format("R$ %.2f", data.getValue().getValorTotal())
                                .replace(".", ",")));

        colRemover.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("🗑");

            {
                btn.setStyle(
                        "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; " +
                                "-fx-background-radius: 6; -fx-font-size: 13px; " +
                                "-fx-cursor: hand; -fx-border-color: transparent; -fx-padding: 4 8;");
                btn.setOnMouseEntered(e -> btn.setStyle(
                        "-fx-background-color: #dc2626; -fx-text-fill: white; " +
                                "-fx-background-radius: 6; -fx-font-size: 13px; " +
                                "-fx-cursor: hand; -fx-border-color: transparent; -fx-padding: 4 8;"));
                btn.setOnMouseExited(e -> btn.setStyle(
                        "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; " +
                                "-fx-background-radius: 6; -fx-font-size: 13px; " +
                                "-fx-cursor: hand; -fx-border-color: transparent; -fx-padding: 4 8;"));
                btn.setOnAction(e -> {
                    PedidoProduto item = getTableView().getItems().get(getIndex());
                    itensPedido.remove(item);
                    atualizarTotal();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                HBox box = new HBox(btn);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });

        tabelaItens.setItems(itensPedido);
    }

    @FXML
    private void onSalvar() {
        limparErros();
        boolean valido = true;

        if (fieldSetor.getValue() == null) {
            erroSetor.setText("Selecione o setor.");
            valido = false;
        }
        if (fieldCentroCusto.getValue() == null) {
            erroCentro.setText("Selecione o centro de custo.");
            valido = false;
        }
        if (itensPedido.isEmpty()) {
            erroProduto.setText("O pedido deve ter pelo menos um produto.");
            valido = false;
        }

        if (!valido) return;

        double total = itensPedido.stream()
                .mapToDouble(PedidoProduto::getValorTotal)
                .sum();

        Pedido pedidoAtualizado = new Pedido(
                pedidoEdicao.getIdPedido(),
                pedidoEdicao.getNumPedido(),
                pedidoEdicao.getDataAbertura(),
                pedidoEdicao.getStatus(),
                total,
                pedidoEdicao.getSolicitante(),
                fieldCentroCusto.getValue(),
                fieldSetor.getValue()
        );

        // Atualiza o pedido
        boolean pedidoOk = pedidoDAO.atualizar(pedidoAtualizado);

        if (!pedidoOk) {
            new Alert(Alert.AlertType.ERROR,
                    "Erro ao atualizar pedido.").showAndWait();
            return;
        }

        // Remove os itens antigos e reinsere os novos
        pedidoDAO.removerItens(pedidoEdicao.getIdPedido());
        boolean itensOk = pedidoDAO.inserirItens(
                pedidoEdicao.getIdPedido(), itensPedido);

        if (itensOk) {
            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Sucesso");
            alerta.setHeaderText(null);
            alerta.setContentText("Pedido " + pedidoEdicao.getNumPedido() +
                    " atualizado com sucesso!");
            alerta.showAndWait();
            voltarParaPedidos();
        } else {
            new Alert(Alert.AlertType.ERROR,
                    "Erro ao atualizar os itens do pedido.").showAndWait();
        }
    }

    @FXML
    private void onCancelar() {
        voltarParaPedidos();
    }

    private void voltarParaPedidos() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/pedido.fxml"));
            Node tela = loader.load();

            pedidoController controller = loader.getController();
            controller.setAreaPrincipal(areaPrincipal);

            AnchorPane.setTopAnchor(tela, 0.0);
            AnchorPane.setBottomAnchor(tela, 0.0);
            AnchorPane.setLeftAnchor(tela, 0.0);
            AnchorPane.setRightAnchor(tela, 0.0);

            areaPrincipal.getChildren().setAll(tela);

        } catch (IOException e) {
            System.err.println("Erro ao voltar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void limparErros() {
        erroSetor.setText("");
        erroCentro.setText("");
        erroProduto.setText("");
    }
}