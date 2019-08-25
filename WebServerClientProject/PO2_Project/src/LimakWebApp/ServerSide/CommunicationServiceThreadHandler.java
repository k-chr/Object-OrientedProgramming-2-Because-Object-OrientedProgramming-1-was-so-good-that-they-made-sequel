package LimakWebApp.ServerSide;

import LimakWebApp.Utils.Constants;
import LimakWebApp.DataPackets.CredentialPacket;
import LimakWebApp.DataPackets.FilePacket;
import LimakWebApp.DataPackets.MessageToSend;
import LimakWebApp.DataPackets.SocketHandler;
import LimakWebApp.ServicesHandler;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.application.Platform;

public class CommunicationServiceThreadHandler extends ServicesHandler {

    private final long err = Long.MAX_VALUE;
    private final String ID;
    private CredentialPacket remoteEndPoint;
    private boolean isSomethingToShare = false;
    private volatile String itemToShare;

    public CommunicationServiceThreadHandler(SocketHandler handler, CredentialPacket packet, String ID) throws IOException{
        super(handler, ServerApp.getController().getCredentialPacket(), ID);
        remoteEndPoint = packet;
        this.ID = ID;
        setController(ServerApp.getController());
        if(!handler.getIsSocketsSet()){
            throw new IOException();
        }
        Platform.runLater(()->ServerApp.getController().putId(ID));
        Runnable task = ()->{
          initCommunication();
        };
        mainPageController.getPool().submit(task);
    }

    public void shareToUser(String itemToShare){
        determineIfThereIsSomethingToShare(true);
        this.itemToShare = itemToShare;
        ArrayList<Object> toSend = new ArrayList<>();
        toSend.add(itemToShare);
        MessageToSend command = new MessageToSend(getLocalEndPoint(), MessageToSend.COMMAND_TYPE.TRANSFER_FILE_DEMAND);
        command.addContents(toSend);
        getNotificationService().sendObject(command);
    }

    private void initCommunication(){
        while(true){
            if(getNotificationService() != null){
                processObject((new MessageToSend(remoteEndPoint, MessageToSend.COMMAND_TYPE.SEND_LIST_OF_FILES)));
                break;
            }
        }
    }

    @Override
    protected void rcvCmd(MessageToSend command){
        Platform.runLater(()->{
            mainPageController.setStatusText("Received command");
            mainPageController.addLog(Constants.LogType.INFO, new Date().toString() + ":\nReceived command: \n\t" + command.getCommandType().name());
        });
        switch(command.getCommandType()){
            case LOG_OUT_DEMAND:{
                getNotificationService().sendObject(new MessageToSend(getLocalEndPoint(), MessageToSend.COMMAND_TYPE.QUIT_CONNECTION));
            }
            case QUIT_CONNECTION: {
                try{
                    Thread.sleep(100);
                }
                catch(InterruptedException ie){
                    ie.printStackTrace();
                }
                cleanUp();
                ((MainPageController)mainPageController).cleanUpSessionForID(ID, remoteEndPoint);
                break;
            }
            case TRANSFER_FILE:{
                if(command.getContents().size() == 1 && isSomethingToShare == true && itemToShare.isEmpty() == false){
                    ArrayList<String> toSend = new ArrayList<>();
                    toSend.add(itemToShare);
                    sendListOfFiles(new AbstractMap.SimpleEntry<>(getLocalEndPoint(), toSend));
                    determineIfThereIsSomethingToShare(false);
                    itemToShare = "";
                }
                else {
                    sendListOfFiles(filesToTransfer);
                }
                break;
            }
            case TRANSFER_FILE_DEMAND:{
                for(int i= 0;i < command.getContents().size(); ++i) {
                    getFileService().getObject(false);
                }
                MessageToSend msg = new MessageToSend(getLocalEndPoint(), MessageToSend.COMMAND_TYPE.TRANSFER_FILE);
                msg.addContents(command.getContents());
                getNotificationService().sendObject(msg);
                Platform.runLater(()->{
                    mainPageController.setStatusText("Receiving files");
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(new Date()).append(":\n").append("Receiving files in progress...\n");
                    mainPageController.addLog(Constants.LogType.INFO, stringBuilder.toString());
                });
                break;
            }
            case SEND_LIST_OF_FILES: {
                System.out.println("block");
                MessageToSend newMessage = new MessageToSend(getLocalEndPoint(), MessageToSend.COMMAND_TYPE.RECEIVE_LIST_OF_FILES);
                newMessage.addContents(new ArrayList<>(ServerApp.getController().getListOfFilesForUser(command.getUser())));
                getNotificationService().sendObject(newMessage);
                break;
            }
            case RECEIVE_LIST_OF_FILES:{
                ArrayList<String>  tmp = command.getContents().stream().map((object)->(String)object).collect(Collectors.toCollection(ArrayList::new));
                filesToTransfer = ServerApp.getController().compareUserAndServerList(command.getUser(), tmp);
                if(filesToTransfer.getValue().size() > 0){
                    MessageToSend newMessage = new MessageToSend(getLocalEndPoint(), MessageToSend.COMMAND_TYPE.TRANSFER_FILE_DEMAND);
                    newMessage.addContents(filesToTransfer.getValue().stream().map(string->(Object)string).collect(Collectors.toCollection(ArrayList::new)));
                    getNotificationService().sendObject(newMessage);
                    Platform.runLater(()->{
                        mainPageController.setStatusText("Sending files");
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(new Date()).append(":\n").append("Sending files in progress...\n");
                        mainPageController.addLog(Constants.LogType.INFO, stringBuilder.toString());
                    });
                }
                break;
            }
            case GIVE_LIST_OF_ACTIVE_USERS: {
                MessageToSend newMessage = new MessageToSend(getLocalEndPoint(), MessageToSend.COMMAND_TYPE.GET_LIST_OF_ACTIVE_USERS);
                newMessage.addContents(new ArrayList<>(ServerApp.getController().getActiveListOfClients()));
                getNotificationService().sendObject(newMessage);
                break;
            }
            case GIVE_LIST_OF_INACTIVE_USERS:{
                MessageToSend newMessage = new MessageToSend(getLocalEndPoint(), MessageToSend.COMMAND_TYPE.GET_LIST_OF_INACTIVE_USERS);
                newMessage.addContents(new ArrayList<>(ServerApp.getController().getInactiveListOfClients()));
                getNotificationService().sendObject(newMessage);
                break;
            }
            case SHARE_FILE_TO_USER:{
                String userName = command.getContents().get(0) instanceof String ? (String)command.getContents().get(0) : "";
                CredentialPacket packet = ServerApp.getController().findUserByName(userName);
                String fileName = command.getContents().get(1) instanceof String ? (String)command.getContents().get(1): "";
                Platform.runLater(()->ServerApp.getController().shareFile(packet, fileName));
                break;
            }
            case REMOVE_USER_FROM_FILE_OWNERS:{
                CredentialPacket packet = command.getContents().get(0) instanceof CredentialPacket ? (CredentialPacket)command.getContents().get(0) : null;
                String fileName = command.getContents().get(1) instanceof String ? (String)command.getContents().get(1): "";
                Platform.runLater(()->ServerApp.getController().removeUserFromFileOwners(fileName, packet));
                break;
            }
        }
    }

    @Override
    protected void saveFile(FilePacket filePacket){
        File root = new File(Constants.getServerDirectory(this));
        if(root != null && root.listFiles() != null) {
            File minFolder = Arrays.stream(root.listFiles()).
                    filter(file -> file.isDirectory()).
                    min(Comparator.comparing(file -> file.listFiles() != null ? file.listFiles().length : err)).get();
            File fileToSave = new File(minFolder, filePacket.getFileName());
            try(BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(fileToSave))) {
                long size = filePacket.getSize();
                writer.write(filePacket.getFileBytes(), 0, (int)size);
            }
            catch (IOException io){
                Platform.runLater(() -> {
                    mainPageController.setStatusText("Can't save the file");
                    StringBuilder stringBuilder = new StringBuilder();
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    PrintStream outStream = new PrintStream(outputStream);
                    io.printStackTrace(outStream);
                    stringBuilder.append(new Date())
                            .append(":\n").append("Can't save the file: ")
                            .append(filePacket.getFileName()).append("\n\t")
                            .append(io.getMessage()).append("\n")
                            .append(outStream.toString()).append("\n");
                    mainPageController.addLog(Constants.LogType.ERROR, stringBuilder.toString());
                });
            }
            int idx = (minFolder.getName().charAt(minFolder.getName().length() - 1)) - '0' - 1;
            Platform.runLater(() -> {
                CredentialPacket packet =  ((MainPageController)mainPageController).findUserByName(filePacket.getUserName());
                ((MainPageController)mainPageController).getDisk(idx).putOwnerToFile(filePacket.getFileName(), packet);
                ((MainPageController)mainPageController).addToRuntimeMap(packet, filePacket.getFileName());
                (mainPageController).setStatusText("File saved successfully");
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(new Date())
                        .append(":\n").append("File saved successfully: ").append("\n\t")
                        .append(filePacket.getFileName());
                mainPageController.addLog(Constants.LogType.SUCCESS, stringBuilder.toString());
            });
            (mainPageController).refreshTree();
        }
    }

    @Override
    protected void sendListOfFiles(Map.Entry<CredentialPacket, ArrayList<String>> data){
        if(data == null || data.getValue().size() == 0 || data.getKey() == null) return;
        int filesSent = 0;
        for(String fileName: data.getValue()){
            String dir =  ((MainPageController)mainPageController).findFileInServer(fileName);
            if(dir.equals("")){
                Platform.runLater(()->{
                    mainPageController.setStatusText("Can't find the file in server");
                    StringBuilder builder = new StringBuilder();
                    builder.append(new Date()).append(":\n").append("Can't find the file in server: \n\t").append(fileName);
                    (mainPageController).addLog(Constants.LogType.ERROR, builder.toString());
                });
                continue;
            }
            FilePacket packet = new FilePacket(data.getKey().getUserName(), fileName,dir);
            getFileService().sendObject(packet);
            ++filesSent;
        }
        boolean success = filesSent == data.getValue().size();
        Platform.runLater(()->{
            mainPageController.setStatusText( success ? "All files were sent" : "Files were sent partially");
            StringBuilder builder = new StringBuilder();
            builder.append(new Date()).append(":\n").append(success ? "All files were sent" : "Files were sent partially");
            mainPageController.addLog(Constants.LogType.INFO, builder.toString());
        });
    }

    private void determineIfThereIsSomethingToShare(boolean value) {
        isSomethingToShare = value;
    }

    public CredentialPacket getRemoteEndPoint() {
        return remoteEndPoint;
    }
}