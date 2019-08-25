package LimakWebApp.ClientSide;

import LimakWebApp.Utils.Controller;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientApp extends Application {

    private static FXMLLoader loader;
    @FXML
    private static MainPageController mainPageController;
    @FXML
    private static LoginPageController loginPageController;

    static public Controller getController(boolean which){
        return which ? mainPageController : loginPageController;
    }

    public ClientApp(){

    }

    static public Stage createStage(String nameFXML, int width, int height, boolean which) throws IOException {
        loader = new FXMLLoader(which ? MainPageController.class.getResource(nameFXML) : LoginPageController.class.getResource(nameFXML));
        Parent root = loader.load();
        if(which) {
            mainPageController = loader.getController();
        }
        else{
            loginPageController = loader.getController();
        }
        Stage stage = new Stage();
        stage.setResizable(false);
        stage.setScene(new Scene(root, width, height));
        stage.setTitle(which ? "Client app::MAIN":"Client app::LOGIN");
        return stage;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage = createStage("ClientLoginPage.fxml", 240, 290, false);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
