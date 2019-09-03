package LimakWebApp.ServerSide;

import LimakWebApp.Utils.Constants;
import LimakWebApp.Utils.Controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

/**
 * <h1>FileAlreadyExistsOnServerException</h1>
 * This class is used to log exceptions to prevent possible duplicates on server
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   31.08.2019
 */
public class FileAlreadyExistsOnServerException extends IOException {

    private Controller controller;

    /**
     * Basic constructor of {@link FileAlreadyExistsOnServerException}
     * @param controller Controller that prints exception data in label
     */
    public FileAlreadyExistsOnServerException(Controller controller){
        super();
        this.controller = controller;
    }

    /**
     * Logs this exception messages into log label in application
     * @param fileName File that caused this exception
     */
    public void log(String fileName){
        controller.setStatusText("File already exists");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream outStream = new PrintStream(outputStream);
        this.printStackTrace(outStream);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(new Date())
                .append(":\n").append("File already exists, can't save the file: ")
                .append(fileName).append("\n\t")
                .append(this.getMessage()).append("\n")
                .append(outStream.toString()).append("\n");
        controller.addLog(Constants.LogType.ERROR, stringBuilder.toString());
    }
}
