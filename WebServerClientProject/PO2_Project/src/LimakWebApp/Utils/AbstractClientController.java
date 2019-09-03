package LimakWebApp.Utils;

import LimakWebApp.ClientSide.Client;
import LimakWebApp.DataPackets.CredentialPacket;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public abstract class AbstractClientController extends Controller {

    /**
     * Hide window
     * @param stage Window to hide
     */
    public abstract void hideWindow(Object stage);

    public AbstractClientController(){
        super();
    }

    public abstract Map.Entry<CredentialPacket, ArrayList<File>>  compareUserAndServerList(ArrayList<String> serverFileList);


    public abstract void cleanUp();

    public abstract boolean checkIfAreNewFiles(ArrayList<String> serverFileList);

    public abstract void showUsers();

    public abstract Client getClient();

    public abstract void updateListOfUsers(ArrayList<String> itemsToAdd, boolean which);

    public abstract void runWatcher();

    public abstract boolean checkIfMinimized();

    public abstract void setSessionID(String sessionID);

    public abstract File getItemToSend();

    public abstract ArrayList<String> getListOfFiles();

    public abstract ExecutorService getWatcherServiceTh();

    public abstract void setCredentialPacket(CredentialPacket credentialPacket);

    public abstract CredentialPacket getCredentialPacket();

    public abstract void setClient(Client client);

    public abstract void setItemToSend(File file);
}
