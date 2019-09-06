package LimakWebApp.ServerSide;

import javafx.application.Application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.stage.Stage;

import java.io.IOException;

/**
 * <h1>ServerApp</h1>
 * This class runs server application
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   12.08.2019
 */
public class ServerApp extends Application {
    private static FXMLLoader loader;

    @FXML
    private static MainPageController mainPageController;

    /**
     * Default constructor
     */
    public ServerApp(){}

    /**
     * This method starts application, creates stage and runs authorization thread.
     * @param primaryStage the parameter inherited from abstract declaration in super class
     * @throws Exception if there are problems with stage creation or application can't start
     */
    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage = createStage("ServerMainPage.fxml", 435, 480);
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

    /**
     * This method launches application
     * @param args Arguments provided to run application
     */
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
}
