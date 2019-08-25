package LimakWebApp.Utils;

import javafx.application.Platform;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Controller {

    private final int MAX_SIZE = 16;
    protected ExecutorService pool;

    public Controller(){
        pool =  Executors.newFixedThreadPool(8);
    }

    public abstract void addLog(Constants.LogType type, String body);
    public abstract void setStatusText(String text);
    public abstract void displayTree();
    public abstract void clearRoot();

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
    public void refreshTree(){
        Platform.runLater(()-> {
            clearRoot();
            displayTree();
        });
    }
    protected int getMaxSize(){
        return MAX_SIZE;
    }

    public ExecutorService getPool() {
        return pool;
    }
}