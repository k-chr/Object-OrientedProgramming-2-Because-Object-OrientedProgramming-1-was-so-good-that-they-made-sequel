package LimakWebApp.ServerSide;

import LimakWebApp.DataPackets.CredentialPacket;

import java.io.File;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <h1>DiskMap</h1>
 * This class is used by {@link MainPageController} to show dependencies between users and files stored on server
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   12.08.2019
 */
public class DiskMap {

    private volatile ConcurrentHashMap<String, ArrayList<CredentialPacket>> dataMap;
    private String path;

    /**
     * This method returns the list of files' names that belong to given user.
     * @param keyUser - the user we want to get a list of files for
     * @return {@code ArrayList<String>}
     */
    public ArrayList<String> getListOfFilesForGivenUser(CredentialPacket keyUser){
        return dataMap.entrySet().stream()
                .filter(fileWithOwnersArrayListEntry -> fileWithOwnersArrayListEntry.getValue().stream()
                .anyMatch(owner -> owner.equals(keyUser)))
                .map(fileWithOwnersArrayListEntry -> fileWithOwnersArrayListEntry.getKey())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * This method returns the map of disk.
     * @return {@code ConcurrentHashMap<String, ArrayList<CredentialPacket>>}
     */
    public ConcurrentHashMap<String, ArrayList<CredentialPacket>> getMap(){
        return dataMap;
    }

    /**
     * Constructor of DiskMap class.
     * @param path - path for disk in server
     */
    public DiskMap(String path){
        dataMap = new ConcurrentHashMap<>();
        this.path = path;
    }

    /**
     * This method puts given user to list of owners of given file and returns <code>true</code> if succeeded, otherwise <code>false</code>
     * @param fileName - the name of file
     * @param owner - the owner of file
     * @return boolean
     */
    public synchronized boolean putOwnerToFile(String fileName, CredentialPacket owner) {
        boolean rV = false;
        synchronized (dataMap) {
            if (dataMap.isEmpty()) {
                ArrayList<CredentialPacket> items = new ArrayList<>();
                items.add(owner);
                dataMap.put(fileName, items);
            }
            for (Map.Entry<String, ArrayList<CredentialPacket>> entry : dataMap.entrySet()) {
                if (entry.getKey().equals(fileName)) {
                    if (!entry.getValue().contains(owner)) {
                        entry.getValue().add(owner);
                        rV = true;
                    }
                    break;
                }
            }
            if (!rV) {
                ArrayList<CredentialPacket> items = new ArrayList<>();
                items.add(owner);
                dataMap.put(fileName, items);
            }
        }
        return !rV;
    }

    /**
     * This method checks if given file name exists in map and returns <code>true</code> if exists, otherwise <code>false</code>
     * @param fileName - the name of file
     * @return boolean
     */
    public boolean checkIfFileExists(String fileName){
        return dataMap.keySet().stream().anyMatch(fName->fName.equals(fileName));
    }

    /**
     * This method removes given user from list of owners of given file and returns <code>true</code> if succeeded, otherwise <code>false</code>
     * @param fileName - the name of file
     * @param owner - the owner of file
     * @return boolean
     */
    public boolean removeFileOwner(String fileName, CredentialPacket owner){
        boolean found = false;
        for(Map.Entry<String, ArrayList<CredentialPacket>> entry : dataMap.entrySet()){
            if(entry.getKey().equals(fileName)){
                for(Iterator<CredentialPacket> iter = entry.getValue().iterator(); iter.hasNext();){
                    CredentialPacket packet = iter.next();
                    if(packet.equals(owner)){
                        iter.remove();
                        found = true;
                        break;
                    }
                }

            }
            if(found){
                if(entry.getValue().size() == 0){
                    File file= new File(path +"\\"+fileName);
                    file.delete();
                }
                break;
            }
        }
        return found;
    }

    void setDataMap(ConcurrentHashMap<String, ArrayList<CredentialPacket>> map){
        if(map != null) {
            dataMap = map;
        }
        else{
            dataMap = new ConcurrentHashMap<>();
        }
    }

}
