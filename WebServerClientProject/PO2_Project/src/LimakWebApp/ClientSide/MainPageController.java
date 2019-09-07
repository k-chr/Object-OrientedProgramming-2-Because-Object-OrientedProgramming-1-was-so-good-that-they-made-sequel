package LimakWebApp.ClientSide;

import LimakWebApp.Utils.AbstractClientController;
import LimakWebApp.Utils.Constants;
import LimakWebApp.Utils.StringFunctionalInterface;
import LimakWebApp.DataPackets.CredentialPacket;
import LimakWebApp.DataPackets.MessageToSend;

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
import javafx.stage.Window;

import java.awt.Desktop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.nio.file.Files;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * <h1>MainPageController</h1>
 * This class performs GUI operations and some business logic for Client
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   20.05.2019
 */
public class MainPageController extends AbstractClientController {

    @FXML
    private Button backButton;
    @FXML
    private Button logOutButton;
    @FXML
    private volatile ListView<String> inactiveUsersListView;
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
    private volatile TreeView<File> tView;
    @FXML
    private TextField  statusText;

    private volatile ArrayList<File> tempListOfFiles;
    private volatile ArrayList<File> listOfFiles;
    private volatile ListProperty<String> activeUsersListProperty;
    private volatile ObservableList<String> activeUsersObservableList;
    private volatile ListProperty<String> inactiveUsersListProperty;
    private volatile ObservableList<String> inactiveUsersObservableList;

    private ApacheWatchService watchService;
    private ReadWriteLock lock;
    private CredentialPacket credentialPacket;

    private ScheduledExecutorService scheduler;
    private Client client;
    private File itemToSend = null;
    private String itemToShare = null;

    /**
     * This constructor calls super().
     */
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
        EventHandler<ActionEvent> backButtonHandler = event -> {
            Platform.runLater(() -> {
                tView.setDisable(false);
                viewOfClientsToShare.setVisible(false);
            });
        };
        viewOfClientsToShare.setVisible(false);
        logOutButton.addEventHandler(ActionEvent.ACTION, actionHandler);
        backButton.addEventHandler(ActionEvent.ACTION, backButtonHandler);
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
                        if (!cell.getItem().equals(credentialPacket.getUserName())) {
                            MessageToSend command = new MessageToSend(credentialPacket, MessageToSend.COMMAND_TYPE.SHARE_FILE_TO_USER);
                            ArrayList<Object> items = new ArrayList<>();
                            items.add(cell.getText());
                            items.add(itemToShare);
                            command.addContents(items);
                            Platform.runLater(() -> {
                                tView.setDisable(false);
                                viewOfClientsToShare.setVisible(false);
                            });
                            client.rcvCmd(command);
                        }
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
                            items.add(cell.getItem());
                            items.add(itemToShare);
                            command.addContents(items);
                            Platform.runLater(() -> {
                                tView.setDisable(false);
                                viewOfClientsToShare.setVisible(false);
                            });
                            client.rcvCmd(command);
                        }
                    });
                    return cell;
                }
        );
        tempListOfFiles = new ArrayList<>();
        listOfFiles = new ArrayList<>();
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(()->setStatusText("Session is active"), 0, 60, SECONDS);
        activeUsersObservableList = FXCollections.observableList(new ArrayList<>());
        activeUsersListProperty.setValue(activeUsersObservableList);
        activeUsersListView.itemsProperty().bindBidirectional(activeUsersListProperty);
        inactiveUsersObservableList = FXCollections.observableList(new ArrayList<>());
        inactiveUsersListProperty.setValue(inactiveUsersObservableList);
        inactiveUsersListView.itemsProperty().bindBidirectional(inactiveUsersListProperty);
        watchService = new ApacheWatchService();
        lock = new ReentrantReadWriteLock();
    }

    /**
     * This method performs shutdown of all {@link ExecutorService} objects, closes {@link WatchService} and {@link Client}.
     */
    @Override
    public void cleanUp(){
        setStatusText("Closing...");
        addLog(Constants.LogType.INFO, new Date().toString() + ":\nClosing...\n");
        client.dropConnection(false);
        pool.shutdown();
        scheduler.shutdown();
        watchService.quit();
        try {
            scheduler.awaitTermination(3, SECONDS);
        }
        catch(InterruptedException ie){
            scheduler.shutdownNow();
        }
        try{
            pool.awaitTermination(3, SECONDS);
        }
        catch(InterruptedException ie) {
            pool.shutdownNow();
        }

        boolean rV = client.isClosed() == false;
        if(rV) {
            client.close();
        }
    }

    void createDirectories(){
        lock.writeLock().lock();
        try {
            File dir = new File(credentialPacket.getUserFolderPath());
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
        }finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Runs {@link ApacheWatchService}
     */
    @Override
    public void runWatcher(){
        watchService.runWatcher();
    }

    /**
     * This method checks if application window is iconified
     * @return boolean
     */
    @Override
    public boolean checkIfMinimized(){
        return ((Stage)tView.getScene().getWindow()).isIconified();
    }

    /**
     * Returns reference to held {@link Client}
     * @return {@link Client}
     */
    @Override
    public Client getClient() {
        return client;
    }

    /**
     * This method fills initially list of files owned by {@link Client} to avoid thread race.
     * @param items Items to add.
     */
    public synchronized void fillListOfFilesInitially(ArrayList<File> items){
        lock.writeLock().lock();
        try{
            listOfFiles.clear();
            listOfFiles.addAll(items);
        }finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * This method updates received list of users from server.
     * @param itemsToAdd List of users' names to add
     * @param which Indicates which list should be updated - the active or inactive users' list
     */
    @Override
    public void updateListOfUsers(ArrayList<String> itemsToAdd, boolean which){
        Platform.runLater(()-> {
            if (which) {
                activeUsersObservableList.clear();
                activeUsersObservableList.addAll(itemsToAdd.stream().filter(name->!name.equals(credentialPacket.getUserName())).collect(Collectors.toCollection(ArrayList::new)));
            } else {
                inactiveUsersObservableList.clear();
                inactiveUsersObservableList.addAll(itemsToAdd);
            }
        });
    }

    /**
     * This method shows lists of users (inactive and active users).
     */
    @Override
    public void showUsers(){
        Platform.runLater(()->{
            tView.setDisable(true);
            viewOfClientsToShare.setVisible(true);
        });
        setStatusText("Choose user");
    }

    private void displayTreeView() {
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
                        if(!item.isDirectory() && !item.getName().equals("Downloads")) {
                            setContextMenu(contextMenu);
                        }
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
                else if(e.getButton() == MouseButton.SECONDARY && !cell.isEmpty() && cell.getTreeItem() != rootItem && !cell.getTreeItem().getValue().getName().equals("Downloads")){
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
        lock.writeLock().lock();
        try{
            listOfFiles.clear();
            listOfFiles.addAll(tempListOfFiles);
        }finally {
            lock.writeLock().unlock();
        }
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
                tempListOfFiles.add(file);
            }finally {
                lock.readLock().unlock();
            }
            parentTreeItem.getChildren().add(new TreeItem<>(file));
        }
    }

    /**
     * This method compares server's list and user's list to indicate if there are any new files on server.
     * @param serverFileList List of user's files that are assigned to him on server
     * @return boolean
     */
    @Override
    public boolean checkIfAreNewFiles(ArrayList<String> serverFileList){
        ArrayList<String> outList = new ArrayList<>();
        lock.readLock().lock();
        try{
            ArrayList<String> tmp = listOfFiles.stream().map(file->file.getName()).collect(Collectors.toCollection(ArrayList::new));
            if (serverFileList.size() > 0) {
                for (String fileName : serverFileList) {
                    if (!tmp.contains(fileName)) {
                        outList.add(fileName);
                    }
                }
            }
        }finally {
            lock.readLock().unlock();
        }
        return outList.size() > 0;
    }

    /**
     * This methods compares local and remote user's list of files and returns for current user list of filenames that are unique locally.
     * @param serverFileList List of user's files that are assigned to him on server
     * @return <pre>{@code Map.Entry<CredentialPacket, ArrayList<String>>}</pre>
     */
    @Override
    public Map.Entry<CredentialPacket, ArrayList<File>> compareUserAndServerList( ArrayList<String> serverFileList){
        Map.Entry<CredentialPacket, ArrayList<File>> rV = null;
        ArrayList<File> outList = new ArrayList<>();
        lock.readLock().lock();
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
            lock.readLock().unlock();
        }
        rV = new AbstractMap.SimpleEntry<>(credentialPacket, outList);
        return rV;
    }

    /**
     * This method sets an ID of current session.
     * @param sessionID received ID from server
     */
    @Override
    public void setSessionID(String sessionID) {
        Platform.runLater(()->this.sessionID.setText(sessionID));
    }


    /**
     * This method sets user's credentials.
     * @param credentialPacket credentials of user to set
     */
    @Override
    public void setCredentialPacket(CredentialPacket credentialPacket) {
        this.credentialPacket = credentialPacket;
    }

    /**
     * This method returns credentials of current user
     * @return {@link CredentialPacket}
     */
    @Override
    public CredentialPacket getCredentialPacket(){
        return this.credentialPacket;
    }

    /**
     * This method sets a client.
     * @param client Client object to set
     */
    @Override
    public void setClient(Client client) {
        this.client = client;
    }
    @Override
    public void setItemToSend(File file) {
        itemToSend = file;
    }

    /**
     * This method returns current stage.
     * @return {@link Stage}
     */
    public Stage getStage(){
        return (Stage)logOutButton.getScene().getWindow();
    }

    /**
     * This method provides access to user's files' list.
     * @return {@code ArrayList<String>}
     */
    @Override
    public ArrayList<String> getListOfFiles(){
        return listOfFiles.stream().map(file -> file.getName()).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * This method returns name of file to send.
     * @return {@link String}
     */
    @Override
    public File getItemToSend(){
        return itemToSend;
    }

    /**
     * This method is a public accessor of {@link #displayTreeView()}.
     */
    @Override
    public void displayTree() {
        Platform.runLater(()->displayTreeView());
    }

    /**
     * This method refreshes a tree of files.
     */
    @Override
    public void refreshTree(){
        Platform.runLater(()-> {
            lock.writeLock().lock();
            try {
                tempListOfFiles.clear();
                clearRoot();
                displayTree();
            }
            finally {
                lock.writeLock().unlock();
            }
        });
    }

    /**
     * This method clears root of tree of files.
     */
    @Override
    public void clearRoot() {
        Platform.runLater(()->tView.setRoot(null));
        Platform.runLater(()-> tView.setStyle(null));
    }

    /**
     * Sets proper watch service for controller.
     * @param watchService {@link ApacheWatchService} to set.
     */
    public void setWatchService(ApacheWatchService watchService) {
        this.watchService = watchService;
    }

    /**
     * Method sets current status of client application.
     * @param string status to set
     */
    @Override
    @FXML
    public void setStatusText(String string){
        Platform.runLater(()-> statusText.setText(string));
    }

    /**
     * Adds new log message to log pane with specified type and content.
     * @param logType {@link Constants.LogType} type of message.
     * @param message content of message, usually contains a date of message and some basic content which indicates performed action.
     */
    @Override
    @FXML
    public void addLog(Constants.LogType logType, String message){
        Text textToAdd = new Text();
        textToAdd.setText(message);
        textToAdd.setWrappingWidth(390);
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
        Platform.runLater(()->logContent.getChildren().add(textToAdd));
    }

    /**
     * This method hides window and loads login page
     * @param stage Window to hide
     */
    @Override
    public void hideWindow(Object stage){
        if (stage instanceof Window){
            Platform.runLater(() -> {
                ((Window)stage).hide();
                try {
                    Stage primaryStage = LimakWebApp.ClientSide.ClientApp.createStage("ClientLoginPage.fxml", 240, 290, false);
                    primaryStage.setOnCloseRequest(e -> {
                        e.consume();
                        Timer timer = new Timer();
                        timer.schedule(
                                new java.util.TimerTask() {
                                    @Override
                                    public void run() {
                                        System.exit(0);
                                        timer.cancel();
                                    }
                                }, 4000);

                    });
                    primaryStage.show();
                } catch (IOException io) {
                    io.printStackTrace();
                }
            });
        }
    }
}