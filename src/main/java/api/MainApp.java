package api;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.image.Image;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 900, 600);

        try {
            scene.getStylesheets().add(
                    getClass().getResource("/style/loginStyle.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("CSS não encontrado, usando estilos inline.");
        }
        try {
            Image icone = new Image(
                    getClass().getResourceAsStream("/images/N.png"));
            primaryStage.getIcons().add(icone);
        } catch (Exception e) {
            System.out.println("Ícone não encontrado.");
        }

        primaryStage.setTitle("Gestão de Compras - Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}