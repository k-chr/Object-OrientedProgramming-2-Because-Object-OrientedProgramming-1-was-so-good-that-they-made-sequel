package LimakWebApp.ServerSide;

import LimakWebApp.DataPackets.CredentialPacket;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <h1>ListOfClients</h1>
 * This class is used by {@link MainPageController} manage users registered on service
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   23.07.2019
 */
public class ListOfClients extends ConcurrentHashMap<CredentialPacket, Boolean>  {

    @Override
    public Boolean put(CredentialPacket C, Boolean B){
        if(C != null && this.size() == 0){
            return super.put(C,B);
        }
        for(Entry<CredentialPacket, Boolean> e : this.entrySet()){
            if(e.getKey().compareTo(C) != 9 && e.getKey().compareTo(C) != 0){
                return false;
            }
            else if(e.getKey().compareTo(C) == 0){
                return super.replace(C,B);
            }
        }
        return super.put(C,B);
    }

    /**
     * This method converts map of clients into Set of {@link DataPair} to maintain list view of clients on server.
     * @return {@code Set<DataPair>}
     */
    public Set<DataPair> toDataPairSet(){
        Set<DataPair> set = new HashSet<>();
        for(Map.Entry<CredentialPacket, Boolean> entry : entrySet()){
            set.add(new DataPair(entry.getKey(), entry.getValue()));
        }
        return set;
    }
}
