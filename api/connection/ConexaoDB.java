package api.connection;

import java.io.InputStream;
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
        String url = null;
        String usuario = null;
        String senha = "";

        try (InputStream input = ConexaoDB.class.getResourceAsStream("/config.properties")) {
            if (input == null) {
                System.err.println("config.properties nao encontrado. Copie config.properties.example e configure o banco.");
            } else {
                props.load(input);
                url = textoObrigatorio(props.getProperty("db.url"));
                usuario = textoObrigatorio(props.getProperty("db.usuario"));
                senha = props.getProperty("db.senha", "");

                if (url == null || usuario == null) {
                    System.err.println("config.properties incompleto. Informe db.url e db.usuario.");
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar config.properties: " + e.getMessage());
        }

        URL = url;
        USUARIO = usuario;
        SENHA = senha;
    }

    private ConexaoDB() {}

    public static Connection getConexao() {
        if (!estaConfigurado()) {
            return null;
        }

        try {
            return DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static boolean estaConfigurado() {
        return URL != null && USUARIO != null;
    }

    private static String textoObrigatorio(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
