package api.controller;

import api.DAO.centroCustoDAO;
import api.model.CentroCusto;
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

public class cadastroCentroCustoController implements Initializable {

    @FXML private TextField        fieldNome;
    @FXML private ComboBox<String> fieldStatus;

    @FXML private Label erroNome;
    @FXML private Label erroStatus;
    @FXML private Label labelTitulo;
    @FXML private Label labelSubtitulo;

    private CentroCusto centrocustoEdicao = null; // ✅ tipo correto
    private AnchorPane  areaPrincipal     = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fieldStatus.setItems(FXCollections.observableArrayList("ATIVO", "INATIVO"));
        fieldStatus.setValue("ATIVO");
    }

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }

    public void setCentrocustoEdicao(CentroCusto centroCusto) { // ✅ tipo correto
        this.centrocustoEdicao = centroCusto;                   // ✅ nome consistente
        labelTitulo.setText("Editar Centro de Custo");
        labelSubtitulo.setText("Altere os dados do centro de custo");
        fieldNome.setText(centroCusto.getCentroCusto());         // ✅ getCentroCusto()
        fieldStatus.setValue(centroCusto.getStatus());
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
        if (fieldStatus.getValue() == null) {
            erroStatus.setText("Selecione o status.");
            valido = false;
        }

        if (!valido) return;

        boolean sucesso;

        if (centrocustoEdicao == null) {
            // ✅ Novo cadastro usando CentroCusto e centroCustoDAO
            CentroCusto novo = new CentroCusto(
                    0,
                    fieldNome.getText().trim(),
                    fieldStatus.getValue()
            );
            sucesso = centroCustoDAO.inserir(novo);

            if (sucesso) {
                HistoricoService.registrar(
                        "Centro de Custo",
                        "Cadastro",
                        novo.getIdCentroCusto(),
                        "Centro de custo \"" + novo.getCentroCusto() + "\" cadastrado"
                );
            }

            exibirAlerta(sucesso,
                    "Centro de custo \"" + novo.getCentroCusto() + "\" cadastrado com sucesso!",
                    "Erro ao cadastrar. Verifique a conexão com o banco.");

        } else {
            // ✅ Edição usando CentroCusto e centroCustoDAO
            CentroCusto editado = new CentroCusto(
                    centrocustoEdicao.getIdCentroCusto(),
                    fieldNome.getText().trim(),
                    fieldStatus.getValue()
            );
            sucesso = centroCustoDAO.atualizar(editado);

            if (sucesso) {
                HistoricoService.registrar(
                        "Centro de Custo",
                        "Alteração",
                        editado.getIdCentroCusto(),
                        "Centro de custo \"" + editado.getCentroCusto() + "\" alterado"
                );
            }

            exibirAlerta(sucesso,
                    "Centro de custo \"" + editado.getCentroCusto() + "\" atualizado com sucesso!",
                    "Erro ao atualizar. Verifique a conexão com o banco.");
        }

        if (sucesso) voltarParaCentroCusto();
    }

    @FXML
    private void onCancelar() { voltarParaCentroCusto(); }

    private void voltarParaCentroCusto() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/centroCusto.fxml"));
            Node tela = loader.load();

            centroCustoController controller = loader.getController();
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
        erroStatus.setText("");
        destacarCampo(fieldNome, false);
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