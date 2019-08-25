package LimakWebApp.ClientSide;

import LimakWebApp.Utils.Constants;
import LimakWebApp.DataPackets.CredentialPacket;
import LimakWebApp.DataPackets.MessageToSend;
import LimakWebApp.Utils.StringFunctionalInterface;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import LimakWebApp.Utils.Controller;

import java.awt.Desktop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.nio.file.StandardWatchEventKinds.*;
import static java.util.concurrent.TimeUnit.SECONDS;

public class MainPageController extends Controller {

    @FXML
    private Button logOutButton;
    @FXML
    private ListView<String> inactiveUsersListView;
    @FXML
    private AnchorPane viewOfClientsToShare;
    @FXML
    private volatile ListView<String> activeUsersListView;
    @FXML
    private Text sessionID;
    @FXML
    private ScrollPane log;
    @FXML
    private VBox logContent;
    @FXML
    private TreeView<File> tView;
    @FXML
    private TextField  statusText;

    private volatile ArrayList<String> listOfFiles;
    private volatile ListProperty<String> activeUsersListProperty;
    private volatile ObservableList<String> activeUsersObservableList;
    private volatile ListProperty<String> inactiveUsersListProperty;
    private volatile ObservableList<String> inactiveUsersObservableList;

    private ReadWriteLock lock;
    private CredentialPacket credentialPacket;
    private ExecutorService watcherServiceTh;
    private ScheduledExecutorService scheduler;
    private Client client;
    private String itemToSend = null;
    private String itemToShare = null;
    private WatchService watchService;

    public MainPageController(){
        super();
    }

    @FXML
    private void initialize(){
        EventHandler<ActionEvent> actionHandler = event -> {
            client.windowToClose = ((Button) event.getSource()).getScene().getWindow();
            client.demandForLogOut();
            event.consume();
        };
        viewOfClientsToShare.setVisible(false);
        logOutButton.addEventHandler(ActionEvent.ACTION, actionHandler);
        log.setContent(logContent);
        activeUsersListProperty = new SimpleListProperty<>();
        inactiveUsersListProperty = new SimpleListProperty<>();
        activeUsersListView.setCellFactory(callback->{
                ListCell<String> cell = new ListCell<>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(item);
                    }
                };
                cell.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.PRIMARY && !cell.isEmpty()) {
                        MessageToSend command = new MessageToSend(credentialPacket, MessageToSend.COMMAND_TYPE.SHARE_FILE_TO_USER);
                        ArrayList<Object> items = new ArrayList<>();
                        items.add(cell.getText());
                        items.add(itemToShare);
                        command.addContents(items);
                        client.rcvCmd(command);
                        Platform.runLater(() -> {
                            tView.setDisable(false);
                            viewOfClientsToShare.setVisible(false);
                            StringBuilder builder = new StringBuilder();
                            builder.append(new Date())
                                    .append(":\n").append("Shared successfully: \n\t").append(itemToShare)
                                    .append("\n").append("to:\n\t").append(cell.getItem()).append("\n");
                            addLog(Constants.LogType.INFO, builder.toString());
                            statusText.setText("Shared successfully");
                        });
                    }
                });
                return cell;
            }
        );
        inactiveUsersListView.setCellFactory(callback->{
                    ListCell<String> cell = new ListCell<>() {
                        @Override
                        public void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(item);
                        }
                    };
                    cell.setOnMouseClicked(event -> {
                        if (event.getButton() == MouseButton.PRIMARY && !cell.isEmpty()) {
                            MessageToSend command = new MessageToSend(credentialPacket, MessageToSend.COMMAND_TYPE.SHARE_FILE_TO_USER);
                            ArrayList<Object> items = new ArrayList<>();
                            items.add(cell.getText());
                            items.add(itemToShare);
                            command.addContents(items);
                            client.rcvCmd(command);
                            Platform.runLater(() -> {
                                tView.setDisable(false);
                                viewOfClientsToShare.setVisible(false);
                                StringBuilder builder = new StringBuilder();
                                builder.append(new Date())
                                        .append(":\n").append("Shared successfully: \n\t").append(itemToShare)
                                        .append("\n").append("to:\n\t").append(cell.getItem()).append("\n");
                                addLog(Constants.LogType.INFO, builder.toString());
                                statusText.setText("Shared successfully");
                            });
                        }
                    });
                    return cell;
                }
        );
        listOfFiles = new ArrayList<>();
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(()->Platform.runLater(()->setStatusText("Session is active")), 10, 60, SECONDS);
        watcherServiceTh = Executors.newFixedThreadPool(1);
        activeUsersObservableList = FXCollections.observableList(new ArrayList<>());
        activeUsersListProperty.setValue(activeUsersObservableList);
        activeUsersListView.itemsProperty().bindBidirectional(activeUsersListProperty);
        inactiveUsersObservableList = FXCollections.observableList(new ArrayList<>());
        inactiveUsersListProperty.setValue(inactiveUsersObservableList);
        inactiveUsersListView.itemsProperty().bindBidirectional(inactiveUsersListProperty);
        try {
            watchService = FileSystems.getDefault().newWatchService();
        }
        catch(IOException io){
            io.printStackTrace();
        }
        lock = new ReentrantReadWriteLock();
    }

    public void cleanUp(){
        Platform.runLater(()->{
            setStatusText("Closing...");
            addLog(Constants.LogType.INFO, new Date().toString() + ":\nClosing...\n");
        });
        pool.shutdown();
        scheduler.shutdown();
        watcherServiceTh.shutdown();
        boolean rV = client.isClosed() == false;
        if(rV) {
            client.close();
        }
        try {
            watchService.close();
        }
        catch(IOException io){
            io.printStackTrace();
        }
        try {
            scheduler.awaitTermination(3, SECONDS);
            watcherServiceTh.awaitTermination(3, SECONDS);
            pool.awaitTermination(3, SECONDS);
        }
        catch(InterruptedException ie){
            pool.shutdownNow();
            scheduler.shutdownNow();
            watcherServiceTh.shutdownNow();
        }
    }

    boolean checkIfMinimized(){
        return ((Stage)tView.getScene().getWindow()).isIconified();
    }

    public void runWatcher(){
        Runnable task = ()->{
            try {
                while (true) {
                    WatchKey watchKey = watchService.take();
                    for(WatchEvent<?> e : watchKey.pollEvents()){
                        WatchEvent.Kind kind = e.kind();
                        if(kind == ENTRY_CREATE){
                            Path fullPath = ((Path)watchKey.watchable()).resolve((Path)e.context());
                           System.out.println("WATCH_SERVICE_ACTION: "+((Path)e.context()).toFile().getName());
                           if((fullPath).toFile().isDirectory()){
                               try {
                                   (fullPath).register(watchService, ENTRY_DELETE);
                               }
                               catch(IOException io){
                                   io.printStackTrace();
                               }
                           }
                           else{
                               itemToSend = ((Path)e.context()).toFile().getName();
                               client.demandForTransferEnforcedByWatchService(itemToSend);
                           }
                        }
                        if(kind == ENTRY_DELETE){
                            MessageToSend command = new MessageToSend(credentialPacket, MessageToSend.COMMAND_TYPE.REMOVE_USER_FROM_FILE_OWNERS);
                            ArrayList<Object> arrList = new ArrayList<>();
                            arrList.add(credentialPacket.getUserName());
                            arrList.add(((Path)e.context()).toFile().getName());
                            command.addContents(arrList);
                            client.rcvCmd(command);
                        }
                    }
                    watchKey.reset();
                    refreshTree();
                }
            }
            catch(InterruptedException|ClosedWatchServiceException ignored){}
        };
        watcherServiceTh.submit(task);
    }
    public void registerDirectoriesForWatchService() throws  IOException{
        Path path = Paths.get(credentialPacket.getUserFolderPath());
        path.register(watchService, ENTRY_CREATE, ENTRY_DELETE);
    }

    public void updateListOfUsers(ArrayList<String> itemsToAdd, boolean which){
        if(which) {
            activeUsersObservableList.clear();
            activeUsersObservableList.addAll(itemsToAdd);
        }
        else{
            inactiveUsersObservableList.clear();
            inactiveUsersObservableList.addAll(itemsToAdd);
        }
    }
    public void showUsers(){
        tView.setDisable(true);
        viewOfClientsToShare.setVisible(true);
        setStatusText("Choose user");
    }
    public void displayTreeView() {
        String inputDirectoryLocation = credentialPacket.getUserFolderPath();
        String[] path = inputDirectoryLocation.split("\\\\");
        StringBuilder stringBuilder = new StringBuilder();
        TreeItem<File> rootItem = new TreeItem<>(new File(inputDirectoryLocation));
        if(!rootItem.getValue().exists())
            while(!rootItem.getValue().mkdir());
        tView.setShowRoot(true);
        tView.setCellFactory(callback-> {
            final Tooltip tooltip = new Tooltip();
            final ContextMenu contextMenu = new ContextMenu();
            MenuItem menuItem = new MenuItem("Share file");
            menuItem.setOnAction((event)->{
                client.demandForListOfUsers();
                itemToShare = callback.getSelectionModel().getSelectedItem().getValue().getName();
            });
            contextMenu.getItems().add(menuItem);
            TreeCell<File> cell = new TreeCell<>() {
                @Override
                public void updateItem(File item, boolean empty) {
                    StringFunctionalInterface stringFunctionalInterface = (StringBuilder stringBuilder)->{
                        stringBuilder = new StringBuilder();
                        BasicFileAttributes fileAttributes;
                        try {
                            fileAttributes = Files.readAttributes(item.toPath(), BasicFileAttributes.class);
                            stringBuilder.append(fileAttributes.isDirectory() ? "Directory" : "File").append('\n');
                            stringBuilder.append("Path: ").append(item.getAbsolutePath()).append('\n');
                            stringBuilder.append("Creation time: ").append(new java.util.Date(fileAttributes.creationTime().toMillis()).toString()).append('\n');
                            stringBuilder.append("Last modification time: ").append(new java.util.Date(fileAttributes.lastModifiedTime().toMillis()).toString()).append('\n');
                            stringBuilder.append("Last access time: ").append(new java.util.Date(fileAttributes.lastAccessTime().toMillis()).toString()).append('\n');
                            long size = fileAttributes.isDirectory() ? Files.walk(item.toPath()).filter(p -> p.toFile().isFile()).mapToLong(p->p.toFile().length()).sum() :  fileAttributes.size();
                            stringBuilder.append("Size: ").append(computeDataStorageUnitAndValue(size)).append('\n');
                            stringBuilder.append("Author/current owner:").append(credentialPacket.getUserName());
                        }
                        catch(IOException io){
                            io.printStackTrace();
                        }
                        return stringBuilder.toString();
                    };
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                        setTooltip(null);
                    } else if (getTreeItem() == rootItem) {
                        setText(path[path.length-1]);
                        tooltip.setText(stringFunctionalInterface.getText(stringBuilder));
                        setTooltip(tooltip);
                    } else {
                        setText(item.getName());
                        tooltip.setText(stringFunctionalInterface.getText(stringBuilder));
                        setTooltip(tooltip);
                        setContextMenu(contextMenu);
                    }
                }
            };
            cell.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2 && ! cell.isEmpty() && cell.getTreeItem() != rootItem) {
                    File file = cell.getItem();
                    try {
                        if (Desktop.isDesktopSupported()) {
                            Desktop desktop = Desktop.getDesktop();
                            desktop.open(file);
                        } else throw new IOException();
                    }
                    catch(IOException io){
                        Platform.runLater(()->{
                            setStatusText("Can't open a file");
                            StringBuilder builder = new StringBuilder();
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            PrintStream outStream = new PrintStream(outputStream);
                            io.printStackTrace(outStream);
                            stringBuilder.append(new Date())
                                    .append(":\n")
                                    .append("Can't open file or Desktop is not supported class in your system: \n\t")
                                    .append(file.getAbsolutePath())
                                    .append("\n")
                                    .append(io.getMessage() + "\n")
                                    .append(outStream.toString())
                                    .append("\n");
                            addLog(Constants.LogType.ERROR, builder.toString());
                        });
                    }
                    Platform.runLater(()-> {
                        setStatusText("File opened");
                        StringBuilder builder = new StringBuilder();
                        builder.append(new Date())
                                .append(":\n")
                                .append("Successfully opened a file: \n\t")
                                .append(file.getAbsolutePath())
                                .append("\n");
                        addLog(Constants.LogType.SUCCESS, builder.toString());
                    });
                }
                else if(e.getButton() == MouseButton.SECONDARY && !cell.isEmpty() && cell.getTreeItem() != rootItem){
                    Platform.runLater(()->{
                        StringBuilder builder = new StringBuilder();
                        builder.append(new Date())
                                .append(":\n")
                                .append("Do you want to share: \n\t")
                                .append(cell.getItem().getName())
                                .append("?\n");
                        addLog(Constants.LogType.INFO, builder.toString());
                        statusText.setText("Attempt to share");
                        contextMenu.show(cell, e.getScreenX(), e.getScreenY());
                    });
                }
            });
            return cell ;
        });
        File fileInputDirectoryLocation = new File(inputDirectoryLocation);
        File[] fileList = fileInputDirectoryLocation.listFiles();
        for (File file : fileList) {
            createTree(file, rootItem);
        }
        rootItem.setExpanded(true);
        tView.setRoot(rootItem);
    }

    private void createTree(File file, TreeItem<File> parentTreeItem){
        if (file.isDirectory()) {
            TreeItem<File> treeItem = new TreeItem<>(file);
            parentTreeItem.getChildren().add(treeItem);
            for (File f : file.listFiles()) {
                createTree(f, treeItem);
            }
        } else {
            lock.readLock().lock();
            try {
                listOfFiles.add(file.getName());
            }finally {
                lock.readLock().unlock();
            }
            parentTreeItem.getChildren().add(new TreeItem<>(file));
        }
    }

    public boolean checkIfAreNewFiles(ArrayList<String> serverFileList){
        ArrayList<String> outList = new ArrayList<>();
        lock.writeLock().lock();
        try{
            ArrayList<String> tmp = listOfFiles;
            if (serverFileList.size() > 0) {
                for (String fileName : serverFileList) {
                    if (!tmp.contains(fileName)) {
                        outList.add(fileName);
                    }
                }
            } else {
                outList.addAll(tmp);
            }
        }finally {
            lock.writeLock().unlock();
        }
        return outList.size() > 0;
    }

    public Map.Entry<CredentialPacket, ArrayList<String>> compareUserAndServerList( ArrayList<String> serverFileList){
        Map.Entry<CredentialPacket, ArrayList<String>> rV = null;
        ArrayList<String> outList = new ArrayList<>();
        lock.writeLock().lock();
        try {
            ArrayList<String> tmp = listOfFiles;
            if (serverFileList.size() > 0) {
                for (String fileName : tmp) {
                    if (!serverFileList.contains(fileName)) {
                        outList.add(fileName);
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

    public void setSessionID(String sessionID) {
        this.sessionID.setText(sessionID);
    }

    public void setCredentialPacket(CredentialPacket credentialPacket) {
        this.credentialPacket = credentialPacket;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Stage getStage(){
        return (Stage)logOutButton.getScene().getWindow();
    }

    public ArrayList<String> getListOfFiles(){
        return listOfFiles;
    }

    String getItemToSend(){
        return itemToSend;
    }

    @Override
    public void displayTree() {
        displayTreeView();
    }

    @Override
    public void refreshTree(){
        Platform.runLater(()-> {
            lock.readLock().lock();
            try {
                synchronized (listOfFiles) {
                    listOfFiles.clear();
                }
                super.refreshTree();
            } finally {
                lock.readLock().unlock();
            }
        });
    }

    @Override
    public void clearRoot() {
        tView.setRoot(null);
    }

    @Override
    @FXML
    public void setStatusText(String string){
        statusText.setText(string);
    }

    @Override
    @FXML
    public void addLog(Constants.LogType logType, String message){
        Text textToAdd = new Text();
        textToAdd.setText(message);
        textToAdd.setWrappingWidth(304);
        switch(logType){
            case INFO:
                textToAdd.setFill(Color.BLUE);
                break;
            case ERROR:
                textToAdd.setFill(Color.RED);
                break;
            case SUCCESS:
                textToAdd.setFill(Color.GREEN);
                break;
        }
        logContent.getChildren().add(textToAdd);
    }
}