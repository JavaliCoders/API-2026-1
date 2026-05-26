package api.DAO;

import api.connection.ConexaoDB;
import api.model.Setor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class SetorDAO {

    public static ObservableList<Setor> listarTodos() {
        ObservableList<Setor> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM tb_setor ORDER BY id_setor ASC";

        try (Connection con = ConexaoDB.getConexao();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Setor(
                        rs.getInt("id_setor"),
                        rs.getString("setor")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar setores: " + e.getMessage());
        }
        return lista;
    }
}