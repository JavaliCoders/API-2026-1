package api.DAO;

import api.connection.ConexaoDB;
import api.model.Cotacao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CotacaoDAO {

    // Inserir
    public static void inserir(Cotacao c) {

        String sql = """
            INSERT INTO tb_cotacao
            (status, data_criacao, data_aprovacao, parecer,
             id_aprovador, valor_total, id_pedido, id_fornecedor, id_anexo)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // status
            ps.setString(1, c.getStatus());

            // data_criacao
            ps.setTimestamp(2, Timestamp.valueOf(c.getDataCriacao()));

            // data_aprovacao (pode ser null)
            if (c.getDataAprovacao() != null) {
                ps.setTimestamp(3, Timestamp.valueOf(c.getDataAprovacao()));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }

            // parecer (pode ser null)
            if (c.getParecer() != null) {
                ps.setString(4, c.getParecer());
            } else {
                ps.setNull(4, Types.VARCHAR);
            }

            // id_aprovador (pode ser null)
            if (c.getAprovador() != null) {
                ps.setInt(5, c.getAprovador().getIdUsuario());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            // valor_total
            ps.setDouble(6, c.getValorTotal());

            // FKs obrigatórias
            ps.setInt(7, c.getPedido().getIdPedido());
            ps.setInt(8, c.getFornecedor().getIdFornecedor());
            ps.setInt(9, c.getAnexo().getIdAnexo());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Listar
    public static List<Cotacao> listar() {

        List<Cotacao> lista = new ArrayList<>();

        String sql = "SELECT * FROM tb_cotacao";

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Cotacao c = new Cotacao(
                        rs.getInt("id_cotacao"),
                        null, // pedido (carregar depois se quiser)
                        null, // fornecedor
                        null, // aprovador
                        null, // anexo
                        rs.getTimestamp("data_criacao").toLocalDateTime(),
                        rs.getTimestamp("data_aprovacao") != null ?
                                rs.getTimestamp("data_aprovacao").toLocalDateTime() : null,
                        rs.getString("status"),
                        rs.getString("parecer"),
                        rs.getDouble("valor_total")
                );

                lista.add(c);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return lista;
    }

    // Deletar
    public static void excluir(int id) {

        String sql = "DELETE FROM tb_cotacao WHERE id_cotacao = ?";

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Atualizar os status
    public static void atualizarStatus(int id, String status, String parecer, Integer idAprovador) {

        String sql = """
            UPDATE tb_cotacao
            SET status = ?, parecer = ?, id_aprovador = ?, data_aprovacao = ?
            WHERE id_cotacao = ?
        """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);

            if (parecer != null) {
                ps.setString(2, parecer);
            } else {
                ps.setNull(2, Types.VARCHAR);
            }

            if (idAprovador != null) {
                ps.setInt(3, idAprovador);
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setTimestamp(4, Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setInt(5, id);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}