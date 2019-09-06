package LimakWebApp.ClientSide;

import LimakWebApp.DataPackets.MessageToSend;
import LimakWebApp.Utils.AbstractClientController;
import LimakWebApp.Utils.Constants;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Date;

/**
 * <h1>ApacheWatchService</h1>
 * This class is used to watch {@link Client}'s directory changes.
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   31.08.2019
 */
public class ApacheWatchService {

    private FileAlterationListener listener;
    private FileAlterationMonitor watcher;
    private FileAlterationObserver observer;
    private AbstractClientController clientController;
    private final int POLLING_INTERVAL = 0x3E8;

    /**
     * Basic constructor of {@link ApacheWatchService}
     */
    public ApacheWatchService() {

    }

    /**
     * Overloaded constructor of {@link ApacheWatchService}
     * @param abstractClientController Controller to get ability to log exceptions
     * @throws IOException if root directory doesn't exist
     */
    public ApacheWatchService(AbstractClientController abstractClientController) throws IOException {
        this.clientController = abstractClientController;
        File root;
        synchronized (this.clientController.getCredentialPacket().getUserFolderPath()) {
            root = new File(this.clientController.getCredentialPacket().getUserFolderPath());
        }
        if (!root.isDirectory()) {
            throw new IOException(new Date().toString() + " Directory doesn't exist: " + root.getAbsolutePath());
        }

        observer = new FileAlterationObserver(root);
        watcher = new FileAlterationMonitor(POLLING_INTERVAL);
        listener = new FileAlterationListenerAdaptor() {

            @Override
            public void onFileCreate(File file) {
                File parent = file.getParentFile();
                clientController.refreshTree();
                if (parent.getName().equals("Downloads")) {
                    return;
                }
                clientController.setItemToSend(file);
                clientController.getClient().demandForTransferEnforcedByWatchService(file);
            }

            @Override
            public void onFileDelete(File file) {
                MessageToSend command = new MessageToSend(clientController.getCredentialPacket(), MessageToSend.COMMAND_TYPE.REMOVE_USER_FROM_FILE_OWNERS);
                ArrayList<Object> arrList = new ArrayList<>();
                arrList.add(clientController.getCredentialPacket().getUserName());
                arrList.add(file.getName());
                command.addContents(arrList);
                clientController.refreshTree();
                clientController.getClient().rcvCmd(command);
            }
        };
        observer.addListener(listener);
        watcher.addObserver(observer);
    }

    /**
     * Quits {@link ApacheWatchService}
     */
    public void quit(){
        try {
            if(watcher != null && observer != null) {
                this.watcher.stop();
                this.observer.destroy();
            }
        } catch (Exception e) {
            clientController.setStatusText("Exception with watcher occurred");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream outStream = new PrintStream(outputStream);
            e.printStackTrace(outStream);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(new Date())
                    .append(":\n").append("Exception with ApacheWatchService occurred!")
                    .append(e.getMessage()).append("\n")
                    .append(outStream.toString()).append("\n");
            clientController.addLog(Constants.LogType.ERROR, stringBuilder.toString());
        }
    }

    /**
     * Starts {@link ApacheWatchService}
     */
    public void runWatcher(){
        try{
            watcher.start();
        }
        catch(Exception e){
            clientController.setStatusText("Exception with watcher occurred");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream outStream = new PrintStream(outputStream);
            e.printStackTrace(outStream);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(new Date())
                    .append(":\n").append("Exception with ApacheWatchService occurred!")
                    .append(e.getMessage()).append("\n")
                    .append(outStream.toString()).append("\n");
            clientController.addLog(Constants.LogType.ERROR, stringBuilder.toString());
        }
    }
}
