package main.dao;

import main.modelo.Perfil;
import main.factory.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PerfilDAO {

    private Connection conexao;

    public PerfilDAO() {this.conexao = new ConnectionFactory().getConnection();}

    public void adiciona(Perfil perfil) {
        String sql = "INSERT INTO tb_perfil (perfil) VALUES (?)";

        try {
            PreparedStatement stmt = conexao.prepareStatement(sql);

            stmt.setString(1, perfil.getPerfil());

            stmt.execute();
            stmt.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
