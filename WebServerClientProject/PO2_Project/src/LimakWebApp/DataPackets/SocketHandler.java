package LimakWebApp.DataPackets;

import java.net.Socket;
import java.util.ArrayList;

public class SocketHandler {

    private final Socket fileTransferSocket;
    private final Socket notificationSocket;
    private final boolean isSocketsSet;

    public SocketHandler(ArrayList<Socket> socketArrayList){
        if(socketArrayList.size() == 2) {
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

    public Socket getFileTransferSocket() {
        return fileTransferSocket;
    }

    public Socket getNotificationSocket() {
        return notificationSocket;
    }

    public boolean getIsSocketsSet() {
        return isSocketsSet;
    }
}
