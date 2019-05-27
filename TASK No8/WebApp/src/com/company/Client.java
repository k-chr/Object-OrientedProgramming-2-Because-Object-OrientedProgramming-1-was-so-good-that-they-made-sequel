package com.company;
import java.net.*;
import java.io.*;
import java.util.Scanner;
class ClientThread extends Thread{
    private ObjectInputStream inputReceiver = null;
    private boolean end = false;
    private boolean safeToCommunicate = true;
    private Socket socket = null;
    public ClientThread(Socket socket){
        try {
            this.socket = socket;
            inputReceiver = new ObjectInputStream(this.socket.getInputStream());
        }
        catch(IOException io){
            io.printStackTrace();
        }
    }
    public void getAndPrintMessage(){

        try {
            synchronized (System.out) {
                System.out.println();
                System.out.println("> I'm checking connection");
                System.out.printf("> ");
                for (int i = 0; i < 101; ++i) {
                    if (i % 10 == 0)
                        System.out.printf(".");
                    if (this.isEnd() || socket.isClosed() || socket.isOutputShutdown() || socket.isInputShutdown())
                        return;
                }
                System.out.printf("\n");
                System.out.println("> Everything is OK");
                System.out.printf("> ");
            }
            if(socket.isClosed()) return;
            MessageToSend msg = (MessageToSend) inputReceiver.readObject();
            String message = msg.getMessage();
            System.out.println();
            System.out.println("< Client has received a notification from server: " + message);
        }
        catch (ClassNotFoundException cl){
            cl.printStackTrace();
        }
        catch(IOException i){
        }
    }
    public void setSafeToCommunicate(boolean value){
        safeToCommunicate = value;
    }
    public void setEnd(boolean value){
        end = value;
    }
    public boolean isEnd(){
        return end;
    }
    public void run(){
        while(!end){
            if(safeToCommunicate)
                getAndPrintMessage();
        }
        try {
            inputReceiver.close();
        }
        catch(IOException io){
            io.printStackTrace();
        }
        System.out.println("> Client thread is dead");
    }
}
public class Client{
    private Socket              socket       = null;
    private Scanner             scanner      = null;
    private ObjectOutputStream  inputSender  = null;
    private ClientThread        cThread      = null;
    public Client(String address, int port){
        try{
            socket = new Socket(address, port);
            System.out.println("> Connected");
            inputSender = new ObjectOutputStream(socket.getOutputStream());
            cThread = new ClientThread(socket);
            scanner = new Scanner(System.in);
            cThread.start();
        }
        catch(UnknownHostException u){
            u.printStackTrace();
        }
        catch(IOException i){
            i.printStackTrace();
        }
        while (!cThread.isEnd()){
            try{
                String line = "";
                synchronized (System.out) {
                    System.out.println("> Input a message (if you type \"Quit\" you close the connection)");
                    System.out.printf("> ");
                }
                line = scanner.nextLine();
                synchronized (cThread) {
                    if (line.equals("Quit")) {
                        cThread.setEnd(true);
                        cThread.setSafeToCommunicate(false);
                        System.out.println("> I'm about to send a message");
                        inputSender.writeObject(new MessageToSend(line));
                        inputSender.flush();
                        break;
                    }
                }
                StringBuilder stb = new StringBuilder(line);
                stb.append('|');
                System.out.println("> Type date, to specify when message will be send back to you by server");
                System.out.printf("> ");
                line = scanner.nextLine();
                stb.append(line);
                MessageToSend msg = new MessageToSend(stb.toString());
                System.out.println("> I'm about to send a message");
                inputSender.writeObject(msg);
                inputSender.flush();
            }
            catch(Exception i){
                System.out.println("> Exception with quitting in loop");
                i.printStackTrace();
                i.getCause();
            }
        }

        try{

            socket.close();
            System.out.println("> Quitting...");
            scanner.close();
            System.out.println("> ...");
        }
        catch(IOException i){
            System.out.println("> Exception with quitting");
            i.printStackTrace();
        }
    }
    public static void main(String args[]){
        Client client = new Client("127.0.0.1", 5000);
    }
}