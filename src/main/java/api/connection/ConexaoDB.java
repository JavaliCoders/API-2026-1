package api.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe responsável por gerenciar a conexão com o banco MySQL.
 * Usa o padrão Singleton para garantir uma única instância de conexão.
 */
public class ConexaoDB {


    private static final String URL      = "jdbc:mysql://localhost:3306/bd_api";
    private static final String USUARIO  = "root";
    private static final String SENHA    = "0511";
    // Instância única da conexão
    private static Connection conexao = null;

    // Construtor privado: ninguém cria instância diretamente
    private ConexaoDB() {}

    /**
     * Retorna a conexão ativa.
     * Se não existir ou estiver fechada, cria uma nova.
     */
    public static Connection getConexao() {
        try {
            if (conexao == null || conexao.isClosed()) {
                conexao = DriverManager.getConnection(URL, USUARIO, SENHA);
                System.out.println("✅ Conexão com banco estabelecida!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao conectar ao banco: " + e.getMessage());
            e.printStackTrace();
        }
        return conexao;
    }

    /**
     * Fecha a conexão com o banco.
     * Chame ao fechar a aplicação.
     */
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