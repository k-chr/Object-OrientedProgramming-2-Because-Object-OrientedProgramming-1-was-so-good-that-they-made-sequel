package LimakWebApp.DataPackets;

import java.net.Socket;
import java.util.ArrayList;

/**
 * <h1>SocketHandler</h1>
 * This class is wrapper for sockets
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   02.08.2019
 */
public class SocketHandler {

    private final Socket fileTransferSocket;
    private final Socket notificationSocket;
    private final boolean isSocketsSet;

    /**
     * Constructor of SocketHandler - sets provided sockets from list, performs validation and sets <code>isSocketSet</code> flag.
     * @param socketArrayList List of sockets to set
     */
    public SocketHandler(ArrayList<Socket> socketArrayList){
        if(socketArrayList.size() == 2 && socketArrayList.get(0) != null && socketArrayList.get(1) != null) {
            fileTransferSocket = socketArrayList.get(0);
            notificationSocket = socketArrayList.get(1);
            isSocketsSet = true;
        }
        else{
            fileTransferSocket = null;
            notificationSocket = null;
            isSocketsSet = false;
        }
    }

    /**
     * Returns socket connected with file transport port
     * @return Socket
     */
    public Socket getFileTransferSocket() {
        return fileTransferSocket;
    }

    /**
     * Returns socket connected with notification port
     * @return Socket
     */
    public Socket getNotificationSocket() {
        return notificationSocket;
    }

    /**
     * Returns flag of sockets' correctness
     * @return boolean
     */
    public boolean getIsSocketsSet() {
        return isSocketsSet;
    }
}
