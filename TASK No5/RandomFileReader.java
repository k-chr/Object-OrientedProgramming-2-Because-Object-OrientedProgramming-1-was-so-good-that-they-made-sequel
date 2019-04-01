import java.awt.EventQueue;
import java.awt.Dimension;
import java.io.*;
import java.util.NoSuchElementException;
import java.util.Random;
import javax.swing.JTextField;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Scanner;
import javax.swing.JFrame;
class RandomFile{
    private String path;
    private BufferedReader fileBuffer;
    private Integer maxLength;
    public RandomFile(String path, Integer length) throws IOException{
        this.path = path;
        fileBuffer = new BufferedReader(new FileReader(path));
        maxLength = length;
        dumpPath();
    }
    private void dumpPath(){
        try(BufferedWriter buff = new BufferedWriter(new FileWriter("pathDump.txt"))){
            buff.write(path);
        }
        catch(IOException io){
            System.out.println("Can't save path to file");
        }
    }
    BufferedReader getBuffer(){
        return fileBuffer;
    }
    Integer getMaxLength(){
        return maxLength;
    }
}
class RandomAccess extends JFrame implements KeyListener{
    private RandomFile file;
    private boolean endOfStream;
    private Random generator;
    JTextField field;
    @Override
    public void keyTyped(KeyEvent e) {
        try{
            if(endOfStream) closeFrame();
            RandomRead();
        }
        catch(IOException io){
            System.out.println("error");
        }
    }
    @Override
    public void keyPressed(KeyEvent e){}
    @Override
    public void keyReleased(KeyEvent e){ }
    private void closeFrame(){
        setVisible(false);
        dispose();
    }
    public RandomAccess(RandomFile buff){
        field = new JTextField();
        file = buff;
        endOfStream = false;
        generator = new Random();
        this.add(field);
        field.addKeyListener(this);
        setPreferredSize(new Dimension(300, 100));
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    private void RandomRead()throws IOException{
        StringBuilder content = new StringBuilder();
        int randomBufferSize = generator.nextInt(file.getMaxLength()) + 1;
        for(int i = 0, charInt; i < randomBufferSize; ++i){
            charInt = file.getBuffer().read();
            if(charInt == -1){
                endOfStream = true;
                break;
            }
            else{
                content.append((char)charInt);
            }
        }
        System.out.println(content.toString());
        content.delete(0,content.length());
    }
}
class RandomFileReader {
    public static void main(String[] args) {
        try{
            System.out.println("Provide file path");
            Scanner scanner = new Scanner(System.in);
            String path = scanner.nextLine();
            RandomFile rFile = new RandomFile(path, 5);
            Runnable task = ()-> new RandomAccess(rFile);
            EventQueue.invokeLater(task);
        }
        catch(NoSuchElementException | IllegalStateException el){
            System.out.println("Scanner exception");
        }
        catch(IOException io){
            System.out.println("IOError");
        }
    }
}