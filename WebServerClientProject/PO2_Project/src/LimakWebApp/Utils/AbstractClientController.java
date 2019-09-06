package LimakWebApp.Utils;

import LimakWebApp.ClientSide.Client;
import LimakWebApp.DataPackets.CredentialPacket;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

/**
 * <h1>AbstractClientController</h1>
 * This class contains basic methods, constants for deriving AbstractClientControllers
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   01.09.2019
 */
public abstract class AbstractClientController extends Controller {

    /**
     * Hide window
     * @param stage Window to hide
     */
    public abstract void hideWindow(Object stage);

    /**
     * Basic constructor of {@link AbstractClientController}
     */
    public AbstractClientController(){
        super();
    }

    /**
     * This method should compare user's and server's lists
     * @param serverFileList list to compare
     * @return {@code Map.Entry<CredentialPacket, ArrayList<File>>}
     */
    public abstract Map.Entry<CredentialPacket, ArrayList<File>>  compareUserAndServerList(ArrayList<String> serverFileList);

    /**
     * This method should clean up resources
     */
    public abstract void cleanUp();

    /**
     * This method should check if there are new files on server for user
     * @param serverFileList List to compare
     * @return boolean
     */
    public abstract boolean checkIfAreNewFiles(ArrayList<String> serverFileList);

    /**
     * This method should show available users
     */
    public abstract void showUsers();

    /**
     * Returns Client
     * @return {@link Client}
     */
    public abstract Client getClient();

    /**
     * This method should update list of users
     * @param itemsToAdd items to update
     * @param which which list method should update
     */
    public abstract void updateListOfUsers(ArrayList<String> itemsToAdd, boolean which);

    /**
     * This method should run {@link LimakWebApp.ClientSide.ApacheWatchService}
     */
    public abstract void runWatcher();

    /**
     * This method should check if window is iconified
     * @return boolean
     */
    public abstract boolean checkIfMinimized();

    /**
     * Sets session ID
     * @param sessionID ID to set
     */
    public abstract void setSessionID(String sessionID);

    /**
     * Returns item to send
     * @return {@link File}
     */
    public abstract File getItemToSend();

    /**
     * Returns list of files
     * @return {@code  ArrayList<String>}
     */
    public abstract ArrayList<String> getListOfFiles();

    /**
     * Sets user's credentials
     * @param credentialPacket credentials to set
     */
    public abstract void setCredentialPacket(CredentialPacket credentialPacket);

    /**
     * Returns credentials of user
     * @return {@link CredentialPacket}
     */
    public abstract CredentialPacket getCredentialPacket();

    /**
     * Sets {@link Client}
     * @param client client to set
     */
    public abstract void setClient(Client client);

    /**
     * Sets item to send
     * @param file File to set
     */
    public abstract void setItemToSend(File file);
}
