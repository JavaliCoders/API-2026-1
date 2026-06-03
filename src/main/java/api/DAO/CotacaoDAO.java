package api.DAO;

import api.connection.ConexaoDB;
import api.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;

public class CotacaoDAO {

    // ── INSERT cotação + anexo + itens em transação única ─────
    public static int inserir(double valorTotal, int idPedido,
                              int idFornecedor, int idCadastrador,
                              String nomeArquivo, String caminhoArquivo,
                              ObservableList<CotacaoItem> itens) {
        Connection con = null;
        try {
            con = ConexaoDB.getConexao();
            con.setAutoCommit(false);

            int idAnexo = inserirAnexo(con, nomeArquivo, caminhoArquivo);
            if (idAnexo == -1) { con.rollback(); return -1; }

            String sql = """
                    INSERT INTO tb_cotacao
                        (status, data_criacao, valor_total,
                         id_pedido, id_fornecedor, id_anexo, id_cadastrador)
                    VALUES ('AGUARDANDO_APROVACAO', NOW(), ?, ?, ?, ?, ?)
                    """;
            int idCotacao = -1;
            try (PreparedStatement ps = con.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setDouble(1, valorTotal);
                ps.setInt   (2, idPedido);
                ps.setInt   (3, idFornecedor);
                ps.setInt   (4, idAnexo);
                ps.setInt   (5, idCadastrador);
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) idCotacao = keys.getInt(1);
            }

            if (idCotacao == -1) { con.rollback(); return -1; }

            inserirItens(con, idCotacao, itens);
            con.commit();
            return idCotacao;

        } catch (SQLException e) {
            System.err.println("Erro ao inserir cotação: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ignored) {}
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } }
            catch (SQLException ignored) {}
        }
        return -1;
    }

    // ── UPDATE cotação (edição pelo cadastrador) ───────────────
    public static boolean atualizar(int idCotacao, double valorTotal,
                                    int idFornecedor,
                                    String nomeArquivo, String caminhoArquivo,
                                    ObservableList<CotacaoItem> itens) {
        Connection con = null;
        try {
            con = ConexaoDB.getConexao();
            con.setAutoCommit(false);

            // Atualiza anexo apenas se um novo arquivo foi fornecido
            if (nomeArquivo != null && !nomeArquivo.isBlank()) {
                // Busca id_anexo atual
                int idAnexo = -1;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT id_anexo FROM tb_cotacao WHERE id_cotacao = ?")) {
                    ps.setInt(1, idCotacao);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) idAnexo = rs.getInt(1);
                }
                if (idAnexo != -1) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE tb_anexo SET nome_arq=?, caminho_arquivo=? WHERE id_anexo=?")) {
                        ps.setString(1, nomeArquivo);
                        ps.setString(2, caminhoArquivo);
                        ps.setInt   (3, idAnexo);
                        ps.executeUpdate();
                    }
                }
            }

            // Atualiza cotação
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE tb_cotacao SET valor_total=?, id_fornecedor=? WHERE id_cotacao=?")) {
                ps.setDouble(1, valorTotal);
                ps.setInt   (2, idFornecedor);
                ps.setInt   (3, idCotacao);
                ps.executeUpdate();
            }

            // Remove itens antigos e reinsere
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM tb_cotacao_item WHERE id_cotacao = ?")) {
                ps.setInt(1, idCotacao);
                ps.executeUpdate();
            }
            inserirItens(con, idCotacao, itens);

            con.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar cotação: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ignored) {}
            return false;
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } }
            catch (SQLException ignored) {}
        }
    }

    // ── INSERT itens da cotação ────────────────────────────────
    private static void inserirItens(Connection con, int idCotacao,
                                     ObservableList<CotacaoItem> itens)
            throws SQLException {
        if (itens == null || itens.isEmpty()) return;
        String sql = """
                INSERT INTO tb_cotacao_item
                    (id_cotacao, id_pedido_produto, qtd_cotada, valor_unitario, valor_total)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (CotacaoItem item : itens) {
                ps.setInt   (1, idCotacao);
                ps.setInt   (2, item.getIdPedidoProduto());
                ps.setInt   (3, item.getQtdCotada());
                ps.setDouble(4, item.getValorUnitario());
                ps.setDouble(5, item.getValorTotal());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // ── SELECT itens de uma cotação ───────────────────────────
    public static ObservableList<CotacaoItem> listarItens(int idCotacao) {
        ObservableList<CotacaoItem> lista = FXCollections.observableArrayList();
        String sql = """
                SELECT ci.id_cotacao_item, ci.id_pedido_produto,
                       pr.produto AS nome_produto, pr.unidade_medida,
                       ci.qtd_cotada, ci.valor_unitario, ci.valor_total
                FROM tb_cotacao_item ci
                JOIN tb_pedido_produto pp ON ci.id_pedido_produto = pp.id_pedido_produto
                JOIN tb_produto pr        ON pp.id_produto        = pr.id_produto
                WHERE ci.id_cotacao = ?
                """;
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCotacao);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new CotacaoItem(
                        rs.getInt   ("id_cotacao_item"),
                        rs.getInt   ("id_pedido_produto"),
                        rs.getString("nome_produto"),
                        rs.getString("unidade_medida"),
                        rs.getInt   ("qtd_cotada"),
                        rs.getDouble("valor_unitario"),
                        rs.getDouble("valor_total")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar itens da cotação: " + e.getMessage());
        }
        return lista;
    }

    // ── SELECT todas as cotações ──────────────────────────────
    public static ObservableList<Cotacao> listarTodas() { return listar(null); }

    public static ObservableList<Cotacao> listarPorPedido(int idPedido) {
        return listar(idPedido);
    }

    private static ObservableList<Cotacao> listar(Integer filtroIdPedido) {
        ObservableList<Cotacao> lista = FXCollections.observableArrayList();
        String where = filtroIdPedido != null ? "WHERE c.id_pedido = ?" : "";
        String sql = """
            SELECT c.*,
                   p.num_pedido,
                   f.nome          AS nome_fornecedor,
                   f.cnpj          AS cnpj_fornecedor,
                   f.pedido_minimo,
                   f.status        AS status_fornecedor,
                   an.id_anexo     AS id_anexo_col,
                   an.nome_arq,
                   an.caminho_arquivo,
                   ua.nome         AS nome_aprovador,
                   uc.nome         AS nome_cadastrador
            FROM tb_cotacao c
            JOIN tb_pedido      p  ON c.id_pedido      = p.id_pedido
            JOIN tb_fornecedor  f  ON c.id_fornecedor  = f.id_fornecedor
            JOIN tb_anexo       an ON c.id_anexo       = an.id_anexo
            LEFT JOIN tb_usuario ua ON c.id_aprovador  = ua.id_usuario
            LEFT JOIN tb_usuario uc ON c.id_cadastrador = uc.id_usuario
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

    // ── UPDATE aprovar / negar ────────────────────────────────
    public static boolean aprovar(int idCotacao, int idAprovador, String parecer) {
        return updateStatus(idCotacao, "APROVADO", idAprovador, parecer);
    }

    public static boolean negar(int idCotacao, int idAprovador, String parecer) {
        return updateStatus(idCotacao, "NEGADO", idAprovador, parecer);
    }

    private static boolean updateStatus(int idCotacao, String novoStatus,
                                        int idAprovador, String parecer) {
        String sql = """
                UPDATE tb_cotacao
                SET status=?, id_aprovador=?, data_aprovacao=NOW(), parecer=?
                WHERE id_cotacao=?
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
            System.err.println("Erro ao atualizar status da cotação: " + e.getMessage());
            return false;
        }
    }

    public static boolean marcarPedidoEmCotacao(int idPedido) {
        String sql = "UPDATE tb_pedido SET status='EM_COTACAO' WHERE id_pedido=?";
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar status do pedido: " + e.getMessage());
            return false;
        }
    }

    // ── INSERT tb_anexo dentro de transação existente ─────────
    private static int inserirAnexo(Connection con,
                                    String nomeArq, String caminho) throws SQLException {
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

        int idCadastrador = rs.getInt("id_cadastrador");
        if (rs.wasNull()) idCadastrador = 0;

        String nomeRegistradoPor = rs.getString("nome_cadastrador");

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
                rs.getString("caminho_arquivo"),
                idCadastrador,
                nomeRegistradoPor  // ← NOVO
        );
    }
}