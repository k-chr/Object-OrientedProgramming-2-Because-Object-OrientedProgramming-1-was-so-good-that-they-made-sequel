package LimakWebApp.ServerSide;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ServerApp extends Application {
    private static FXMLLoader loader;

    @FXML
    private static MainPageController mainPageController;

    public ServerApp(){}

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage = createStage("ServerMainPage.fxml", 400, 400);
        primaryStage.show();
        mainPageController.getServer().setAuthThread(new Thread(Thread.currentThread().getThreadGroup(),()->mainPageController.authorize(), "ServerSocketAuth"));
        mainPageController.getServer().getAuthThread().start();
        primaryStage.setOnCloseRequest(event-> {
            if(mainPageController.getServer() == null){
                System.out.println("server is null");
            }
            mainPageController.cleanUp();
            event.consume();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    private Stage createStage(String nameFXML, int width, int height) throws IOException {
        loader = new FXMLLoader(MainPageController.class.getResource(nameFXML));
        Parent root = loader.load();
        mainPageController = loader.getController();
        Stage stage = new Stage();
        stage.setResizable(false);
        stage.setScene(new Scene(root, width, height));
        stage.setTitle("Server app::MAIN");
        return stage;
    }

    @FXML
    static MainPageController getController(){
        return mainPageController ;
    }
}
