package LimakWebApp.ClientSide;

import LimakWebApp.Utils.Controller;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * <h1>ClientApp</h1>
 * This class launches client application and handles controllers and initial stage
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   17.05.2019
 */
public class ClientApp extends Application {

    private static FXMLLoader loader;
    @FXML
    private static MainPageController mainPageController;
    @FXML
    private static LoginPageController loginPageController;

    private static String loadedCSS;
    /**
     * This method returns reference to object of class:{@link Controller} specified by <code>boolean which</code>
     * @param which This value specifies which controller method should return
     * @return {@link Controller}
     */
    static public Controller getController(boolean which){
        return which ? mainPageController : loginPageController;
    }

    /**
     * Default constructor
     */
    public ClientApp(){
        loadedCSS = getCSS();
    }

    /**
     * This method creates instance of {@link Stage} specified with provided params
     * @param nameFXML Name of FXML file that specifies all GUI of created stage
     * @param width Indicates the width of application
     * @param height Indicates the height of application
     * @param which Indicates which controller should be responsible to handle GUI operations
     * @return {@link Stage}
     * @throws IOException if method fails to load FXML or create the stage
     */
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
        stage.getScene().getStylesheets().add(loadedCSS);
        stage.setTitle(which ? "Client app::MAIN":"Client app::LOGIN");
        return stage;
    }


    private String getCSS(){
        URL url = this.getClass().getResource("..\\Resources\\modena.css");
        if (url == null) {
            System.out.println("Resource not found. Aborting.");
            System.exit(-1);
        }
        return url.toExternalForm();
    }
    /**
     * This method indicates necessary operations to perform to start an application
     * @param primaryStage initial stage, in case of client it will be login page by default
     * @throws Exception The troubles of run application
     */
    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage = createStage("ClientLoginPage.fxml", 240, 290, false);
        primaryStage.show();
    }

    /**
     * Launches an application
     * @param args Arguments to launch an application
     */
    public static void main(String[] args) {
        launch(args);
    }
}
