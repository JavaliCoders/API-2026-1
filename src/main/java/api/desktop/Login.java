package api.desktop;

import api.model.UsuarioAutenticado;
import api.service.AutenticacaoService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.util.Optional;

public class Login extends JFrame {

    private final AutenticacaoService autenticacaoService = new AutenticacaoService();
    private JTextField txtIdentificador;
    private JPasswordField txtSenha;

    public Login() {
        setTitle("Login do Sistema");
        setSize(380, 220);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel lblUser = new JLabel("Usuario ou e-mail:");
        lblUser.setBounds(20, 30, 120, 25);
        add(lblUser);

        txtIdentificador = new JTextField();
        txtIdentificador.setBounds(150, 30, 190, 25);
        add(txtIdentificador);

        JLabel lblSenha = new JLabel("Senha:");
        lblSenha.setBounds(20, 70, 80, 25);
        add(lblSenha);

        txtSenha = new JPasswordField();
        txtSenha.setBounds(150, 70, 190, 25);
        add(txtSenha);

        JButton btnLogin = new JButton("Entrar");
        btnLogin.setBounds(150, 110, 110, 30);
        btnLogin.addActionListener(e -> fazerLogin());
        add(btnLogin);

        setVisible(true);
    }

    private void fazerLogin() {
        String identificador = txtIdentificador.getText();
        String senha = new String(txtSenha.getPassword());

        try {
            Optional<UsuarioAutenticado> usuario = autenticacaoService.autenticar(identificador, senha);

            if (usuario.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Usuario/e-mail ou senha invalidos!");
                return;
            }

            dispose();
            SwingUtilities.invokeLater(() -> new TelaPerfil(usuario.get()));
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Erro ao validar login: " + e.getMessage());
        }
    }
}
