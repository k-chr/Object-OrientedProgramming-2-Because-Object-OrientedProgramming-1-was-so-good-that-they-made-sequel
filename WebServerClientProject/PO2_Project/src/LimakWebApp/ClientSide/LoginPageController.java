package LimakWebApp.ClientSide;

import LimakWebApp.Utils.Constants;
import LimakWebApp.DataPackets.CredentialPacket;
import LimakWebApp.DataPackets.SocketHandler;
import LimakWebApp.Utils.Controller;

import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.io.File;
import java.io.IOException;

import java.net.Socket;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * <h1>LoginPageController</h1>
 * This class provides ability to log in server or register new user.
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   22.06.2019
 */
public class LoginPageController  extends Controller {

    Socket socket;

    private AuthAgent authAgent;
    private Client client;
    private ExecutorService executorService;
    private volatile Set<File> listOfFilesFromDisk = new HashSet<>();
    @FXML
    private TextField userNameInput;
    @FXML
    private TextField userEmailInput;
    @FXML
    private TextField userFolderPathInput;
    @FXML
    private Label errLogLabel;
    @FXML
    private Button loginButton;

    /**
     * Default constructor
     */
    public LoginPageController(){

    }

    /**
     * This method is prepared to be implemented by deriving classes, this method should add new log for Controller
     * @param type Type of log
     * @param body Contents
     */
    @Override
    public void addLog(Constants.LogType type, String body) {

    }

    /**
     * This method should set status bar in deriving Controller
     * @param text Text to set
     */
    @Override
    public void setStatusText(String text) {

    }

    /**
     * This method should display {@link TreeView}
     */
    @Override
    public void displayTree() {

    }

    /**
     * This method should clear root
     */
    @Override
    public void clearRoot() {

    }

    /**
     * This method should refresh {@link TreeView} owned by deriving Controller
     */
    @Override
    public void refreshTree() {

    }

    private void getListOfClientFilesFromDisk(File root){
        if(root.isDirectory()){
            if(root.listFiles() != null){
                for(File file : root.listFiles()){
                    if(file.isDirectory()){
                        getListOfClientFilesFromDisk(file);
                    }
                    else{
                        listOfFilesFromDisk.add(file);
                    }
                }
            }
        }
    }

    @FXML
    private void initialize(){
        authAgent = new AuthAgent();
        executorService = Executors.newFixedThreadPool(2);
        EventHandler<ActionEvent> actionHandler = event -> {
            try {
                socket = new Socket(Constants.serverIP, Constants.authPort);
            }
            catch(IOException ignore){
            }
            Integer integer = isValidUser();
            System.out.println(integer);
            if(integer.equals(0) || integer.equals(9)){
                File root = new File(authAgent.getCredentialPacket().getUserFolderPath());
                if(root.exists()){
                    getListOfClientFilesFromDisk(root);
                }

                String sessionID = authAgent.getSessionID();
                ((Button) event.getSource()).getScene().getWindow().hide();
                try {
                    Stage stage = ClientApp.createStage("ClientMainPage.fxml", 435, 480, true);
                    MainPageController controller = (MainPageController) ClientApp.getController(true);
                    ArrayList<File> out = new ArrayList<>(listOfFilesFromDisk);
                    controller.fillListOfFilesInitially(out);
                    controller.setStatusText("Session is active");
                    stage.setOnCloseRequest(e-> {
                        controller.cleanUp();
                        e.consume();
                    });
                    stage.show();
                    ArrayList<Socket> sockets = new ArrayList<>();
                    sockets.add(new Socket(Constants.serverIP, Constants.filePort));
                    sockets.add(new Socket(Constants.serverIP, Constants.commPort));
                    controller.setSessionID(sessionID);
                    client = new Client(new SocketHandler(sockets), authAgent.getCredentialPacket(), controller);
                    controller.setClient(client);
                    controller.setCredentialPacket( authAgent.getCredentialPacket());
                    client.setController(controller);
                    controller.clearRoot();
                    controller.createDirectories();
                    controller.displayTree();
                    ApacheWatchService apacheWatchService = new ApacheWatchService(controller);
                    controller.setWatchService(apacheWatchService);
                    controller.runWatcher();
                }
                catch (IOException io) {
                    io.printStackTrace();
                }
            }
            else{
                switch(integer){
                    case 1: {
                        errLogLabel.setText("Error! Invalid username");
                        break;
                    }
                    case 3: {
                        errLogLabel.setText("Error! Invalid email");
                        break;
                    }
                    case 4: {
                        errLogLabel.setText("Error! Invalid path");
                        break;
                    }
                    case -1:{
                        errLogLabel.setText("Error occurred!");
                        break;
                    }
                }
                userEmailInput.setText("");
                userFolderPathInput.setText("");
                userNameInput.setText("");
            }
            authAgent.closeInitConnection();
        };
        loginButton.addEventHandler(ActionEvent.ACTION, actionHandler);
    }

    private int isValidUser(){
        authAgent.setSocket(socket);
        authAgent.setCredentialPacket(new CredentialPacket(userEmailInput.getText(), userNameInput.getText(), userFolderPathInput.getText()));
        Future<Integer> rV = executorService.submit(authAgent);
        int result = -1;
        try {
            result = rV.get();
        }
        catch(Exception ee){
            ee.printStackTrace();
        }
        return result;
    }
}
