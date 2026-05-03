package api.controller;

import api.DAO.fornecedorDAO;
import api.model.Fornecedor;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class cadastroFornecedorController implements Initializable {

    @FXML private TextField        fieldNome;
    @FXML private TextField        fieldCnpj;
    @FXML private ComboBox<String> fieldTipoPagamento;
    @FXML private TextField        fieldPedidoMinimo;
    @FXML private ComboBox<String> fieldStatus;

    @FXML private Label erroNome;
    @FXML private Label erroCnpj;
    @FXML private Label erroTipoPagamento;
    @FXML private Label erroStatus;
    @FXML private Label labelTitulo;
    @FXML private Label labelSubtitulo;

    private Fornecedor fornecedorEdicao = null;
    private AnchorPane areaPrincipal    = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fieldTipoPagamento.setItems(FXCollections.observableArrayList(
                "PIX", "CARTAO", "TRANSFERENCIA", "BOLETO", "FATURADO"));

        fieldStatus.setItems(FXCollections.observableArrayList("ATIVO", "INATIVO"));
        fieldStatus.setValue("ATIVO");

        // Apenas números no CNPJ, limite de 14 caracteres
        fieldCnpj.textProperty().addListener((obs, antigo, novo) -> {
            if (!novo.matches("\\d*")) fieldCnpj.setText(antigo);
            if (novo.length() > 14)    fieldCnpj.setText(antigo);
        });

        // Apenas números e vírgula/ponto no pedido mínimo
        fieldPedidoMinimo.textProperty().addListener((obs, antigo, novo) -> {
            if (!novo.matches("\\d*[,.]?\\d{0,2}")) fieldPedidoMinimo.setText(antigo);
        });
    }

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }

    public void setFornecedorEdicao(Fornecedor fornecedor) {
        this.fornecedorEdicao = fornecedor;
        labelTitulo.setText("Editar Fornecedor");
        labelSubtitulo.setText("Altere os dados do fornecedor selecionado");
        fieldNome.setText(fornecedor.getNome());
        fieldCnpj.setText(fornecedor.getCnpj());
        fieldTipoPagamento.setValue(fornecedor.getTipoPagamento());
        fieldStatus.setValue(fornecedor.getStatus());
        if (fornecedor.getPedidoMinimo() > 0) {
            fieldPedidoMinimo.setText(
                    String.format("%.2f", fornecedor.getPedidoMinimo()).replace(".", ","));
        }
    }

    @FXML
    private void onSalvar() {
        limparErros();
        boolean valido = true;

        if (fieldNome.getText().isBlank()) {
            erroNome.setText("Nome é obrigatório.");
            destacarCampo(fieldNome, true);
            valido = false;
        }
        if (fieldCnpj.getText().isBlank() || fieldCnpj.getText().length() != 14) {
            erroCnpj.setText("CNPJ deve ter 14 dígitos.");
            destacarCampo(fieldCnpj, true);
            valido = false;
        }
        if (fieldTipoPagamento.getValue() == null) {
            erroTipoPagamento.setText("Selecione o tipo de pagamento.");
            valido = false;
        }
        if (fieldStatus.getValue() == null) {
            erroStatus.setText("Selecione o status.");
            valido = false;
        }

        if (!valido) return;

        double pedidoMinimo = fieldPedidoMinimo.getText().isBlank() ? 0.0
                : Double.parseDouble(fieldPedidoMinimo.getText().replace(",", "."));

        boolean sucesso;

        if (fornecedorEdicao == null) {
            Fornecedor novo = new Fornecedor(
                    fieldNome.getText().trim(),
                    fieldCnpj.getText().trim(),
                    fieldTipoPagamento.getValue(),
                    pedidoMinimo,
                    fieldStatus.getValue()
            );
            sucesso = fornecedorDAO.inserir(novo);
            exibirAlerta(sucesso,
                    "Fornecedor \"" + novo.getNome() + "\" cadastrado com sucesso!",
                    "Erro ao cadastrar. Verifique a conexão com o banco.");
        } else {
            Fornecedor editado = new Fornecedor(
                    fornecedorEdicao.getIdFornecedor(),
                    fieldNome.getText().trim(),
                    fieldCnpj.getText().trim(),
                    fieldTipoPagamento.getValue(),
                    pedidoMinimo,
                    fieldStatus.getValue()
            );
            sucesso = fornecedorDAO.atualizar(editado);
            exibirAlerta(sucesso,
                    "Fornecedor \"" + editado.getNome() + "\" atualizado com sucesso!",
                    "Erro ao atualizar. Verifique a conexão com o banco.");
        }

        if (sucesso) voltarParaFornecedores();
    }

    @FXML
    private void onCancelar() {
        voltarParaFornecedores();
    }

    private void voltarParaFornecedores() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/fornecedor.fxml"));
            Node tela = loader.load();

            fornecedorController controller = loader.getController();
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

    private void exibirAlerta(boolean sucesso, String msgSucesso, String msgErro) {
        Alert alert = new Alert(
                sucesso ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle(sucesso ? "Sucesso" : "Erro");
        alert.setHeaderText(null);
        alert.setContentText(sucesso ? msgSucesso : msgErro);
        alert.showAndWait();
    }

    private void limparErros() {
        erroNome.setText("");
        erroCnpj.setText("");
        erroTipoPagamento.setText("");
        erroStatus.setText("");
        destacarCampo(fieldNome, false);
        destacarCampo(fieldCnpj, false);
    }

    private void destacarCampo(TextField field, boolean erro) {
        if (erro) {
            field.setStyle(field.getStyle() + " -fx-border-color: #ef4444;");
        } else {
            field.setStyle(field.getStyle().replace(" -fx-border-color: #ef4444;", ""));
        }
    }
}