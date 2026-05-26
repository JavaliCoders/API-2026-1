package api.DAO;

import api.connection.ConexaoDB;
import api.model.Historico;
import api.model.SessaoUsuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;

public class historicoDAO {

    // ── INSERT ────────────────────────────────────────────────
    public static void registrar(String entidade,
                                 String acao,
                                 Integer entidadeId,
                                 String descricao) {
        String sql = "INSERT INTO tb_historico " +
                "(entidade_tipo, acao, entidade_id, descricao, id_usuario, data) " +
                "VALUES (?, ?, ?, ?, ?, NOW())";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, entidade);
            stmt.setString(2, acao);
            if (entidadeId != null) stmt.setInt(3, entidadeId);
            else                    stmt.setNull(3, Types.INTEGER);
            stmt.setString(4, descricao);
            stmt.setInt(5, SessaoUsuario.getInstancia().getIdUsuarioLogado());
            stmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Erro ao registrar histórico: " + e.getMessage());
        }
    }

    // ── SELECT com filtros opcionais ──────────────────────────
    public static ObservableList<Historico> listarFiltrado(
            LocalDate dataInicio,
            LocalDate dataFim,
            String entidade,
            String acao,
            Integer idUsuario,
            Integer entidadeId) {

        ObservableList<Historico> lista = FXCollections.observableArrayList();

        StringBuilder sql = new StringBuilder("""
                SELECT h.id_historico,
                       h.entidade_tipo,
                       h.acao,
                       h.entidade_id,
                       h.descricao,
                       h.id_usuario,
                       h.data,
                       u.nome AS nome_usuario
                FROM tb_historico h
                LEFT JOIN tb_usuario u ON u.id_usuario = h.id_usuario
                WHERE 1=1
                """);

        if (dataInicio  != null) sql.append(" AND h.data >= ?");
        if (dataFim     != null) sql.append(" AND h.data <= ?");
        if (entidade    != null && !entidade.equals("Todas")) sql.append(" AND h.entidade_tipo = ?");
        if (acao        != null && !acao.equals("Todas"))     sql.append(" AND h.acao = ?");
        if (idUsuario   != null) sql.append(" AND h.id_usuario = ?");
        if (entidadeId  != null) sql.append(" AND h.entidade_id = ?");

        sql.append(" ORDER BY h.data DESC");

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int idx = 1;
            if (dataInicio  != null) ps.setTimestamp(idx++, Timestamp.valueOf(dataInicio.atStartOfDay()));
            if (dataFim     != null) ps.setTimestamp(idx++, Timestamp.valueOf(dataFim.atTime(23, 59, 59)));
            if (entidade    != null && !entidade.equals("Todas")) ps.setString(idx++, entidade);
            if (acao        != null && !acao.equals("Todas"))     ps.setString(idx++, acao);
            if (idUsuario   != null) ps.setInt(idx++, idUsuario);
            if (entidadeId  != null) ps.setInt(idx, entidadeId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Historico h = new Historico();
                h.setIdHistorico(rs.getInt("id_historico"));
                h.setEntidadeTipo(rs.getString("entidade_tipo"));
                h.setAcao(rs.getString("acao"));
                h.setEntidadeId(rs.getObject("entidade_id") != null ? rs.getInt("entidade_id") : null);
                h.setDescricao(rs.getString("descricao"));
                h.setIdUsuario(rs.getInt("id_usuario"));
                h.setNomeUsuario(rs.getString("nome_usuario"));
                Timestamp ts = rs.getTimestamp("data");
                if (ts != null) h.setData(ts.toLocalDateTime());
                lista.add(h);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar histórico: " + e.getMessage());
        }
        return lista;
    }

    // ── SELECT todos os usuários que têm histórico ────────────
    public static ObservableList<String[]> listarUsuariosComHistorico() {
        ObservableList<String[]> lista = FXCollections.observableArrayList();
        String sql = """
                SELECT DISTINCT u.id_usuario, u.nome
                FROM tb_historico h
                JOIN tb_usuario u ON u.id_usuario = h.id_usuario
                ORDER BY u.nome
                """;
        try (Connection con = ConexaoDB.getConexao();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new String[]{
                        String.valueOf(rs.getInt("id_usuario")),
                        rs.getString("nome")
                });
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar usuários do histórico: " + e.getMessage());
        }
        return lista;
    }
}