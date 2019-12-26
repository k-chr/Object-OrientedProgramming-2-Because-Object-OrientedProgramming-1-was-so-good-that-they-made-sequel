package LimakWebApp.Utils;

import LimakWebApp.DataPackets.CredentialPacket;
import LimakWebApp.ServerSide.DiskMap;
import LimakWebApp.ServerSide.EmailUtil;
import LimakWebApp.ServerSide.ListOfClients;
import LimakWebApp.ServerSide.Server;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * <h1>AbstractServerController</h1>
 * This class contains basic methods, constants for deriving AbstractServerControllers
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   01.09.2019
 */
public abstract class AbstractServerController extends Controller {

    /**
     * Basic constructor of {@link AbstractServerController}
     */
    public AbstractServerController(){
        super();
    }

    /**
     * Should add file to user in run time map
     * @param user Owner of file
     * @param fileName File to add
     */
    public abstract void addToRuntimeMap(CredentialPacket user, String fileName);

    /**
     * Should remove user from file owners
     * @param fileName key File
     * @param user User to remove
     * @return boolean
     */
    public abstract boolean removeUserFromFileOwners(String fileName, CredentialPacket user);

    /**
     * Should share file to user
     * @param to User who'll receive a file
     * @param item Item to share
     */
    public abstract void shareFile(CredentialPacket to, String item);

    /**
     * should return list of inactive users
     * @return {@code ArrayList<CredentialPacket>}
     */
    public abstract ArrayList<CredentialPacket> getInactiveListOfClients();

    /**
     * should return list of inactive users
     * @return {@code ArrayList<CredentialPacket>}
     */
    public abstract ArrayList<CredentialPacket> getActiveListOfClients();

    /**
     * Should return list of user for file
     * @param packet User to compute list of files
     * @return {@code Set<String>}
     */
    public abstract Set<String> getListOfFilesForUser(CredentialPacket packet);

    /**
     * Should compare user and server List
     * @param user owner of files
     * @param userFileList user's list to compare
     * @return {@code Map.Entry<CredentialPacket, ArrayList<File>>}
     */
    public abstract Map.Entry<CredentialPacket, ArrayList<File>> compareUserAndServerList(CredentialPacket user, ArrayList<String> userFileList);

    /**
     * This method should clean up
     */
    public abstract void cleanUp();

    /**
     * This method should put id to set of ids
     * @param strId id to put
     */
    public abstract void putId(String strId);

    /**
     * This method should find file in server
     * @param fileName file to find
     * @return {@link String}
     */
    public abstract String findFileInServer(String fileName);

    /**
     * This method should update list of clients
     * @param packet User to update
     * @param value Value to update
     */
    public abstract void updateListOfClients(CredentialPacket packet, Boolean value);

    /**
     * This method should authorize new clients
     */
    public abstract void authorize();

    /**
     * This method should clean up session for given ID and user credentials
     * @param ID Session's ID
     * @param user Session's user
     */
    public abstract void cleanUpSessionForID(String ID, CredentialPacket user);

    /**
     * Should return data of provided client or empty packet if given user is not present in Server
     * @param userName user ti find
     * @return {@link CredentialPacket}
     */
    public abstract CredentialPacket findUserByName(String userName);

    /**
     * This method should generate ID
     * @param accessor valid object
     * @return {@link String}
     */
    public abstract String generateID(Object accessor);

    /**
     * Should return {@link DiskMap} under provided index
     * @param idx Index
     * @return {@link DiskMap}
     */
    public abstract DiskMap getDisk(int idx);

    /**
     * This method should return {@link Server}'s credentials
     * @return {@link CredentialPacket}
     */
    public abstract CredentialPacket getCredentialPacket();

    /**
     * Should return reference to {@link Server}
     * @return {@link Server}
     */
    public abstract Server getServer();

    /**
     * Should return reference to email session held by this {@link AbstractServerController}
     * @return {@link EmailUtil}
     */
    public abstract EmailUtil getEmailSession();

    /**
     * This method should return list of clients
     * @return {@link ListOfClients}
     */
    public abstract ListOfClients getListOfClients();
}
