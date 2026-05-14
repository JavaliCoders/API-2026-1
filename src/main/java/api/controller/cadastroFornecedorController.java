package api.controller;

import api.DAO.FormaPagamentoDAO;
import api.DAO.fornecedorDAO;
import api.model.FormaPagamento;
import api.model.Fornecedor;
import api.service.HistoricoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class cadastroFornecedorController implements Initializable {

    @FXML private TextField        fieldNome;
    @FXML private TextField        fieldCnpj;
    @FXML private TextField        fieldPedidoMinimo;
    @FXML private FlowPane         painelFormasPagamento;
    @FXML private ComboBox<String> fieldStatus;

    @FXML private Label erroNome;
    @FXML private Label erroCnpj;
    @FXML private Label erroFormasPagamento;
    @FXML private Label erroStatus;
    @FXML private Label labelTitulo;
    @FXML private Label labelSubtitulo;

    private Fornecedor fornecedorEdicao = null;
    private AnchorPane areaPrincipal    = null;
    private ObservableList<FormaPagamento> todasFormas;
    private final ObservableList<FormaPagamento> formasSelecionadas =
            FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fieldStatus.setItems(FXCollections.observableArrayList("ATIVO", "INATIVO"));
        fieldStatus.setValue("ATIVO");

        fieldCnpj.textProperty().addListener((obs, antigo, novo) -> {
            if (!novo.matches("\\d*")) fieldCnpj.setText(antigo);
            if (novo.length() > 14)    fieldCnpj.setText(antigo);
        });

        fieldPedidoMinimo.textProperty().addListener((obs, antigo, novo) -> {
            if (!novo.matches("\\d*[,.]?\\d{0,2}")) fieldPedidoMinimo.setText(antigo);
        });

        carregarCheckboxes();
    }

    private void carregarCheckboxes() {
        todasFormas = FormaPagamentoDAO.listarTodos();
        painelFormasPagamento.getChildren().clear();

        for (FormaPagamento fp : todasFormas) {

            CheckBox cb = new CheckBox();

            // ✔ garante que sempre aparece algo
            cb.setText(fp.getForma() != null ? fp.getForma() : "Sem nome");

            // ✔ estilo compatível com sua tela
            cb.setStyle(
                    "-fx-text-fill: #1e293b;" +
                            "-fx-font-size: 13px;" +
                            "-fx-padding: 6 10;"
            );

            // ✔ posicionamento correto
            cb.setContentDisplay(ContentDisplay.LEFT);
            cb.setGraphicTextGap(8);

            // ✔ lógica já correta
            cb.selectedProperty().addListener((obs, antigo, selecionado) -> {
                if (selecionado) formasSelecionadas.add(fp);
                else             formasSelecionadas.remove(fp);
            });

            painelFormasPagamento.getChildren().add(cb);
        }
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
        fieldStatus.setValue(fornecedor.getStatus());
        if (fornecedor.getPedidoMinimo() > 0) {
            fieldPedidoMinimo.setText(
                    String.format("%.2f", fornecedor.getPedidoMinimo())
                            .replace(".", ","));
        }

        for (Node node : painelFormasPagamento.getChildren()) {
            if (node instanceof CheckBox cb) {
                boolean marcado = fornecedor.getFormasPagamento().stream()
                        .anyMatch(fp -> fp.getForma().equals(cb.getText()));
                cb.setSelected(marcado);
            }
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
        if (formasSelecionadas.isEmpty()) {
            erroFormasPagamento.setText("Selecione pelo menos uma forma de pagamento.");
            valido = false;
        }
        if (fieldStatus.getValue() == null) {
            erroStatus.setText("Selecione o status.");
            valido = false;
        }

        if (!valido) return;

        double pedidoMinimo = fieldPedidoMinimo.getText().isBlank() ? 0.0
                : Double.parseDouble(
                fieldPedidoMinimo.getText().replace(",", "."));

        boolean sucesso;

        if (fornecedorEdicao == null) {
            Fornecedor novo = new Fornecedor(
                    fieldNome.getText().trim(),
                    fieldCnpj.getText().trim(),
                    pedidoMinimo,
                    fieldStatus.getValue()
            );
            sucesso = fornecedorDAO.inserir(novo, formasSelecionadas);

            if (sucesso) {
                HistoricoService.registrar(
                        "Fornecedor",
                        "Cadastro",
                        novo.getIdFornecedor(),
                        "Fornecedor \"" + novo.getNome() + "\" cadastrado"
                );
            }

            exibirAlerta(sucesso,
                    "Fornecedor \"" + novo.getNome() + "\" cadastrado com sucesso!",
                    "Erro ao cadastrar. Verifique a conexão com o banco.");
        } else {
            Fornecedor editado = new Fornecedor(
                    fornecedorEdicao.getIdFornecedor(),
                    fieldNome.getText().trim(),
                    fieldCnpj.getText().trim(),
                    pedidoMinimo,
                    fieldStatus.getValue()
            );
            sucesso = fornecedorDAO.atualizar(editado, formasSelecionadas);

            if (sucesso) {
                HistoricoService.registrar(
                        "Fornecedor",
                        "Alteração",
                        editado.getIdFornecedor(),
                        "Fornecedor \"" + editado.getNome() + "\" alterado"
                );
            }
            exibirAlerta(sucesso,
                    "Fornecedor \"" + editado.getNome() + "\" atualizado com sucesso!",
                    "Erro ao atualizar. Verifique a conexão com o banco.");
        }

        if (sucesso) voltarParaFornecedores();
    }

    @FXML
    private void onCancelar() { voltarParaFornecedores(); }

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
        erroFormasPagamento.setText("");
        erroStatus.setText("");
        destacarCampo(fieldNome, false);
        destacarCampo(fieldCnpj, false);
    }

    private void destacarCampo(TextField field, boolean erro) {
        if (erro) {
            field.setStyle(field.getStyle() + " -fx-border-color: #ef4444;");
        } else {
            field.setStyle(field.getStyle()
                    .replace(" -fx-border-color: #ef4444;", ""));
        }
    }
}
