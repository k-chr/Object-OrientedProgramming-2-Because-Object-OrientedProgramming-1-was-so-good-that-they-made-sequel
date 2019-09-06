package LimakWebApp.Tests;

import LimakWebApp.DataPackets.CredentialPacket;
import LimakWebApp.ServerSide.DiskMap;
import LimakWebApp.ServerSide.EmailUtil;
import LimakWebApp.ServerSide.ListOfClients;
import LimakWebApp.ServerSide.Server;
import LimakWebApp.Utils.AbstractServerController;
import LimakWebApp.Utils.Constants;
import LimakWebApp.Utils.DataPair;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.stream.JsonReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * <h1>TestServerController</h1>
 * This class performs operations and some business logic for Server for test suite.
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   2.09.2019
 */
public class TestServerController extends AbstractServerController {

    private String status;
    private final Object lock = new Object();
    private static Server server;
    private volatile CredentialPacket credentialPacket;
    private ScheduledExecutorService scheduler;
    private EmailUtil emailSession;
    private volatile ConcurrentHashMap<CredentialPacket, ArrayList<String>> runTimeMapOfFileOwners;
    private volatile ListOfClients listOfClients;
    private volatile ArrayList<DiskMap> serverDiskMap;
    private volatile ArrayList<DataPair> packetBooleanList;
    private volatile Set<String> ids;

    /**
     * Basic constructor of {@link TestServerController}
     * @param testPacket Data of {@link Server}
     */
    public TestServerController(CredentialPacket testPacket){
        ids = new HashSet<>();
        this.credentialPacket = testPacket;
        String[] folderNames = Constants.getDirectories(this);
        createServerDirectoriesIfNotExist(folderNames);
        createImportantStartUpFilesIfNotExist();
        listOfClients = new ListOfClients();
        runTimeMapOfFileOwners = new ConcurrentHashMap<>();
        readInitListOfClientsFromFile();
        serverDiskMap = new ArrayList<>();
        emailSession = new EmailUtil(this);
        emailSession.testSession();
        for(int i = 0; i < 5; ++i){
            serverDiskMap.add(new DiskMap(credentialPacket.getUserFolderPath() + "\\" + Constants.getDirectories(this)[i]));
            serverDiskMap.get(i).setDataMap(readContentsFromJson(i));
        }
        listOfClients.put(credentialPacket, true);
        packetBooleanList = new ArrayList<>(listOfClients.toDataPairSet());
        fillRunTimeMap();
        scheduler = Executors.newScheduledThreadPool(4);
    }

    /**
     * Sets {@link Server} to this class
     * @param server server to set
     */
    public void setServer(Server server) {
        TestServerController.server = server;
    }


    private void readInitListOfClientsFromFile(){
        File root = new File(credentialPacket.getUserFolderPath());
        File listOfClientsFile = new File(root, Constants.getListOfClientsFileName(this));
        Gson gson = new Gson();
        try (JsonReader jsonReader = new JsonReader(new FileReader(listOfClientsFile))){
            CredentialPacket[] packets = gson.fromJson(jsonReader, CredentialPacket[].class);
            if(packets != null) {
                for (CredentialPacket packet : packets) {
                    listOfClients.put(packet, false);
                }
            }
        }
        catch(IOException| JsonParseException io) {
            setStatusText("Can't read or parse file");
        }
    }

    private synchronized void removeId(String strId){
        synchronized (lock) {
            ids.remove(strId);
        }
    }

    private synchronized ConcurrentHashMap<String, ArrayList<CredentialPacket>> readContentsFromJson(int disk){
        ConcurrentHashMap<String, ArrayList<LinkedTreeMap<String,String>>> outMap = new ConcurrentHashMap<>();
        File root = new File(credentialPacket.getUserFolderPath());
        File subFolder = new File(root, Constants.getDirectories(this)[disk]);
        File fileToRead = new File(subFolder, Constants.getDirectoriesControlFile(this));
        if(fileToRead.length() < 1){
            return new ConcurrentHashMap<>();
        }
        Gson gson = new Gson();
        try(JsonReader reader = new JsonReader(new FileReader(fileToRead))){
            outMap = gson.fromJson(reader, outMap.getClass());
        }
        catch(IOException io) {
            setStatusText("Can't read file");
        }
        ConcurrentHashMap<String, ArrayList<CredentialPacket>> rV = new ConcurrentHashMap<>();
        for(Map.Entry<String, ArrayList<LinkedTreeMap<String,String>>> node : outMap.entrySet()){
            ArrayList<CredentialPacket> list = node.getValue().stream().map(e -> new CredentialPacket(e.get("userEmail"), e.get("userName"), e.get("userFolderPath"))).collect(Collectors.toCollection(ArrayList::new));
            rV.put(node.getKey(), list);
        }
        return rV;
    }

    private void createServerDirectoriesIfNotExist(String[] directories){
        String stringPath = credentialPacket.getUserFolderPath();
        File rootFile = new File (stringPath);
        if(!rootFile.exists()){
            rootFile.mkdirs();
        }
        File[] listingDir = rootFile.listFiles();
        if(listingDir == null || ( listingDir.length < 5)) {
            for (String str : directories) {
                new File(stringPath + "\\" + str).mkdir();
            }
        }
        return;
    }


    /**
     * This method adds given file to user ownership's list
     * @param user User who owns file
     * @param fileName file to add
     */
    @Override
    public void addToRuntimeMap(CredentialPacket user, String fileName) {
        for (Map.Entry<CredentialPacket, ArrayList<String>> next : runTimeMapOfFileOwners.entrySet()) {
            if (next.getKey().equals(user)) {
                next.getValue().add(fileName);
                break;
            }
        }
    }

    /**
     * This method removes users's ownership of provided file
     * @param fileName File to remove ownership
     * @param user User, the former owner of file
     * @return boolean
     */
    @Override
    public boolean removeUserFromFileOwners(String fileName, CredentialPacket user) {
        boolean rV = false;
        for(DiskMap disk : serverDiskMap){
            if(disk.checkIfFileExists(fileName)){
                rV = disk.removeFileOwner(fileName, user);
                break;
            }
        }
        return rV;
    }

    /**
     * This method shares file to user
     * @param to User who will receive a new file
     * @param item item to share
     */
    @Override
    public void shareFile(CredentialPacket to, String item) {
        addUserToFileOwners(item,to);
        if(getActiveListOfClients().contains(to)){
            if(!runTimeMapOfFileOwners.get(to).contains(item)){
                runTimeMapOfFileOwners.get(to).add(item);
            }
            server.shareToUser(to, item);
        }
    }

    /**
     * Returns list of inactive users
     * @return {@code ArrayList<CredentialPacket>}
     */
    @Override
    public ArrayList<CredentialPacket> getInactiveListOfClients() {
        return listOfClients.entrySet().stream()
                .filter(client-> !client.getValue() && !client.getKey().getUserName().equals("Server"))
                .map(client->client.getKey())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Returns list of active users
     * @return {@code ArrayList<CredentialPacket>}
     */
    @Override
    public ArrayList<CredentialPacket> getActiveListOfClients() {
        return listOfClients.entrySet().stream()
                .filter(client->client.getValue() && !client.getKey().getUserName().equals("Server"))
                .map(client->client.getKey())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Returns set of filenames owned by given user
     * @param packet User who probably owns some files on server
     * @return {@code Set<String>}
     */
    @Override
    public Set<String> getListOfFilesForUser(CredentialPacket packet) {
        Set<String> rV = new HashSet<>();
        for(DiskMap disk : serverDiskMap){
            if(disk == null || disk.getMap() == null || disk.getMap().isEmpty()) continue;
            rV.addAll(disk.getListOfFilesForGivenUser(packet));
        }
        return rV;
    }

    /**
     * Compares server's list of files for given user with user's list of files
     * @param user User who owns some files on server
     * @param userFileList list to compare
     * @return {@code Map.Entry< CredentialPacket, ArrayList<File>>}
     */
    @Override
    public Map.Entry<CredentialPacket, ArrayList<File>> compareUserAndServerList(CredentialPacket user, ArrayList<String> userFileList) {
        Map.Entry<CredentialPacket, ArrayList<File>> rV = null;
        ArrayList<File> outList = new ArrayList<>();
        ArrayList<String> tmp = new ArrayList<>(getListOfFilesForUser(user));
        for(String fileName: tmp){
            if(!userFileList.contains(fileName)){
                outList.add(new File(findFileInServer(fileName) ,fileName));
            }
        }
        rV = new AbstractMap.SimpleEntry<>(user, outList);
        return rV;
    }

    private synchronized boolean addUserToFileOwners(String fileName, CredentialPacket user){
        boolean rV = false;
        synchronized (lock) {
            for (DiskMap disk : serverDiskMap) {
                if (disk.checkIfFileExists(fileName)) {
                    if (!user.isEmpty()) {
                        rV = disk.putOwnerToFile(fileName, user);
                    }
                    break;
                }
            }
        }
        return rV;
    }

    private void createImportantStartUpFilesIfNotExist(){
        File rootFile = new File (credentialPacket.getUserFolderPath());
        if(rootFile.exists()){
            File clientsFile = new File(rootFile, Constants.getListOfClientsFileName(this));
            if(!clientsFile.exists()){
                try(BufferedWriter ignored = new BufferedWriter(new FileWriter(clientsFile))){}
                catch (IOException io){
                    io.printStackTrace();
                }
            }
            for(File f : rootFile.listFiles()){
                if(f.isDirectory()){
                    File contentFile = new File(f, Constants.getDirectoriesControlFile(this));
                    if(!contentFile.exists()){
                        try(BufferedWriter ignored = new BufferedWriter(new FileWriter(contentFile))){}
                        catch(IOException io){
                            io.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private void fillRunTimeMap(){
        for(Map.Entry<CredentialPacket, Boolean> entry : listOfClients.entrySet()){
            if(!entry.getKey().getUserName().equals("Server")) {
                runTimeMapOfFileOwners.put(entry.getKey(), getListOfFilesForUser(entry.getKey()).stream().collect(Collectors.toCollection(ArrayList::new)));
            }
        }
    }

    /**
     * Perform clean of {@link TestServerController} resources
     */
    @Override
    public void cleanUp() {
        dumpListOfClients();
        for(int i = 0; i < serverDiskMap.size(); ++i){
            dumpContentsToJson(i);
        }
        setStatusText("Closing...");
        addLog(Constants.LogType.INFO, new Date().toString() + ":\nClosing...\n");
        emailSession.dropSession();
        server.setItTimeToStop(true);
        scheduler.shutdown();
        pool.shutdown();
        try {
            scheduler.awaitTermination(10, SECONDS);
            pool.awaitTermination(10, SECONDS);
        }
        catch(InterruptedException ie){
            scheduler.shutdownNow();
            pool.shutdownNow();
        }
        server.clearUpConnection();
    }

    /**
     * Puts given id to {@code Set<String>}
     * @param strId ID to put
     */
    @Override
    public void putId(String strId) {
        synchronized(lock){ids.add(strId);}
    }

    /**
     * Returns the path to parent directory of given file if exists
     * @param fileName File to find
     * @return {@link String}
     */
    @Override
    public String findFileInServer(String fileName) {
        for(String dir : Constants.getDirectories(this)){
            String outName = credentialPacket.getUserName() + "\\" + dir;
            File tmp = new File(outName);
            if(tmp.exists() && tmp.isDirectory()){
                for(String file : tmp.list()){
                    if(file.equals(fileName)){
                        return outName;
                    }
                }
            }
        }
        return "";
    }

    private synchronized void dumpContentsToJson(int disk){
        File root = new File(credentialPacket.getUserFolderPath());
        File subFolder = new File(root, Constants.getDirectories(this)[disk]);
        File fileToDump = new File(subFolder, Constants.getDirectoriesControlFile(this));
        Gson gson = new Gson();
        try(Writer writer= new FileWriter(fileToDump)){
            ConcurrentHashMap<String, ArrayList<CredentialPacket>> toDump = serverDiskMap.get(disk).getMap();
            gson.toJson(toDump, writer);
        }
        catch (IOException io) {
            setStatusText("Can't save file");
        }
    }
    private void dumpListOfClients(){
        File root = new File(credentialPacket.getUserFolderPath());
        File listOfClientsFile = new File(root, Constants.getListOfClientsFileName(this));
        Gson gson= new Gson();
        try(Writer writer = new FileWriter(listOfClientsFile)) {
            CredentialPacket[] packets = listOfClients.keySet().toArray(new CredentialPacket[listOfClients.keySet().size()]);
            gson.toJson(packets, writer);
            setStatusText("File is saved successfully");
        }
        catch(IOException io) {
            setStatusText("Can't save file");
        }
    }

    /**
     * Cleans up session for given ID and user
     * @param ID Id of session
     * @param user Session's user
     */
    @Override
    public void cleanUpSessionForID(String ID, CredentialPacket user) {
        removeId(ID);
        updateListOfClients(user, false);
    }

    /**
     * Finds user by provided name, returns his credentials or empty packet.
     * @param userName User to find
     * @return {@link CredentialPacket}
     */
    @Override
    public CredentialPacket findUserByName(String userName) {
        return listOfClients.entrySet().stream().map(entry->entry.getKey()).filter(user-> user.getUserName().equals(userName)).findAny().orElse(new CredentialPacket("","",""));
    }

    /**
     * Generates Id for session
     * @param accessor valid Object
     * @return {@link String}
     */
    @Override
    public String generateID(Object accessor) {
        if(! (accessor instanceof LimakWebApp.Utils.AbstractServerController)) return null;
        Random generator = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        while(true) {
            for (int i = 0; i < getMaxSize(); ++i) {
                int chk = generator.nextInt() % 3;
                char c = chk == 2 ?
                        (char)(65 + generator.nextInt(26))
                        : (chk == 1 ?
                        (char)(97 + generator.nextInt(26))
                        : (char)(48 + generator.nextInt(10)));
                stringBuilder.append(c);
            }
            if(!checkIfIdIsValid(stringBuilder.toString())){
                return stringBuilder.toString();
            }
        }
    }
    private synchronized boolean checkIfIdIsValid(String idToPut){
        return ids.contains(idToPut);
    }

    /**
     * Updates list of clients
     * @param packet User to update
     * @param value Value to update
     */
    @Override
    public synchronized void updateListOfClients(CredentialPacket packet, Boolean value){
        synchronized (lock) {
            listOfClients.put(packet, value);
        }
        synchronized (lock) {
            packetBooleanList.clear();
            packetBooleanList.addAll(listOfClients.toDataPairSet());
        }
    }

    /**
     * this method accepts clients
     */
    @Override
    public void authorize() {
        server.acceptClients();
    }

    /**
     * This method returns reference to {@link DiskMap}
     * @param idx Index to find
     * @return {@link DiskMap}
     */
    @Override
    public DiskMap getDisk(int idx) {
        return serverDiskMap.get(idx);
    }

    /**
     * Returns data of user
     * @return {@link CredentialPacket}
     */
    @Override
    public CredentialPacket getCredentialPacket() {
        return credentialPacket;
    }

    /**
     * Returns reference to {@link Server}
     * @return {@link Server}
     */
    @Override
    public Server getServer() {
        return server;
    }

    /**
     * Returns email session for server
     * @return {@link EmailUtil}
     */
    @Override
    public EmailUtil getEmailSession() {
        return emailSession;
    }

    /**
     * Returns list of clients
     * @return {@link ListOfClients}
     */
    @Override
    public ListOfClients getListOfClients() {
        return  listOfClients;
    }

    /**
     * This method is prepared to be implemented by deriving classes, this method should add new log for Controller
     * @param type Type of log
     * @param body Contents
     */
    @Override
    public void addLog(Constants.LogType type, String body) { }

    /**
     * This method should set status bar in deriving Controller
     * @param text Text to set
     */
    @Override
    public void setStatusText(String text) { status = text; }

    /**
     * This method should display TreeView
     */
    @Override
    public void displayTree() { }

    /**
     * This method should clear root
     */
    @Override
    public void clearRoot() { }

    /**
     * This method should refresh TreeView owned by deriving Controller
     */
    @Override
    public void refreshTree() { }

    /**
     * Returns status for testing purpose
     * @return {@link String}
     */
    public String getStatus() {
        return status;
    }
}
