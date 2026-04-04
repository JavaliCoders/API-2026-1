package api.DAO;

import api.connection.ConexaoDB;
import api.model.Perfil;
import api.model.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class UsuarioDAO {

    public static boolean inserir(Usuario u) {
        String sql = """
                INSERT INTO tb_usuario
                    (nome, usuario, senha, email, status, id_perfil)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getNome());
            ps.setString(2, u.getUsuario());
            ps.setString(3, u.getSenha());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getStatus());
            ps.setInt   (6, u.getPerfil().getIdPerfil());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao inserir usuário: " + e.getMessage());
            return false;
        }
    }

    public static boolean atualizar(Usuario u) {
        String sql = """
                UPDATE tb_usuario SET
                    nome      = ?,
                    usuario   = ?,
                    senha     = ?,
                    email     = ?,
                    status    = ?,
                    id_perfil = ?
                WHERE id_usuario = ?
                """;
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getNome());
            ps.setString(2, u.getUsuario());
            ps.setString(3, u.getSenha());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getStatus());
            ps.setInt   (6, u.getPerfil().getIdPerfil());
            ps.setInt   (7, u.getIdUsuario());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar usuário: " + e.getMessage());
            return false;
        }
    }

    public static ObservableList<Usuario> listarTodos() {
        ObservableList<Usuario> lista = FXCollections.observableArrayList();
        String sql = """
                SELECT u.id_usuario, u.nome, u.usuario, u.email, u.status,
                       p.id_perfil, p.perfil
                FROM tb_usuario u
                JOIN tb_perfil p ON u.id_perfil = p.id_perfil
                ORDER BY u.id_usuario ASC
                """;

        try (Connection con = ConexaoDB.getConexao();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Perfil p = new Perfil(
                        rs.getInt("id_perfil"),
                        rs.getString("perfil")
                );
                lista.add(new Usuario(
                        rs.getInt   ("id_usuario"),
                        rs.getString("nome"),
                        rs.getString("usuario"),
                        "",
                        rs.getString("email"),
                        rs.getString("status"),
                        p
                ));
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar usuários: " + e.getMessage());
        }
        return lista;
    }

    public static ObservableList<Perfil> listarPerfis() {
        ObservableList<Perfil> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM tb_perfil ORDER BY id_perfil ASC";

        try (Connection con = ConexaoDB.getConexao();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Perfil(
                        rs.getInt("id_perfil"),
                        rs.getString("perfil")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar perfis: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Verifica se já existe um usuário com o mesmo login.
     * No caso de edição, ignora o próprio ID para não bloquear
     * o usuário de salvar sem alterar o login.
     *
     * @param usuario    login a verificar
     * @param idIgnorar  ID a ignorar (0 para novo cadastro)
     */
    public static boolean usuarioJaExiste(String usuario, int idIgnorar) {
        String sql = "SELECT COUNT(*) FROM tb_usuario WHERE usuario = ? AND id_usuario != ?";

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario);
            ps.setInt   (2, idIgnorar);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getInt(1) > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao verificar usuário: " + e.getMessage());
        }
        return false;
    }

    /**
     * Verifica se já existe um usuário com o mesmo email.
     * No caso de edição, ignora o próprio ID.
     *
     * @param email      email a verificar
     * @param idIgnorar  ID a ignorar (0 para novo cadastro)
     */
    public static boolean emailJaExiste(String email, int idIgnorar) {
        String sql = "SELECT COUNT(*) FROM tb_usuario WHERE email = ? AND id_usuario != ?";

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setInt   (2, idIgnorar);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getInt(1) > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao verificar email: " + e.getMessage());
        }
        return false;
    }
}