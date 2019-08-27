package LimakWebApp.ServerSide;

import LimakWebApp.Utils.Constants;
import LimakWebApp.DataPackets.CredentialPacket;
import LimakWebApp.DataPackets.SocketHandler;

import javafx.application.Platform;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;

/**
 * <h1>Server</h1>
 * This class provides clients authorization and handles all networking on server's side
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   12.06.2019
 */
public class Server{

    private Thread authThread;
    private ServerSocket server = null;
    private ServerSocket fileServer = null;
    private ServerSocket notificationServer = null;

    private volatile boolean isItTimeToStop = false;
    private volatile Socket socket = null;
    private volatile ArrayList<CommunicationServiceThreadHandler> threadList = new ArrayList<>();

    /**
     * Constructor of Server. Initializes server's sockets.
     */
    public Server(){
        try{
            server = new ServerSocket(Constants.authPort);
            fileServer = new ServerSocket(Constants.filePort);
            notificationServer = new ServerSocket(Constants.commPort);
        }
        catch(IOException i){
            Platform.runLater(()-> {
                ServerApp.getController().setStatusText("Connection issue!");
                StringBuilder stringBuilder = new StringBuilder();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                PrintStream outStream = new PrintStream(outputStream);
                i.printStackTrace(outStream);
                stringBuilder.append(new Date()).append(":\n").append("Connection issue! \n\t").append(i.getMessage()).append("\n").append(outStream.toString()).append("\n");
                ServerApp.getController().addLog(Constants.LogType.ERROR, stringBuilder.toString());
                i.printStackTrace();
                System.exit(1);
            });
        }
    }

    /**
     * This method destroys all connections and closes ServerApp
     */
    public void clearUpConnection(){
        Timer timer = new Timer();
        timer.schedule(
            new java.util.TimerTask() {
                @Override
                public void run() {
                    for(CommunicationServiceThreadHandler handler : threadList){
                        if(!handler.isClosed()) {
                            handler.close();
                        }
                    }
                    try {
                        if(server != null && !server.isClosed()) {
                            server.close();
                        }
                    }
                    catch(IOException io){
                        io.printStackTrace();
                    }
                    try{
                        if(fileServer != null && !fileServer.isClosed()){
                            fileServer.close();
                        }
                    }
                    catch(IOException io){
                        io.printStackTrace();
                    }
                    try{
                        if(notificationServer != null && !notificationServer.isClosed()){
                            notificationServer.close();
                        }
                    }
                    catch(IOException io){
                        io.printStackTrace();
                    }
                    timer.cancel();
                    System.exit(0);
                }
            }, 4000
        );
    }

    /**
     * This method serves given socket in separated thread. Performs authorization of single connection and rejects or accepts new client.
     * @param socket Accepted socket to authorize
     */
    public void processSocket(Socket socket){
        Socket authSocket = socket;
        Runnable task = ()->{
            try {
                ObjectInputStream inputStream;
                ObjectOutputStream outputStream;
                outputStream = new ObjectOutputStream(authSocket.getOutputStream());
                outputStream.flush();
                InputStream input = authSocket.getInputStream();
                inputStream = new ObjectInputStream(input);
                Map.Entry<CredentialPacket, Integer> entry = authorize(outputStream, inputStream);
                Integer errCode;
                if (entry == null) {
                    errCode = -1;
                } else {
                    errCode = entry.getValue();
                }
                if (errCode.equals(9) || errCode.equals(0)) {
                    Platform.runLater(() -> {
                        ServerApp.getController().updateListOfClients(entry.getKey(), true);
                        ServerApp.getController().setStatusText("Accepted client: " + entry.getKey().getUserName());
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(new Date()).append(":\n").append("Accepted client: \n\t").append(entry.getKey().getUserName()).append("\n");
                        ServerApp.getController().addLog(Constants.LogType.INFO, stringBuilder.toString());
                        if (errCode.equals(9)) {
                            ServerApp.getController().getEmailSession().sendEmail(entry.getKey(), true, null);
                        }
                    });
                    ArrayList<Socket> socketList = new ArrayList<>();
                    String ID = ServerApp.getController().generateID(ServerApp.getController().getClass());
                    outputStream.writeObject(ID);
                    socketList.add(fileServer.accept());
                    socketList.add(notificationServer.accept());
                    SocketHandler socketHandler = new SocketHandler(socketList);
                    synchronized (threadList) {
                        threadList.add(new CommunicationServiceThreadHandler(socketHandler, entry.getKey(), ID));
                    }
                }
                else {
                    Platform.runLater(() -> {
                        if(entry != null) {
                            ServerApp.getController().setStatusText("Rejected client: " + "\"" + entry.getKey().getUserName() + "\"");
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(new Date()).append(":\n").append("Rejected client: \n\t").append(entry.getKey().getUserName()).append("\n");
                            ServerApp.getController().addLog(Constants.LogType.ERROR, stringBuilder.toString());
                        }
                    });
                }
                inputStream.close();
                outputStream.close();
                authSocket.close();
            }
            catch (IOException ignored){

            }
        };
        new Thread(task).start();
    }

    /**
     * This method accepts new sockets in loop.
     */
    public void acceptClients(){
        while(!isItTimeToStop) {
            try {
                socket = server.accept();
                processSocket(socket);
            }
            catch (IOException ignored) {
            }
        }
    }

    /**
     * This method finds connection assigned to user and shares file to user
     * @param to User - the future owner of file
     * @param item item to share
     */
    public void shareToUser(CredentialPacket to, String item){
        CommunicationServiceThreadHandler handlerTemp =  threadList.stream().filter(handler->handler.getRemoteEndPoint().equals(to)).findAny().orElse(null);
        if(handlerTemp != null){
            handlerTemp.shareToUser(item);
        }
    }

    /**
     * This method sets boolean flag to given value to stop or not process of accepting new sockets.
     * @param value value to set
     */
    public synchronized void setItTimeToStop(boolean value){
        isItTimeToStop = value;
    }

    /**
     * This method sets the reference to authorization thread
     * @param authThread Reference to thread
     */
    public void setAuthThread(Thread authThread) {
        this.authThread = authThread;
    }

    /**
     * This method gives access to instance of authorization thread.
     * @return Thread
     */
    public Thread getAuthThread() {
        return authThread;
    }

    private Map.Entry<CredentialPacket, Integer> authorize(ObjectOutputStream outputStream, ObjectInputStream inputStream){
        try{
            CredentialPacket credentialPacket = (CredentialPacket) inputStream.readObject();
            if(credentialPacket.isEmpty()){
                outputStream.writeObject(-1);
                outputStream.flush();
                return null;
            }
            Set<DataPair> listOfClients = ServerApp.getController().getListOfClients().toDataPairSet();
            boolean newUser = listOfClients.stream().allMatch(dataPair -> {
                Integer ret = dataPair.getKey().compareTo(credentialPacket);
                return ret.equals(9);
            });
            boolean oldUser = listOfClients.stream().anyMatch(dataPair -> {
                Integer ret = dataPair.getKey().compareTo(credentialPacket);
                return ret.equals(0);
            });
            int rV = newUser ? 9 : (oldUser ? 0 : -1);
            outputStream.writeObject(rV);
            outputStream.flush();
            return new HashMap.SimpleEntry<>(credentialPacket, rV);
        }
        catch(ClassNotFoundException|IOException e){
            Platform.runLater(()-> {
                ServerApp.getController().setStatusText("Connection issue!");
                StringBuilder stringBuilder = new StringBuilder();
                ByteArrayOutputStream outputByteStream = new ByteArrayOutputStream();
                PrintStream outStream = new PrintStream(outputByteStream);
                e.printStackTrace(outStream);
                stringBuilder.append(new Date()).append(":\n").append("Connection issue! \n\t").append(e.getMessage()).append("\n").append(outStream.toString()).append("\n");
                ServerApp.getController().addLog(Constants.LogType.ERROR, stringBuilder.toString());
            });
            e.printStackTrace();
            return null;
        }
    }
}