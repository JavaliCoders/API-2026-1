package api.DAO;

import api.connection.ConexaoDB;
import api.model.Fornecedor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class fornecedorDAO {

    public static boolean inserir(Fornecedor f) {
        String sql = """
                INSERT INTO tb_fornecedor
                    (nome, cnpj, tipo_pagamento, pedido_minimo, status)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, f.getNome());
            ps.setString(2, f.getCnpj());
            ps.setString(3, f.getTipoPagamento());
            ps.setDouble(4, f.getPedidoMinimo());
            ps.setString(5, f.getStatus());
            ps.executeUpdate();
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

    public static boolean atualizar(Fornecedor f) {
        String sql = """
                UPDATE tb_fornecedor SET
                    nome           = ?,
                    cnpj           = ?,
                    tipo_pagamento = ?,
                    pedido_minimo  = ?,
                    status         = ?
                WHERE id_fornecedor = ?
                """;
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, f.getNome());
            ps.setString(2, f.getCnpj());
            ps.setString(3, f.getTipoPagamento());
            ps.setDouble(4, f.getPedidoMinimo());
            ps.setString(5, f.getStatus());
            ps.setInt   (6, f.getIdFornecedor());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar fornecedor: " + e.getMessage());
            return false;
        }
    }

    private static Fornecedor mapear(ResultSet rs) throws SQLException {
        return new Fornecedor(
                rs.getInt   ("id_fornecedor"),
                rs.getString("nome"),
                rs.getString("cnpj"),
                rs.getString("tipo_pagamento"),
                rs.getDouble("pedido_minimo"),
                rs.getString("status")
        );
    }
}