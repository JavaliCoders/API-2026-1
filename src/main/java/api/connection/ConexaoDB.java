package api.connection;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexaoDB {

    private static final String URL     = "jdbc:mysql://localhost:3306/bd_api";
    private static final String USUARIO = "root";
    private static final String SENHA   = "0511";

    private ConexaoDB() {}

    public static Connection getConexao() {
        try {
            return DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch (SQLException e) {
            System.err.println("❌ Erro ao conectar ao banco: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}