package LimakWebApp.Utils;

import LimakWebApp.DataPackets.CredentialPacket;

import java.util.Map;

/**
 * <h1>DataPair</h1>
 * This class is used by {@link LimakWebApp.ServerSide.MainPageController} maintain list of clients
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   12.08.2019
 */
public class DataPair implements Map.Entry<CredentialPacket, Boolean>{

    private CredentialPacket key;
    private Boolean value;

    public DataPair(CredentialPacket packet, Boolean value){
        this.value = value;
        key = packet;
    }

    @Override
    public Boolean setValue(Boolean value){
        Boolean oldValue = this.value;
        this.value = value;
        return oldValue;
    }

    @Override
    public CredentialPacket getKey(){
        return key;
    }

    @Override
    public Boolean getValue(){
        return value;
    }

    @Override
    public int hashCode() {
        int keyHash = (key==null ? 0 : key.hashCode());
        int valueHash = (value==null ? 0 : value.hashCode());
        return keyHash ^ valueHash;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Map.Entry) || !(((Map.Entry) o).getKey() instanceof CredentialPacket) ||  !(((Map.Entry) o).getValue() instanceof Boolean))
            return false;
        return ((Map.Entry<CredentialPacket, Boolean>)o).getKey().compareTo(key) == 0 && ((Map.Entry<CredentialPacket, Boolean>) o).getValue().equals(value);
    }

    @Override
    public String toString() {
        return key.getUserName() + " " + (value ? "online" : "offline");
    }

}