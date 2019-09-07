package LimakWebApp.ClientSide;

import LimakWebApp.Utils.AbstractClientController;
import LimakWebApp.Utils.Constants;
import LimakWebApp.DataPackets.CredentialPacket;
import LimakWebApp.DataPackets.FilePacket;
import LimakWebApp.DataPackets.MessageToSend;
import LimakWebApp.DataPackets.SocketHandler;
import LimakWebApp.ServicesHandler;

import javafx.stage.Window;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * <h1>Client</h1>
 * This class is used by:
 * <pre>
 * {@link MainPageController}
 * </pre>
 * to manage business logic operations
 *
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   20.05.2019
 */
public class Client extends ServicesHandler {

    private final Object lock = new Object();
    Window windowToClose;

    /**
     * This constructor sets a controller provided from {@link ClientApp} and calls super()
     * @param controller {@link AbstractClientController} controller to manage communication with app's GUI
     * @param socketHandler indicates package of sockets needed to perform any networking
     * @param packet        indicates the data of user who uses the client service
     * @throws IOException if {@code socketHandler} is not valid.
     */
    public Client(SocketHandler socketHandler, CredentialPacket packet, AbstractClientController controller) throws IOException {
        super(socketHandler, packet, "");
        setController(controller);
    }

    void demandForListOfUsers() {
        getNotificationService().sendObject(new MessageToSend(getLocalEndPoint(), MessageToSend.COMMAND_TYPE.GIVE_LIST_OF_ACTIVE_USERS));
        getNotificationService().sendObject(new MessageToSend(getLocalEndPoint(), MessageToSend.COMMAND_TYPE.GIVE_LIST_OF_INACTIVE_USERS));
    }

    void demandForTransferEnforcedByWatchService(File fileToSend) {
        MessageToSend command = new MessageToSend(getLocalEndPoint(), MessageToSend.COMMAND_TYPE.TRANSFER_FILE_DEMAND);
        ArrayList<Object> items = new ArrayList<>();
        items.add(fileToSend);
        command.addContents(items);
        getNotificationService().sendObject(command);
    }

    /**
     * Drops connection and quit client application
     * @param ifTests indicates if Test's session is active
     */
    public void dropConnection(boolean ifTests) {
        getNotificationService().sendObject(new MessageToSend(getLocalEndPoint(), MessageToSend.COMMAND_TYPE.QUIT_CONNECTION));
        getNotificationService().submitTask(this::cleanUp);
        if (!ifTests) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.exit(0);
                }
            }, 4000);
        }
    }

    /**
     * Gives access outside the package to give orders to {@link Client}
     * @param command Command to perform
     */
    public void submitCmd(MessageToSend command){
        rcvCmd(command);
    }
    void demandForLogOut() {
        getNotificationService().sendObject(new MessageToSend(getLocalEndPoint(), MessageToSend.COMMAND_TYPE.LOG_OUT_DEMAND));
    }

    /**
     * This method handles received commands to perform indicated operations
     * @param command the data package with several instructions
     */
    @Override
    protected void rcvCmd(MessageToSend command) {
        mainPageController.setStatusText("Received command");
        mainPageController.addLog(Constants.LogType.INFO, new Date().toString() + ":\nReceived command: \n\t" + command.getCommandType().name());
        switch (command.getCommandType()) {
            case QUIT_CONNECTION: {
                synchronized (lock) {
                    getNotificationService().sendObject(new MessageToSend(getLocalEndPoint(), MessageToSend.COMMAND_TYPE.QUIT_CONNECTION));
                    getNotificationService().submitTask(this::cleanUp);
                }
                ((AbstractClientController)mainPageController).hideWindow(windowToClose);
                break;
            }
            case GET_LIST_OF_ACTIVE_USERS: {
                ((AbstractClientController) mainPageController).updateListOfUsers(command.getContents().stream().map((object) -> (((CredentialPacket) object).getUserName())).collect(Collectors.toCollection(ArrayList::new)), true);
                break;
            }
            case GET_LIST_OF_INACTIVE_USERS: {
                ((AbstractClientController) mainPageController).updateListOfUsers(command.getContents().stream().map((object) -> ((CredentialPacket) object).getUserName()).collect(Collectors.toCollection(ArrayList::new)), false);
                ((AbstractClientController) mainPageController).showUsers();
                break;
            }
            case TRANSFER_FILE: {
                if (command.getContents().size() == 1 && (command.getContents().get(0)).equals(((AbstractClientController) mainPageController).getItemToSend())) {
                    ArrayList<File> toSend = new ArrayList<>();
                    toSend.add(((AbstractClientController) mainPageController).getItemToSend());
                    sendListOfFiles(new AbstractMap.SimpleEntry<>(getLocalEndPoint(), toSend));
                } else {
                    sendListOfFiles(filesToTransfer);
                }
                break;
            }
            case TRANSFER_FILE_DEMAND: {
                for (int i = 0; i < command.getContents().size(); ++i) {
                    getFileService().getObject(false);
                }
                MessageToSend msg = new MessageToSend(getLocalEndPoint(), MessageToSend.COMMAND_TYPE.TRANSFER_FILE);
                msg.addContents(command.getContents());
                getNotificationService().sendObject(msg);
                mainPageController.setStatusText("Receiving files");
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(new Date()).append(":\n").append("Receiving files in progress...\n");
                mainPageController.addLog(Constants.LogType.INFO, stringBuilder.toString());
                break;
            }
            case SEND_LIST_OF_FILES: {
                MessageToSend newMessage = new MessageToSend(getLocalEndPoint(), MessageToSend.COMMAND_TYPE.RECEIVE_LIST_OF_FILES);
                newMessage.addContents(new ArrayList<>(((AbstractClientController) mainPageController).getListOfFiles()));
                getNotificationService().sendObject(newMessage);
                break;
            }
            case RECEIVE_LIST_OF_FILES: {
                ArrayList<String> tmp = command.getContents().stream().map((object) -> (String) object).collect(Collectors.toCollection(ArrayList::new));
                filesToTransfer = ((AbstractClientController) mainPageController).compareUserAndServerList(tmp);
                if (filesToTransfer.getValue().size() > 0) {
                    MessageToSend newMessage = new MessageToSend(getLocalEndPoint(), MessageToSend.COMMAND_TYPE.TRANSFER_FILE_DEMAND);
                    newMessage.addContents(filesToTransfer.getValue().stream().map(string -> (Object) string).collect(Collectors.toCollection(ArrayList::new)));
                    getNotificationService().sendObject(newMessage);
                    mainPageController.setStatusText("Sending files");
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(new Date()).append(":\n").append("Sending files in progress...\n");
                    mainPageController.addLog(Constants.LogType.INFO, stringBuilder.toString());
                }
                if (((AbstractClientController) mainPageController).checkIfAreNewFiles(tmp)) {
                    MessageToSend newMessage = new MessageToSend(getLocalEndPoint(), MessageToSend.COMMAND_TYPE.RECEIVE_LIST_OF_FILES);
                    newMessage.addContents(((AbstractClientController) mainPageController).getListOfFiles().stream().map(string -> (Object) string).collect(Collectors.toCollection(ArrayList::new)));
                    getNotificationService().sendObject(newMessage);
                }
                break;
            }
            case SHARE_FILE_TO_USER: {
                getNotificationService().sendObject(command);
                StringBuilder builder = new StringBuilder();
                builder.append(new Date())
                        .append(":\n").append("Shared successfully: \n\t").append(((String)command.getContents().get(1)))
                        .append("\n").append("to:\n\t").append(command.getContents().get(0)).append("\n");
                mainPageController.addLog(Constants.LogType.INFO, builder.toString());
                mainPageController.setStatusText("Shared successfully");
                break;
            }
            case REMOVE_USER_FROM_FILE_OWNERS: {
                getNotificationService().sendObject(command);
                break;
            }
        }
    }

    /**
     * This method saves file in Client's <i>Download</i> directory
     * @param filePacket the data package to save on drive
     */
    @Override
    protected void saveFile(FilePacket filePacket) {
        File root = new File(getLocalEndPoint().getUserFolderPath());
        if (root.listFiles() != null) {
            File downloadFolder = new File(root, Constants.getClientDownloadDirectory(this));
            if (!downloadFolder.isDirectory()) {
                downloadFolder.mkdir();
            }
            File fileToSave = new File(downloadFolder, filePacket.getFileName());
            try (BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(fileToSave))) {
                long size = filePacket.getSize();
                writer.write(filePacket.getFileBytes(), 0, (int) size);
            } catch (IOException io) {
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
            }
            mainPageController.setStatusText("File saved successfully");
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(new Date())
                    .append(":\n").append("File saved successfully: ").append("\n\t")
                    .append(filePacket.getFileName());
            mainPageController.addLog(Constants.LogType.SUCCESS, stringBuilder.toString());
            mainPageController.refreshTree();
            if (((AbstractClientController) mainPageController).checkIfMinimized()) {
                new TrayIconNotification(getLocalEndPoint(), ((MainPageController) mainPageController).getStage());
            }

        }
    }

    /**
     * These method sends a list of files to server
     * @param data the entry of map that contains a user's credentials as a <code>key</code> and list of file names as a <code>value</code>
     */
    @Override
    protected synchronized void sendListOfFiles(Map.Entry<CredentialPacket, ArrayList<File>> data) {
        if (data == null || data.getValue().size() == 0 || data.getKey() == null) return;
        int filesSent = 0;
        for (File file : data.getValue()) {
            FilePacket packet;
            try {
                packet = new FilePacket(data.getKey().getUserName(), file);
                ++filesSent;
            }
            catch (IOException io){
                mainPageController.setStatusText("Can't send the file");
                StringBuilder stringBuilder = new StringBuilder();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                PrintStream outStream = new PrintStream(outputStream);
                io.printStackTrace(outStream);
                stringBuilder.append(new Date())
                        .append(":\n").append("Can't send the file: ")
                        .append(file.getName()).append("\n\t")
                        .append(io.getMessage()).append("\n")
                        .append(outStream.toString()).append("\n");
                mainPageController.addLog(Constants.LogType.ERROR, stringBuilder.toString());
                packet = null;
            }
            getFileService().sendObject(packet);
        }
        boolean success = filesSent == data.getValue().size();
        mainPageController.setStatusText(success ? "All files were sent" : "Files were sent partially");
        StringBuilder builder = new StringBuilder();
        builder.append(new Date()).append(":\n").append(success ? "All files were sent" : "Files were sent partially");
        mainPageController.addLog(Constants.LogType.INFO, builder.toString());
    }
}