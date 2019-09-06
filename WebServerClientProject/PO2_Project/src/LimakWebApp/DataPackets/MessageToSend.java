package LimakWebApp.DataPackets;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * <h1>MessageToSend</h1>
 * This class provides data packet to communication purpose.
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   15.07.2019
 */
public class MessageToSend  implements Serializable{

    /**
     * This enum indicates the type of message, defines the way of processing this object in rcvCmd method in {@link LimakWebApp.ServicesHandler} classes.
     */
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

    /**
     * Basic constructor of MessageToSend.
     * @param from indicates the author of the message
     * @param type specifies the type of message
     */
    public MessageToSend( CredentialPacket from, COMMAND_TYPE type) {
        this.from = from;
        this.command_type = type;
    }

    /**
     * Provides contents for created message
     * @param arrayList additional contents of message
     */
    public void addContents(ArrayList<Object> arrayList){
        this.additional_contents = arrayList;
    }

    /**
     * Returns the contents of MessageToSend
     * @return {@code ArrayList<Object>}
     */
    public ArrayList<Object> getContents() {
        return additional_contents;
    }

    /**
     * Returns user, the creator of message.
     * @return {@link CredentialPacket}
     */
    public CredentialPacket getUser(){
        return from;
    }

    /**
     * Returns a type of command
     * @return {@link COMMAND_TYPE}
     */
    public COMMAND_TYPE getCommandType() {
        return command_type;
    }

}