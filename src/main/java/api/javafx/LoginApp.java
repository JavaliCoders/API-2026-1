package api.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class LoginApp extends Application {

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(LoginApp.class.getResource("/view/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            URL stylesheet = LoginApp.class.getResource("/style/loginStyle.css");
            if (stylesheet == null) {
                throw new IllegalStateException("Nao foi possivel localizar /style/loginStyle.css");
            }
            scene.getStylesheets().add(stylesheet.toExternalForm());

            stage.setTitle("Sistema de Compras - Login");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            System.err.println("Erro critico ao iniciar a aplicacao.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
