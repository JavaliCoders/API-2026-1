package api.database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class Conexao {

    private static final String DATABASE_NAME = "bd_api";
    private static final String HOST = env("API2026_DB_HOST", "localhost");
    private static final String PORT = env("API2026_DB_PORT", "3306");
    private static final String BASE_URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE_NAME
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = env("API2026_DB_USER", "root");
    private static final String PASSWORD = env("API2026_DB_PASSWORD", "");
    private static final String[] SCRIPT_RESOURCES = {
            "/bd_api.sql",
            "/database/bd_api.sql"
    };
    private static final String[] SCRIPT_PATHS = {
            "bd_api.sql",
            "src/database/bd_api.sql"
    };

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Driver JDBC do MySQL nao encontrado.", e);
        }
    }

    private Conexao() {
    }

    public static Connection conectar() {
        try {
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            prepararBanco(connection);
            return connection;
        } catch (SQLException e) {
            if (bancoNaoExiste(e)) {
                inicializarBanco();
                try {
                    Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                    prepararBanco(connection);
                    return connection;
                } catch (SQLException ex) {
                    throw new RuntimeException("Banco criado, mas a conexao falhou: " + ex.getMessage(), ex);
                }
            }
            throw new RuntimeException("Erro na conexao: " + e.getMessage(), e);
        }
    }

    private static void prepararBanco(Connection connection) throws SQLException {
        if (precisaExecutarScript(connection)) {
            executarScriptInicial(connection);
        }
        garantirUsuariosPadrao(connection);
    }

    private static String env(String nome, String valorPadrao) {
        String valor = System.getenv(nome);
        return valor == null || valor.isBlank() ? valorPadrao : valor;
    }

    private static boolean bancoNaoExiste(SQLException e) {
        return e.getMessage() != null
                && e.getMessage().toLowerCase().contains("unknown database");
    }

    private static void inicializarBanco() {
        try (Connection connection = DriverManager.getConnection(BASE_URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME);
        } catch (SQLException e) {
            throw new RuntimeException("Nao foi possivel criar o banco " + DATABASE_NAME + ": " + e.getMessage(), e);
        }

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            if (precisaExecutarScript(connection)) {
                executarScriptInicial(connection);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Banco criado, mas houve falha ao inicializar as tabelas: " + e.getMessage(), e);
        }
    }

    private static boolean precisaExecutarScript(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet tables = metaData.getTables(connection.getCatalog(), null, "tb_usuario", null)) {
            return !tables.next();
        }
    }

    private static void executarScriptInicial(Connection connection) {
        String script = carregarScriptInicial();

        String[] comandos = script.split(";");
        for (String comando : comandos) {
            String sql = comando.trim();
            if (sql.isEmpty() || deveIgnorar(sql)) {
                continue;
            }

            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
            } catch (SQLException e) {
                throw new RuntimeException("Falha ao executar o script do banco: " + e.getMessage(), e);
            }
        }
    }

    private static boolean deveIgnorar(String sql) {
        return sql.equalsIgnoreCase("USE " + DATABASE_NAME)
                || sql.equalsIgnoreCase("CREATE DATABASE " + DATABASE_NAME)
                || sql.equalsIgnoreCase("CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME);
    }

    private static String carregarScriptInicial() {
        String scriptDoClasspath = carregarScriptDoClasspath();
        if (scriptDoClasspath != null) {
            return scriptDoClasspath;
        }

        for (String scriptPath : SCRIPT_PATHS) {
            Path caminho = Path.of(scriptPath);
            if (!Files.exists(caminho)) {
                continue;
            }

            try {
                return Files.readString(caminho, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("Nao foi possivel ler o arquivo " + scriptPath + ": " + e.getMessage(), e);
            }
        }

        throw new RuntimeException("Nenhum script de banco foi encontrado. Verifique bd_api.sql.");
    }

    private static String carregarScriptDoClasspath() {
        for (String scriptResource : SCRIPT_RESOURCES) {
            try (InputStream inputStream = Conexao.class.getResourceAsStream(scriptResource)) {
                if (inputStream == null) {
                    continue;
                }

                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(
                        "Nao foi possivel ler o recurso " + scriptResource + ": " + e.getMessage(),
                        e
                );
            }
        }

        return null;
    }

    private static void garantirUsuariosPadrao(Connection connection) {
        inserirUsuarioSeNaoExistir(connection, "Diretor Geral", "diretor", "123", "diretor@email.com", 1);
        inserirUsuarioSeNaoExistir(connection, "Usuario Financeiro", "financeiro", "123", "financeiro@email.com", 2);
        inserirUsuarioSeNaoExistir(connection, "Usuario Estoque", "estoque", "123", "estoque@email.com", 3);
        inserirUsuarioSeNaoExistir(connection, "Usuario Operacional", "operacional", "123", "operacional@email.com", 4);
    }

    private static void inserirUsuarioSeNaoExistir(
            Connection connection,
            String nome,
            String usuario,
            String senha,
            String email,
            int idPerfil
    ) {
        String consulta = "SELECT COUNT(*) FROM tb_usuario WHERE usuario = ? OR email = ?";
        String insercao = "INSERT INTO tb_usuario (nome, usuario, senha, email, status, id_perfil) "
                + "VALUES (?, ?, ?, ?, 'ATIVO', ?)";

        try (PreparedStatement psConsulta = connection.prepareStatement(consulta)) {
            psConsulta.setString(1, usuario);
            psConsulta.setString(2, email);

            try (ResultSet rs = psConsulta.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao consultar usuarios padrao: " + e.getMessage(), e);
        }

        try (PreparedStatement psInsercao = connection.prepareStatement(insercao)) {
            psInsercao.setString(1, nome);
            psInsercao.setString(2, usuario);
            psInsercao.setString(3, senha);
            psInsercao.setString(4, email);
            psInsercao.setInt(5, idPerfil);
            psInsercao.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao inserir usuarios padrao: " + e.getMessage(), e);
        }
    }
}
