package api.DAO;

import api.connection.ConexaoDB;
import api.model.Movimentacao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;

public class movimentacaoDAO {

    // ── SELECT todas ──────────────────────────────────────────
    public static ObservableList<Movimentacao> listarTodas() {
        return listar(null, null, null);
    }

    // ── SELECT com filtros ────────────────────────────────────
    public static ObservableList<Movimentacao> listar(LocalDate dataInicio,
                                                      LocalDate dataFim,
                                                      String tipo) {
        ObservableList<Movimentacao> lista = FXCollections.observableArrayList();

        StringBuilder sql = new StringBuilder("""
                SELECT mv.id_movimentacao,
                       pr.produto            AS nome_produto,
                       mv.`tipo_movimentação`  AS tipo,
                       mv.quantidade,
                       u.nome                AS nome_usuario,
                       p.num_pedido,
                       nf.numero_nota,
                       mv.data,
                       mv.observacao
                FROM tb_movimentacao mv
                JOIN tb_produto      pr ON pr.id_produto = mv.id_produto
                JOIN tb_usuario       u ON u.id_usuario  = mv.id_usuario
                LEFT JOIN tb_pedido   p ON p.id_pedido   = mv.id_pedido
                LEFT JOIN tb_notasfiscal nf ON nf.id_nota = mv.id_nota
                WHERE 1=1
                """);

        if (dataInicio != null) sql.append(" AND mv.data >= ?");
        if (dataFim    != null) sql.append(" AND mv.data <= ?");
        if (tipo != null && !tipo.isBlank() && !tipo.equals("Todos"))
            sql.append(" AND mv.`tipo_movimentação` = ?");

        sql.append(" ORDER BY mv.data DESC");

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int idx = 1;
            if (dataInicio != null)
                ps.setTimestamp(idx++, Timestamp.valueOf(dataInicio.atStartOfDay()));
            if (dataFim != null)
                ps.setTimestamp(idx++, Timestamp.valueOf(dataFim.atTime(23, 59, 59)));
            if (tipo != null && !tipo.isBlank() && !tipo.equals("Todos"))
                ps.setString(idx, tipo);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new Movimentacao(
                        rs.getInt   ("id_movimentacao"),
                        rs.getString("nome_produto"),
                        rs.getString("tipo"),
                        rs.getInt   ("quantidade"),
                        rs.getString("nome_usuario"),
                        rs.getString("num_pedido"),
                        rs.getString("numero_nota"),
                        rs.getTimestamp("data").toLocalDateTime(),
                        rs.getString("observacao")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar movimentações: " + e.getMessage());
        }
        return lista;
    }

    // ── INSERT saída vinculada a pedido ───────────────────────
    public static boolean inserirSaida(int idProduto, int quantidade,
                                       int idUsuario, int idPedido, String observacao) {
        String sqlMov = """
                INSERT INTO tb_movimentacao
                    (id_produto, `tipo_movimentação`, quantidade, id_usuario, id_pedido, data, observacao)
                VALUES (?, 'SAÍDA', ?, ?, ?, NOW(), ?)
                """;
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sqlMov)) {
            ps.setInt   (1, idProduto);
            ps.setInt   (2, quantidade);
            ps.setInt   (3, idUsuario);
            ps.setInt   (4, idPedido);
            ps.setString(5, observacao);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao inserir saída: " + e.getMessage());
            return false;
        }
    }

    // ── INSERT saída manual (sem pedido) ──────────────────────
    public static boolean inserirSaidaManual(int idProduto, int quantidade,
                                             int idUsuario, String observacao) {
        String sqlEstoque = "UPDATE tb_produto SET saldo = saldo - ? WHERE id_produto = ?";
        String sqlMov     = """
                INSERT INTO tb_movimentacao
                    (id_produto, `tipo_movimentação`, quantidade, id_usuario, data, observacao)
                VALUES (?, 'SAIDA_MANUAL', ?, ?, NOW(), ?)
                """;
        try (Connection con = ConexaoDB.getConexao()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sqlEstoque)) {
                    ps.setInt(1, quantidade);
                    ps.setInt(2, idProduto);
                    ps.executeUpdate();
                }
                int idMov;
                try (PreparedStatement ps = con.prepareStatement(sqlMov,
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt   (1, idProduto);
                    ps.setInt   (2, quantidade);
                    ps.setInt   (3, idUsuario);
                    ps.setString(4, observacao);
                    ps.executeUpdate();
                    ResultSet keys = ps.getGeneratedKeys();
                    idMov = keys.next() ? keys.getInt(1) : -1;
                }
                con.commit();
                historicoDAO.registrar(
                        "Produto", "Saída", idProduto,
                        "Saída manual de " + quantidade + " unidade(s). Motivo: " + observacao);
                return true;
            } catch (SQLException e) {
                con.rollback();
                System.err.println("Erro ao inserir saída manual (rollback): " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Erro de conexão na saída manual: " + e.getMessage());
            return false;
        }
    }

    // ── INSERT entrada manual ─────────────────────────────────
    public static boolean inserirEntradaManual(int idProduto, int quantidade,
                                               int idUsuario, String observacao) {
        String sqlEstoque = "UPDATE tb_produto SET saldo = saldo + ? WHERE id_produto = ?";
        String sqlMov     = """
                INSERT INTO tb_movimentacao
                    (id_produto, `tipo_movimentação`, quantidade, id_usuario, data, observacao)
                VALUES (?, 'ENTRADA_MANUAL', ?, ?, NOW(), ?)
                """;
        try (Connection con = ConexaoDB.getConexao()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sqlEstoque)) {
                    ps.setInt(1, quantidade);
                    ps.setInt(2, idProduto);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(sqlMov,
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt   (1, idProduto);
                    ps.setInt   (2, quantidade);
                    ps.setInt   (3, idUsuario);
                    ps.setString(4, observacao);
                    ps.executeUpdate();
                }
                con.commit();
                historicoDAO.registrar(
                        "Produto", "Entrada", idProduto,
                        "Entrada manual de " + quantidade + " unidade(s). Motivo: " + observacao);
                return true;
            } catch (SQLException e) {
                con.rollback();
                System.err.println("Erro ao inserir entrada manual (rollback): " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Erro de conexão na entrada manual: " + e.getMessage());
            return false;
        }
    }
}