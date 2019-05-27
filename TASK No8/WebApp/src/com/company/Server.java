package com.company;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
public class Server
{
    private Socket socket = null;
    private ServerSocket server = null;
    public static int clients;
    private ArrayList<Thread> threadList = new ArrayList<>();
    public Server(int port){
        try{
            server = new ServerSocket(port);
            System.out.println("> Server started");
            System.out.printf("> Waiting for a client No %d \n",  (clients + 1));
            System.out.println("> ...");
            socket = server.accept();
            System.out.println(String.format("> Client %d accepted", (clients + 1)));
            clients++;
            threadList.add(new MessageServiceThread(socket, clients));
            threadList.get(threadList.size() - 1).start();
            while(true){
                System.out.printf("> Waiting for a client No %s \n",  (clients + 1));
                System.out.println("> ...");
                socket = server.accept();
                System.out.println(String.format("> Client %d accepted", (clients + 1)));
                clients++;
                threadList.add(new MessageServiceThread(socket, clients));
                threadList.get(threadList.size() - 1).start();
            }
        }
        catch(IOException i){
            i.printStackTrace();
        }
    }
    public static void main(String args[])
    {
        Server server = new Server(5000);
    }
}