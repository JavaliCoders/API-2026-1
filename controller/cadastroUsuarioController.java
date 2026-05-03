package api.controller;

import api.DAO.UsuarioDAO;
import api.model.Perfil;
import api.model.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class cadastroUsuarioController implements Initializable {

    @FXML private TextField        fieldNome;
    @FXML private TextField        fieldUsuario;
    @FXML private TextField        fieldEmail;
    @FXML private PasswordField    fieldSenha;
    @FXML private ComboBox<Perfil> fieldPerfil;
    @FXML private ComboBox<String> fieldStatus;

    @FXML private Label erroNome;
    @FXML private Label erroUsuario;
    @FXML private Label erroEmail;
    @FXML private Label erroSenha;
    @FXML private Label erroPerfil;
    @FXML private Label erroStatus;
    @FXML private Label labelTitulo;
    @FXML private Label labelSubtitulo;

    private Usuario    usuarioEdicao = null;
    private AnchorPane areaPrincipal = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Carrega perfis do banco dinamicamente
        ObservableList<Perfil> perfis = UsuarioDAO.listarPerfis();
        fieldPerfil.setItems(perfis);

        fieldStatus.setItems(FXCollections.observableArrayList("ATIVO", "INATIVO"));
        fieldStatus.setValue("ATIVO");

        // Remove destaque de erro ao digitar no campo usuário
        fieldUsuario.textProperty().addListener((obs, antigo, novo) -> {
            erroUsuario.setText("");
            destacarCampo(fieldUsuario, false);
        });

        // Remove destaque de erro ao digitar no campo email
        fieldEmail.textProperty().addListener((obs, antigo, novo) -> {
            erroEmail.setText("");
            destacarCampo(fieldEmail, false);
        });
    }

    public void setAreaPrincipal(AnchorPane areaPrincipal) {
        this.areaPrincipal = areaPrincipal;
    }

    public void setUsuarioEdicao(Usuario usuario) {
        this.usuarioEdicao = usuario;
        labelTitulo.setText("Editar Usuário");
        labelSubtitulo.setText("Altere os dados do usuário selecionado");

        fieldNome.setText(usuario.getNome());
        fieldUsuario.setText(usuario.getUsuario());
        fieldEmail.setText(usuario.getEmail());
        fieldStatus.setValue(usuario.getStatus());
        // Senha não é preenchida por segurança

        // Seleciona o perfil correspondente no ComboBox
        fieldPerfil.getItems().stream()
                .filter(p -> p.getIdPerfil() == usuario.getPerfil().getIdPerfil())
                .findFirst()
                .ifPresent(fieldPerfil::setValue);
    }

    @FXML
    private void onSalvar() {
        limparErros();

        // ── Validações de campos obrigatórios ─────────────────
        boolean valido = true;

        if (fieldNome.getText().isBlank()) {
            erroNome.setText("Nome é obrigatório.");
            destacarCampo(fieldNome, true);
            valido = false;
        }
        if (fieldUsuario.getText().isBlank()) {
            erroUsuario.setText("Usuário é obrigatório.");
            destacarCampo(fieldUsuario, true);
            valido = false;
        }
        if (fieldEmail.getText().isBlank()) {
            erroEmail.setText("Email é obrigatório.");
            destacarCampo(fieldEmail, true);
            valido = false;
        }
        if (usuarioEdicao == null && fieldSenha.getText().isBlank()) {
            erroSenha.setText("Senha é obrigatória.");
            valido = false;
        }
        if (fieldPerfil.getValue() == null) {
            erroPerfil.setText("Selecione o perfil.");
            valido = false;
        }
        if (fieldStatus.getValue() == null) {
            erroStatus.setText("Selecione o status.");
            valido = false;
        }

        if (!valido) return;

        // ── Verificações de duplicidade no banco ──────────────

        // ID a ignorar: 0 para novo cadastro, ID real para edição
        // Para novo cadastro: idIgnorar = 0 (compara com todos)
        // Para edição: idIgnorar = id real do usuário (ignora o próprio)
        int idIgnorar = usuarioEdicao == null ? 0 : usuarioEdicao.getIdUsuario();

        if (UsuarioDAO.usuarioJaExiste(fieldUsuario.getText().trim(), idIgnorar)) {
            erroUsuario.setText("Este usuário já está cadastrado no sistema.");
            // ↑ Só aparece se OUTRO registro tiver o mesmo login
        }

        if (UsuarioDAO.emailJaExiste(fieldEmail.getText().trim(), idIgnorar)) {
            erroEmail.setText("Este email já está cadastrado no sistema.");
            // ↑ Só aparece se OUTRO registro tiver o mesmo email
        }

        // ── Salva no banco ────────────────────────────────────
        boolean sucesso;

        if (usuarioEdicao == null) {
            // Novo cadastro
            Usuario novo = new Usuario(
                    fieldNome.getText().trim(),
                    fieldUsuario.getText().trim(),
                    fieldSenha.getText(),
                    fieldEmail.getText().trim(),
                    fieldStatus.getValue(),
                    fieldPerfil.getValue()
            );
            sucesso = UsuarioDAO.inserir(novo);
            exibirAlerta(sucesso,
                    "Usuário \"" + novo.getNome() + "\" cadastrado com sucesso!",
                    "Erro ao cadastrar. Verifique a conexão com o banco.");
        } else {
            // Edição — se senha não foi digitada, mantém a anterior
            String senha = fieldSenha.getText().isBlank()
                    ? usuarioEdicao.getSenha()
                    : fieldSenha.getText();

            Usuario editado = new Usuario(
                    usuarioEdicao.getIdUsuario(),
                    fieldNome.getText().trim(),
                    fieldUsuario.getText().trim(),
                    senha,
                    fieldEmail.getText().trim(),
                    fieldStatus.getValue(),
                    fieldPerfil.getValue()
            );
            sucesso = UsuarioDAO.atualizar(editado);
            exibirAlerta(sucesso,
                    "Usuário \"" + editado.getNome() + "\" atualizado com sucesso!",
                    "Erro ao atualizar. Verifique a conexão com o banco.");
        }

        // Volta para a tela de usuários após salvar com sucesso
        if (sucesso) voltarParaUsuarios();
    }

    @FXML
    private void onCancelar() {
        voltarParaUsuarios();
    }

    // ── Utilitários ───────────────────────────────────────────

    private void voltarParaUsuarios() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/usuario.fxml"));
            Node tela = loader.load();

            usuarioController controller = loader.getController();
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
        erroUsuario.setText("");
        erroEmail.setText("");
        erroSenha.setText("");
        erroPerfil.setText("");
        erroStatus.setText("");
        destacarCampo(fieldNome, false);
        destacarCampo(fieldUsuario, false);
        destacarCampo(fieldEmail, false);
    }

    private void destacarCampo(TextField field, boolean erro) {
        if (erro) {
            field.setStyle(field.getStyle() + " -fx-border-color: #ef4444;");
        } else {
            field.setStyle(field.getStyle().replace(" -fx-border-color: #ef4444;", ""));
        }
    }
}