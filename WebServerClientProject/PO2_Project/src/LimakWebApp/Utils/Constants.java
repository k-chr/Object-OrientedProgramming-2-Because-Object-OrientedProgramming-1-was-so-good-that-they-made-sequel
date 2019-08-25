package LimakWebApp.Utils;

public final class Constants {

    static final public int filePort = 1488;
    static final public int authPort = 2137;
    static final public int commPort = 1109;
    static final public String serverIP = "127.0.0.1";

    private final static String clientDownloadDirectory = "Downloads";
    private final static String serverDirectory = "D:\\Project\\Server";
    private final static String[] directories = {"Server1", "Server2","Server3","Server4","Server5"};
    private final static String listOfClientsFileName = "Clients.json";
    private final static String directoriesControlFile = "DiscContents.json";
    private final static String serverEMail = "noreply.webappjavaproject@gmail.com";

    public enum LogType{
        ERROR,
        SUCCESS,
        INFO
    }
    public final static String getServerDirectory(Object o) {
        return o instanceof LimakWebApp.ServerSide.MainPageController || o instanceof LimakWebApp.ServerSide.CommunicationServiceThreadHandler ? serverDirectory : null;
    }

    public final static String[] getDirectories(Object o){
        return o instanceof LimakWebApp.ServerSide.MainPageController || o instanceof LimakWebApp.ServerSide.CommunicationServiceThreadHandler ? directories : null;
    }

    public final static String getListOfClientsFileName(Object o){
        return o instanceof LimakWebApp.ServerSide.MainPageController ? listOfClientsFileName : null;
    }

    public final static String getDirectoriesControlFile(Object o){
        return o instanceof LimakWebApp.ServerSide.MainPageController ? directoriesControlFile : null;
    }

    public final static String getServerEMail(Object o){
        return o instanceof LimakWebApp.ServerSide.MainPageController || o instanceof LimakWebApp.ServerSide.Server || o instanceof LimakWebApp.ServerSide.EmailUtil ? serverEMail : null;
    }

    public final static String getClientDownloadDirectory(Object o){
        return o instanceof LimakWebApp.ClientSide.MainPageController || o instanceof LimakWebApp.ClientSide.Client ? clientDownloadDirectory : null;
    }

    private Constants(){
    }
}