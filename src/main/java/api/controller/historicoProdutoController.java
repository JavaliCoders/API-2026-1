package api.controller;

import api.model.Pedido;
import api.model.Cotacao;
import api.model.Compra;

import api.DAO.pedidoDAO;
import api.DAO.cotacaoDAO;
import api.DAO.compraDAO;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import java.net.URL;
import java.util.ResourceBundle;

public class historicoProdutoController implements Initializable {

    @FXML private TextField campoBuscaProduto;
    @FXML private Label labelErro;
    @FXML private TabPane tabPaneDetalhes;

    @FXML private TableView<Pedido> tabelaPedidos;
    @FXML private TableColumn<Pedido, String> colPedidoId, colPedidoData, colPedidoStatus, colPedidoQtd;

    @FXML private TableView<Cotacao> tabelaCotacoes;
    @FXML private TableColumn<Cotacao, String> colCotacaoFornecedor, colCotacaoValor, colCotacaoStatus;

    @FXML private ListView<String> listaProdutosCotacao;

    @FXML private TableView<Compra> tabelaCompras;
    @FXML private TableColumn<Compra, String> colCompraId, colCompraNota, colCompraData, colCompraValor;

    private AnchorPane areaPrincipal;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarColunas();
        configurarEventosDeSelecao();
        tabPaneDetalhes.setVisible(false);
        labelErro.setVisible(false);
    }

    private void configurarColunas() {
        colPedidoId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getIdPedido())));
        colPedidoData.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataAberturaFormatada()));
        colPedidoStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        colPedidoQtd.setCellValueFactory(c -> new SimpleStringProperty("Ver Itens")); 
        
        colCotacaoFornecedor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomeFornecedor()));
        colCotacaoValor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getValorFormatado()));
        colCotacaoStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatusFormatado()));

        colCompraId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getIdCompra())));
        colCompraNota.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNotaFiscal()));
        colCompraData.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataFormatada()));
        colCompraValor.setCellValueFactory(c -> new SimpleStringProperty(String.format("R$ %.2f", c.getValue().getValorTotal()).replace(".", ",")));
    }

    private void configurarEventosDeSelecao() {
        tabelaPedidos.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novoPedido) -> {
            if (novoPedido != null) {
                tabPaneDetalhes.setVisible(true);
                carregarDetalhes(novoPedido.getIdPedido());
            }
        });
        
        tabelaCotacoes.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novaCotacao) -> {
            if (novaCotacao != null) {
                carregarProdutosCotacao(novaCotacao.getIdCotacao());
            }
        });
    }

    @FXML
    void onBuscarProduto(ActionEvent event) {
        labelErro.setVisible(false);
        String termo = campoBuscaProduto.getText();

        if (termo == null || termo.trim().isEmpty()) {
            mostrarErro("Atenção: Digite o nome de um produto.");
            return;
        }

        try {
            tabelaPedidos.setItems(pedidoDAO.buscarPorProduto(termo));
            tabPaneDetalhes.setVisible(false);
        } catch (Exception e) {
            mostrarErro("Erro ao buscar no banco de dados.");
        }
    }

    private void carregarDetalhes(int idPedido) {
        try {
        	tabelaCotacoes.setItems(cotacaoDAO.listarPorPedido(idPedido));
        	tabelaCompras.setItems(compraDAO.listarPorPedido(idPedido));
        } catch (Exception e) {
            mostrarErro("Erro ao carregar os detalhes do pedido.");
        }
    }
    
    private void carregarProdutosCotacao(int idCotacao) {
        try {
            listaProdutosCotacao.setItems(cotacaoDAO.buscarProdutosPorCotacao(idCotacao));
        } catch (Exception e) {
            mostrarErro("Erro ao carregar os produtos desta cotação.");
        }
    }

    private void mostrarErro(String msg) {
        labelErro.setText(msg);
        labelErro.setVisible(true);
    }

    @FXML
    void onLimpar(ActionEvent event) {
        campoBuscaProduto.clear();
        tabelaPedidos.getItems().clear();
        tabelaCotacoes.getItems().clear();
        tabelaCompras.getItems().clear();
        listaProdutosCotacao.getItems().clear();
        tabPaneDetalhes.setVisible(false);
        labelErro.setVisible(false);
    }

    public void setAreaPrincipal(AnchorPane areaPrincipal) { this.areaPrincipal = areaPrincipal; }
}
