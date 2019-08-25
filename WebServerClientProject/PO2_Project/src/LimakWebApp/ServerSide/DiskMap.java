package LimakWebApp.ServerSide;

import LimakWebApp.DataPackets.CredentialPacket;

import java.io.File;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DiskMap {

    private volatile ConcurrentHashMap<String, ArrayList<CredentialPacket>> dataMap;
    private String path;

    public ArrayList<String> getListOfFilesForGivenUser(CredentialPacket keyUser){
        return dataMap.entrySet().stream()
                .filter(fileWithOwnersArrayListEntry -> fileWithOwnersArrayListEntry.getValue().stream()
                .anyMatch(owner -> owner.equals(keyUser)))
                .map(fileWithOwnersArrayListEntry -> fileWithOwnersArrayListEntry.getKey())
                .collect(Collectors.toCollection(ArrayList::new));
    }
    public ConcurrentHashMap<String, ArrayList<CredentialPacket>> getMap(){
        return dataMap;
    }

    public DiskMap(String path){
        dataMap = new ConcurrentHashMap<>();
        this.path = path;
    }

    public boolean putOwnerToFile(String fileName, CredentialPacket owner) {
        boolean rV = false;
        if(dataMap.isEmpty()){
            ArrayList<CredentialPacket> items = new ArrayList<>();
            items.add(owner);
            dataMap.put(fileName, items);
        }
        for(Map.Entry<String, ArrayList<CredentialPacket>> entry : dataMap.entrySet()){
            if(entry.getKey().equals(fileName)){
                if(!entry.getValue().contains(owner)) {
                    entry.getValue().add(owner);
                    rV = true;
                }
                break;
            }
        }
        if(!rV){
            ArrayList<CredentialPacket> items = new ArrayList<>();
            items.add(owner);
            dataMap.put(fileName, items);
        }
        return rV;
    }

    public boolean checkIfFileExists(String fileName){
        return dataMap.keySet().stream().anyMatch(fName->fName.equals(fileName));
    }

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
