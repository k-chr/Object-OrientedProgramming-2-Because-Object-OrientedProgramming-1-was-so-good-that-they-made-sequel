package LimakWebApp;

import LimakWebApp.DataPackets.CredentialPacket;
import LimakWebApp.DataPackets.FilePacket;
import LimakWebApp.DataPackets.MessageToSend;
import LimakWebApp.DataPackets.SocketHandler;

import javafx.application.Platform;
import LimakWebApp.Utils.Constants;
import LimakWebApp.Utils.Controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class ServicesHandler {
    private ExecutorService serviceForNotificationService =  Executors.newFixedThreadPool(1);
    private SocketHandler socketHandler;
    private CredentialPacket localEndPoint;

    private volatile ServiceThread fileService;
    private volatile ServiceThread notificationService;
    protected volatile Map.Entry<CredentialPacket, ArrayList<String>> filesToTransfer;
    protected volatile Controller mainPageController;
    protected volatile boolean closed = false;

    public ServicesHandler(SocketHandler handler, CredentialPacket packet, String ID) throws IOException {
        socketHandler = handler;
        localEndPoint = packet;
        if(!handler.getIsSocketsSet()){
            throw new IOException();
        }
        else{
            Runnable task = ()->{
                try {
                    fileService = new ServiceThread(socketHandler.getFileTransferSocket(), "FileService", this);
                    notificationService = new ServiceThread(socketHandler.getNotificationSocket(), "NotificationService", this);
                    notificationService.getObject(true);
                }
                catch(IOException io){
                    io.printStackTrace();
                }
            };
            serviceForNotificationService.submit(task);
        }
    }

    protected void processObject(Object o){
        if(o instanceof FilePacket){
            saveFile((FilePacket) o);
        }
        else if(o instanceof MessageToSend){
            rcvCmd((MessageToSend) o);
        }
    }

    public boolean isClosed(){
        return closed;
    }

    void submitNotificationWatcher(Runnable task){
        this.serviceForNotificationService.submit(task);
    }

    public void close(){
        if(!isClosed()) {
            cleanUp();
        }
    }

    protected void cleanUp(){
        closed = true;
        notificationService.setSendExit(true);
        notificationService.cleanUp();
        fileService.cleanUp();
        serviceForNotificationService.shutdown();
        if(!serviceForNotificationService.isTerminated()){
            try {
                serviceForNotificationService.awaitTermination(10, TimeUnit.SECONDS);
                socketHandler.getFileTransferSocket().close();
                socketHandler.getNotificationSocket().close();
            }
            catch (InterruptedException| IOException ie){
                serviceForNotificationService.shutdownNow();
                ie.printStackTrace();
                Platform.runLater(() -> {
                    mainPageController.setStatusText("Can't terminate service!");
                    StringBuilder stringBuilder = new StringBuilder();
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    PrintStream outStream = new PrintStream(outputStream);
                    ie.printStackTrace(outStream);
                    stringBuilder.append(new Date())
                            .append(":\n").append("Can't terminate service!")
                            .append("\n\t")
                            .append(ie.getMessage()).append("\n")
                            .append(outStream.toString()).append("\n");
                    mainPageController.addLog(Constants.LogType.ERROR, stringBuilder.toString());
                });
            }
            serviceForNotificationService.shutdownNow();
        }
    }

    public void setController(Controller controller){
        mainPageController = controller;
    }

    public Controller getController(){
        return mainPageController;
    }

    protected CredentialPacket getLocalEndPoint() {
        return localEndPoint;
    }

    protected ServiceThread getFileService() {
        return fileService;
    }

    protected ServiceThread getNotificationService() {
        return notificationService;
    }

    protected abstract void rcvCmd(MessageToSend command);

    protected abstract void saveFile(FilePacket filePacket);

    protected abstract void sendListOfFiles(Map.Entry<CredentialPacket, ArrayList<String>> data);
}
