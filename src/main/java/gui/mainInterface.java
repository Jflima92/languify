package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class mainInterface extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage = primaryStage;
//        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("main.fxml"));
        primaryStage.setTitle("Languify");
        /*primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();*/

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main.fxml"));
        Parent root = loader.load();
        mainController controller = loader.getController();

        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();

        controller.setStage(this.primaryStage);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
