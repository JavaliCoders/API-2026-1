package main.dao;

import main.factory.ConnectionFactory;
import main.modelo.Setor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SetorDAO {

    private Connection conexao;

    public SetorDAO() {this.conexao = new ConnectionFactory().getConnection();}

    public void adiciona(Setor setor) {
        String sql = "INSERT INTO tb_setor (setor) VALUES (?)";

        try {
            PreparedStatement stmt = conexao.prepareStatement(sql);

            stmt.setString(1, setor.getSetor());

            stmt.execute();
            stmt.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
