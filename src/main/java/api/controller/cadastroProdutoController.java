package api.controller;

import api.DAO.produtoDAO;
import api.model.Produto;
import api.service.HistoricoService;
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

public class cadastroProdutoController implements Initializable {

    @FXML private TextField        fieldProduto;
    @FXML private TextArea         fieldDescricao;
    @FXML private ComboBox<String> fieldUnidade;
    @FXML private TextField        fieldNivelMinimo;
    @FXML private TextField        fieldValorEstimado;
    @FXML private ComboBox<String> fieldStatus;

    @FXML private Label erroProduto;
    @FXML private Label erroDescricao;
    @FXML private Label erroUnidade;
    @FXML private Label erroNivelMinimo;
    @FXML private Label erroValorEstimado;
    @FXML private Label erroStatus;
    @FXML private Label contadorDescricao;
    @FXML private Label labelTitulo;
    @FXML private Label labelSubtitulo;

    private Produto    produtoEdicao = null;
    private AnchorPane areaPrincipal = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fieldUnidade.setItems(FXCollections.observableArrayList(
                "UN", "CX", "KG", "LT", "MT", "PC", "RL", "SC", "TB", "VD"));

        fieldStatus.setItems(FXCollections.observableArrayList("ATIVO", "INATIVO"));
        fieldStatus.setValue("ATIVO");

        fieldDescricao.textProperty().addListener((obs, antigo, novo) -> {
            int tamanho = novo.length();
            if (tamanho > 155) { fieldDescricao.setText(antigo); return; }
            contadorDescricao.setText(tamanho + "/155");
            contadorDescricao.setStyle(tamanho > 130
                    ? "-fx-text-fill: #ef4444; -fx-font-size: 11px;"
                    : "-fx-text-fill: #64748b; -fx-font-size: 11px;");
        });

        fieldNivelMinimo.textProperty().addListener((obs, antigo, novo) -> {
            if (!novo.matches("\\d*")) fieldNivelMinimo.setText(antigo);
        });

        fieldValorEstimado.textProperty().addListener((obs, antigo, novo) -> {
            if (!novo.matches("\\d*[,.]?\\d{0,2}")) fieldValorEstimado.setText(antigo);
        });
    }

    // ── Métodos chamados externamente ─────────────────────────

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }

    public void setProdutoEdicao(Produto produto) {
        this.produtoEdicao = produto;
        labelTitulo.setText("Editar Produto");
        labelSubtitulo.setText("Altere os dados do produto selecionado");
        fieldProduto.setText(produto.getProduto());
        fieldDescricao.setText(produto.getDescricao());
        fieldUnidade.setValue(produto.getUnidadeMedida());
        fieldNivelMinimo.setText(String.valueOf(produto.getNivelMinimo()));
        fieldValorEstimado.setText(
                String.format("%.2f", produto.getValorEstimado()).replace(".", ","));
        fieldStatus.setValue(produto.getStatus());
    }

    // ── Handlers ──────────────────────────────────────────────

    @FXML
    private void onSalvar() {
        limparErros();

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

        boolean sucesso;

        if (produtoEdicao == null) {
            // Novo cadastro
            Produto novo = new Produto(
                    fieldProduto.getText().trim(),
                    fieldDescricao.getText().trim(),
                    fieldUnidade.getValue(),
                    Integer.parseInt(fieldNivelMinimo.getText()),
                    Double.parseDouble(fieldValorEstimado.getText().replace(",", ".")),
                    fieldStatus.getValue(),
                    0
            );
            sucesso = produtoDAO.inserir(novo);

            if (sucesso) {
                HistoricoService.registrar(
                        "Produto",
                        "Cadastro",
                        novo.getIdProduto(),
                        "Produto \"" + novo.getProduto() + "\" cadastrado"
                );
            }
            exibirAlerta(sucesso,
                    "Produto \"" + novo.getProduto() + "\" cadastrado com sucesso!",
                    "Erro ao cadastrar. Verifique a conexão com o banco.");
        } else {
            // Edição
            Produto editado = new Produto(
                    produtoEdicao.getIdProduto(),
                    fieldProduto.getText().trim(),
                    fieldDescricao.getText().trim(),
                    fieldUnidade.getValue(),
                    Integer.parseInt(fieldNivelMinimo.getText()),
                    Double.parseDouble(fieldValorEstimado.getText().replace(",", ".")),
                    fieldStatus.getValue(),
                    produtoEdicao.getSaldo()
            );
            sucesso = produtoDAO.atualizar(editado);

            if (sucesso) {
                HistoricoService.registrar(
                        "Produto",
                        "Alteração",
                        editado.getIdProduto(),
                        "Produto \"" + editado.getProduto() + "\" alterado"
                );
            }
            exibirAlerta(sucesso,
                    "Produto \"" + editado.getProduto() + "\" atualizado com sucesso!",
                    "Erro ao atualizar. Verifique a conexão com o banco.");
        }

        // Após salvar (com sucesso ou não), volta para o estoque
        if (sucesso) {
            voltarParaEstoque();
        }
    }


    @FXML
    private void onCancelar() {
        // Volta para o estoque sem salvar nada
        voltarParaEstoque();
    }

    // ── Utilitários ───────────────────────────────────────────

    private void voltarParaEstoque() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/estoque.fxml"));
            Node tela = loader.load();

            estoqueController controller = loader.getController();
            controller.setAreaPrincipal(areaPrincipal);

            AnchorPane.setTopAnchor(tela, 0.0);
            AnchorPane.setBottomAnchor(tela, 0.0);
            AnchorPane.setLeftAnchor(tela, 0.0);
            AnchorPane.setRightAnchor(tela, 0.0);

            areaPrincipal.getChildren().setAll(tela);

        } catch (IOException e) {
            System.err.println("Erro ao voltar para estoque: " + e.getMessage());
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

    private void destacarCampo(TextField field, boolean erro) {
        if (erro) {
            field.setStyle(field.getStyle() + " -fx-border-color: #ef4444;");
        } else {
            field.setStyle(field.getStyle().replace(" -fx-border-color: #ef4444;", ""));
        }
    }
}
