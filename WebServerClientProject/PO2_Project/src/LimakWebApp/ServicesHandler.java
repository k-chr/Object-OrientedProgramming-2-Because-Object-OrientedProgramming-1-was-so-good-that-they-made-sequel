package LimakWebApp;

import LimakWebApp.DataPackets.CredentialPacket;
import LimakWebApp.DataPackets.FilePacket;
import LimakWebApp.DataPackets.MessageToSend;
import LimakWebApp.DataPackets.SocketHandler;

import LimakWebApp.Utils.Constants;
import LimakWebApp.Utils.Controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * <h1>ServicesHandler</h1>
 * This abstract class is base for server and client main communication classes
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   22.07.2019
 */
public abstract class ServicesHandler {
    private ExecutorService serviceForNotificationService =  Executors.newFixedThreadPool(1);
    private SocketHandler socketHandler;
    private CredentialPacket localEndPoint;

    private volatile ServiceThread fileService;
    private volatile ServiceThread notificationService;
    protected volatile Map.Entry<CredentialPacket, ArrayList<File>> filesToTransfer;
    protected volatile Controller mainPageController;
    protected volatile boolean closed = false;

    /**
     *This constructor sets basic fields of object and runs a runnable that perform sockets operations (access to streams)
     * @param handler Object received from socket
     * @param packet credentials of local user
     * @param ID id of session
     * @throws IOException if provided sockets in <code>handler</code> are <code>null</code>
     */
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

    /**
     * This method processes given object and passes it into appropriate method
     * @param o Object received from socket
     */
    protected void processObject(Object o){
        if(o instanceof FilePacket){
            saveFile((FilePacket) o);
        }
        else if(o instanceof MessageToSend){
            if(this.getLocalEndPoint().equals((CredentialPacket) (((MessageToSend)o).getUser()))) return;
            rcvCmd((MessageToSend) o);
        }
    }

    /**
     * This method returns the value of state of ServicesHandler - determines whether it's closed or not
     * @return boolean
     */
    public boolean isClosed(){
        return closed;
    }

    void submitNotificationWatcher(Runnable task){
        this.serviceForNotificationService.submit(task);
    }

    /**
     * This method is public accessor of method clean
     */
    public void close(){
        if(!isClosed()) {
            cleanUp();
        }
    }

    /**
     * This method terminates {@link ExecutorService}s, closes held sockets
     */
    protected void cleanUp(){
        closed = true;
        notificationService.setSendExit(true);
        notificationService.cleanUp();
        fileService.cleanUp();
        serviceForNotificationService.shutdown();
        if(!serviceForNotificationService.isTerminated()){
            try {
                serviceForNotificationService.awaitTermination(3, TimeUnit.SECONDS);
                socketHandler.getFileTransferSocket().close();
                socketHandler.getNotificationSocket().close();
            }
            catch (InterruptedException| IOException ie) {
                serviceForNotificationService.shutdownNow();
                ie.printStackTrace();
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
            }
            serviceForNotificationService.shutdownNow();
        }
    }
    /**
     * This method provides for class controller to communicate with GUI thread
     * @param controller the reference to controller to set
     */
    public void setController(Controller controller){
        mainPageController = controller;
    }

    /**
     * This method returns reference to hold controller
     * @return {@link Controller}
     */
    public Controller getController(){
        return mainPageController;
    }

    /**
     * This method returns reference to local user's credentials
     * @return {@link CredentialPacket}
     */
    protected CredentialPacket getLocalEndPoint() {
        return localEndPoint;
    }

    /**
     * This method returns reference to file service
     * @return {@link ServiceThread}
     */
    protected ServiceThread getFileService() {
        return fileService;
    }

    /**
     * This method returns reference to notification service
     * @return {@link ServiceThread}
     */
    protected ServiceThread getNotificationService() {
        return notificationService;
    }

    /**
     * This abstract method is a base of communication system invented in this web project - receives commands send usually from peer
     * @param command the data package with several instructions
     */
    protected abstract void rcvCmd(MessageToSend command);

    /**
     * This abstract method is model, deriving classes can also put other functionality
     * @param filePacket the data package to save on drive
     */
    protected abstract void saveFile(FilePacket filePacket);

    /**
     * This abstract method is model, deriving classes can also put other functionality
     * @param data the entry of map that contains a user's credentials as a <code>key</code> and list of file names as a <code>value</code>
     */
    protected abstract void sendListOfFiles(Map.Entry<CredentialPacket, ArrayList<File>> data);
}
