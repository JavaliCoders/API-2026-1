package api.connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class ConexaoDB {

    private static final HikariDataSource dataSource;

    static {
        Properties props = new Properties();
        try (InputStream input = ConexaoDB.class.getResourceAsStream("/config.properties")) {
            props.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar config.properties", e);
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.usuario"));
        config.setPassword(props.getProperty("db.senha"));

        config.setMinimumIdle(2);
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30_000);
        config.setIdleTimeout(600_000);
        config.setMaxLifetime(1_800_000);
        config.setConnectionTestQuery("SELECT 1");

        dataSource = new HikariDataSource(config);
    }

    private ConexaoDB() {}

    public static Connection getConexao() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            System.err.println("Erro ao obter conexão do pool: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}