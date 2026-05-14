package api.DAO;

import api.connection.ConexaoDB;
import api.model.FormaPagamento;
import api.model.Fornecedor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class fornecedorDAO {

    public static boolean inserir(Fornecedor f,
                                  ObservableList<FormaPagamento> formas) {
        String sql = """
                INSERT INTO tb_fornecedor (nome, cnpj, pedido_minimo, status)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, f.getNome());
            ps.setString(2, f.getCnpj());
            ps.setDouble(3, f.getPedidoMinimo());
            ps.setString(4, f.getStatus());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int idGerado = keys.getInt(1);
                FormaPagamentoDAO.inserirParaFornecedor(idGerado, formas);
            }
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao inserir fornecedor: " + e.getMessage());
            return false;
        }
    }

    public static ObservableList<Fornecedor> listarTodos() {
        ObservableList<Fornecedor> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM tb_fornecedor ORDER BY id_fornecedor ASC";

        try (Connection con = ConexaoDB.getConexao();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            System.err.println("Erro ao listar fornecedores: " + e.getMessage());
        }
        return lista;
    }

    public static boolean atualizar(Fornecedor f,
                                    ObservableList<FormaPagamento> formas) {
        String sql = """
                UPDATE tb_fornecedor SET
                    nome          = ?,
                    cnpj          = ?,
                    pedido_minimo = ?,
                    status        = ?
                WHERE id_fornecedor = ?
                """;
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, f.getNome());
            ps.setString(2, f.getCnpj());
            ps.setDouble(3, f.getPedidoMinimo());
            ps.setString(4, f.getStatus());
            ps.setInt   (5, f.getIdFornecedor());
            ps.executeUpdate();

            FormaPagamentoDAO.removerPorFornecedor(f.getIdFornecedor());
            FormaPagamentoDAO.inserirParaFornecedor(f.getIdFornecedor(), formas);
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar fornecedor: " + e.getMessage());
            return false;
        }
    }

    private static Fornecedor mapear(ResultSet rs) throws SQLException {
        Fornecedor f = new Fornecedor(
                rs.getInt   ("id_fornecedor"),
                rs.getString("nome"),
                rs.getString("cnpj"),
                rs.getDouble("pedido_minimo"),
                rs.getString("status")
        );
        f.setFormasPagamento(
                FormaPagamentoDAO.listarPorFornecedor(f.getIdFornecedor()));
        return f;
    }

    public static ObservableList<Fornecedor> listarAtivos() {
        ObservableList<Fornecedor> lista = FXCollections.observableArrayList();

        String sql = "SELECT * FROM tb_fornecedor WHERE status = 'ATIVO'";

        try (Connection con = ConexaoDB.getConexao();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Fornecedor f = new Fornecedor(
                        rs.getInt("id_fornecedor"),
                        rs.getString("nome"),
                        rs.getString("cnpj"),
                        rs.getDouble("pedido_minimo"),
                        rs.getString("status")
                );
                lista.add(f);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }
}