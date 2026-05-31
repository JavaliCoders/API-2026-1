package api.controller;

import api.DAO.UsuarioDAO;
import api.model.SessaoUsuario;
import api.model.Usuario;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField     fieldUsuario;
    @FXML private PasswordField fieldSenha;
    @FXML private Label         labelErro;

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    @FXML
    private void onEntrar() {
        String usuario = fieldUsuario.getText().trim();
        String senha   = fieldSenha.getText();

        labelErro.setText("");

        // Valida campos vazios
        if (usuario.isBlank() || senha.isBlank()) {
            exibirErro("Preencha todos os campos!");
            return;
        }

        // Busca no banco
        Usuario usuarioEncontrado = UsuarioDAO.buscarPorLoginESenha(usuario, senha);

        if (usuarioEncontrado == null) {
            exibirErro("Usuário ou senha incorretos.");
            fieldSenha.clear();
            return;
        }
        

        // Verifica se está ATIVO
        if (usuarioEncontrado.getStatus().equals("INATIVO")) {
            exibirErro("Acesso negado: usuário inativo. Contate o administrador.");
            fieldSenha.clear();
            return;
        }

        // Salva na sessão
        SessaoUsuario.getInstancia().setUsuarioLogado(usuarioEncontrado);
        System.out.println("Perfil logado: " + usuarioEncontrado.getNomePerfil());

        abrirTelaPrincipal();
    }

    // Limpa o erro assim que o usuário começa a digitar
    @FXML
    private void aoDigitar() {
        labelErro.setVisible(false);
        labelErro.setManaged(false);

        // Remove borda vermelha dos campos
        String estiloNormal =
                "-fx-background-color: #f1f5f9; -fx-border-color: transparent; " +
                        "-fx-background-radius: 10; -fx-border-radius: 10; " +
                        "-fx-padding: 12 16; -fx-font-size: 14px;";
        fieldUsuario.setStyle(estiloNormal);
        fieldSenha.setStyle(estiloNormal);
    }

    private void abrirTelaPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/index.fxml"));
            BorderPane root = loader.load();

            Stage stage = (Stage) fieldUsuario.getScene().getWindow();
            Scene scene = new Scene(root, 1100, 650);
            stage.setScene(scene);
            stage.setTitle("Sistema de Pedidos");
            stage.setMinWidth(900);
            stage.setMinHeight(550);
            stage.setResizable(true);
            stage.show();

        } catch (IOException e) {
            System.err.println("Erro ao abrir tela principal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void exibirErro(String mensagem) {
        labelErro.setText(mensagem);
        labelErro.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-font-weight: bold;");
        labelErro.setVisible(true);
        labelErro.setManaged(true);

        String estiloErro =
                "-fx-background-color: #f1f5f9; -fx-border-color: #ef4444; " +
                        "-fx-background-radius: 10; -fx-border-radius: 10; " +
                        "-fx-padding: 12 16; -fx-font-size: 14px;";
        fieldUsuario.setStyle(estiloErro);
        fieldSenha.setStyle(estiloErro + " -fx-text-fill: #0f172a;");
    }
}
