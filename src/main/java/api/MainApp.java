package api;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/view/login.fxml"));
            Parent root = loader.load();

            // ── Imagem do caminhão como fundo real ──────────────
            Image truck = new Image(
                    MainApp.class.getResource("/images/login-background.png").toExternalForm());
            ImageView bg = new ImageView(truck);
            bg.setPreserveRatio(false);
            bg.setSmooth(true);

            ColorAdjust cold = new ColorAdjust();
            cold.setHue(0.08);
            cold.setSaturation(-0.25);
            cold.setBrightness(-0.35);
            GaussianBlur blur = new GaussianBlur(22);
            blur.setInput(cold);
            bg.setEffect(blur);
            bg.setOpacity(0.55);

            // ── Monta a cena ────────────────────────────────────
            StackPane viewport = new StackPane(bg, root);
            viewport.setStyle("-fx-background-color: #050d1a;");

            Scene scene = new Scene(viewport);

            bg.fitWidthProperty().bind(scene.widthProperty());
            bg.fitHeightProperty().bind(scene.heightProperty());

            URL stylesheet = MainApp.class.getResource("/style/loginStyle.css");
            if (stylesheet != null)
                scene.getStylesheets().add(stylesheet.toExternalForm());

            // ── Ícone da janela ─────────────────────────────────
            try {
                Image icone = new Image(
                        MainApp.class.getResourceAsStream("/images/N.png"));
                stage.getIcons().add(icone);
            } catch (Exception e) {
                System.out.println("Ícone não encontrado.");
            }

            stage.setTitle("Newe - Login");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(820);
            stage.setMinHeight(540);
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            System.err.println("Erro crítico ao iniciar a aplicação.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}