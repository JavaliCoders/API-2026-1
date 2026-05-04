package api.DAO;

import api.connection.ConexaoDB;
import api.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;

public class notaFiscalDAO {

    // ── SELECT todas as notas ─────────────────────────────────
    public static ObservableList<NotaFiscal> listarTodas() {
        ObservableList<NotaFiscal> lista = FXCollections.observableArrayList();
        String sql = """
        SELECT nf.*,
               p.id_pedido,
               p.num_pedido,
               f.id_fornecedor,
               f.nome AS nome_fornecedor,
               f.cnpj,
               f.pedido_minimo,
               f.status AS status_fornecedor,
               c.data AS data_compra,
               c.valor_total AS valor_compra,
               u.nome AS nome_registro,
               uc.nome AS nome_conferencia
        FROM tb_notasfiscal nf
        JOIN tb_compra c ON c.id_compra = nf.id_compra
        JOIN tb_pedido p ON p.id_pedido = c.id_pedido
        JOIN tb_fornecedor f ON f.id_fornecedor = c.id_fornecedor
        JOIN tb_usuario u ON u.id_usuario = nf.id_usuario_registro
        LEFT JOIN tb_usuario uc ON uc.id_usuario = nf.id_usuario_conferencia
        ORDER BY nf.data_registro DESC
        """;
        try (Connection con = ConexaoDB.getConexao();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.err.println("Erro ao listar notas: " + e.getMessage());
        }
        return lista;
    }

    // ── Verificar se a entrada está completa para a nota ──────
    /**
     * Retorna true se todos os itens da nota já foram completamente recebidos
     * (qtd_recebida >= qtd_aprovada para todos os itens do pedido da compra da nota).
     * Usado para desabilitar "Dar entrada" quando já está completo.
     */
    public static boolean entradaCompleta(int idNota) {
        String sql = """
                SELECT COUNT(*) AS total,
                       SUM(CASE WHEN pp.qtd_recebida >= pp.qtd_aprovada THEN 1 ELSE 0 END) AS completos
                FROM tb_nf_item nfi
                JOIN tb_pedido_produto pp ON pp.id_pedido_produto = nfi.id_pedido_produto
                WHERE nfi.id_nf_item IN (
                    SELECT id_nf_item FROM tb_nf_item
                    WHERE id_pedido_produto IN (
                        SELECT ci.id_pedido_produto
                        FROM tb_compra_item ci
                        JOIN tb_compra c ON c.id_compra = ci.id_compra
                        JOIN tb_notasfiscal nf ON nf.id_compra = c.id_compra
                        WHERE nf.id_nota = ?
                    )
                )
                """;
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idNota);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int total     = rs.getInt("total");
                int completos = rs.getInt("completos");
                return total > 0 && total == completos;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar entrada completa: " + e.getMessage());
        }
        return false;
    }

    // ── Mapper ────────────────────────────────────────────────
    private static NotaFiscal mapear(ResultSet rs) throws SQLException {
        Timestamp tsConf = rs.getTimestamp("data_conferencia");

        Usuario usuarioRegistro = new Usuario(
                rs.getInt("id_usuario_registro"),
                rs.getString("nome_registro"),
                "", "", "", "ATIVO", new Perfil()
        );

        Usuario usuarioConferencia = rs.getInt("id_usuario_conferencia") > 0
                ? new Usuario(
                rs.getInt("id_usuario_conferencia"),
                rs.getString("nome_conferencia"),
                "", "", "", "ATIVO", new Perfil()
        )
                : null;

        Pedido pedido = new Pedido();
        pedido.setIdPedidoSimples(rs.getInt("id_pedido"));
        pedido.setNumPedidoSimples(rs.getString("num_pedido"));

        Fornecedor fornecedor = new Fornecedor(
                rs.getInt("id_fornecedor"),
                rs.getString("nome_fornecedor"),
                rs.getString("cnpj"),
                rs.getDouble("pedido_minimo"),
                rs.getString("status_fornecedor")
        );

        Compra compra = new Compra(
                pedido,
                fornecedor,
                rs.getTimestamp("data_compra") != null
                        ? rs.getTimestamp("data_compra").toLocalDateTime()
                        : null,
                null,
                rs.getDouble("valor_compra"),
                null
        );

        return new NotaFiscal(
                rs.getInt("id_nota"),
                rs.getString("numero_nota"),
                rs.getTimestamp("data_emissao").toLocalDateTime(),
                rs.getTimestamp("data_registro").toLocalDateTime(),
                usuarioRegistro,
                compra,
                rs.getDouble("valor_nf"),
                rs.getString("status"),
                rs.getInt("total_itens"),
                null,
                null,
                usuarioConferencia,
                tsConf != null ? tsConf.toLocalDateTime() : null
        );
    }
    public static int inserir(NotaFiscal nf) {

        String sql = """
        INSERT INTO tb_notasfiscal
        (numero_nota, data_emissao, data_registro,
         id_usuario_registro, id_compra, valor_nf,
         id_anexo, status, total_itens)
        VALUES (?, ?, NOW(), ?, ?, ?, ?, 'REGISTRADA', ?)
    """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nf.getNumeroNota());

            ps.setTimestamp(2,
                    nf.getDataEmissao() != null
                            ? Timestamp.valueOf(nf.getDataEmissao())
                            : null
            );

            ps.setInt(3, nf.getUsuarioRegistro().getIdUsuario());
            ps.setInt(4, nf.getCompra().getIdCompra());
            ps.setDouble(5, nf.getValorNf());

            // ⚠️ se não tiver anexo ainda, pode ser NULL ou 0
            ps.setNull(6, Types.INTEGER);

            ps.setInt(7, nf.getTotalItens());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao inserir nota fiscal: " + e.getMessage());
        }

        return -1;
    }
    public static ObservableList<NfItem> listarItensParaConferencia(int idNota) {

        ObservableList<NfItem> lista = FXCollections.observableArrayList();

        String sql = """
        SELECT ci.id_pedido_produto,
               pr.produto,
               pr.unidade_medida,
               pp.qtd_aprovada,
               ci.qtd_comprada
        FROM tb_notasfiscal nf
        JOIN tb_compra_item ci ON ci.id_compra = nf.id_compra
        JOIN tb_pedido_produto pp ON pp.id_pedido_produto = ci.id_pedido_produto
        JOIN tb_produto pr ON pr.id_produto = pp.id_produto
        WHERE nf.id_nota = ?
    """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idNota);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new NfItem(
                        rs.getInt("id_pedido_produto"),
                        rs.getString("produto"),
                        rs.getString("unidade_medida"),
                        rs.getInt("qtd_aprovada"),
                        rs.getInt("qtd_comprada")
                ));
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return lista;
    }
    public static ObservableList<NfItem> listarItensConferidos(int idNota) {

        ObservableList<NfItem> lista = FXCollections.observableArrayList();

        String sql = """
        SELECT ni.id_nf_item,
               ni.id_pedido_produto,
               pr.produto,
               pr.unidade_medida,
               pp.qtd_aprovada,
               ni.qtd_recebida,
               ni.qtd_rejeitada,
               ni.motivo_divergencia,
               ci.qtd_comprada
        FROM tb_nf_item ni
        JOIN tb_pedido_produto pp ON pp.id_pedido_produto = ni.id_pedido_produto
        JOIN tb_produto pr ON pr.id_produto = pp.id_produto
        JOIN tb_compra_item ci ON ci.id_pedido_produto = ni.id_pedido_produto
        WHERE ni.id_nota = ?
    """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idNota);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new NfItem(
                        rs.getInt("id_nf_item"),
                        rs.getInt("id_pedido_produto"),
                        rs.getString("produto"),
                        rs.getString("unidade_medida"),
                        rs.getInt("qtd_aprovada"),
                        rs.getInt("qtd_recebida"),
                        rs.getInt("qtd_rejeitada"),
                        rs.getString("motivo_divergencia"),
                        rs.getInt("qtd_comprada")
                ));
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return lista;
    }
    public static boolean inserirItensConferencia(int idNota, ObservableList<NfItem> itens) {

        String sql = """
        INSERT INTO tb_nf_item
        (id_nota, id_pedido_produto, qtd_recebida, qtd_rejeitada, motivo_divergencia)
        VALUES (?, ?, ?, ?, ?)
    """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (NfItem item : itens) {
                ps.setInt(1, idNota);
                ps.setInt(2, item.getIdPedidoProduto());
                ps.setInt(3, item.getQtdRecebida());
                ps.setInt(4, item.getQtdRejeitada());
                ps.setString(5, item.getMotivoDivergencia());
                ps.addBatch();
            }

            ps.executeBatch();
            return true;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
    public static boolean conferir(int idNota, int idUsuario, String status) {

        String sql = """
        UPDATE tb_notasfiscal
        SET status = ?,
            id_usuario_conferencia = ?,
            data_conferencia = NOW()
        WHERE id_nota = ?
    """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, idUsuario);
            ps.setInt(3, idNota);
            ps.executeUpdate();

            return true;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
    public static boolean darEntrada(int idNota, int idUsuario) {

        String sql = """
        SELECT ni.id_pedido_produto,
               ni.qtd_recebida,
               pp.id_pedido,
               pp.id_produto
        FROM tb_nf_item ni
        JOIN tb_pedido_produto pp ON pp.id_pedido_produto = ni.id_pedido_produto
        WHERE ni.id_nota = ?
    """;

        try (Connection con = ConexaoDB.getConexao()) {

            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, idNota);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {

                    int idProduto = rs.getInt("id_produto");
                    int qtd = rs.getInt("qtd_recebida");
                    int idPedido = rs.getInt("id_pedido");

                    try (PreparedStatement up = con.prepareStatement(
                            "UPDATE tb_produto SET saldo = saldo + ? WHERE id_produto = ?")) {
                        up.setInt(1, qtd);
                        up.setInt(2, idProduto);
                        up.executeUpdate();
                    }

                    try (PreparedStatement mov = con.prepareStatement(
                            "INSERT INTO tb_movimentacao (id_produto, tipo_movimentação, quantidade, id_usuario, id_pedido, data) VALUES (?, 'ENTRADA', ?, ?, ?, NOW())")) {

                        mov.setInt(1, idProduto);
                        mov.setInt(2, qtd);
                        mov.setInt(3, idUsuario);
                        mov.setInt(4, idPedido);
                        mov.executeUpdate();
                    }
                }
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
}