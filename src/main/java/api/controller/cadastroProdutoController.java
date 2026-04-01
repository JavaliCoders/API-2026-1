package api.controller;

import api.DAO.produtoDAO;
import api.model.Produto;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class cadastroProdutoController implements Initializable {

    @FXML private TextField  fieldProduto;
    @FXML private TextArea   fieldDescricao;
    @FXML private ComboBox<String> fieldUnidade;
    @FXML private TextField  fieldNivelMinimo;
    @FXML private TextField  fieldValorEstimado;
    @FXML private ComboBox<String> fieldStatus;

    // Labels de erro
    @FXML private Label erroProduto;
    @FXML private Label erroDescricao;
    @FXML private Label erroUnidade;
    @FXML private Label erroNivelMinimo;
    @FXML private Label erroValorEstimado;
    @FXML private Label erroStatus;

    // Contador de caracteres da descrição
    @FXML private Label contadorDescricao;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Popula unidades de medida (CHAR 2)
        fieldUnidade.setItems(FXCollections.observableArrayList(
                "UN", "CX", "KG", "LT", "MT", "PC", "RL", "SC", "TB", "VD"
        ));

        // Popula status (ENUM)
        fieldStatus.setItems(FXCollections.observableArrayList(
                "ATIVO", "INATIVO"
        ));
        fieldStatus.setValue("ATIVO");


        // Contador de caracteres da descrição
        fieldDescricao.textProperty().addListener((obs, antigo, novo) -> {
            int tamanho = novo.length();
            // Limita a 155 caracteres conforme o banco
            if (tamanho > 155) {
                fieldDescricao.setText(antigo);
                return;
            }
            contadorDescricao.setText(tamanho + "/155");
            // Muda cor quando está perto do limite
            contadorDescricao.setStyle(tamanho > 130
                    ? "-fx-text-fill: #ef4444; -fx-font-size: 11px;"
                    : "-fx-text-fill: #9ca3af; -fx-font-size: 11px;");
        });

        // Permite apenas números no nível mínimo
        fieldNivelMinimo.textProperty().addListener((obs, antigo, novo) -> {
            if (!novo.matches("\\d*")) fieldNivelMinimo.setText(antigo);
        });

        // Permite apenas números e vírgula/ponto no valor
        fieldValorEstimado.textProperty().addListener((obs, antigo, novo) -> {
            if (!novo.matches("\\d*[,.]?\\d{0,2}")) fieldValorEstimado.setText(antigo);
        });
    }

    @FXML
    private void onSalvar() {
        // Limpa erros anteriores
        limparErros();

        // Valida os campos
        boolean valido = true;

        if (fieldProduto.getText().isBlank()) {
            erroProduto.setText("Nome do produto é obrigatório.");
            destacarCampo(fieldProduto, true);
            valido = false;
        }

        if (fieldDescricao.getText().isBlank()) {
            erroDescricao.setText("Descrição é obrigatória.");
            valido = false;
        }

        if (fieldUnidade.getValue() == null) {
            erroUnidade.setText("Selecione a unidade de medida.");
            valido = false;
        }

        if (fieldNivelMinimo.getText().isBlank()) {
            erroNivelMinimo.setText("Nível mínimo é obrigatório.");
            destacarCampo(fieldNivelMinimo, true);
            valido = false;
        }

        if (fieldValorEstimado.getText().isBlank()) {
            erroValorEstimado.setText("Valor estimado é obrigatório.");
            destacarCampo(fieldValorEstimado, true);
            valido = false;
        }

        if (fieldStatus.getValue() == null) {
            erroStatus.setText("Selecione o status.");
            valido = false;
        }

        if (!valido) return;

        // Monta o objeto Produto
        Produto produto = new Produto(
                fieldProduto.getText().trim(),
                fieldDescricao.getText().trim(),
                fieldUnidade.getValue(),
                Integer.parseInt(fieldNivelMinimo.getText()),
                Double.parseDouble(fieldValorEstimado.getText().replace(",", ".")),
                fieldStatus.getValue(),
                0 // saldo inicial sempre 0
        );

        // Substitua o bloco "Por enquanto exibe confirmação" no onSalvar() por:

        boolean sucesso = produtoDAO.inserir(produto);

        if (sucesso) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sucesso");
            alert.setHeaderText(null);
            alert.setContentText("Produto \"" + produto.getProduto() + "\" cadastrado com sucesso!");
            alert.showAndWait();
            limparFormulario();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText("Erro ao cadastrar produto. Verifique a conexão com o banco.");
            alert.showAndWait();
        }
    }

    @FXML
    private void onCancelar() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja cancelar o cadastro?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Cancelar");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) limparFormulario();
        });
    }

    private void limparErros() {
        erroProduto.setText("");
        erroDescricao.setText("");
        erroUnidade.setText("");
        erroNivelMinimo.setText("");
        erroValorEstimado.setText("");
        erroStatus.setText("");
        destacarCampo(fieldProduto, false);
        destacarCampo(fieldNivelMinimo, false);
        destacarCampo(fieldValorEstimado, false);
    }

    private void limparFormulario() {
        fieldProduto.clear();
        fieldDescricao.clear();
        fieldUnidade.setValue(null);
        fieldNivelMinimo.clear();
        fieldValorEstimado.clear();
        fieldStatus.setValue("ATIVO");
        contadorDescricao.setText("0/155");
        limparErros();
    }

    // Destaca o campo com borda vermelha se inválido
    private void destacarCampo(TextField field, boolean erro) {
        if (erro) {
            field.setStyle(field.getStyle() +
                    " -fx-border-color: #ef4444;");
        } else {
            field.setStyle(field.getStyle()
                    .replace(" -fx-border-color: #ef4444;", ""));
        }
    }
}
