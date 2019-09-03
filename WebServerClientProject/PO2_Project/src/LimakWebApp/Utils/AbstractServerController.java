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

public abstract class AbstractServerController extends Controller {

    public AbstractServerController(){
        super();
    }

    public abstract void addToRuntimeMap(CredentialPacket user, String fileName);
    public abstract boolean removeUserFromFileOwners(String fileName, CredentialPacket user);
    public abstract void shareFile(CredentialPacket to, String item);

    public abstract ArrayList<CredentialPacket> getInactiveListOfClients();

    public abstract ArrayList<CredentialPacket> getActiveListOfClients();

    public abstract Set<String> getListOfFilesForUser(CredentialPacket packet);

    public abstract Map.Entry<CredentialPacket, ArrayList<File>> compareUserAndServerList(CredentialPacket user, ArrayList<String> userFileList);

    public abstract void cleanUp();

    public abstract void putId(String strId);

    public abstract String findFileInServer(String fileName);

    public abstract void updateListOfClients(CredentialPacket packet, Boolean value);

    public abstract void authorize();

    public abstract void cleanUpSessionForID(String ID, CredentialPacket user);

    public abstract CredentialPacket findUserByName(String userName);

    public abstract String generateID(Object accessor);

    public abstract DiskMap getDisk(int idx);

    public abstract CredentialPacket getCredentialPacket();

    public abstract Server getServer();

    public abstract EmailUtil getEmailSession();

    public abstract ListOfClients getListOfClients();
}
