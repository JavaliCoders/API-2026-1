package api.DAO;

import api.connection.ConexaoDB;
import api.model.Perfil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class PerfilDAO {

    public static ObservableList<Perfil> listarTodos() {
        ObservableList<Perfil> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM tb_perfil ORDER BY id_perfil ASC";

        try (Connection con = ConexaoDB.getConexao();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Perfil(
                        rs.getInt("id_perfil"),
                        rs.getString("perfil")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar perfis: " + e.getMessage());
        }
        return lista;
    }
}