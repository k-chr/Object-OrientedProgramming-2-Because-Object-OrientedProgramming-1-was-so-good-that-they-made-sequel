package LimakWebApp.ServerSide;

import LimakWebApp.DataPackets.CredentialPacket;
import LimakWebApp.DataPackets.SocketHandler;
import LimakWebApp.Utils.AbstractServerController;
import LimakWebApp.Utils.Constants;
import LimakWebApp.Utils.Controller;
import LimakWebApp.Utils.DataPair;

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
import java.util.TimerTask;

/**
 * <h1>Server</h1>
 * This class provides clients authorization and handles all networking on server's side
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   12.06.2019
 */
public class Server {

    private Thread authThread;
    private ServerSocket server = null;
    private ServerSocket fileServer = null;
    private ServerSocket notificationServer = null;

    /**
     * Returns {@link Controller} of {@link Server}.
     *
     * @return Controller
     */
    public Controller getController() {
        return controller;
    }

    /**
     * This method sets {@link Controller} for {@link Server}.
     *
     * @param controller Controller to set.
     */
    public void setController(Controller controller) {
        this.controller = controller;
    }

    private Controller controller;
    private volatile boolean isItTimeToStop = false;
    private volatile Socket socket = null;
    private volatile ArrayList<CommunicationServiceThreadHandler> threadList = new ArrayList<>();

    /**
     * Constructor of Server. Initializes server's sockets.
     */
    public Server() {
        try {
            server = new ServerSocket(Constants.authPort);
            fileServer = new ServerSocket(Constants.filePort);
            notificationServer = new ServerSocket(Constants.commPort);
        } catch (IOException i) {
            controller.setStatusText("Connection issue!");
            StringBuilder stringBuilder = new StringBuilder();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream outStream = new PrintStream(outputStream);
            i.printStackTrace(outStream);
            stringBuilder.append(new Date()).append(":\n").append("Connection issue! \n\t").append(i.getMessage()).append("\n").append(outStream.toString()).append("\n");
            controller.addLog(Constants.LogType.ERROR, stringBuilder.toString());
            i.printStackTrace();
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    System.exit(1);
                    timer.cancel();
                }
            };
            timer.schedule(task, 4000);

        }
    }

    /**
     * Quits app and clears connections
     */
    public void quit() {
        ((AbstractServerController) controller).getServer().clearUpConnection();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.exit(0);
            }
        };
        timer.schedule(task, 4000);
    }

    /**
     * This method destroys all connections
     */
    public void clearUpConnection() {
        for (CommunicationServiceThreadHandler handler : threadList) {
            if (!handler.isClosed()) {
                handler.close();
            }
        }
        try {
            if (server != null && !server.isClosed()) {
                server.close();
            }
        } catch (IOException io) {
            io.printStackTrace();
        }
        try {
            if (fileServer != null && !fileServer.isClosed()) {
                fileServer.close();
            }
        } catch (IOException io) {
            io.printStackTrace();
        }
        try {
            if (notificationServer != null && !notificationServer.isClosed()) {
                notificationServer.close();
            }
        } catch (IOException io) {
            io.printStackTrace();
        }

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
                    ((AbstractServerController)controller).updateListOfClients(entry.getKey(), true);
                    controller.setStatusText("Accepted client: " + entry.getKey().getUserName());
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(new Date()).append(":\n").append("Accepted client: \n\t").append(entry.getKey().getUserName()).append("\n");
                    controller.addLog(Constants.LogType.INFO, stringBuilder.toString());
                    if (errCode.equals(9)) {
                        ((AbstractServerController)controller).getEmailSession().sendEmail(entry.getKey(), true, null);
                    }
                    ArrayList<Socket> socketList = new ArrayList<>();
                    String ID = ((AbstractServerController)controller).generateID((controller));
                    outputStream.writeObject(ID);
                    socketList.add(fileServer.accept());
                    socketList.add(notificationServer.accept());
                    SocketHandler socketHandler = new SocketHandler(socketList);
                    synchronized (threadList) {
                        threadList.add(new CommunicationServiceThreadHandler(socketHandler, entry.getKey(), ID, controller));
                    }
                }
                else {
                    if (entry != null) {
                        getController().setStatusText("Rejected client: " + "\"" + entry.getKey().getUserName() + "\"");
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(new Date()).append(":\n").append("Rejected client: \n\t").append(entry.getKey().getUserName()).append("\n");
                        controller.addLog(Constants.LogType.ERROR, stringBuilder.toString());
                    }
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
            Set<DataPair> listOfClients = ((AbstractServerController)controller).getListOfClients().toDataPairSet();
            boolean newUser = listOfClients.stream().allMatch(dataPair -> {
                Integer ret = dataPair.getKey().compareTo(credentialPacket);
                return ret.equals(9);
            });
            boolean oldUser = listOfClients.stream().anyMatch(dataPair -> {
                Integer ret = dataPair.getKey().compareTo(credentialPacket);
                return ret.equals(0);
            });
            boolean invalidEmail = listOfClients.stream().anyMatch(dataPair -> {
                Integer ret = dataPair.getKey().compareTo(credentialPacket);
                return ret.equals(3) || ret.equals(6);
            });
            boolean invalidUsername = listOfClients.stream().anyMatch(dataPair -> {
                Integer ret = dataPair.getKey().compareTo(credentialPacket);
                return ret.equals(1) || ret.equals(8);
            });
            boolean invalidPath = listOfClients.stream().anyMatch(dataPair -> {
                Integer ret = dataPair.getKey().compareTo(credentialPacket);
                return ret.equals(4) || ret.equals(5);
            });
            int rV = newUser ? 9 : (oldUser ? 0 : (invalidEmail ? 3 :(invalidUsername ? 1 : (invalidPath ? 4 : -1))));
            outputStream.writeObject(rV);
            outputStream.flush();
            return new HashMap.SimpleEntry<>(credentialPacket, rV);
        }
        catch(ClassNotFoundException|IOException e) {
            controller.setStatusText("Connection issue!");
            StringBuilder stringBuilder = new StringBuilder();
            ByteArrayOutputStream outputByteStream = new ByteArrayOutputStream();
            PrintStream outStream = new PrintStream(outputByteStream);
            e.printStackTrace(outStream);
            stringBuilder.append(new Date()).append(":\n").append("Connection issue! \n\t").append(e.getMessage()).append("\n").append(outStream.toString()).append("\n");
            getController().addLog(Constants.LogType.ERROR, stringBuilder.toString());
            e.printStackTrace();
            return null;
        }
    }
}