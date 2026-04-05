package api.desktop;

import api.model.UsuarioAutenticado;
import api.service.PerfilMensagemService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Font;

public class TelaPerfil extends JFrame {

    public TelaPerfil(UsuarioAutenticado usuario) {
        setTitle("Area do " + usuario.perfil());
        setSize(520, 320);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));

        JLabel titulo = new JLabel("Bem-vindo, " + usuario.nome() + " - Perfil " + usuario.perfil());
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(titulo, BorderLayout.NORTH);

        JTextArea descricao = new JTextArea(PerfilMensagemService.mensagemPara(usuario.perfil()));
        descricao.setEditable(false);
        descricao.setLineWrap(true);
        descricao.setWrapStyleWord(true);
        descricao.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(descricao, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        JButton sair = new JButton("Sair");
        sair.addActionListener(e -> {
            dispose();
            new Login();
        });
        add(sair, BorderLayout.SOUTH);

        setVisible(true);
    }
}
