package api.DAO;

import api.connection.ConexaoDB;
import api.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;

public class cotacaoDAO {

    // ── INSERT cotação + anexo em transação única ─────────────
    public static int inserir(double valorTotal, int idPedido,
                              int idFornecedor,
                              String nomeArquivo, String caminhoArquivo) {
        Connection con = null;
        try {
            con = ConexaoDB.getConexao();
            con.setAutoCommit(false);

            // 1. Insere o anexo em tb_anexo
            int idAnexo = inserirAnexo(con, nomeArquivo, caminhoArquivo);
            if (idAnexo == -1) { con.rollback(); return -1; }

            // 2. Insere a cotação referenciando o anexo
            String sql = """
                    INSERT INTO tb_cotacao
                        (status, data_criacao, valor_total,
                         id_pedido, id_fornecedor, id_anexo)
                    VALUES ('AGUARDANDO_APROVACAO', NOW(), ?, ?, ?, ?)
                    """;
            try (PreparedStatement ps = con.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS)) {

                ps.setDouble(1, valorTotal);
                ps.setInt   (2, idPedido);
                ps.setInt   (3, idFornecedor);
                ps.setInt   (4, idAnexo);
                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    int id = keys.getInt(1);
                    con.commit();
                    return id;
                }
            }
            con.rollback();

        } catch (SQLException e) {
            System.err.println("Erro ao inserir cotação: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ignored) {}
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } }
            catch (SQLException ignored) {}
        }
        return -1;
    }

    // ── INSERT tb_anexo dentro de transação existente ─────────
    private static int inserirAnexo(Connection con,
                                    String nomeArq,
                                    String caminho) throws SQLException {
        String sql = """
                INSERT INTO tb_anexo (tipo, nome_arq, caminho_arquivo, data_upload)
                VALUES ('COTACAO', ?, ?, NOW())
                """;
        try (PreparedStatement ps = con.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nomeArq);
            ps.setString(2, caminho);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        return -1;
    }

    // ── SELECT todas as cotações ──────────────────────────────
    public static ObservableList<Cotacao> listarTodas() {
        return listar(null);
    }

    // ── SELECT cotações de um pedido ──────────────────────────
    public static ObservableList<Cotacao> listarPorPedido(int idPedido) {
        return listar(idPedido);
    }

    private static ObservableList<Cotacao> listar(Integer filtroIdPedido) {
        ObservableList<Cotacao> lista = FXCollections.observableArrayList();
        String where = filtroIdPedido != null ? "WHERE c.id_pedido = ?" : "";
        String sql = """
                SELECT c.*,
                       p.num_pedido,
                       f.nome        AS nome_fornecedor,
                       f.cnpj        AS cnpj_fornecedor,
                       f.pedido_minimo,
                       f.status      AS status_fornecedor,
                       an.id_anexo   AS id_anexo_col,
                       an.nome_arq,
                       an.caminho_arquivo,
                       ua.nome       AS nome_aprovador
                FROM tb_cotacao c
                JOIN tb_pedido      p  ON c.id_pedido     = p.id_pedido
                JOIN tb_fornecedor  f  ON c.id_fornecedor = f.id_fornecedor
                JOIN tb_anexo       an ON c.id_anexo      = an.id_anexo
                LEFT JOIN tb_usuario ua ON c.id_aprovador = ua.id_usuario
                """ + where + "\nORDER BY c.data_criacao DESC";

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (filtroIdPedido != null) ps.setInt(1, filtroIdPedido);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            System.err.println("Erro ao listar cotações: " + e.getMessage());
        }
        return lista;
    }

    // ── UPDATE — aprovar cotação ──────────────────────────────
    public static boolean aprovar(int idCotacao, int idAprovador, String parecer) {
        return updateStatus(idCotacao, "APROVADO", idAprovador, parecer);
    }

    // ── UPDATE — negar cotação ────────────────────────────────
    public static boolean negar(int idCotacao, int idAprovador, String parecer) {
        return updateStatus(idCotacao, "NEGADO", idAprovador, parecer);
    }

    private static boolean updateStatus(int idCotacao, String novoStatus,
                                        int idAprovador, String parecer) {
        String sql = """
                UPDATE tb_cotacao
                SET status         = ?,
                    id_aprovador   = ?,
                    data_aprovacao = NOW(),
                    parecer        = ?
                WHERE id_cotacao   = ?
                """;
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, novoStatus);
            ps.setInt   (2, idAprovador);
            ps.setString(3, parecer);
            ps.setInt   (4, idCotacao);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar cotação: " + e.getMessage());
            return false;
        }
    }

    // ── UPDATE status do pedido para EM_COTACAO ───────────────
    public static boolean marcarPedidoEmCotacao(int idPedido) {
        return updateStatusPedido(idPedido, "EM_COTACAO");
    }

    private static boolean updateStatusPedido(int idPedido, String status) {
        String sql = "UPDATE tb_pedido SET status = ? WHERE id_pedido = ?";
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt   (2, idPedido);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar status do pedido: " + e.getMessage());
            return false;
        }
    }

    // ── Mapper ────────────────────────────────────────────────
    private static Cotacao mapear(ResultSet rs) throws SQLException {
        Fornecedor fornecedor = new Fornecedor(
                rs.getInt   ("id_fornecedor"),
                rs.getString("nome_fornecedor"),
                rs.getString("cnpj_fornecedor"),
                rs.getDouble("pedido_minimo"),
                rs.getString("status_fornecedor")
        );

        Usuario aprovador = null;
        int idAprov = rs.getInt("id_aprovador");
        if (!rs.wasNull()) {
            aprovador = new Usuario(idAprov,
                    rs.getString("nome_aprovador"),
                    "", "", "", "ATIVO", new Perfil());
        }

        LocalDateTime dataAprov = null;
        Timestamp ts = rs.getTimestamp("data_aprovacao");
        if (ts != null) dataAprov = ts.toLocalDateTime();

        return new Cotacao(
                rs.getInt   ("id_cotacao"),
                rs.getString("status"),
                rs.getTimestamp("data_criacao").toLocalDateTime(),
                dataAprov,
                rs.getString("parecer"),
                aprovador,
                rs.getDouble("valor_total"),
                rs.getInt   ("id_pedido"),
                rs.getString("num_pedido"),
                fornecedor,
                rs.getInt   ("id_anexo_col"),
                rs.getString("nome_arq"),
                rs.getString("caminho_arquivo")
        );
    }
}