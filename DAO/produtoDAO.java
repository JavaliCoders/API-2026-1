package api.DAO;

import api.connection.ConexaoDB;
import api.model.Produto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

/**
 * DAO (Data Access Object) do Produto.
 * Responsável por todas as operações de banco relacionadas a tb_produto.
 */
public class produtoDAO {

    // ── INSERT ────────────────────────────────────────────────

    /**
     * Insere um novo produto no banco.
     * @return true se inserido com sucesso, false caso contrário.
     */
    public static boolean inserir(Produto produto) {
        String sql = """
                INSERT INTO tb_produto
                    (produto, descricao, unidade_medida, nivel_minimo, valor_estimado, status, saldo)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, produto.getProduto());
            ps.setString(2, produto.getDescricao());
            ps.setString(3, produto.getUnidadeMedida());
            ps.setInt   (4, produto.getNivelMinimo());
            ps.setDouble(5, produto.getValorEstimado());
            ps.setString(6, produto.getStatus());
            ps.setInt   (7, produto.getSaldo());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao inserir produto: " + e.getMessage());
            return false;
        }
    }

    // ── SELECT TODOS ──────────────────────────────────────────

    /**
     * Retorna todos os produtos do banco.
     */
    public static ObservableList<Produto> listarTodos() {
        ObservableList<Produto> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM tb_produto ORDER BY id_produto ASC";

        try (Connection con = ConexaoDB.getConexao();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar produtos: " + e.getMessage());
        }
        return lista;
    }

    // ── SELECT POR STATUS ─────────────────────────────────────

    /**
     * Retorna produtos filtrados por status.
     */
    public static ObservableList<Produto> listarPorStatus(String status) {
        ObservableList<Produto> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM tb_produto WHERE status = ? ORDER BY produto";

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erro ao filtrar produtos: " + e.getMessage());
        }
        return lista;
    }

    // ── UPDATE ────────────────────────────────────────────────

    /**
     * Atualiza um produto existente no banco.
     */
    public static boolean atualizar(Produto produto) {
        String sql = """
                UPDATE tb_produto SET
                    produto        = ?,
                    descricao      = ?,
                    unidade_medida = ?,
                    nivel_minimo   = ?,
                    valor_estimado = ?,
                    status         = ?
                WHERE id_produto = ?
                """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, produto.getProduto());
            ps.setString(2, produto.getDescricao());
            ps.setString(3, produto.getUnidadeMedida());
            ps.setInt   (4, produto.getNivelMinimo());
            ps.setDouble(5, produto.getValorEstimado());
            ps.setString(6, produto.getStatus());
            ps.setInt   (7, produto.getIdProduto());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar produto: " + e.getMessage());
            return false;
        }
    }

    // ── DELETE ────────────────────────────────────────────────

    /**
     * Remove um produto pelo ID.
     */
    public static boolean deletar(int idProduto) {
        String sql = "DELETE FROM tb_produto WHERE id_produto = ?";

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idProduto);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao deletar produto: " + e.getMessage());
            return false;
        }
    }

    // ── MAPPER ────────────────────────────────────────────────

    /**
     * Converte uma linha do ResultSet em um objeto Produto.
     */
    private static Produto mapear(ResultSet rs) throws SQLException {
        return new Produto(
                rs.getInt   ("id_produto"),
                rs.getString("produto"),
                rs.getString("descricao"),
                rs.getString("unidade_medida"),
                rs.getInt   ("nivel_minimo"),
                rs.getDouble("valor_estimado"),
                rs.getString("status"),
                rs.getInt   ("saldo")
        );
    }
}
