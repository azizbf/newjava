package org.example;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

 public class MainProgGUI extends Application { // ✅ Extending Application
    public static void main(String[] args) {
        launch(args); // ✅ Now it works
    }
    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/Login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Gestion de trajet");
        // Force database connection
        utils.dataSource.getInstance();
        primaryStage.show();
    }
}