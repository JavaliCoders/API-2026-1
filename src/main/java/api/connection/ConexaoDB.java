package api.connection;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Classe responsável por gerenciar a conexão com o banco MySQL.
 * Usa o padrão Singleton para garantir uma única instância de conexão.
 * Lê as configurações do config.properties:
 *   - Primeiro verifica se existe um arquivo externo (mesma pasta do JAR)
 *   - Se não encontrar, usa o arquivo interno (dentro do JAR em resources/)
 */
public class ConexaoDB {

    private static Connection conexao = null;
    private static String url;
    private static String usuario;
    private static String senha;

    // Bloco estático: executado uma única vez quando a classe é carregada
    static {
        Properties props = new Properties();

        // 1º — tenta ler o config.properties externo (mesma pasta do JAR)
        // Útil para distribuição: cada máquina tem seu próprio config
        Path configExterno = Paths.get("config.properties");

        if (Files.exists(configExterno)) {
            try (InputStream input = Files.newInputStream(configExterno)) {
                props.load(input);
                System.out.println("✅ Config externa carregada.");
            } catch (Exception e) {
                System.err.println("❌ Erro ao ler config externo: " + e.getMessage());
            }
        } else {
            // 2º — usa o config.properties interno (dentro do JAR)
            // Útil para desenvolvimento: src/main/resources/config.properties
            try (InputStream input = ConexaoDB.class
                    .getResourceAsStream("/config.properties")) {
                if (input != null) {
                    props.load(input);
                    System.out.println("✅ Config interna carregada.");
                } else {
                    System.err.println("❌ config.properties não encontrado!");
                }
            } catch (Exception e) {
                System.err.println("❌ Erro ao ler config interno: " + e.getMessage());
            }
        }

        url     = props.getProperty("db.url");
        usuario = props.getProperty("db.usuario");
        senha   = props.getProperty("db.senha");
    }

    private ConexaoDB() {}

    public static Connection getConexao() {
        try {
            if (conexao == null || conexao.isClosed()) {
                if (url == null || url.isBlank()) {
                    System.err.println("❌ URL do banco não configurada! Verifique o config.properties.");
                    return null;
                }
                conexao = DriverManager.getConnection(url, usuario, senha);
                System.out.println("✅ Conexão com banco estabelecida!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao conectar ao banco: " + e.getMessage());
        }
        return conexao;
    }

    public static void fecharConexao() {
        try {
            if (conexao != null && !conexao.isClosed()) {
                conexao.close();
                System.out.println("🔒 Conexão encerrada.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}