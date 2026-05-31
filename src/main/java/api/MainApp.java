package api;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Blend;
import javafx.scene.layout.Pane;
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

            // Blur + tom frio (hue shift azulado, saturação reduzida)
            ColorAdjust cold = new ColorAdjust();
            cold.setHue(0.08);          // desloca para azul frio
            cold.setSaturation(-0.25);  // menos saturado
            cold.setBrightness(-0.35);  // mais escuro
            GaussianBlur blur = new GaussianBlur(22);
            blur.setInput(cold);
            bg.setEffect(blur);
            bg.setOpacity(0.55);

            // ── Monta a cena ───────────────────────────────────
            // O FXML já tem o gradiente azul como camadas internas,
            // o Group com o root fica no topo sem escala extra
            // (o próprio StackPane do FXML preenche a tela)
            StackPane viewport = new StackPane(bg, root);
            viewport.setStyle("-fx-background-color: #050d1a;");

            Scene scene = new Scene(viewport);

            bg.fitWidthProperty() .bind(scene.widthProperty());
            bg.fitHeightProperty().bind(scene.heightProperty());

            // CSS só se existir (o novo FXML usa inline styles)
            URL stylesheet = MainApp.class.getResource("/style/loginStyle.css");
            if (stylesheet != null)
                scene.getStylesheets().add(stylesheet.toExternalForm());

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