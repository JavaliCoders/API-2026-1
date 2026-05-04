package api.DAO;

import api.connection.ConexaoDB;
import api.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class compraDAO {

    // ── REGISTRAR nova compra ─────────────────────────────────────────────────
    public static int registrar(Compra compra, ObservableList<CompraItem> itens) {

        if (!pedidoTemCotacaoAprovada(compra.getPedido().getIdPedido())) {
            System.err.println("Pedido sem cotação aprovada.");
            return -1;
        }

        boolean temItemValido = itens.stream().anyMatch(i -> i.getQtdComprada() > 0);
        if (!temItemValido) {
            System.err.println("Nenhum item com quantidade informada.");
            return -2;
        }

        String sqlCompra = """
                INSERT INTO tb_compra
                (id_pedido, id_fornecedor, data, id_comprador, valor_total, data_prevista, status)
                VALUES (?, ?, NOW(), ?, ?, ?, 'REALIZADA')
                """;

        String sqlItem = """
                INSERT INTO tb_compra_item
                (id_compra, id_pedido_produto, valor_uni, qtd_comprada, valor_total)
                VALUES (?, ?, ?, ?, ?)
                """;

        String sqlUpdateQtd = """
                UPDATE tb_pedido_produto
                SET qtd_comprada = COALESCE(qtd_comprada, 0) + ?
                WHERE id_pedido_produto = ?
                """;

        try (Connection con = ConexaoDB.getConexao()) {
            con.setAutoCommit(false);

            int idCompra;

            try (PreparedStatement ps = con.prepareStatement(sqlCompra, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, compra.getPedido().getIdPedido());
                ps.setInt(2, compra.getFornecedor().getIdFornecedor());
                ps.setInt(3, compra.getComprador().getIdUsuario());
                ps.setDouble(4, compra.getValorTotal());

                if (compra.getDataPrevista() != null)
                    ps.setTimestamp(5, Timestamp.valueOf(compra.getDataPrevista()));
                else
                    ps.setNull(5, Types.TIMESTAMP);

                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (!rs.next()) { con.rollback(); return -1; }
                idCompra = rs.getInt(1);
            }

            try (PreparedStatement psItem = con.prepareStatement(sqlItem);
                 PreparedStatement psQtd  = con.prepareStatement(sqlUpdateQtd)) {

                for (CompraItem item : itens) {
                    if (item.getQtdComprada() <= 0) continue;

                    PedidoProduto pp = item.getPedidoProduto();
                    double pendente = pp.getQtdAprovada() - pp.getQtdComprada();
                    if (item.getQtdComprada() > pendente) {
                        System.err.println("Qtd excede pendente para: " + pp.getNomeProduto());
                        con.rollback();
                        return -3;
                    }

                    psItem.setInt(1, idCompra);
                    psItem.setInt(2, pp.getIdPedidoProduto());
                    psItem.setDouble(3, item.getValorUni());
                    psItem.setDouble(4, item.getQtdComprada());
                    psItem.setDouble(5, item.getValorTotal());
                    psItem.addBatch();

                    psQtd.setDouble(1, item.getQtdComprada());
                    psQtd.setInt(2, pp.getIdPedidoProduto());
                    psQtd.addBatch();
                }

                psItem.executeBatch();
                psQtd.executeBatch();
            }

            atualizarStatusPedido(con, compra.getPedido().getIdPedido());

            con.commit();
            return idCompra;

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    // ── CANCELAR compra — reverte qtd_comprada ────────────────────────────────
    public static boolean cancelar(int idCompra) {

        String sqlItens    = "SELECT id_pedido_produto, qtd_comprada FROM tb_compra_item WHERE id_compra = ?";
        String sqlReverter = """
                UPDATE tb_pedido_produto
                SET qtd_comprada = GREATEST(0, COALESCE(qtd_comprada, 0) - ?)
                WHERE id_pedido_produto = ?
                """;
        String sqlCancelar = "UPDATE tb_compra SET status = 'CANCELADA' WHERE id_compra = ?";

        try (Connection con = ConexaoDB.getConexao()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sqlItens)) {
                ps.setInt(1, idCompra);
                ResultSet rs = ps.executeQuery();

                try (PreparedStatement psRev = con.prepareStatement(sqlReverter)) {
                    while (rs.next()) {
                        psRev.setDouble(1, rs.getDouble("qtd_comprada"));
                        psRev.setInt(2, rs.getInt("id_pedido_produto"));
                        psRev.addBatch();
                    }
                    psRev.executeBatch();
                }
            }

            try (PreparedStatement ps = con.prepareStatement(sqlCancelar)) {
                ps.setInt(1, idCompra);
                ps.executeUpdate();
            }

            int idPedido = getPedidoByCompra(con, idCompra);
            if (idPedido > 0) atualizarStatusPedido(con, idPedido);

            con.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── LISTAR compras de um pedido (todas: realizadas e canceladas) ──────────
    public static ObservableList<Compra> listarPorPedido(int idPedido) {
        ObservableList<Compra> lista = FXCollections.observableArrayList();

        String sql = """
                SELECT c.*,
                       f.nome          AS nome_fornecedor,
                       u.nome          AS nome_comprador,
                       p.num_pedido
                FROM tb_compra c
                JOIN tb_fornecedor f ON f.id_fornecedor = c.id_fornecedor
                JOIN tb_usuario u    ON u.id_usuario    = c.id_comprador
                JOIN tb_pedido  p    ON p.id_pedido     = c.id_pedido
                WHERE c.id_pedido = ?
                ORDER BY c.id_compra ASC
                """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPedido);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Fornecedor f = new Fornecedor(rs.getInt("id_fornecedor"),
                        rs.getString("nome_fornecedor"), "", 0.0, "ATIVO");
                Usuario u = new Usuario(rs.getInt("id_comprador"),
                        rs.getString("nome_comprador"), "", "", "", "ATIVO", new Perfil());

                Pedido pedido = new Pedido();
                pedido.setNumPedidoSimples(rs.getString("num_pedido"));

                Timestamp tsPrev = rs.getTimestamp("data_prevista");

                lista.add(new Compra(
                        rs.getInt("id_compra"),
                        pedido, f,
                        rs.getTimestamp("data").toLocalDateTime(),
                        u,
                        rs.getDouble("valor_total"),
                        tsPrev != null ? tsPrev.toLocalDateTime() : null,
                        rs.getString("status")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    // ── LISTAR itens de uma compra ────────────────────────────────────────────
    public static ObservableList<CompraItem> listarItensPorCompra(int idCompra) {
        ObservableList<CompraItem> lista = FXCollections.observableArrayList();

        String sql = """
                SELECT ci.*, pp.id_pedido_produto, pp.qtd_solicitada, pp.qtd_aprovada,
                       pr.produto AS nome_produto, pr.unidade_medida, pr.valor_estimado
                FROM tb_compra_item ci
                JOIN tb_pedido_produto pp ON pp.id_pedido_produto = ci.id_pedido_produto
                JOIN tb_produto pr        ON pr.id_produto        = pp.id_produto
                WHERE ci.id_compra = ?
                """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCompra);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                PedidoProduto pp = new PedidoProduto(
                        rs.getInt("id_pedido_produto"),
                        rs.getString("nome_produto"),
                        rs.getString("unidade_medida"),
                        rs.getInt("qtd_solicitada"),
                        rs.getInt("qtd_aprovada"),
                        rs.getDouble("valor_estimado")
                );

                lista.add(new CompraItem(
                        rs.getInt("id_compra_item"),
                        idCompra,
                        pp,
                        rs.getDouble("valor_uni"),
                        rs.getDouble("qtd_comprada"),
                        rs.getDouble("valor_total")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    // ── QTD comprada por pedido-produto (excluindo canceladas) ────────────────
    public static double getQtdCompradaPorPedidoProduto(int idPedidoProduto) {
        String sql = """
                SELECT COALESCE(SUM(ci.qtd_comprada), 0)
                FROM tb_compra_item ci
                JOIN tb_compra c ON c.id_compra = ci.id_compra
                WHERE ci.id_pedido_produto = ?
                AND c.status <> 'CANCELADA'
                """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPedidoProduto);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ── Verifica se pedido tem ao menos uma cotação aprovada ──────────────────
    public static boolean pedidoTemCotacaoAprovada(int idPedido) {
        String sql = """
                SELECT 1
                FROM tb_cotacao_item ci
                JOIN tb_pedido_produto pp ON pp.id_pedido_produto = ci.id_pedido_produto
                WHERE pp.id_pedido = ?
                AND ci.aprovado = true
                LIMIT 1
                """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            return ps.executeQuery().next();
        } catch (Exception e) {
            return false;
        }
    }

    // ── Verifica se ainda há itens com quantidade pendente de compra ──────────
    public static boolean pedidoTemItensPendentes(int idPedido) {
        String sql = """
                SELECT 1
                FROM tb_pedido_produto pp
                WHERE pp.id_pedido = ?
                  AND pp.qtd_aprovada > 0
                  AND COALESCE(pp.qtd_comprada, 0) < pp.qtd_aprovada
                LIMIT 1
                """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            return ps.executeQuery().next();
        } catch (Exception e) {
            return false;
        }
    }

    // ── LISTAR compras REALIZADAS que ainda não têm nota fiscal ───────────────
    // CORRIGIDO: não lê cnpj/pedido_minimo/status que não estão no SELECT
    public static ObservableList<Compra> listarRealizadasSemNota() {
        ObservableList<Compra> lista = FXCollections.observableArrayList();

        String sql = """
                SELECT c.id_compra,
                       c.id_pedido,
                       c.id_fornecedor,
                       c.data,
                       c.valor_total,
                       c.data_prevista,
                       c.status,
                       f.nome   AS nome_fornecedor,
                       p.num_pedido,
                       u.nome   AS nome_comprador,
                       u.id_usuario AS id_comprador
                FROM tb_compra c
                JOIN tb_fornecedor f ON f.id_fornecedor = c.id_fornecedor
                JOIN tb_pedido     p ON p.id_pedido     = c.id_pedido
                JOIN tb_usuario    u ON u.id_usuario    = c.id_comprador
                WHERE c.status = 'REALIZADA'
                  AND NOT EXISTS (
                      SELECT 1 FROM tb_notasfiscal nf
                      WHERE nf.id_compra = c.id_compra
                  )
                ORDER BY c.id_compra DESC
                """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Fornecedor só com campos disponíveis no SELECT (sem cnpj/pedido_minimo)
                Fornecedor f = new Fornecedor(
                        rs.getInt("id_fornecedor"),
                        rs.getString("nome_fornecedor"),
                        "", 0.0, "ATIVO"
                );

                Usuario comprador = new Usuario(
                        rs.getInt("id_comprador"),
                        rs.getString("nome_comprador"),
                        "", "", "", "ATIVO", new Perfil()
                );

                Pedido pedido = new Pedido();
                pedido.setIdPedidoSimples(rs.getInt("id_pedido"));
                pedido.setNumPedidoSimples(rs.getString("num_pedido"));

                Timestamp tsPrev = rs.getTimestamp("data_prevista");

                lista.add(new Compra(
                        rs.getInt("id_compra"),
                        pedido, f,
                        rs.getTimestamp("data").toLocalDateTime(),
                        comprador,
                        rs.getDouble("valor_total"),
                        tsPrev != null ? tsPrev.toLocalDateTime() : null,
                        rs.getString("status")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    // ── Atualiza status do pedido com base na qtd_comprada ────────────────────
    private static void atualizarStatusPedido(Connection con, int idPedido) throws SQLException {
        String sql = """
                UPDATE tb_pedido
                SET status = (
                    CASE
                        WHEN NOT EXISTS (
                            SELECT 1 FROM tb_pedido_produto
                            WHERE id_pedido = ? AND qtd_aprovada > 0
                        ) THEN status
                        WHEN EXISTS (
                            SELECT 1 FROM tb_pedido_produto
                            WHERE id_pedido = ?
                            AND qtd_aprovada > 0
                            AND COALESCE(qtd_comprada, 0) < qtd_aprovada
                        ) THEN 'EM_COMPRA'
                        ELSE 'FINALIZADO'
                    END
                )
                WHERE id_pedido = ?
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            ps.setInt(2, idPedido);
            ps.setInt(3, idPedido);
            ps.executeUpdate();
        }
    }

    private static int getPedidoByCompra(Connection con, int idCompra) throws SQLException {
        String sql = "SELECT id_pedido FROM tb_compra WHERE id_compra = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCompra);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public static ObservableList<Compra> listarTodas() {
        ObservableList<Compra> lista = FXCollections.observableArrayList();

        String sql = """
                SELECT c.*,
                       f.nome         AS nome_fornecedor,
                       u.nome         AS nome_comprador,
                       p.num_pedido
                FROM tb_compra c
                JOIN tb_fornecedor f ON f.id_fornecedor = c.id_fornecedor
                JOIN tb_usuario u    ON u.id_usuario    = c.id_comprador
                JOIN tb_pedido  p    ON p.id_pedido     = c.id_pedido
                ORDER BY c.id_compra DESC
                """;

        try (Connection con = ConexaoDB.getConexao();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Fornecedor f = new Fornecedor(rs.getInt("id_fornecedor"),
                        rs.getString("nome_fornecedor"), "", 0.0, "ATIVO");
                Usuario u = new Usuario(rs.getInt("id_comprador"),
                        rs.getString("nome_comprador"), "", "", "", "ATIVO", new Perfil());

                Pedido pedido = new Pedido();
                pedido.setNumPedidoSimples(rs.getString("num_pedido"));
                pedido.setIdPedidoSimples(rs.getInt("id_pedido"));

                Timestamp tsPrev = rs.getTimestamp("data_prevista");

                lista.add(new Compra(
                        rs.getInt("id_compra"),
                        pedido, f,
                        rs.getTimestamp("data").toLocalDateTime(),
                        u,
                        rs.getDouble("valor_total"),
                        tsPrev != null ? tsPrev.toLocalDateTime() : null,
                        rs.getString("status")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }
}