package api.DAO;

import api.connection.ConexaoDB;
import api.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class pedidoDAO {

    // ── INSERT pedido ─────────────────────────────────────────
    public static int inserir(Pedido pedido) {
        String sql = """
                INSERT INTO tb_pedido
                    (num_pedido, data_abertura, status, valor_total_estimado,
                     id_solicitante, id_centrocusto, id_setor)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString   (1, pedido.getNumPedido());
            ps.setTimestamp(2, Timestamp.valueOf(pedido.getDataAbertura()));
            ps.setString   (3, pedido.getStatus());
            ps.setDouble   (4, pedido.getValorTotalEstimado());
            ps.setInt      (5, pedido.getSolicitante().getIdUsuario());
            ps.setInt      (6, pedido.getCentroCusto().getIdCentroCusto());
            ps.setInt      (7, pedido.getSetor().getIdSetor());
            ps.executeUpdate();

            // Retorna o ID gerado para inserir os produtos
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);

        } catch (SQLException e) {
            System.err.println("Erro ao inserir pedido: " + e.getMessage());
        }
        return -1;
    }

    // ── INSERT itens do pedido ────────────────────────────────
    public static boolean inserirItens(int idPedido,
                                       ObservableList<PedidoProduto> itens) {
        String sql = """
                INSERT INTO tb_pedido_produto
                    (id_pedido, id_produto, qtd_solicitada)
                VALUES (?, ?, ?)
                """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (PedidoProduto item : itens) {
                ps.setInt(1, idPedido);
                ps.setInt(2, item.getProduto().getIdProduto());
                ps.setInt(3, item.getQtdSolicitada());
                ps.addBatch();
            }
            ps.executeBatch();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao inserir itens: " + e.getMessage());
            return false;
        }
    }

    // ── SELECT todos os pedidos ───────────────────────────────
    public static ObservableList<Pedido> listarTodos() {
        ObservableList<Pedido> lista = FXCollections.observableArrayList();
        String sql = """
                SELECT p.*, u.nome AS nome_usuario,
                       cc.centro_custo, s.setor,
                       ap.nome AS nome_aprovador
                FROM tb_pedido p
                JOIN tb_usuario u      ON p.id_solicitante  = u.id_usuario
                JOIN tb_centrocusto cc ON p.id_centrocusto  = cc.id_centrocusto
                JOIN tb_setor s        ON p.id_setor        = s.id_setor
                LEFT JOIN tb_usuario ap ON p.id_aprovador   = ap.id_usuario
                ORDER BY p.id_pedido ASC
                """;

        try (Connection con = ConexaoDB.getConexao();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            System.err.println("Erro ao listar pedidos: " + e.getMessage());
        }
        return lista;
    }

    // ── SELECT itens de um pedido ─────────────────────────────
    public static ObservableList<PedidoProduto> listarItens(int idPedido) {
        ObservableList<PedidoProduto> lista = FXCollections.observableArrayList();
        String sql = """
                SELECT pp.*, pr.produto, pr.unidade_medida, pr.valor_estimado
                FROM tb_pedido_produto pp
                JOIN tb_produto pr ON pp.id_produto = pr.id_produto
                WHERE pp.id_pedido = ?
                """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPedido);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Produto prod = new Produto(
                        rs.getInt   ("id_produto"),
                        rs.getString("produto"),
                        "",
                        rs.getString("unidade_medida"),
                        0,
                        rs.getDouble("valor_estimado"),
                        "ATIVO",
                        0
                );
                lista.add(new PedidoProduto(
                        rs.getInt("id_pedido_produto"),
                        idPedido,
                        prod,
                        rs.getInt("qtd_solicitada"),
                        rs.getInt("qtd_aprovada"),
                        rs.getInt("qtd_recebida")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar itens: " + e.getMessage());
        }
        return lista;
    }

    // ── Gera número do pedido automático ──────────────────────
    public static String gerarNumeroPedido() {
        String sql = "SELECT COUNT(*) FROM tb_pedido";
        try (Connection con = ConexaoDB.getConexao();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                int total = rs.getInt(1) + 1;
                return String.format("PED-%04d", total);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao gerar número: " + e.getMessage());
        }
        return "PED-0001";
    }

    // ── Mapper ────────────────────────────────────────────────
    private static Pedido mapear(ResultSet rs) throws SQLException {
        Usuario solicitante = new Usuario(
                rs.getInt("id_solicitante"),
                rs.getString("nome_usuario"),
                "", "", "", "ATIVO",
                new Perfil()
        );
        CentroCusto cc = new CentroCusto(
                rs.getInt("id_centrocusto"),
                rs.getString("centro_custo")
        );
        Setor setor = new Setor(
                rs.getInt("id_setor"),
                rs.getString("setor")
        );
        Pedido pedido = new Pedido(
                rs.getInt      ("id_pedido"),
                rs.getString   ("num_pedido"),
                rs.getTimestamp("data_abertura").toLocalDateTime(),
                rs.getString   ("status"),
                rs.getDouble   ("valor_total_estimado"),
                solicitante, cc, setor
        );

        int idAprovador = rs.getInt("id_aprovador");
        if (!rs.wasNull()) {
            pedido.setAprovador(new Usuario(
                    idAprovador, rs.getString("nome_aprovador"),
                    "", "", "", "ATIVO", new Perfil()
            ));
        }
        Timestamp tsAprovacao = rs.getTimestamp("data_aprovacao");
        if (tsAprovacao != null) pedido.setDataAprovacao(tsAprovacao.toLocalDateTime());
        pedido.setParecer(rs.getString("parecer"));

        return pedido;
    }
    // Atualiza setor e centro de custo do pedido
    public static boolean atualizar(Pedido pedido) {
        String sql = """
            UPDATE tb_pedido SET
                id_setor       = ?,
                id_centrocusto = ?,
                valor_total_estimado = ?
            WHERE id_pedido = ?
            """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt   (1, pedido.getSetor().getIdSetor());
            ps.setInt   (2, pedido.getCentroCusto().getIdCentroCusto());
            ps.setDouble(3, pedido.getValorTotalEstimado());
            ps.setInt   (4, pedido.getIdPedido());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar pedido: " + e.getMessage());
            return false;
        }
    }

    // Remove todos os itens do pedido para reinserir
    public static boolean removerItens(int idPedido) {
        String sql = "DELETE FROM tb_pedido_produto WHERE id_pedido = ?";

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPedido);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao remover itens: " + e.getMessage());
            return false;
        }
    }
    public static boolean negar(int idPedido, int idAprovador, String parecer) {
        String sql = """
            UPDATE tb_pedido
            SET status = 'NEGADO',
                id_aprovador = ?,
                data_aprovacao = NOW(),
                parecer = ?
            WHERE id_pedido = ?
            """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt   (1, idAprovador);
            ps.setString(2, parecer);
            ps.setInt   (3, idPedido);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao negar pedido: " + e.getMessage());
            return false;
        }
    }
}