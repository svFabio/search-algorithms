package edu.ai.misioneros;

import edu.ai.misioneros.controlador.ControladorPrincipal;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        ControladorPrincipal controller = new ControladorPrincipal();
        Scene scene = new Scene(controller.getRoot(), 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/estilos.css").toExternalForm());

        stage.setTitle("Misioneros y Caníbales - A* vs Profundidad");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


