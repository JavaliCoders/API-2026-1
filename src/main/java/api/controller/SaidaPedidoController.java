package api.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import api.DAO.movimentacaoDAO;
import api.DAO.pedidoDAO;
import api.model.Pedido;
import api.model.PedidoProduto;
import api.model.SessaoUsuario;
import api.model.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Button;

public class SaidaPedidoController implements Initializable {

    @FXML private TableView<PedidoProduto> pedidosTable;
    @FXML private TableColumn<PedidoProduto, Integer> colID;
    @FXML private TableColumn<PedidoProduto, String> colProduto;
    @FXML private TableColumn<PedidoProduto, Integer> colQuantidade;
    @FXML private TableColumn<PedidoProduto, String> colStatus;
    
    @FXML private TextField txtQuantidadeSaida;
	@FXML TextField txtBuscaPedido;
	@FXML Button btnRegistrarSaida;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colID.setCellValueFactory(new PropertyValueFactory<>("idPedido"));
        colProduto.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("qtdAprovada"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        atualizarTabela();
        
    }

    private void atualizarTabela() {
    	ObservableList<Pedido> todosPedidos = pedidoDAO.listarTodos();
        ObservableList<PedidoProduto> todosOsItens = FXCollections.observableArrayList();

        for (Pedido p : todosPedidos) {
            // Busca os itens de cada pedido individualmente e junta tudo
            todosOsItens.addAll(pedidoDAO.listarItens(p.getIdPedido()));
        }
        
        FilteredList<PedidoProduto> filteredData = new FilteredList<>(todosOsItens, p -> true);

        txtBuscaPedido.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(item -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return item.getNomeProduto().toLowerCase().contains(lowerCaseFilter) ||
                       String.valueOf(item.getIdPedido()).contains(lowerCaseFilter);
            });
        });

        pedidosTable.setItems(filteredData);
        
    }

    @FXML
    private void handleRegistrarSaida() {
        PedidoProduto itemSelecionado = pedidosTable.getSelectionModel().getSelectedItem();

        if (itemSelecionado == null) {
            exibirAlerta("Seleção", "Por favor, selecione um item na tabela.");
            return;
        }

        String qtdTexto = txtQuantidadeSaida.getText();
        if (qtdTexto == null || qtdTexto.isEmpty()) {
            exibirAlerta("Campo Vazio", "Informe a quantidade de saída.");
            return;
        }

        try {
            int qtdSaida = Integer.parseInt(qtdTexto);
            if (qtdSaida > itemSelecionado.getQtdAprovada()) {
                exibirAlerta("Quantidade Excedida", "Você não pode retirar mais do que a quantidade aprovada.");
                return;
            }
            
            int saldoEmEstoque = itemSelecionado.getProduto().getSaldo(); 
            if (qtdSaida > saldoEmEstoque) {
                exibirAlerta("Estoque Insuficiente", "O estoque possui apenas " + saldoEmEstoque + " unidades disponíveis.");
                return;
            }
         
            // Busca o usuário que foi salvo no momento do Login
            Usuario logado = SessaoUsuario.getInstancia().getUsuarioLogado();
            int idUsuarioLogado = logado.getIdUsuario();

            boolean sucesso = movimentacaoDAO.registrarSaida(
                itemSelecionado.getIdPedido(),
                itemSelecionado.getProduto().getIdProduto(),
                itemSelecionado.getIdPedidoProduto(),
                qtdSaida,
                idUsuarioLogado
            );

            if (sucesso) {
                exibirAlerta("Sucesso", "Saída registrada com sucesso!");
                txtQuantidadeSaida.clear();
                atualizarTabela();
                voltarMenuPrincipal(new ActionEvent());
            } else {
                exibirAlerta("Erro", "Não foi possível registrar a saída no banco.");
            }

        } catch (NumberFormatException e) {
            exibirAlerta("Erro de Formato", "A quantidade deve ser um número inteiro.");
        }
    }
    
    @FXML
    private void voltarMenuPrincipal(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/estoque.fxml"));
            Node tela = loader.load();

            estoqueController controller = loader.getController();
            controller.setAreaPrincipal(areaPrincipal);

            AnchorPane.setTopAnchor(tela, 0.0);
            AnchorPane.setBottomAnchor(tela, 0.0);
            AnchorPane.setLeftAnchor(tela, 0.0);
            AnchorPane.setRightAnchor(tela, 0.0);

            areaPrincipal.getChildren().setAll(tela);
            
        } catch (IOException e) {
            System.err.println("Erro ao voltar para o menu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void exibirAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    private AnchorPane areaPrincipal;

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }
}