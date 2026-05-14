package api.DAO;

import api.connection.ConexaoDB;
import api.model.CentroCusto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class centroCustoDAO {

    public static boolean inserir(CentroCusto f) {
        String sql = """
                INSERT INTO tb_centrocusto (centro_custo, status)
                VALUES (?,?)
                """;
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, f.getCentroCusto());
            ps.setString(2, f.getStatus());
            ps.executeUpdate();

            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao inserir centro de custo: " + e.getMessage());
            return false;
        }
    }

    public static ObservableList<CentroCusto> listarTodos() {
        ObservableList<CentroCusto> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM tb_centrocusto ORDER BY id_centrocusto ASC";

        try (Connection con = ConexaoDB.getConexao();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            System.err.println("Erro ao listar Centros de custo: " + e.getMessage());
        }
        return lista;
    }

    public static boolean atualizar(CentroCusto f) {
        String sql = """
                UPDATE tb_centrocusto SET
                    centro_custo = ?,
                    status       = ?
                WHERE id_centrocusto = ?
                """;
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, f.getCentroCusto());
            ps.setString(2, f.getStatus());
            ps.setInt   (3, f.getIdCentroCusto());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar Centro de custo! " +
                    ": " + e.getMessage());
            return false;
        }
    }

    private static CentroCusto mapear(ResultSet rs) throws SQLException {
        CentroCusto f = new CentroCusto(
                rs.getInt   ("id_centrocusto"),
                rs.getString("centro_custo"),
                rs.getString("status")
        );
        return f;
    }

    public static ObservableList<CentroCusto> listarAtivos() {
        ObservableList<CentroCusto> lista = FXCollections.observableArrayList();

        String sql = "SELECT * FROM tb_centrocusto WHERE status = 'ATIVO'";

        try (Connection con = ConexaoDB.getConexao();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }
}