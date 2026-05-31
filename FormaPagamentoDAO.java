package api.DAO;

import api.connection.ConexaoDB;
import api.model.Anexo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnexoDAO {

    // Inserir
    public static int inserir(Anexo anexo) {
        String sql = """
            INSERT INTO tb_anexo
            (tipo, nome_arq, caminho_arquivo, data_upload)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, anexo.getTipo());
            ps.setString(2, anexo.getNomeArquivo());
            ps.setString(3, anexo.getCaminhoArquivo());
            ps.setTimestamp(4, Timestamp.valueOf(anexo.getDataUpload()));

            ps.executeUpdate();

            // Pega o ID gerado
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return -1;
    }

    // Listar
    public static List<Anexo> listar() {
        List<Anexo> lista = new ArrayList<>();

        String sql = "SELECT * FROM tb_anexo";

        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Anexo a = new Anexo(
                        rs.getInt("id_anexo"),
                        rs.getString("tipo"),
                        rs.getString("nome_arq"),
                        rs.getString("caminho_arquivo"),
                        rs.getTimestamp("data_upload").toLocalDateTime()
                );

                lista.add(a);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return lista;
    }
}