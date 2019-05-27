package com.company;
import java.net.*;
import java.io.*;
import java.lang.Thread;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
class InvalidMessageException extends Exception{
    public InvalidMessageException(){
        System.out.println("> Something went wrong with received message");
    }
}
class MessageParser{
    private Date                date    = null;
    private SimpleDateFormat    dtf     = null;
    private String              msg     = "";
    private boolean             end     = false;
    public MessageParser(MessageToSend mts) throws InvalidMessageException, ParseException {
        if(mts.getMessage().equals("Quit")){
            msg = mts.getMessage();
            end = true;
            date = null;
            return;
        }
        String tmp[] = mts.getMessage().split("\\|");
        if(tmp.length != 2){
            throw new InvalidMessageException();
        }
        msg = tmp[0];
        dtf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        date = dtf.parse(tmp[1]);
    }
    public boolean isEnd(){
        return end;
    }
    public Date getDate(){
        return date;
    }
    public String getMessage(){
        return msg;
    }
}
public class MessageServiceThread extends Thread{
    public MessageServiceThread(Socket socket, Integer id) throws IOException{
        clientSocket = socket;
        clientName = "Client " + id.toString();
        input = new ObjectInputStream(clientSocket.getInputStream());
        inputSender = new ObjectOutputStream(clientSocket.getOutputStream());
    }
    private String              clientName      = "";
    private boolean             isEnd           = false;
    private Socket              clientSocket    = null;
    private ObjectInputStream   input           = null;
    private ObjectOutputStream  inputSender     = null;
    private MessageToSend       received        = null;

    public synchronized void getMessageFromClient(){
        MessageParser parser = null;
        String notification = "";
        Date date = new Date();
        try {
            if(!isEnd){
                if((clientSocket.isClosed() || clientSocket.isOutputShutdown() || clientSocket.isOutputShutdown())) {
                    isEnd = true;
                    return;
                }
                received = (MessageToSend) input.readObject();
                try {
                    parser = new MessageParser(received);
                    notification = parser.getMessage();
                    date = parser.getDate();
                    isEnd = parser.isEnd();
                }
                catch(ParseException | InvalidMessageException exc){
                    try {
                        exc.printStackTrace();
                        synchronized (Thread.currentThread()) {
                            inputSender.writeObject(new MessageToSend("Provided invalid date or all input is not valid"));
                            inputSender.flush();
                        }
                    }
                    catch(IOException i){
                        i.printStackTrace();
                    }
                }
            }
        }
        catch(IOException | ClassNotFoundException ex){
            ex.printStackTrace();
        }
        System.out.println("> Received message from " + clientName);
        System.out.println("< Send me back: " + notification);
        if(!isEnd)
            sendNotificationToClient(notification, date);
    }
    public void sendNotificationToClient(String notification, Date date){
        try {
            Date now = new Date();
            long dateDiff = (date.getTime() - now.getTime());
            if(dateDiff < 0){
                throw new InvalidMessageException();
            }
            Timer timer = new Timer();
            timer.schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                inputSender.writeObject(new MessageToSend(notification));
                                inputSender.flush();
                            } catch (IOException io) {
                                io.printStackTrace();
                            }
                            timer.cancel();
                        }
                    }, dateDiff
            );
        }
        catch(InvalidMessageException exc){
            try {
                exc.printStackTrace();
                synchronized (Thread.currentThread()) {
                    inputSender.writeObject(new MessageToSend("Provided invalid date or all input is not valid"));
                    inputSender.flush();
                }
            }
            catch(IOException i){
               i.printStackTrace();
            }
        }
    }
    public void run(){
        while(!isEnd){
            getMessageFromClient();
        }
        try {
            System.out.println("> Quitting connection with " + clientName);
            inputSender.close();
            input.close();
            clientSocket.close();
        }
        catch(IOException i){
            i.printStackTrace();
        }
    }
}