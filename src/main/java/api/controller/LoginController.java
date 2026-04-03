package api.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
	
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtSenha;
    @FXML private Label lblMensagem;

    @FXML
    private void entrar(ActionEvent event) {
    	
    	// CA1 - Dado que o usuário acessa o sistema, então deve informar seu login e senha para que suas credenciais sejam validadas.
        String email = txtEmail.getText().trim();
        String senha = txtSenha.getText();
        
        lblMensagem.setText("");
        
        // CA3 - Dado que o usuário insere dados inválidos, então o sistema deve exibir uma mensagem de erro.
        if (email.isEmpty() || senha.isEmpty()) {
            exibirErro("Preencha todos os campos!");
            return;
        }
        
        // CA3 - Dado que o usuário insere dados inválidos, então o sistema deve exibir uma mensagem de erro.
        if (!email.contains("@") || !email.contains(".")) {
            exibirErro("Digite um e-mail válido (ex: usuario@email.com)");
            txtEmail.requestFocus();
            return;
        }

        // - Simulação
        
        // CA4 - Dado que o usuário está INATIVO, então o sistema deve bloquear acesso.
        if (email.equals("inativo@email.com")) {
            exibirErro("Acesso Negado: Usuário inativo.");
            return;
        }
        
        // CA3 - Dado que o usuário insere dados inválidos, então o sistema deve exibir uma mensagem de erro.
        //if (!email.equals("existente@email.com")) {
        //    exibirErro("Usuário não existe.");
        //    return;
        //}

        String perfil = "";

        // - Perfis simulados
        
        if (email.equals("diretor@email.com") && senha.equals("123")) {
            perfil = "DIRETOR";

        } else if (email.equals("financeiro@email.com") && senha.equals("123")) {
            perfil = "FINANCEIRO";

        } else if (email.equals("estoque@email.com") && senha.equals("123")) {
            perfil = "ESTOQUE";

        } else if (email.equals("operacional@email.com") && senha.equals("123")) {
            perfil = "OPERACIONAL";
           
        } else {
            // CA3 - inválido
            exibirErro("Usuário ou senha inválidos!");
            return;
        }

        // CA2 - Sucesso
        lblMensagem.setText("Login realizado! Perfil: " + perfil);
        lblMensagem.setStyle("-fx-text-fill: green;");
        lblMensagem.setVisible(true);
        lblMensagem.setManaged(true);
        // irParaPainelPrincipal(event, perfil);

        System.out.println("Perfil logado: " + perfil);
        
    }
    
    private void exibirErro(String mensagem) {
        lblMensagem.setText(mensagem);
        lblMensagem.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        lblMensagem.setVisible(true);  // Torna visível para o olho
        lblMensagem.setManaged(true);  // Faz o layout dar espaço para ele aparecer
    }
    
    @FXML
    private void aoDigitar() {
        lblMensagem.setVisible(false);
        lblMensagem.setManaged(false);
    }
    
}