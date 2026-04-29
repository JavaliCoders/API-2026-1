package api.DAO;

import api.connection.ConexaoDB;
import api.model.*;
import javafx.collections.ObservableList;

import java.sql.*;

public class compraDAO {

    // método de registrar compra
    public static int registrar(Compra compra, ObservableList<CompraItem> itens) {
        String sqlCompra = """
                INSERT INTO tb_compra
                    (id_pedido, id_fornecedor, data, id_comprador,
                     valor_total, data_prevista, status)
                VALUES (?, ?, NOW(), ?, ?, ?, 'REALIZADA')
                """;

        String sqlItem = """
                INSERT INTO tb_compra_item
                    (id_compra, id_pedido_produto, valor_uni, qtd_comprada, valor_total)
                VALUES (?, ?, ?, ?, ?)
                """;

        // CA3: ao registrar a compra, o status do pedido muda para EM_COMPRA
        String sqlPedido = "UPDATE tb_pedido SET status = 'EM_COMPRA' WHERE id_pedido = ?";

        try (Connection con = ConexaoDB.getConexao()) {
            con.setAutoCommit(false);

            int idCompra;
            try (PreparedStatement ps = con.prepareStatement(sqlCompra,
                    Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt   (1, compra.getPedido().getIdPedido());
                ps.setInt   (2, compra.getFornecedor().getIdFornecedor());
                ps.setInt   (3, compra.getComprador().getIdUsuario());
                ps.setDouble(4, compra.getValorTotal());
                if (compra.getDataPrevista() != null)
                    ps.setTimestamp(5, Timestamp.valueOf(compra.getDataPrevista()));
                else
                    ps.setNull(5, Types.TIMESTAMP);
                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                if (!keys.next()) { con.rollback(); return -1; }
                idCompra = keys.getInt(1);
            }

            // CA4: registrar os produtos comprados em tb_compra_item
            try (PreparedStatement ps = con.prepareStatement(sqlItem)) {
                for (CompraItem item : itens) {
                    ps.setInt   (1, idCompra);
                    ps.setInt   (2, item.getPedidoProduto().getIdPedidoProduto());
                    ps.setDouble(3, item.getValorUni());
                    ps.setDouble(4, item.getQtdComprada());
                    ps.setDouble(5, item.getValorTotal());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            try (PreparedStatement ps = con.prepareStatement(sqlPedido)) {
                ps.setInt(1, compra.getPedido().getIdPedido());
                ps.executeUpdate();
            }

            con.commit();
            return idCompra;

        } catch (SQLException e) {
            System.err.println("Erro ao registrar compra: " + e.getMessage());
            return -1;
        }
    }

    // CA6: método de cancelar compra
    public static boolean cancelar(int idCompra) {
        String sql = "UPDATE tb_compra SET status = 'CANCELADA' WHERE id_compra = ?";

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCompra);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao cancelar compra: " + e.getMessage());
            return false;
        }
    }
}
