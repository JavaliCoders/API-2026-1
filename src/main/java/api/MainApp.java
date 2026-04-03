package api;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/index.fxml")
        );

        Scene scene = new Scene(loader.load(), 1100, 650);

        primaryStage.setTitle("Gestão de compras");

        // 👇 AQUI você adiciona o ícone
        primaryStage.getIcons().add(
                new Image(getClass().getResourceAsStream("/images/N.png"))
        );

        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(550);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}