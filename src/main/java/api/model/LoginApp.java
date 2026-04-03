package api.model;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoginApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            
            Scene scene = new Scene(root);
            
            try {
                scene.getStylesheets().add(getClass().getResource("/style/loginStyle.css").toExternalForm());
            } catch (Exception e) {
                System.out.println("Aviso: Arquivo CSS não encontrado no caminho especificado.");
            }

            stage.setTitle("Sistema de Compras - Login");
            stage.setScene(scene);
            stage.setResizable(true); //impedir o usuário de redimensionar a tela se bugar
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Erro crítico ao iniciar a aplicação!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}