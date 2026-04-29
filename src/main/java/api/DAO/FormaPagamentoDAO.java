package api.DAO;

import api.connection.ConexaoDB;
import api.model.FormaPagamento;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class FormaPagamentoDAO {

    public static ObservableList<FormaPagamento> listarTodos() {
        ObservableList<FormaPagamento> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM tb_forma_pagamento ORDER BY forma";

        try (Connection con = ConexaoDB.getConexao();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next())
                lista.add(new FormaPagamento(
                        rs.getInt("id_forma_pagamento"),
                        rs.getString("forma")));

        } catch (SQLException e) {
            System.err.println("Erro ao listar formas de pagamento: " + e.getMessage());
        }
        return lista;
    }

    public static ObservableList<FormaPagamento> listarPorFornecedor(int idFornecedor) {
        ObservableList<FormaPagamento> lista = FXCollections.observableArrayList();
        String sql = """
                SELECT fp.*
                FROM tb_fornecedor_pagamento fp_rel
                JOIN tb_forma_pagamento fp
                  ON fp_rel.id_forma_pagamento = fp.id_forma_pagamento
                WHERE fp_rel.id_fornecedor = ?
                """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idFornecedor);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                lista.add(new FormaPagamento(
                        rs.getInt("id_forma_pagamento"),
                        rs.getString("forma")));

        } catch (SQLException e) {
            System.err.println("Erro ao listar pagamentos do fornecedor: " + e.getMessage());
        }
        return lista;
    }

    public static boolean inserirParaFornecedor(int idFornecedor,
                                                ObservableList<FormaPagamento> formas) {
        String sql = """
                INSERT INTO tb_fornecedor_pagamento (id_fornecedor, id_forma_pagamento)
                VALUES (?, ?)
                """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (FormaPagamento fp : formas) {
                ps.setInt(1, idFornecedor);
                ps.setInt(2, fp.getIdFormaPagamento());
                ps.addBatch();
            }
            ps.executeBatch();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao inserir formas de pagamento: " + e.getMessage());
            return false;
        }
    }

    public static boolean removerPorFornecedor(int idFornecedor) {
        String sql = "DELETE FROM tb_fornecedor_pagamento WHERE id_fornecedor = ?";

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idFornecedor);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao remover formas de pagamento: " + e.getMessage());
            return false;
        }
    }
}