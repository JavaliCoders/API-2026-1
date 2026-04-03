package main.gui;

import java.sql.Connection;
import main.modelo.Usuario;
import main.modelo.Perfil;
import main.dao.UsuarioDAO;
import main.factory.ConnectionFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CadastroUsuarioGUI {
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JButton confirmarButton;
    private JButton limparButton;
    private JButton cancelarButton;
    private JComboBox comboBoxperfil;
    private JComboBox comboBoxstatus;
    private JPanel painelprincipal;

    {
        cancelarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {System.exit(0);}

        });

        limparButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textField1.setText("");
                textField2.setText("");
                textField3.setText("");
                textField4.setText("");
                comboBoxperfil.setSelectedIndex(0);
                comboBoxstatus.setSelectedIndex(0);
            }
        });
        confirmarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String nome = textField1.getText().trim();
                String usuario = textField2.getText().trim();
                String senha = textField3.getText().trim();
                String email = textField4.getText().trim();
                String status = comboBoxstatus.getSelectedItem().toString();
                String perfil = comboBoxperfil.getSelectedItem().toString();

                if(nome.isEmpty() || usuario.isEmpty() ||senha.isEmpty() || email.isEmpty() || status.isEmpty() || perfil.isEmpty()){
                    JOptionPane.showMessageDialog(null, "Preencha todos os campos!");
                } else {
                    try {
                        Perfil p = new Perfil();

                        if (perfil.equals("DIRETOR")) {
                            p.setIdperfil(1);
                        } else if (perfil.equals("FINANCEIRO")) {
                            p.setIdperfil(2);
                        } else if (perfil.equals("ESTOQUE")) {
                            p.setIdperfil(3);
                        } else if (perfil.equals("OPERACIONAL")) {
                            p.setIdperfil(4);
                        }

                        Usuario u = new Usuario();
                        u.setNome(nome);
                        u.setUsuario(usuario);
                        u.setSenha(senha);
                        u.setEmail(email);
                        u.setStatus(status);
                        u.setPerfil(p);

                        Connection conn = new ConnectionFactory().getConnection();
                        UsuarioDAO dao = new UsuarioDAO(conn);

                        dao.adiciona(u);

                        JOptionPane.showMessageDialog(null, "Usuário cadastrado com sucesso!");

                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Erro ao cadastrar: " + ex.getMessage());
                    }

                }



            }
        });
    }

static void main(String[] args) {
    JFrame frame = new JFrame("Formulário de cadastro de Usuários");
    frame.setContentPane(new CadastroUsuarioGUI().painelprincipal);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
}
}
