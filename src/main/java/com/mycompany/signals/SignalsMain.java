package com.mycompany.signals;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SignalsMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Cargar el FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/fxml/MainView.fxml"));
        Parent root = loader.load(); // Esto crea la interfaz y conecta con el controller

        // 2. Crear la escena
        Scene scene = new Scene(root);

        // 3. (Opcional) aplicar estilos CSS
        scene.getStylesheets().add(getClass().getResource("/ui/styles/mainview.css").toExternalForm());

        // 4. Configurar y mostrar la ventana
        primaryStage.setTitle("Mi App JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args); // Esto inicia JavaFX
    }
}