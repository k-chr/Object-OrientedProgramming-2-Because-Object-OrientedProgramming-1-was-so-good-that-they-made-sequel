package LimakWebApp.ClientSide;

import javafx.stage.Stage;
import LimakWebApp.Utils.Constants;
import LimakWebApp.DataPackets.CredentialPacket;
import LimakWebApp.DataPackets.SocketHandler;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import LimakWebApp.Utils.Controller;

import java.io.IOException;

import java.net.Socket;

import java.util.ArrayList;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoginPageController  extends Controller {

    Socket socket;

    private AuthAgent authAgent;
    private Client client;
    private ExecutorService executorService;

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

    public LoginPageController(){

    }

    @Override
    public void addLog(Constants.LogType type, String body) {

    }
    @Override
    public void setStatusText(String text) {

    }

    @Override
    public void displayTree() {

    }

    @Override
    public void clearRoot() {

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
                ignore.printStackTrace();
            }
            Integer integer = isValidUser();
            System.out.println(integer);
            if(integer.equals(0) || integer.equals(9)){
                String sessionID = authAgent.getSessionID();
                ((Button) event.getSource()).getScene().getWindow().hide();
                try {
                    Stage stage = ClientApp.createStage("ClientMainPage.fxml", 350, 400, true);
                    MainPageController controller = (MainPageController) ClientApp.getController(true);
                    controller.setStatusText("Session is active");
                    stage.setOnCloseRequest(e-> {
                        controller.cleanUp();
                        e.consume();
                        Timer timer = new Timer();
                        timer.schedule(
                                new java.util.TimerTask() {
                                    @Override
                                    public void run() {
                                        System.exit(0);
                                    }
                                }, 4000);
                        timer.cancel();
                    });
                    stage.show();
                    ArrayList<Socket> sockets = new ArrayList<>();
                    sockets.add(new Socket(Constants.serverIP, Constants.filePort));
                    sockets.add(new Socket(Constants.serverIP, Constants.commPort));
                    controller.setSessionID(sessionID);
                    client = new Client(new SocketHandler(sockets), authAgent.getCredentialPacket());
                    controller.setClient(client);
                    controller.setCredentialPacket( authAgent.getCredentialPacket());
                    client.setController(controller);
                    controller.displayTreeView();
                    controller.refreshTree();
                    controller.registerDirectoriesForWatchService();
                    controller.runWatcher();
                }
                catch (IOException io) {
                    io.printStackTrace();
                }
            }
            else{
                switch(integer){
                    case 1: case 8:{
                        errLogLabel.setText("Error! Invalid username");
                        break;
                    }
                    case 3: case 6:{
                        errLogLabel.setText("Error! Invalid email");
                        break;
                    }
                    case 4: case 5:{
                        errLogLabel.setText("Error! Invalid path");
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
        int result = 0;
        try {
            result = rV.get();
        }
        catch(Exception ee){
            ee.printStackTrace();
        }
        return result;
    }
}
