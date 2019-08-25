package LimakWebApp.DataPackets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;

public class FilePacket implements Serializable {

    public String getUserName() {
        return userName;
    }
    public byte[] getFileBytes() {
        return fileBytes;
    }
    public String getFileName() {
        return fileName;
    }

    private String userName;
    private byte[] fileBytes;
    private String fileName;
    private long size;

    public long getSize() {
        return size;
    }

    public FilePacket(String userName, String fileName, String directoryPath) {
        this.userName = userName;
        this.fileName = fileName;
        StringBuilder stringBuilder = new StringBuilder(directoryPath);
        stringBuilder.append('/').append(fileName);
        File file = new File(stringBuilder.toString());
        try (FileInputStream fileReader = new FileInputStream(file)) {
            fileBytes = fileReader.readAllBytes();
            size = file.length();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}