package com.hampicare.app;

import com.hampicare.controller.DashboardController;
import com.hampicare.model.Usuario;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        stage.setTitle("Hampi Pharma — Cuidamos tu salud");

        cargarIconoVentana(stage);

        Parent root = FXMLLoader.load(Main.class.getResource("/com/hampicare/view/login.fxml"));
        stage.setScene(new Scene(root, 1000, 600));
        stage.setMinWidth(950);
        stage.setMinHeight(620);
        stage.show();
    }


    private static void cargarIconoVentana(Stage stage) {
        String ruta = "/com/hampicare/images/logo.png";
        try (InputStream is = Main.class.getResourceAsStream(ruta)) {
            if (is != null) {
                stage.getIcons().add(new Image(is));
            } else {
                System.out.println("⚠ No se encontró el ícono en " + ruta +
                        " — revisa que el archivo exista con ese nombre exacto en src/main/resources" + ruta);
            }
        } catch (Exception e) {
            System.out.println("⚠ No se pudo cargar el ícono de la ventana: " + e.getMessage());
        }
    }

    /** Vuelve a la pantalla de login. */
    public static void irALogin() throws Exception {
        Parent root = FXMLLoader.load(Main.class.getResource("/com/hampicare/view/login.fxml"));
        primaryStage.setScene(new Scene(root, 1000, 600));
        primaryStage.setMinWidth(950);
        primaryStage.setMinHeight(620);
    }

    public static void irADashboard(Usuario usuario) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/hampicare/view/dashboard.fxml"));
        Parent root = loader.load();

        DashboardController controller = loader.getController();
        controller.setUsuario(usuario);

        primaryStage.setScene(new Scene(root, 1280, 800));
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(680);
    }

    public static void main(String[] args) {
        launch(args);
    }
}