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

    private static Connection conexao = null;
    private static String url;
    private static String usuario;
    private static String senha;

    static {
        Properties props = new Properties();
        Path configExterno = Paths.get("config.properties");

        if (Files.exists(configExterno)) {
            try (InputStream input = Files.newInputStream(configExterno)) {
                props.load(input);
                System.out.println("Config externa carregada.");
            } catch (Exception e) {
                System.err.println("Erro config externo: " + e.getMessage());
            }
        } else {
            try (InputStream input = ConexaoDB.class
                    .getResourceAsStream("/distribuicao/config.properties")) {
                if (input != null) {
                    props.load(input);
                    System.out.println("Config interna carregada.");
                }
            } catch (Exception e) {
                System.err.println("Erro config interno: " + e.getMessage());
            }
        }

        url     = props.getProperty("db.url");
        usuario = props.getProperty("db.usuario");
        senha   = props.getProperty("db.senha");
    }

    public static Connection getConexao() {
        try {
            if (conexao == null || conexao.isClosed()) {
                conexao = DriverManager.getConnection(url, usuario, senha);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar com o banco: " + e.getMessage());
        }
        return conexao;
    }

    public static void fecharConexao() {
        try {
            if (conexao != null && !conexao.isClosed()) {
                conexao.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}