package LimakWebApp.Utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <h1>Controller</h1>
 * This class contains basic methods, constants for deriving Controllers
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   03.07.2019
 */
public abstract class Controller {

    private final int MAX_SIZE = 16;

    /**
     * Threads pool provided for Controller instances
     */
    protected ExecutorService pool;

    /**
     * The basic Constructor of Controller, instantiates thread pool
     */
    public Controller(){
        pool =  Executors.newFixedThreadPool(8);
    }

    /**
     * This method is prepared to be implemented by deriving classes, this method should add new log for Controller
     * @param type Type of log
     * @param body Contents
     */
    public abstract void addLog(Constants.LogType type, String body);

    /**
     * This method should set status bar in deriving Controller
     * @param text Text to set
     */
    public abstract void setStatusText(String text);

    /**
     * This method should display {@link javafx.scene.control.TreeView}
     */
    public abstract void displayTree();

    /**
     * This method should clear root
     */
    public abstract void clearRoot();

    /**
     * This methods returns text representation of provided size in bytes
     * @param bytes Size to convert
     * @return {@link String}
     */
    public String computeDataStorageUnitAndValue(long bytes){
        final String bytesUnit = " Bytes";
        final String kBytesUnit = " kB";
        final String mBytesUnit = " MB";
        final String gBytesUnit = " GB";
        final int b = 1024;
        return bytes < b ? ((Long)bytes).toString() + bytesUnit :
                (bytes/b < b ? ((Long)(bytes/b)).toString() + kBytesUnit :
                (bytes/(b*b) < b ? ((Long)(bytes/(b*b))).toString() + mBytesUnit : ((Long)(bytes/(b*b))).toString() + gBytesUnit));
    }

    /**
     * This method should refresh {@link javafx.scene.control.TreeView} owned by deriving Controller
     */
    public abstract void refreshTree();

    /**
     * This method returns MAX_SIZE to compute ID of session
     * @return int
     */
    protected int getMaxSize(){
        return MAX_SIZE;
    }

    /**
     * This method gives access to thread pool
     * @return {@link ExecutorService}
     */
    public ExecutorService getPool() {
        return pool;
    }
}