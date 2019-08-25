package LimakWebApp.DataPackets;

import java.io.Serializable;
import java.util.ArrayList;

public class MessageToSend  implements Serializable{

    public enum COMMAND_TYPE{
        QUIT_CONNECTION,
        LOG_OUT_DEMAND,
        GIVE_LIST_OF_ACTIVE_USERS,
        GET_LIST_OF_ACTIVE_USERS,
        GIVE_LIST_OF_INACTIVE_USERS,
        GET_LIST_OF_INACTIVE_USERS,
        SEND_LIST_OF_FILES,
        RECEIVE_LIST_OF_FILES,
        SHARE_FILE_TO_USER,
        REMOVE_USER_FROM_FILE_OWNERS,
        TRANSFER_FILE,
        TRANSFER_FILE_DEMAND
    }

    private COMMAND_TYPE command_type;
    private CredentialPacket from;
    private ArrayList<Object> additional_contents = null;

    public MessageToSend( CredentialPacket from, COMMAND_TYPE type) {
        this.from = from;
        this.command_type = type;
    }

    public void addContents(ArrayList<Object> arrayList){
        this.additional_contents = arrayList;
    }

    public ArrayList<Object> getContents() {
        return additional_contents;
    }

    public CredentialPacket getUser(){
        return from;
    }

    public COMMAND_TYPE getCommandType() {
        return command_type;
    }

}