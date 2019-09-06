package LimakWebApp.Utils;

import LimakWebApp.Tests.TestServerController;
import LimakWebApp.Tests.TestsForClientAndServer;

/**
 * <h1>Constants</h1>
 * This class contains all necessary constants to run ClientApp and ServerApp
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   03.07.2019
 */
public final class Constants {

    /**
     * Number of file port, already set to {@value}
     */
    static final public int filePort = 1488;

    /**
     * Number of authorization port, already set to {@value}
     */
    static final public int authPort = 2137;

    /**
     * Number of communication port, already set to {@value}
     */
    static final public int commPort = 1109;

    /**
     * IP address of server, already set to {@value}
     */
    static final public String serverIP = "127.0.0.1";

    private final static String clientDownloadDirectory = "Downloads";
    private final static String serverDirectory = "D:\\Project\\Server";
    private final static String testServerDirectory = "TestServer";
    private final static String[] directories = {"Server1", "Server2","Server3","Server4","Server5"};
    private final static String listOfClientsFileName = "Clients.json";
    private final static String directoriesControlFile = "DiscContents.json";
    private final static String serverEMail = "noreply.webappjavaproject@gmail.com";

    /**
     * Indicates the colour of the log displayed in log text field
     */
    public enum LogType{
        ERROR,
        SUCCESS,
        INFO
    }

    /**
     * This method returns text representation of server directory path if  <code> o </code> is valid, otherwise <code> null </code>.
     * @param o a valid Object, who wants to get access to data:
     * {@link LimakWebApp.ServerSide.MainPageController}
     * or
     * {@link LimakWebApp.ServerSide.CommunicationServiceThreadHandler}
     * or
     * {@link LimakWebApp.Tests.TestServerController}
     * @return {@link String}
     */
    public final static String getServerDirectory(Object o) {
        return o instanceof LimakWebApp.ServerSide.MainPageController || o instanceof LimakWebApp.ServerSide.CommunicationServiceThreadHandler ? serverDirectory : (o instanceof LimakWebApp.Tests.TestServerController || o instanceof TestsForClientAndServer ? testServerDirectory : null);
    }

    /**
     * This method returns an array of server subdirectories if  <code> o </code> is valid, otherwise <code> null </code>.
     * @param o a valid Object, who wants to get access to data:
     * {@link LimakWebApp.Utils.AbstractServerController}
     * or
     * {@link LimakWebApp.ServerSide.CommunicationServiceThreadHandler}
     * @return {@link String}[]
     */
    public final static String[] getDirectories(Object o){
        return o instanceof LimakWebApp.Utils.AbstractServerController || o instanceof LimakWebApp.ServerSide.CommunicationServiceThreadHandler ? directories : null;
    }

    /**
     * This method returns the name of file, that contains data of clients if  <code> o </code> is valid, otherwise <code> null </code>.
     * @param o a valid Object, who wants to get access to data:
     * {@link LimakWebApp.Utils.AbstractServerController}
     * @return {@link String}
     */
    public final static String getListOfClientsFileName(Object o){
        return o instanceof LimakWebApp.Utils.AbstractServerController ? listOfClientsFileName : null;
    }

    /**
     * This method returns the name of file, that contains data of disk of server if  <code> o </code> is valid, otherwise <code> null </code>.
     * @param o a valid Object, who wants to get access to data:
     * {@link LimakWebApp.Utils.AbstractServerController}
     * @return {@link String}
     */
    public final static String getDirectoriesControlFile(Object o){
        return o instanceof LimakWebApp.Utils.AbstractServerController ? directoriesControlFile : null;
    }

    /**
     * This method returns an email of server if  <code> o </code> is valid, otherwise <code> null </code>.
     * @param o a valid Object, who wants to get access to data:
     * {@link LimakWebApp.ServerSide.MainPageController}
     * or
     * {@link LimakWebApp.ServerSide.CommunicationServiceThreadHandler}
     * or
     * {@link LimakWebApp.ServerSide.EmailUtil}
     * or
     * {@link LimakWebApp.Tests.TestsForClientAndServer}
     * @return {@link String}
     */
    public final static String getServerEMail(Object o){
        return o instanceof LimakWebApp.Tests.TestsForClientAndServer ||  o instanceof LimakWebApp.ServerSide.MainPageController || o instanceof LimakWebApp.ServerSide.Server || o instanceof LimakWebApp.ServerSide.EmailUtil ? serverEMail : null;
    }

    /**
     * This method returns an constant download directory of client if  <code> o </code> is valid, otherwise <code> null </code>.
     * @param o a valid Object, who wants to get access to data:
     * {@link LimakWebApp.Utils.AbstractClientController}
     * or
     * {@link LimakWebApp.ClientSide.Client}
     * @return {@link String}
     */
    public final static String getClientDownloadDirectory(Object o){
        return o instanceof LimakWebApp.Utils.AbstractClientController || o instanceof LimakWebApp.ClientSide.Client ? clientDownloadDirectory : null;
    }

    private Constants(){
    }
}