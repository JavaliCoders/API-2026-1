package api.DAO;

import api.connection.ConexaoDB;
import api.model.CentroCusto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class centroCustoDAO {

    public static ObservableList<CentroCusto> listarTodos() {
        ObservableList<CentroCusto> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM tb_centrocusto ORDER BY id_centrocusto ASC";

        try (Connection con = ConexaoDB.getConexao();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new CentroCusto(
                        rs.getInt("id_centrocusto"),
                        rs.getString("centro_custo")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar centros de custo: " + e.getMessage());
        }
        return lista;
    }
}