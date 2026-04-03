package main.dao;

import main.modelo.Usuario;
import main.modelo.Perfil;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    private Connection conexao;

    public UsuarioDAO(Connection conexao) {this.conexao = conexao;}

    public void adiciona(Usuario usuario) {
        String sql = "INSERT INTO tb_usuario (nome, usuario, senha, email, status, id_perfil) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement stmt = conexao.prepareStatement(sql);

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getUsuario());
            stmt.setString(3, usuario.getSenha());
            stmt.setString(4, usuario.getEmail());
            stmt.setString(5, usuario.getStatus());
            stmt.setInt(6,usuario.getPerfil().getIdperfil());

            stmt.execute();
            stmt.close();
        } catch (SQLException u) {
            throw new RuntimeException(u);
        }
    }

    public List<Usuario> listar() {
        List<Usuario> lista = new ArrayList<>();

        String sql = "SELECT u.id_usuario, u.nome, u.usuario, u.email, u.status, " +
                "p.id_perfil, p.perfil " +
                "FROM tb_usuario u " +
                "JOIN tb_perfil p ON u.id_perfil = p.id_perfil";

        try {
            PreparedStatement stmt = conexao.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                Perfil p = new Perfil();
                p.setIdperfil(rs.getInt("id_perfil"));
                p.setPerfil(rs.getString("perfil"));

                Usuario u = new Usuario();
                u.setIdusuario(rs.getInt("id_usuario"));
                u.setNome(rs.getString("nome"));
                u.setUsuario(rs.getString("usuario"));
                u.setEmail(rs.getString("email"));
                u.setStatus(rs.getString("status"));
                u.setPerfil(p);

                lista.add(u);
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return lista;
    }
}
