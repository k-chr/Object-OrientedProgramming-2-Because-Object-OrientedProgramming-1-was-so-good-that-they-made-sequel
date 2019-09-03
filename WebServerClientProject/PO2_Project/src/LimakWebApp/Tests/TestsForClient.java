package LimakWebApp.Tests;

import LimakWebApp.ClientSide.AuthAgent;
import LimakWebApp.ClientSide.Client;
import LimakWebApp.DataPackets.CredentialPacket;
import LimakWebApp.ServerSide.Server;
import LimakWebApp.Utils.AbstractClientController;
import LimakWebApp.Utils.Constants;
import javafx.collections.ObservableList;
//import org.junit.*;

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReadWriteLock;

public class TestsForClient {
    private Server server = new Server();
    private AuthAgent authAgent = new AuthAgent();

    private AbstractClientController clientController = new AbstractClientController() {
        @Override
        public void hideWindow(Object stage) {

        }


        @Override
        public void cleanUp() {

        }

        @Override
        public boolean checkIfAreNewFiles(ArrayList<String> serverFileList) {
            return false;
        }

        @Override
        public void showUsers() {

        }

        @Override
        public Client getClient() {
            return null;
        }

        @Override
        public void updateListOfUsers(ArrayList<String> itemsToAdd, boolean which) {

        }

        @Override
        public void runWatcher() {

        }

        @Override
        public boolean checkIfMinimized() {
            return false;
        }
        @Override
        public void setSessionID(String sessionID) {

        }
        @Override
        public File getItemToSend() {
            return null;
        }
        @Override
        public ArrayList<String> getListOfFiles() {
            return null;
        }

        @Override
        public ExecutorService getWatcherServiceTh() {
            return null;
        }

        @Override
        public void addLog(Constants.LogType type, String body) {

        }
        @Override
        public void setStatusText(String text) {

        }
        @Override
        public void displayTree() {

        }
        @Override
        public void clearRoot() {

        }
        @Override
        public void refreshTree() {

        }
        private ReadWriteLock lock;
        private CredentialPacket credentialPacket;
        private ExecutorService watcherServiceTh;
        private ScheduledExecutorService scheduler;
        private Client client;
        private String itemToSend = null;
        private String itemToShare = null;
        private volatile ArrayList<File> listOfFiles;
        private volatile ObservableList<String> activeUsersObservableList;

        public Map.Entry<CredentialPacket, ArrayList<File>> compareUserAndServerList(ArrayList<String> serverFileList){
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
            }
            finally {
                lock.writeLock().unlock();
            }
            rV = new AbstractMap.SimpleEntry<>(credentialPacket, outList);
            return rV;
        }
        public void setClient(Client client) {
            this.client = client;
        }

        @Override
        public void setItemToSend(File file) {

        }

        public void setCredentialPacket(CredentialPacket credentialPacket) {
            this.credentialPacket = credentialPacket;
        }

        @Override
        public CredentialPacket getCredentialPacket() {
            return null;
        }
    };
}
