package api.DAO;

import api.connection.ConexaoDB;
import api.model.Notificacao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class notificacaoDAO {

    // ── Inserir notificação ───────────────────────────────────
    public static boolean inserir(Notificacao n) {
        String sql = """
                INSERT INTO tb_notificacao
                    (id_usuario, titulo, mensagem, entidade_tipo, entidade_id, lida, data)
                VALUES (?, ?, ?, ?, ?, 0, NOW())
                """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt   (1, n.getIdUsuario());
            ps.setString(2, n.getTitulo());
            ps.setString(3, n.getMensagem());
            ps.setString(4, n.getEntidadeTipo());

            if (n.getEntidadeId() != null) {
                ps.setInt(5, n.getEntidadeId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao inserir notificação: " + e.getMessage());
            return false;
        }
    }

    // ── Listar notificações de um usuário ─────────────────────
    public static ObservableList<Notificacao> listarPorUsuario(int idUsuario) {
        ObservableList<Notificacao> lista = FXCollections.observableArrayList();
        String sql = """
                SELECT * FROM tb_notificacao
                WHERE id_usuario = ?
                ORDER BY data DESC
                """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            System.err.println("Erro ao listar notificações: " + e.getMessage());
        }
        return lista;
    }

    // ── Contar não lidas de um usuário ────────────────────────
    public static int contarNaoLidas(int idUsuario) {
        String sql = "SELECT COUNT(*) FROM tb_notificacao WHERE id_usuario = ? AND lida = 0";

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("Erro ao contar não lidas: " + e.getMessage());
        }
        return 0;
    }

    // ── Marcar uma notificação como lida ──────────────────────
    public static boolean marcarComoLida(int idNotificacao) {
        String sql = "UPDATE tb_notificacao SET lida = 1 WHERE id_notificacao = ?";

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idNotificacao);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao marcar como lida: " + e.getMessage());
            return false;
        }
    }

    // ── Marcar todas como lidas para um usuário ───────────────
    public static boolean marcarTodasComoLidas(int idUsuario) {
        String sql = "UPDATE tb_notificacao SET lida = 1 WHERE id_usuario = ? AND lida = 0";

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao marcar todas como lidas: " + e.getMessage());
            return false;
        }
    }

    // ── Excluir uma notificação específica ────────────────────
    public static boolean excluir(int idNotificacao) {
        String sql = "DELETE FROM tb_notificacao WHERE id_notificacao = ?";

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idNotificacao);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao excluir notificação: " + e.getMessage());
            return false;
        }
    }

    // ── Excluir todas as notificações lidas de um usuário ─────
    public static boolean excluirTodasLidas(int idUsuario) {
        String sql = "DELETE FROM tb_notificacao WHERE id_usuario = ? AND lida = 1";

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao excluir notificações lidas: " + e.getMessage());
            return false;
        }
    }

    // ── Buscar IDs dos usuários por perfil ────────────────────
    public static List<int[]> buscarDiretores() {
        return buscarUsuariosPorPerfil("DIRETOR");
    }

    public static List<int[]> buscarUsuariosPorPerfil(String perfil) {
        List<int[]> lista = new ArrayList<>();
        String sql = """
                SELECT u.id_usuario
                FROM tb_usuario u
                JOIN tb_perfil p ON u.id_perfil = p.id_perfil
                WHERE p.perfil = ? AND u.status = 'ATIVO'
                """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, perfil);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new int[]{rs.getInt("id_usuario")});
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuários por perfil: " + e.getMessage());
        }
        return lista;
    }

    // ── Buscar emails dos DIRETOREs ativos ────────────────────
    public static List<String> buscarEmailsDiretores() {
        List<String> emails = new ArrayList<>();
        String sql = """
                SELECT u.email
                FROM tb_usuario u
                JOIN tb_perfil p ON u.id_perfil = p.id_perfil
                WHERE p.perfil = 'DIRETOR' AND u.status = 'ATIVO'
                """;

        try (Connection con = ConexaoDB.getConexao();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) emails.add(rs.getString("email"));

        } catch (SQLException e) {
            System.err.println("Erro ao buscar emails diretores: " + e.getMessage());
        }
        return emails;
    }

    // ── Mapper ────────────────────────────────────────────────
    private static Notificacao mapear(ResultSet rs) throws SQLException {
        Integer entidadeId = rs.getInt("entidade_id");
        if (rs.wasNull()) entidadeId = null;

        return new Notificacao(
                rs.getInt   ("id_notificacao"),
                rs.getInt   ("id_usuario"),
                rs.getString("titulo"),
                rs.getString("mensagem"),
                rs.getString("entidade_tipo"),
                entidadeId,
                rs.getInt   ("lida") == 1,
                rs.getTimestamp("data").toLocalDateTime()
        );
    }
    public static String buscarEmailUsuario(int idUsuario) {
        String sql = "SELECT email FROM tb_usuario WHERE id_usuario = ? AND status = 'ATIVO'";

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("email");
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar email do usuário: " + e.getMessage());
        }

        return null;
    }
}