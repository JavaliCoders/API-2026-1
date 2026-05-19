package api.javafx;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;

public class LoginApp extends Application {

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(LoginApp.class.getResource("/view/login.fxml"));
            Parent root = loader.load();

            Image backgroundImage = new Image(
                    LoginApp.class.getResource("/images/login-background.png").toExternalForm()
            );
            ImageView background = new ImageView(backgroundImage);
            background.setPreserveRatio(false);
            background.setSmooth(true);

            Pane shade = new Pane();
            shade.setStyle("-fx-background-color: rgba(0, 0, 0, 0.08);");

            Group design = new Group(root);
            StackPane viewport = new StackPane(background, shade, design);
            viewport.setStyle("-fx-background-color: #111111;");

            Scene scene = new Scene(viewport, 1092, 720);
            background.fitWidthProperty().bind(scene.widthProperty());
            background.fitHeightProperty().bind(scene.heightProperty());
            shade.prefWidthProperty().bind(scene.widthProperty());
            shade.prefHeightProperty().bind(scene.heightProperty());

            var scale = Bindings.createDoubleBinding(
                    () -> Math.min(scene.getWidth() / 1092.0, scene.getHeight() / 720.0),
                    scene.widthProperty(),
                    scene.heightProperty()
            );
            design.scaleXProperty().bind(scale);
            design.scaleYProperty().bind(scale);

            URL stylesheet = LoginApp.class.getResource("/style/loginStyle.css");
            if (stylesheet == null) {
                throw new IllegalStateException("Nao foi possivel localizar /style/loginStyle.css");
            }
            scene.getStylesheets().add(stylesheet.toExternalForm());

            stage.setTitle("Newe - Login");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(820);
            stage.setMinHeight(540);
            stage.show();
            stage.centerOnScreen();
        } catch (Exception e) {
            System.err.println("Erro critico ao iniciar a aplicacao.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
