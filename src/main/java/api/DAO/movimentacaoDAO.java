package api.DAO;

import api.connection.ConexaoDB;
import api.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class movimentacaoDAO {

    // método de registrar saída do estoque para atendimento do pedido
    public static boolean registrarSaida(int idPedido, int idProduto,
                                         int idPedidoProduto, int quantidade,
                                         int idUsuario) {
        String sqlMovimentacao = """
                INSERT INTO tb_movimentacao
                    (id_produto, `tipo_movimentação`, quantidade,
                     id_usuario, id_pedido, data)
                VALUES (?, 'SAÍDA', ?, ?, ?, NOW())
                """;

        //reduz saldo do produto no estoque
        String sqlSaldo = """
                UPDATE tb_produto
                SET saldo = saldo - ?
                WHERE id_produto = ?
                """;

        //atualiza qtd_recebida do item do pedido
        String sqlItem = """
                UPDATE tb_pedido_produto
                SET qtd_recebida = qtd_recebida + ?
                WHERE id_pedido_produto = ?
                """;

        //verifica se todos os itens do pedido foram atendidos
        String sqlVerifica = """
                SELECT COUNT(*) AS total,
                       SUM(CASE WHEN qtd_recebida >= qtd_aprovada THEN 1 ELSE 0 END) AS atendidos
                FROM tb_pedido_produto
                WHERE id_pedido = ?
                """;

        //finaliza o pedido quando todos os itens estão atendidos
        String sqlFinaliza = """
                UPDATE tb_pedido SET status = 'FINALIZADO'
                WHERE id_pedido = ?
                """;

        //registra no histórico quando pedido é finalizado
        String sqlHistorico = """
                INSERT INTO tb_historico (entidade_tipo, acao, id_usuario, data)
                VALUES ('Pedido', 'Saída', ?, NOW())
                """;

        try (Connection con = ConexaoDB.getConexao()) {
            con.setAutoCommit(false);

            // registra movimentação de saída
            try (PreparedStatement ps = con.prepareStatement(sqlMovimentacao)) {
                ps.setInt(1, idProduto);
                ps.setInt(2, quantidade);
                ps.setInt(3, idUsuario);
                ps.setInt(4, idPedido);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(sqlSaldo)) {
                ps.setInt(1, quantidade);
                ps.setInt(2, idProduto);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(sqlItem)) {
                ps.setInt(1, quantidade);
                ps.setInt(2, idPedidoProduto);
                ps.executeUpdate();
            }

            // verifica se todos os itens foram atendidos
            boolean todosAtendidos = false;
            try (PreparedStatement ps = con.prepareStatement(sqlVerifica)) {
                ps.setInt(1, idPedido);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int total     = rs.getInt("total");
                    int atendidos = rs.getInt("atendidos");
                    todosAtendidos = total > 0 && total == atendidos;
                }
            }

            if (todosAtendidos) {
                try (PreparedStatement ps = con.prepareStatement(sqlFinaliza)) {
                    ps.setInt(1, idPedido);
                    ps.executeUpdate();
                }
                //registra no histórico
                try (PreparedStatement ps = con.prepareStatement(sqlHistorico)) {
                    ps.setInt(1, idUsuario);
                    ps.executeUpdate();
                }
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao registrar saída: " + e.getMessage());
            return false;
        }
    }

    // método de listar movimentações de um pedido
    public static ObservableList<Movimentacao> listarPorPedido(int idPedido) {
        ObservableList<Movimentacao> lista = FXCollections.observableArrayList();
        String sql = """
                SELECT m.*, p.produto, u.nome AS nome_usuario
                FROM tb_movimentacao m
                JOIN tb_produto  p ON m.id_produto  = p.id_produto
                JOIN tb_usuario  u ON m.id_usuario  = u.id_usuario
                WHERE m.id_pedido = ?
                ORDER BY m.data ASC
                """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPedido);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Produto prod = new Produto(
                        rs.getInt("id_produto"),
                        rs.getString("produto"),
                        "", "", 0, 0, "ATIVO", 0
                );
                Usuario usuario = new Usuario(
                        rs.getInt("id_usuario"),
                        rs.getString("nome_usuario"),
                        "", "", "", "ATIVO", new Perfil()
                );
                lista.add(new Movimentacao(
                        rs.getInt("id_movimentacao"),
                        prod,
                        rs.getString("tipo_movimentação"),
                        rs.getInt("quantidade"),
                        usuario,
                        rs.getTimestamp("data").toLocalDateTime(),
                        rs.getString("observacao")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar movimentações: " + e.getMessage());
        }
        return lista;
    }
}
