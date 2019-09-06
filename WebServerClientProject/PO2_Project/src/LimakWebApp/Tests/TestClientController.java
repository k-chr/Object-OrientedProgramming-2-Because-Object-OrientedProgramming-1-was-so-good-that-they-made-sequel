package LimakWebApp.Tests;

import LimakWebApp.ClientSide.ApacheWatchService;
import LimakWebApp.ClientSide.Client;
import LimakWebApp.DataPackets.CredentialPacket;
import LimakWebApp.Utils.AbstractClientController;
import LimakWebApp.Utils.Constants;

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * <h1>TestClientController</h1>
 * This class performs  operations and some business logic for Client for test suite.
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   2.09.2019
 */
public class TestClientController extends  AbstractClientController {

    private volatile String log="";
    private ReadWriteLock lock;
    private CredentialPacket credentialPacket;
    private Client client;
    private File itemToSend = null;
    private File itemToShare = null;
    private volatile ArrayList<File> listOfFiles;
    private volatile ArrayList<String> activeUsers;
    private volatile ArrayList<String> inactiveUsers;
    private String sessionID;
    private volatile String status;
    private ApacheWatchService watchService;

    /**
     * Basic constructor of {@link TestClientController}
     */
    public TestClientController(){
        super();
        lock = new ReentrantReadWriteLock();
        listOfFiles = new ArrayList<>();
        activeUsers = new ArrayList<>();
        inactiveUsers = new ArrayList<>();
        watchService = null;
    }

    /**
     * This method pretends to hide window
     * @param stage Window to hide
     */
    @Override
    public void hideWindow(Object stage) {
        System.out.println("Hide window");
    }

    /**
     * This method returns the file user has chosen to share
     * @return {@link File}
     */
    public File getItemToShare() {
        return itemToShare;
    }


    /**
     * This method sets item to share
     * @param itemToShare File to set
     */
    public void setItemToShare(File itemToShare) {
        this.itemToShare = itemToShare;
    }

    /**
     * This method cleans all connections and stops {@link java.util.concurrent.Executors} from performing any action
     */
    @Override
    public void cleanUp() {
        setStatusText("Closing...");
        client.dropConnection(true);
        pool.shutdown();
        if(watchService != null) {
            watchService.quit();
        }
        try{
            pool.awaitTermination(3, SECONDS);
        }
        catch(InterruptedException ie) {
            pool.shutdownNow();
        }

        boolean rV = client.isClosed() == false;
        if(rV) {
            client.close();
        }
    }

    /**
     * This method checks if there are new files on server
     * @param serverFileList Files' list to compare
     * @return boolean
     */
    @Override
    public boolean checkIfAreNewFiles(ArrayList<String> serverFileList) {
        ArrayList<String> outList = new ArrayList<>();
        lock.readLock().lock();
        try{
            ArrayList<String> tmp = listOfFiles.stream().map(file->file.getName()).collect(Collectors.toCollection(ArrayList::new));
            if (serverFileList.size() > 0) {
                for (String fileName : serverFileList) {
                    if (!tmp.contains(fileName)) {
                        outList.add(fileName);
                    }
                }
            } else {
                outList.addAll(tmp);
            }
        }finally {
            lock.readLock().unlock();
        }
        return outList.size() > 0;
    }

    @Override
    public void showUsers() { }

    /**
     * Returns reference to held {@link Client}
     * @return {@link Client}
     */
    @Override
    public Client getClient() {
        return client;
    }

    /**
     * Updates list of users by given list
     * @param itemsToAdd List of users to add
     * @param which indicates the list to update
     */
    @Override
    public void updateListOfUsers(ArrayList<String> itemsToAdd, boolean which) {
        if (which) {
            activeUsers.clear();
            activeUsers.addAll(itemsToAdd.stream().filter(name->!name.equals(credentialPacket.getUserName())).collect(Collectors.toCollection(ArrayList::new)));
        } else {
            inactiveUsers.clear();
            inactiveUsers.addAll(itemsToAdd);
        }
    }

    /**
     * Runs watcher
     */
    @Override
    public void runWatcher() {
        watchService.runWatcher();
    }


    @Override
    public boolean checkIfMinimized() {
        return false;
    }

    /**
     * This method sets Session ID
     * @param sessionID ID to set
     */
    @Override
    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    /**
     * This method returns text of status bar (that not exists because I turned off GUI operations in tests' classes)
     * @return {@link String}
     */
    public String getStatus() {
        return status;
    }

    /**
     * This method returns item to send
     * @return {@link File}
     */
    @Override
    public File getItemToSend() {
        return itemToSend;
    }

    /**
     * This method returns list of files of user
     * @return {@code ArrayList<String>}
     */
    @Override
    public ArrayList<String> getListOfFiles() {
        return listOfFiles.stream().map(file->file.getName()).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * This method adds new message to log
     * @param type Type of log
     * @param body Contents
     */
    @Override
    public void addLog(Constants.LogType type, String body) { log=body; }

    /**
     * Instead of displaying log in GUI this method returns text held in log
     * @return {@link String}
     */
    public String getLog() {
        return log;
    }

    /**
     * This method sets text to field, without GUI
     * @param text Text to set
     */
    @Override
    public synchronized void setStatusText(String text) {
        lock.writeLock().lock();
        try {
            status = text;
        }finally {
            lock.writeLock().unlock();
        }
    }


    @Override
    public void displayTree() {}


    @Override
    public void clearRoot() {}


    @Override
    public void refreshTree() {}


    /**
     * This method compares user and server list of files
     * @param serverFileList List of files stored on server by this user
     * @return {@code Map.Entry<CredentialPacket, ArrayList<File>>}
     */
    @Override
    public Map.Entry<CredentialPacket, ArrayList<File>> compareUserAndServerList(ArrayList<String> serverFileList) {
        Map.Entry<CredentialPacket, ArrayList<File>> rV = null;
        ArrayList<File> outList = new ArrayList<>();
        lock.writeLock().lock();
        try {
            ArrayList<File> tmp = listOfFiles;
            if (serverFileList.size() > 0) {
                for (File file : tmp) {
                    if (!serverFileList.contains(file.getName())) {
                        outList.add(file);
                    }
                }
            } else {
                outList.addAll(tmp);
            }
        } finally {
            lock.writeLock().unlock();
        }
        rV = new AbstractMap.SimpleEntry<>(credentialPacket, outList);
        return rV;
    }

    /**
     * This method sets client to controller
     * @param client Reference to {@link Client}
     */
    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * This method sets item to send (detected by {@link ApacheWatchService})
     * @param file File detected by watcher to send.
     */
    @Override
    public void setItemToSend(File file) {
        itemToSend = file;
    }


    /**
     * This method sets data of owner to controller
     * @param credentialPacket Credentials of user
     */
    @Override
    public void setCredentialPacket(CredentialPacket credentialPacket) {
        this.credentialPacket = credentialPacket;
    }

    /**
     * This method returns credentials of user
     * @return {@link CredentialPacket}
     */
    @Override
    public CredentialPacket getCredentialPacket() {
        return credentialPacket;
    }

    /**
     * This method sets {@code watchService}
     * @param apacheWatchService An {@link ApacheWatchService}'s object to set
     */
    public void setWatchService(ApacheWatchService apacheWatchService) {
        watchService = apacheWatchService;
    }

    /**
     * This method creates directories for client
     */
    public void createDirectories() {
        lock.writeLock().lock();
        try {
            File dir = new File(credentialPacket.getUserFolderPath());
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
        }finally {
            lock.writeLock().unlock();
        }
    }
}
