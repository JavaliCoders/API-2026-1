package api.controller;

import api.model.UsuarioAutenticado;
import api.service.AutenticacaoService;
import api.service.PerfilMensagemService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    private final AutenticacaoService autenticacaoService = new AutenticacaoService();

    @FXML private TextField txtIdentificador;
    @FXML private PasswordField txtSenha;
    @FXML private Label lblMensagem;

    @FXML
    private void entrar(ActionEvent event) {
        String identificador = txtIdentificador.getText().trim();
        String senha = txtSenha.getText();

        lblMensagem.setText("");

        try {
            var usuario = autenticacaoService.autenticar(identificador, senha);
            if (usuario.isEmpty()) {
                exibirErro("Usuario/e-mail ou senha invalidos!");
                return;
            }

            exibirSucesso(usuario.get());
        } catch (IllegalArgumentException e) {
            exibirErro(e.getMessage());
        } catch (RuntimeException e) {
            exibirErro("Falha ao acessar o banco: " + e.getMessage());
        }
    }

    private void exibirErro(String mensagem) {
        lblMensagem.setText(mensagem);
        lblMensagem.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        lblMensagem.setVisible(true);
        lblMensagem.setManaged(true);
    }

    private void exibirSucesso(UsuarioAutenticado usuario) {
        lblMensagem.setText("Login realizado! Perfil: " + usuario.perfil());
        lblMensagem.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        lblMensagem.setVisible(true);
        lblMensagem.setManaged(true);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acesso liberado");
        alert.setHeaderText("Bem-vindo, " + usuario.nome());
        alert.setContentText(PerfilMensagemService.mensagemPara(usuario.perfil()));
        alert.showAndWait();
    }

    @FXML
    private void aoDigitar() {
        lblMensagem.setVisible(false);
        lblMensagem.setManaged(false);
    }
}
