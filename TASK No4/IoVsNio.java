import java.io.*;
import java.nio.file.*;
import java.util.Random;
class RandomizeFile{
    private String pathIO;
    private String pathNIO;
    private StringBuilder content;
    private Integer size;
    private Integer timeIOWrite;
    private Integer timeIORead;
    private Integer timeNIORead;
    private Integer timeNIOWrite;
    public RandomizeFile(String path, Integer size){
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(path).append("IO").append(".txt");
        pathIO = pathBuilder.toString();
        pathBuilder.delete(0,pathBuilder.length());
        pathBuilder.append(path).append("NIO").append(".txt");
        pathNIO = pathBuilder.toString();
        this.size = size;
        content = new StringBuilder();
    }
    public void randomContent(){
        Random generator = new Random();
        for(int i = 0; i < size; ++i){
            content.append((char)generator.nextInt(Character.MAX_VALUE-20000));
        }
    }
    private void dumpIO(){
        int time = (int)(System.nanoTime()/1000);
        try(BufferedWriter fileBuffer = new BufferedWriter(new FileWriter(pathIO))){
            fileBuffer.write(content.toString());
        }
        catch(IOException io){
            System.out.println("Unable to dump data");
        }
        timeIOWrite = ((int)(System.nanoTime()/1000) - time);
    }
    private void dumpNIO(){
        int time = (int)(System.nanoTime()/1000);
        Path path = FileSystems.getDefault().getPath(pathNIO);

        try(BufferedWriter fileBuffer = Files.newBufferedWriter(path)){
            fileBuffer.write(content.toString());
        }
        catch(IOException io){
            System.out.println("Unable to dump data");
        }
        timeNIOWrite = ((int)(System.nanoTime()/1000) - time);
    }
    private void nioRead(){
        System.out.println("---------NIORead----------");
        int time = (int)(System.nanoTime()/1000);
        Path path = FileSystems.getDefault().getPath(pathNIO);
        try(BufferedReader buffNIO = Files.newBufferedReader(path)){
            StringBuilder builder = new StringBuilder();
            for(String s = ""; s != null; s = buffNIO.readLine()){
                builder.append(s);
            }
            System.out.println(builder.toString());
        }
        catch(IOException io){
            System.out.println("Unable to read data");
        }
        timeNIORead = ((int)(System.nanoTime()/1000) - time);
    }
    private void ioRead(){
        System.out.println("---------IORead----------");
        int time = (int)(System.nanoTime()/1000);
        try(BufferedReader buffIO = new BufferedReader(new FileReader(pathIO))){
            StringBuilder builder = new StringBuilder();
            for(String s = ""; s != null; s = buffIO.readLine()){
                builder.append(s);
            }
            System.out.println(builder.toString());
        }
        catch(IOException io){
            System.out.println("Unable to read data");
        }
        timeIORead = ((int)(System.nanoTime()/1000) - time);
    }
    public void compareIOAndNIO(){
        dumpNIO();
        dumpIO();
        nioRead();
        ioRead();
        System.out.println("---------Write (in microseconds)----------");
        System.out.println("NIO: " + timeNIOWrite.toString());
        System.out.println("IO: " + timeIOWrite.toString());
        System.out.println("---------Read (in microseconds)----------");
        System.out.println("NIO: " + timeNIORead.toString());
        System.out.println("IO: " + timeIORead.toString());
    }
}
class IoVsNio {
    public static void main(String[] args) {
        String path = "file";
        RandomizeFile rFile = new RandomizeFile(path, 1000);
        rFile.randomContent();
        rFile.compareIOAndNIO();
    }
}