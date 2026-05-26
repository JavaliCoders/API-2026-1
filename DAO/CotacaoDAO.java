package api.DAO;

import api.connection.ConexaoDB;
import api.model.Anexo;
import api.model.CentroCusto;
import api.model.Cotacao;
import api.model.Fornecedor;
import api.model.Pedido;
import api.model.Perfil;
import api.model.Setor;
import api.model.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class CotacaoDAO {

    public static ObservableList<Cotacao> listarTodos() {
        ObservableList<Cotacao> lista = FXCollections.observableArrayList();
        String sql = """
                SELECT c.*,
                       p.num_pedido, p.data_abertura, p.status AS pedido_status,
                       p.valor_total_estimado, p.id_solicitante, p.id_centrocusto, p.id_setor,
                       u.nome AS nome_solicitante,
                       cc.centro_custo,
                       s.setor,
                       f.nome AS fornecedor_nome, f.cnpj, f.tipo_pagamento,
                       f.pedido_minimo, f.status AS fornecedor_status,
                       ap.id_usuario AS id_aprovador_real,
                       ap.nome AS nome_aprovador,
                       a.tipo AS anexo_tipo,
                       a.nome_arq,
                       a.caminho_arquivo,
                       a.data_upload
                FROM tb_cotacao c
                JOIN tb_pedido p        ON c.id_pedido = p.id_pedido
                JOIN tb_usuario u       ON p.id_solicitante = u.id_usuario
                JOIN tb_centrocusto cc  ON p.id_centrocusto = cc.id_centrocusto
                JOIN tb_setor s         ON p.id_setor = s.id_setor
                JOIN tb_fornecedor f    ON c.id_fornecedor = f.id_fornecedor
                LEFT JOIN tb_usuario ap ON c.id_aprovador = ap.id_usuario
                LEFT JOIN tb_anexo a    ON c.id_anexo = a.id_anexo
                ORDER BY c.id_cotacao ASC
                """;

        try (Connection con = ConexaoDB.getConexao();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            System.err.println("Erro ao listar cotações: " + e.getMessage());
        }

        return lista;
    }

    public static boolean atualizarStatus(int idCotacao, int idAprovador, String status, String parecer) {
        Connection con = ConexaoDB.getConexao();
        if (con == null) return false;

        boolean autoCommitOriginal;
        try {
            autoCommitOriginal = con.getAutoCommit();
            con.setAutoCommit(false);

            String sqlCotacao = """
                    UPDATE tb_cotacao
                    SET status = ?,
                        id_aprovador = ?,
                        data_aprovacao = NOW(),
                        parecer = ?
                    WHERE id_cotacao = ?
                    """;

            try (PreparedStatement ps = con.prepareStatement(sqlCotacao)) {
                ps.setString(1, status);
                ps.setInt(2, idAprovador);
                ps.setString(3, parecer);
                ps.setInt(4, idCotacao);
                ps.executeUpdate();
            }

            String statusPedido = statusPedidoParaCotacao(status);
            if (statusPedido != null) {
                String sqlPedido = """
                        UPDATE tb_pedido p
                        JOIN tb_cotacao c ON c.id_pedido = p.id_pedido
                        SET p.status = ?
                        WHERE c.id_cotacao = ?
                        """;

                try (PreparedStatement ps = con.prepareStatement(sqlPedido)) {
                    ps.setString(1, statusPedido);
                    ps.setInt(2, idCotacao);
                    ps.executeUpdate();
                }
            }

            con.commit();
            con.setAutoCommit(autoCommitOriginal);
            return true;

        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException rollbackErro) {
                System.err.println("Erro ao desfazer atualização da cotação: " + rollbackErro.getMessage());
            }
            System.err.println("Erro ao atualizar cotação: " + e.getMessage());
            return false;
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar conexão da cotação: " + e.getMessage());
            }
        }
    }

    private static Cotacao mapear(ResultSet rs) throws SQLException {
        Usuario solicitante = new Usuario(
                rs.getInt("id_solicitante"),
                rs.getString("nome_solicitante"),
                "", "", "", "ATIVO",
                new Perfil()
        );

        CentroCusto centroCusto = new CentroCusto(
                rs.getInt("id_centrocusto"),
                rs.getString("centro_custo")
        );

        Setor setor = new Setor(
                rs.getInt("id_setor"),
                rs.getString("setor")
        );

        Pedido pedido = new Pedido(
                rs.getInt("id_pedido"),
                rs.getString("num_pedido"),
                toLocalDateTime(rs.getTimestamp("data_abertura")),
                rs.getString("pedido_status"),
                rs.getDouble("valor_total_estimado"),
                solicitante,
                centroCusto,
                setor
        );

        Fornecedor fornecedor = new Fornecedor(
                rs.getInt("id_fornecedor"),
                rs.getString("fornecedor_nome"),
                rs.getString("cnpj"),
                rs.getString("tipo_pagamento"),
                rs.getDouble("pedido_minimo"),
                rs.getString("fornecedor_status")
        );

        Usuario aprovador = null;
        int idAprovador = rs.getInt("id_aprovador_real");
        if (!rs.wasNull()) {
            aprovador = new Usuario(
                    idAprovador,
                    rs.getString("nome_aprovador"),
                    "", "", "", "ATIVO",
                    new Perfil()
            );
        }

        Anexo anexo = null;
        int idAnexo = rs.getInt("id_anexo");
        if (!rs.wasNull()) {
            anexo = new Anexo(
                    idAnexo,
                    rs.getString("anexo_tipo"),
                    rs.getString("nome_arq"),
                    rs.getString("caminho_arquivo"),
                    toLocalDateTime(rs.getTimestamp("data_upload"))
            );
        }

        return new Cotacao(
                rs.getInt("id_cotacao"),
                pedido,
                fornecedor,
                aprovador,
                anexo,
                toLocalDateTime(rs.getTimestamp("data_criacao")),
                toLocalDateTime(rs.getTimestamp("data_aprovacao")),
                rs.getString("status"),
                rs.getString("parecer"),
                rs.getDouble("valor_total")
        );
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private static String statusPedidoParaCotacao(String statusCotacao) {
        return switch (statusCotacao) {
            case "APROVADO", "APROVADO_PARCIALMENTE" -> "EM_COMPRA";
            case "NEGADO" -> "EM_COTACAO";
            default -> null;
        };
    }
}
