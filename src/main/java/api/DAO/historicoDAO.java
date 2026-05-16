package api.DAO;

import api.connection.ConexaoDB;
import api.model.SessaoUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class historicoDAO {

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

            if (entidadeId != null) {
                stmt.setInt(3, entidadeId);
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }

            stmt.setString(4, descricao);
            stmt.setInt(5, SessaoUsuario.getInstancia().getIdUsuarioLogado());

            stmt.executeUpdate();

        } catch (Exception e) {
            System.err.println("Erro ao registrar histórico: " + e.getMessage());
        }
    }
}