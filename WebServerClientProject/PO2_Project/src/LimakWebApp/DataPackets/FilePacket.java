package LimakWebApp.DataPackets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * <h1>FilePacket</h1>
 * This class is a wrapper of file and owner assigned to file.
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   09.08.2019
 */
public class FilePacket implements Serializable {

    private String userName;
    private byte[] fileBytes;
    private String fileName;
    private long size;


    /**
     * Basic constructor of {@link FilePacket}. Reads data
     * @param userName indicates the owner of file
     * @param file indicates the file to wrap
     * @throws IOException if any problem with reading data from provided {@code file} occurs
     */
    public FilePacket(String userName, File file) throws IOException{
        this.userName = userName;
        this.fileName = file.getName();
        FileInputStream fileReader = new FileInputStream(file);
        fileBytes = fileReader.readAllBytes();
        size = file.length();
        fileReader.close();
    }

    /**
     * Method returns user's name.
     * @return {@link String}
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Method returns content of the file as a byte array.
     * @return byte[]
     */
    public byte[] getFileBytes() {
        return fileBytes;
    }

    /**
     * Method returns file's name.
     * @return {@link String}
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Meethod returns size of file.
     * @return long
     */
    public long getSize() {
        return size;
    }
}