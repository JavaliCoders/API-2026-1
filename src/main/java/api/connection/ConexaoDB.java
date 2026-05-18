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

    private static final String URL;
    private static final String USUARIO;
    private static final String SENHA;

    static {
        Properties props = new Properties();
        try (InputStream input = ConexaoDB.class.getResourceAsStream("/config.properties")) {
            props.load(input);
            URL     = props.getProperty("db.url");
            USUARIO = props.getProperty("db.usuario");
            SENHA   = props.getProperty("db.senha");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar config.properties", e);
        }
    }

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