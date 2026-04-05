package api.service;

import api.database.Conexao;
import api.model.UsuarioAutenticado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AutenticacaoService {

    public Optional<UsuarioAutenticado> autenticar(String identificador, String senha) {
        String login = identificador == null ? "" : identificador.trim();
        String senhaInformada = senha == null ? "" : senha;

        if (login.isEmpty() || senhaInformada.isEmpty()) {
            throw new IllegalArgumentException("Preencha usuario/e-mail e senha.");
        }

        String sql = "SELECT u.nome, u.usuario, u.email, p.perfil "
                + "FROM tb_usuario u "
                + "JOIN tb_perfil p ON u.id_perfil = p.id_perfil "
                + "WHERE (u.usuario = ? OR u.email = ?) "
                + "AND u.senha = ? "
                + "AND u.status = 'ATIVO'";

        try (Connection con = Conexao.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, login);
            ps.setString(2, login);
            ps.setString(3, senhaInformada);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(new UsuarioAutenticado(
                        rs.getString("nome"),
                        rs.getString("usuario"),
                        rs.getString("email"),
                        rs.getString("perfil")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao validar credenciais: " + e.getMessage(), e);
        }
    }
}
